package com.sphereon.uniregistrar.driver.did.factom;

import com.google.gson.Gson;
import com.sphereon.factom.identity.did.DIDVersion;
import com.sphereon.factom.identity.did.IdentityClient;
import com.sphereon.factom.identity.did.entry.CreateIdentityRequestEntry;
import com.sphereon.factom.identity.did.entry.ResolvedFactomDIDEntry;
import com.sphereon.factom.identity.did.request.CreateFactomDidRequest;
import org.blockchain_innovation.factom.client.api.FactomResponse;
import org.blockchain_innovation.factom.client.api.FactomdClient;
import org.blockchain_innovation.factom.client.api.errors.FactomRuntimeException;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.client.api.model.Entry;
import org.blockchain_innovation.factom.client.api.model.response.factomd.EntryTransactionResponse;
import org.blockchain_innovation.factom.client.api.ops.Encoding;
import org.blockchain_innovation.factom.client.api.ops.EntryOperations;
import org.blockchain_innovation.factom.client.api.ops.StringUtils;
import org.blockchain_innovation.factom.client.impl.Networks;
import org.factomprotocol.identity.did.model.FactomDidContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uniregistrar.RegistrationException;
import uniregistrar.driver.AbstractDriver;
import uniregistrar.driver.Driver;
import com.sphereon.uniregistrar.driver.did.factom.model.JobMetadata;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.RegisterState;
import uniregistrar.state.UpdateState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.sphereon.uniregistrar.driver.did.factom.Constants.MAINNET_KEY;

@Component
public class DidFactomDriver extends AbstractDriver implements Driver {
    private static final Logger log = LoggerFactory.getLogger(DidFactomDriver.class);
    private final EntryOperations entryOperations;
    private final Gson gson;

    public DidFactomDriver() {
        ClientFactory clientFactory = new ClientFactory();
        List<IdentityClient> clients = clientFactory.fromEnvironment(getProperties());
        if (clients.isEmpty()) {
            log.warn("No Factom networks defined in environment. Using default mainnet and testnet values using OpenNode");
            clients = clientFactory.fromDefaults();
        }
        clients.forEach(IdentityClient.Registry::put);
        this.entryOperations = new EntryOperations();
        this.gson = new Gson();
    }


    @Override
    public RegisterState register(RegisterRequest registerRequest) throws RegistrationException {
        if (StringUtils.isNotEmpty(registerRequest.getJobId())) {
            return handleJobStatusResponse(registerRequest.getJobId());
        }

        String networkId = getNetworkFrom(registerRequest);
        IdentityClient identityClient = getClient(networkId);
        DIDVersion didVersion = getDidVersionFrom(registerRequest);
        Address ecAddress = getECAddressFor(networkId).orElseThrow(() ->
                new RegistrationException("No EC address available for network id: " + networkId));

        if (DIDVersion.FACTOM_IDENTITY_CHAIN.equals(didVersion)) {
            // ToDo: finish create(CreateIdentityRequestEntry entry, Optional<Address> ecAddress) method
            // in IdentityClient for backwards compatibility with Factom Identity Chains
            throw new RegistrationException("Factom Identity Chain DID creation is not yet implemented.");
        }

        CreateFactomDidRequest createRequest = createFactomDidRequestFrom(registerRequest);
        final ResolvedFactomDIDEntry<FactomDidContent> result;
        try {
            result = identityClient.create(createRequest, ecAddress);
        } catch (FactomRuntimeException e) {
            throw new RegistrationException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RegistrationException("Could not create new DID", e);
        }
        String jobId = new JobMetadata(networkId, result.getChainId(), getEntryHash(result)).getId();
        Map<String, Object> didState = new HashMap<>();
        didState.put(Constants.ResponseKeywords.STATE, Constants.DidState.PENDING);
        didState.put(Constants.ResponseKeywords.IDENTIFIER, constructDidUri(networkId, result.getChainId()));
        return RegisterState.build(
                jobId,
                didState,
                null,
                null);
    }

