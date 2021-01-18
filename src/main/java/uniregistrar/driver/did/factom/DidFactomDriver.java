package uniregistrar.driver.did.factom;

import org.blockchain_innovation.factom.client.api.SigningMode;
import org.blockchain_innovation.factom.client.api.model.Address;
import org.blockchain_innovation.factom.identiy.did.IdentityClient;
import org.blockchain_innovation.factom.identiy.did.entry.ResolvedFactomDIDEntry;
import org.blockchain_innovation.factom.identiy.did.request.CreateFactomDidRequest;
import org.factomprotocol.identity.did.model.FactomDidContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.driver.AbstractDriver;
import uniregistrar.driver.Driver;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.RegisterState;
import uniregistrar.state.UpdateState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uniregistrar.driver.did.factom.Constants.FACTOMD_URL_KEY;
import static uniregistrar.driver.did.factom.Constants.FACTOMD_URL_MAINNET;
import static uniregistrar.driver.did.factom.Constants.FACTOMD_URL_TESTNET;
import static uniregistrar.driver.did.factom.Constants.MAINNET_KEY;
import static uniregistrar.driver.did.factom.Constants.SIGNING_MODE_KEY;
import static uniregistrar.driver.did.factom.Constants.TESTNET_KEY;

public class DidFactomDriver extends AbstractDriver implements Driver {
    private static Logger log = LoggerFactory.getLogger(DidFactomDriver.class);

    public DidFactomDriver() throws RegistrationException {
        ClientFactory clientFactory = new ClientFactory();
        List<IdentityClient> clients = clientFactory.fromEnvironment(getProperties());
        if (clients.isEmpty()) {
            log.warn("No Factom networks defined in environment. Using default mainnet and testnet values using OpenNode");
            clients.add(new IdentityClient.Builder().networkName(MAINNET_KEY)
                    .property(FACTOMD_URL_KEY, FACTOMD_URL_MAINNET)
                    .property(SIGNING_MODE_KEY, SigningMode.OFFLINE.toString().toLowerCase())
                    .build());
            clients.add(new IdentityClient.Builder().networkName(TESTNET_KEY)
                    .property(SIGNING_MODE_KEY, SigningMode.OFFLINE.toString().toLowerCase())
                    .property(FACTOMD_URL_KEY, FACTOMD_URL_TESTNET).build());
        }
        clients.forEach(IdentityClient.Registry::put);
    }


    @Override
    public RegisterState register(RegisterRequest registerRequest) throws RegistrationException {
        String networkId = getNetworkFrom(registerRequest).orElse(MAINNET_KEY);
        IdentityClient identityClient = getClient(networkId);
        CreateFactomDidRequest createRequest = FactomRegisterRequest.from(registerRequest).getCreateRequest();
        try {
            ResolvedFactomDIDEntry<FactomDidContent> result = identityClient.create(
                    createRequest,
                    getECAddressFor(networkId).orElseThrow(() ->
                            new RegistrationException("No EC address available for network id: " + networkId)));
            return RegisterState.build(result.getChainId(),
                    null,
                    null,
                    Map.of("tags", Arrays.toString(createRequest.getTags())));
        } catch (Exception e) {
            throw new RegistrationException("Could not create new DID", e);
        }
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

    private Optional<String> getNetworkFrom(RegisterRequest registerRequest) {
        return Optional.of((String) registerRequest.getOptions().get("networkName"));
    }

    private Optional<Address> getECAddressFor(String networkId) {
        for (int nr = 1; nr < 10; nr++) {
            String nrNetworkId = getProperties().get(ClientFactory.Env.NETWORK_ID.key(nr));
            if (nrNetworkId.equals(networkId)) {
                return Optional.of(new Address(getProperties().get(ClientFactory.Env.ES_ADDRESS.key(nr))));
            }
        }
        return Optional.empty();
    }
}
