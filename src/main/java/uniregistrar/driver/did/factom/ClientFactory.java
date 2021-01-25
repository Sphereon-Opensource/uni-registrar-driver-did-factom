package uniregistrar.driver.did.factom;


import org.blockchain_innovation.factom.client.api.SigningMode;
import org.blockchain_innovation.factom.client.api.ops.StringUtils;
import org.blockchain_innovation.factom.client.api.settings.RpcSettings;
import com.sphereon.factom.identity.did.IdentityClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static uniregistrar.driver.did.factom.Constants.EC_ADDRESS_KEY;
import static uniregistrar.driver.did.factom.Constants.URL_KEY;
import static uniregistrar.driver.did.factom.Constants.FACTOMD_URL_MAINNET;
import static uniregistrar.driver.did.factom.Constants.FACTOMD_URL_TESTNET;
import static uniregistrar.driver.did.factom.Constants.MAINNET_KEY;
import static uniregistrar.driver.did.factom.Constants.SIGNING_MODE_KEY;
import static uniregistrar.driver.did.factom.Constants.TESTNET_KEY;

public class ClientFactory {
    public enum Env {
        ENABLED, FACTOMD_URL, WALLETD_URL, NETWORK_ID, MODE, ES_ADDRESS;

        public String key(int id) {
            if (id < 1 || id > 9) {
                throw new RuntimeException("Invalid value for id specified " + id + " for creating environment key " + name());
            }
            return String.format("NODE%d_%s", id, name());
        }
    }


    public List<IdentityClient> fromEnvironment(Properties properties) {
        return fromEnvironment(toMap(properties));
    }

    public List<IdentityClient> fromEnvironment(Map<String, String> environment) {
        List<IdentityClient> clients = new ArrayList<>();
        if (environment == null) {
            return clients;
        }
        for (int nr = 1; nr < 10; nr++) {
            fromEnvironment(environment, nr).ifPresent(clients::add);
        }
        return clients;
    }

    public List<IdentityClient> fromDefaults() {
        List<IdentityClient> clients = new ArrayList<>();
        clients.add(new IdentityClient.Builder().networkName(MAINNET_KEY)
                .property(constructPropertyKey(MAINNET_KEY, RpcSettings.SubSystem.FACTOMD, URL_KEY),
                        FACTOMD_URL_MAINNET)
                .property(constructPropertyKey(MAINNET_KEY, RpcSettings.SubSystem.WALLETD, SIGNING_MODE_KEY),
                        SigningMode.OFFLINE.toString().toLowerCase())
                .build());
        clients.add(new IdentityClient.Builder().networkName(TESTNET_KEY)
                .property(constructPropertyKey(TESTNET_KEY, RpcSettings.SubSystem.FACTOMD, URL_KEY),
                        FACTOMD_URL_TESTNET)
                .property(constructPropertyKey(TESTNET_KEY, RpcSettings.SubSystem.WALLETD, SIGNING_MODE_KEY),
                        SigningMode.OFFLINE.toString().toLowerCase())
                .build());
        return clients;
    }

    public Optional<IdentityClient> fromEnvironment(Properties properties, int nr) {
        return fromEnvironment(toMap(properties), nr);
    }


    public Optional<IdentityClient> fromEnvironment(Map<String, String> environment, int nr) {
        String enabled = Optional.ofNullable(environment.get(Env.ENABLED.key(nr))).orElse("false");
        if (!Boolean.parseBoolean(enabled)) {
            return Optional.empty();
        }

        String factomdUrl = environment.get(Env.FACTOMD_URL.key(nr));
        if (StringUtils.isEmpty(factomdUrl)) {
            return Optional.empty();
        }

        String id = environment.get(Env.NETWORK_ID.key(nr));
        if (StringUtils.isEmpty(id)) {
            id = MAINNET_KEY;
        }

        String esAddress = environment.get(Env.ES_ADDRESS.key(nr));

        String mode = environment.get(Env.MODE.key(nr));
        IdentityClient.Builder clientBuilder = new IdentityClient.Builder()
                .networkName(id)
                .property(constructPropertyKey(id, RpcSettings.SubSystem.FACTOMD, URL_KEY), factomdUrl)
                .property(constructPropertyKey(id, RpcSettings.SubSystem.WALLETD, SIGNING_MODE_KEY),
                        SigningMode.fromModeString(mode).toString());

        if (StringUtils.isNotEmpty(esAddress)) {
            clientBuilder.property(constructPropertyKey(id, RpcSettings.SubSystem.WALLETD, EC_ADDRESS_KEY), esAddress);
        }

        return Optional.of(clientBuilder.build());
    }

    private Map<String, String> toMap(Properties properties) {
        Map<String, String> map = new HashMap<>();
        properties.forEach((key, value) -> map.put((String) key, (String) value));
        return map;
    }

    private String constructPropertyKey(String networkId, RpcSettings.SubSystem subsystem, String key) {
        return String.format("%s.%s.%s", networkId, subsystem.configKey(), key);
    }
}