    @Override
    public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public Map<String, Object> properties() {
        return new HashMap<>(System.getenv());
    }

    private Map<String, String> getProperties() {
        Map<String, Object> map = properties();
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
    }

    private IdentityClient getClient(String id) {
        IdentityClient identityClient = IdentityClient.Registry.get(Optional.of(id));
        if (identityClient == null) {
            throw new RuntimeException("Could not get client for network with id: " + id);
        }
        return identityClient;
    }

    private String getNetworkFrom(RegisterRequest registerRequest) {
        return Optional.of((String) registerRequest.getOptions().get(Constants.RequestOptions.NETWORK_NAME)).orElse(MAINNET_KEY);
    }

    private DIDVersion getDidVersionFrom(RegisterRequest registerRequest) {
        String versionString = (String) registerRequest.getOptions().get(Constants.RequestOptions.DID_VERSION);
        if (versionString == null) {
            return DIDVersion.FACTOM_V1_JSON;
        }
        return DIDVersion.valueOf(versionString);
    }

    private Optional<Address> getECAddressFor(String networkId) {
        for (int nr = 1; nr < 10; nr++) {
            String nrNetworkId = getProperties().get(ClientFactory.Env.NETWORK_ID.key(nr));
            if (nrNetworkId.equals(networkId)) {
                return Optional.of(new Address(getProperties().get(ClientFactory.Env.EC_ADDRESS.key(nr))));
            }
        }
        return Optional.empty();
    }

    private String getEntryHash(ResolvedFactomDIDEntry<FactomDidContent> resolvedEntry) {
        Entry didEntry = resolvedEntry.toEntry(Optional.of(resolvedEntry.getChainId()));
        return Encoding.HEX.encode(
                entryOperations.calculateEntryHash(
                        didEntry.getExternalIds(),
                        didEntry.getContent(),
                        didEntry.getChainId())
        );
    }

    private RegisterState handleJobStatusResponse(String jobId) throws RegistrationException {
        JobMetadata jobMetadata = JobMetadata.from(jobId);
        FactomdClient factomdClient = getFactomdFor(jobMetadata.getNetwork());
        final FactomResponse<EntryTransactionResponse> response;
        try {
            response = factomdClient.ackEntryTransactions(jobMetadata.getEntryHash()).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RegistrationException("Could not check job status for jobId: " + jobId, e);
        }
        Map<String, Object> didState = new HashMap<>();
        didState.put("state", entryStateFromResponse(response));
        didState.put("identifier", constructDidUri(jobMetadata.getNetwork(), jobMetadata.getChainId()));
        return RegisterState.build(
                jobId,
                didState,
                null,
                null);
    }

    private String entryStateFromResponse(FactomResponse<EntryTransactionResponse> response) {
        switch (response.getResult().getCommitData().getStatus()) {
            case NotConfirmed:
            case TransactionACK:
                return Constants.DidState.PENDING;
            case DBlockConfirmed:
                return Constants.DidState.ANCHORED;
            case Unknown:
            default:
                return Constants.DidState.NOT_FOUND;
        }
    }

    private String constructDidUri(String network, String chainId) {
        if (network == null || MAINNET_KEY.equals(network)) {
            return String.format("did:factom:%s", chainId);
        }
        return String.format("did:factom:%s:%s", network, chainId);
    }

    private FactomdClient getFactomdFor(String networkId) {
        if (!Networks.hasFactomd(networkId)) {
            throw new RuntimeException("Could not find network with id: " + networkId);
        }
        return Networks.factomd(Optional.of(networkId));
    }

    private CreateFactomDidRequest createFactomDidRequestFrom(RegisterRequest registerRequest) {
        return gson.fromJson(gson.toJsonTree(registerRequest.getOptions()), CreateFactomDidRequest.class);
    }

    private CreateIdentityRequestEntry createIdentityRequestEntryFrom(RegisterRequest registerRequest) {
        return gson.fromJson(gson.toJsonTree(registerRequest.getOptions()), CreateIdentityRequestEntry.class);
    }
}
