package com.sphereon.uniregistrar.driver.did.factom;

import com.google.gson.Gson;
import com.sphereon.factom.identity.did.DIDVersion;
import com.sphereon.factom.identity.did.IdentityClient;
import com.sphereon.factom.identity.did.entry.ResolvedFactomDIDEntry;
import com.sphereon.factom.identity.did.request.CreateFactomDidRequest;
import com.sphereon.factom.identity.did.request.CreateFactomIdentityRequest;
import com.sphereon.uniregistrar.driver.did.factom.model.JobMetadata;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uniregistrar.RegistrationException;
import uniregistrar.driver.AbstractDriver;
import uniregistrar.driver.Driver;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.CreateState;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.UpdateState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.sphereon.factom.identity.did.Constants.DID.DID_FACTOM;
import static com.sphereon.uniregistrar.driver.did.factom.Constants.MAINNET_KEY;
import static com.sphereon.uniregistrar.driver.did.factom.Constants.RequestOptions.NETWORK_NAME;

@Component
@Slf4j
public class DidFactomDriver extends AbstractDriver implements Driver {
    private final EntryOperations entryOperations;
    private final Gson gson;
    private final ClientFactory clientFactory;

    @Autowired
    public DidFactomDriver(final ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.entryOperations = new EntryOperations();
        this.gson = new Gson();
        initClients();
    }

    private void initClients() {
        List<IdentityClient> clients = clientFactory.fromEnvironment(getProperties());
        if (clients.isEmpty()) {
            log.warn("No Factom networks defined in environment. Using default mainnet and testnet values using OpenNode");
            clientFactory.fromDefaults();
            // We don't need to keep track of the clients, as the Networks class has a registry we can use
        }
    }

    @Override
    public CreateState create(CreateRequest createRequest) throws RegistrationException {
        if (StringUtils.isNotEmpty(createRequest.getJobId())) {
            if (createRequest.getOptions() != null && createRequest.getOptions().size() > 0) {
                throw new RegistrationException("Do not supply any options when passing in a job id. It is used to lookup existing registrations only!");
            }
            return handleJobStatusResponse(createRequest.getJobId());
        }

        final var networkId = getNetworkFrom(createRequest.getOptions().get(NETWORK_NAME));
        final var result = getResolvedFactomDIDEntry(networkId, createRequest);
        return createState(networkId, result);
    }

    private CreateState createState(String networkId, ResolvedFactomDIDEntry<?> result) {
        final var jobId = new JobMetadata(networkId, result.getChainId(), getEntryHash(result)).getId();
        final var didState = new HashMap<String, Object>();
        didState.put(Constants.ResponseKeywords.STATE, Constants.DidState.PENDING);
        didState.put(Constants.ResponseKeywords.IDENTIFIER, constructDidUri(networkId, result.getChainId()));
        return CreateState.build(
                jobId,
                didState,
                null,
                null);
    }

    private ResolvedFactomDIDEntry<?> getResolvedFactomDIDEntry(String networkId, CreateRequest createRequest) throws RegistrationException {
        final var identityClient = getClient(networkId);
        final var didVersion = getDidVersionFrom(createRequest);
        final var ecAddress = getECAddressFor(networkId).orElseThrow(() ->
                new RegistrationException("No EC address available for network id: " + networkId));

        final ResolvedFactomDIDEntry<?> result;
        log.info("Creating DID on network '{}', version '{}'...", networkId, didVersion);
        try {
            if (DIDVersion.FACTOM_IDENTITY_CHAIN.equals(didVersion)) {
                result = identityClient.create(createIdentityRequestEntryFrom(createRequest).toCreateIdentityRequestEntry(), Optional.of(ecAddress));
            } else if (DIDVersion.FACTOM_V1_JSON == didVersion) {
                result = identityClient.create(createFactomDidRequestFrom(createRequest), Optional.of(ecAddress));
            } else {
                throw new RegistrationException("Cannot create DID/identity when no version is supplied");
            }
            log.info("Created DID on network '{}', version '{}', chain: '{}'", networkId, didVersion, result.getChainId());

        } catch (FactomRuntimeException e) {
            throw new RegistrationException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RegistrationException("Could not create new DID", e);
        }
        return result;
    }

    @Override
    public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {
        throw new RuntimeException("Not yet implemented for Factom DIDs.");
    }

    @Override
    public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {
        throw new RuntimeException("Not yet implemented for Factom DIDs.");
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

    private String getNetworkFrom(Object networkName) {
        return Optional.ofNullable((String) networkName).orElse(MAINNET_KEY);
    }

    private DIDVersion getDidVersionFrom(CreateRequest createRequest) {
        String versionString = (String) createRequest.getOptions().get(Constants.RequestOptions.DID_VERSION);
        if (versionString == null) {
            return DIDVersion.FACTOM_V1_JSON;
        }
        return DIDVersion.valueOf(versionString);
    }

    private Optional<Address> getECAddressFor(String networkId) {
        return Networks.getDefaultECAddress(Optional.ofNullable(networkId));
    }

    private String getEntryHash(ResolvedFactomDIDEntry<?> resolvedEntry) {
        Entry didEntry = resolvedEntry.toEntry(Optional.of(resolvedEntry.getChainId()));
        return Encoding.HEX.encode(
                entryOperations.calculateEntryHash(
                        didEntry.getExternalIds(),
                        didEntry.getContent(),
                        didEntry.getChainId())
        );
    }

    private CreateState handleJobStatusResponse(String jobId) throws RegistrationException {
        log.info("Retrieving job status for job with id '{}'....", jobId);
        final var jobMetadata = JobMetadata.from(jobId);
        final var factomdClient = getFactomdFor(jobMetadata.getNetwork());
        final FactomResponse<EntryTransactionResponse> response;
        try {
            response = factomdClient.ackTransactions(jobMetadata.getEntryHash(),jobMetadata.getChainId(), EntryTransactionResponse.class).get();
        } catch (ExecutionException | InterruptedException e) {
            log.warn("Error retrieving job status for job with id '{}': {}", jobId, e.getMessage());
            throw new RegistrationException("Could not check job status for jobId: " + jobId, e);
        }
        final var didState = new HashMap<String, Object>();
        final var state = entryStateFromResponse(response);
        didState.put("state", state);
        didState.put("identifier", constructDidUri(jobMetadata.getNetwork(), jobMetadata.getChainId()));
        log.info("Job status for job with id '{}': {}", jobId, state);
        return CreateState.build(
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
        if (chainId.startsWith(DID_FACTOM)) {
            return chainId;
        } else if (network == null || MAINNET_KEY.equals(network)) {
            return String.format(DID_FACTOM + "%s", chainId);
        }
        return String.format(DID_FACTOM + "%s:%s", network, chainId);
    }

    private FactomdClient getFactomdFor(String networkId) {
        if (!Networks.hasFactomd(networkId)) {
            throw new RuntimeException("Could not find network with id: " + networkId);
        }
        return Networks.factomd(Optional.of(networkId));
    }

    private CreateFactomDidRequest createFactomDidRequestFrom(CreateRequest createRequest) {
        return gson.fromJson(gson.toJsonTree(createRequest.getOptions()), CreateFactomDidRequest.Builder.class).build();
    }

    private CreateFactomIdentityRequest createIdentityRequestEntryFrom(CreateRequest createRequest) {
        return gson.fromJson(gson.toJsonTree(createRequest.getOptions()), CreateFactomIdentityRequest.Builder.class).build();
    }


}
