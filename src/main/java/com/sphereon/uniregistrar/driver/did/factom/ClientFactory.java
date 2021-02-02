package com.sphereon.uniregistrar.driver.did.factom;


import com.sphereon.factom.identity.did.IdentityClient;
import org.blockchain_innovation.factom.client.api.SigningMode;
import org.blockchain_innovation.factom.client.api.ops.StringUtils;
import org.blockchain_innovation.factom.client.api.settings.RpcSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class ClientFactory {
    public enum Env {
        ENABLED, FACTOMD_URL, WALLETD_URL, NETWORK_ID, MODE, EC_ADDRESS;

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
        clients.add(new IdentityClient.Builder().networkName(Constants.MAINNET_KEY)
                .property(constructPropertyKey(Constants.MAINNET_KEY, RpcSettings.SubSystem.FACTOMD, Constants.URL_KEY),
                        Constants.FACTOMD_URL_MAINNET)
                .property(constructPropertyKey(Constants.MAINNET_KEY, RpcSettings.SubSystem.WALLETD, Constants.SIGNING_MODE_KEY),
                        SigningMode.OFFLINE.toString().toLowerCase())
                .build());
        clients.add(new IdentityClient.Builder().networkName(Constants.TESTNET_KEY)
                .property(constructPropertyKey(Constants.TESTNET_KEY, RpcSettings.SubSystem.FACTOMD, Constants.URL_KEY),
                        Constants.FACTOMD_URL_TESTNET)
                .property(constructPropertyKey(Constants.TESTNET_KEY, RpcSettings.SubSystem.WALLETD, Constants.SIGNING_MODE_KEY),
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
            id = Constants.MAINNET_KEY;
        }

        String ecAddress = environment.get(Env.EC_ADDRESS.key(nr));

        String mode = environment.get(Env.MODE.key(nr));
        IdentityClient.Builder clientBuilder = new IdentityClient.Builder()
                .networkName(id)
                .property(constructPropertyKey(id, RpcSettings.SubSystem.FACTOMD, Constants.URL_KEY), factomdUrl)
                .property(constructPropertyKey(id, RpcSettings.SubSystem.WALLETD, Constants.SIGNING_MODE_KEY),
                        SigningMode.fromModeString(mode).toString());

        if (StringUtils.isNotEmpty(ecAddress)) {
            clientBuilder.property(constructPropertyKey(id, RpcSettings.SubSystem.WALLETD, Constants.EC_ADDRESS_KEY), ecAddress);
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
