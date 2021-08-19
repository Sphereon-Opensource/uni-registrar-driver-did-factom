package com.sphereon.uniregistrar.driver.did.factom;


import com.sphereon.factom.identity.did.IdentityClient;
import lombok.extern.slf4j.Slf4j;
import org.blockchain_innovation.factom.client.api.AddressKeyConversions;
import org.blockchain_innovation.factom.client.api.SigningMode;
import org.blockchain_innovation.factom.client.api.ops.StringUtils;
import org.blockchain_innovation.factom.client.api.settings.RpcSettings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Component
@Slf4j
public class ClientFactory {
    private final static AddressKeyConversions ADDRESS_CONV = new AddressKeyConversions();

    public List<IdentityClient> fromEnvironment(Properties properties) {
        return fromEnvironment(toMap(properties));
    }

    public List<IdentityClient> fromEnvironment(Map<String, String> environment) {
        final List<IdentityClient> clients = new ArrayList<>();
        if (environment == null) {
            return clients;
        }
        for (int nr = 1; nr < 10; nr++) {
            final int finalNr = nr;
            fromEnvironment(environment, nr).ifPresent(identityClient -> {
                log.info("Adding client {} from environment vars for network {}", finalNr, identityClient.getNetworkName());
                clients.add(identityClient);
            });
        }
        return clients;
    }

    public List<IdentityClient> fromDefaults() {
        final List<IdentityClient> clients = new ArrayList<>();
        clients.add(new IdentityClient.Builder().networkName(Constants.MAINNET_KEY)
                .property(constructPropertyKey(Constants.MAINNET_KEY, RpcSettings.SubSystem.FACTOMD, Constants.URL_KEY),
                        Constants.FACTOMD_URL_MAINNET)
                .property(constructPropertyKey(Constants.MAINNET_KEY, RpcSettings.SubSystem.WALLETD, Constants.SIGNING_MODE_KEY),
                        SigningMode.OFFLINE.toString().toLowerCase())
                .property(constructPropertyKey(Constants.MAINNET_KEY, RpcSettings.SubSystem.WALLETD, Constants.EC_ADDRESS_KEY), Constants.DEFAULT_EC_ADDRESS_MAINNET)
                .autoRegister(true)
                .build());
        log.info("Configured default mainnet client using '{}', signing-mode '{}' and public EC-address '{}'", Constants.FACTOMD_URL_MAINNET, SigningMode.OFFLINE, ADDRESS_CONV.addressToPublicAddress(Constants.DEFAULT_EC_ADDRESS_MAINNET));

        clients.add(new IdentityClient.Builder().networkName(Constants.TESTNET_KEY)
                .property(constructPropertyKey(Constants.TESTNET_KEY, RpcSettings.SubSystem.FACTOMD, Constants.URL_KEY),
                        Constants.FACTOMD_URL_TESTNET)
                .property(constructPropertyKey(Constants.TESTNET_KEY, RpcSettings.SubSystem.WALLETD, Constants.SIGNING_MODE_KEY),
                        SigningMode.OFFLINE.toString().toLowerCase())
                .property(constructPropertyKey(Constants.TESTNET_KEY, RpcSettings.SubSystem.WALLETD, Constants.EC_ADDRESS_KEY), Constants.DEFAULT_EC_ADDRESS_TESTNET)
                .autoRegister(true)
                .build());
        log.info("Configured default testnet client using '{}', signing-mode '{}' and public EC-address '{}'", Constants.FACTOMD_URL_TESTNET, SigningMode.OFFLINE, ADDRESS_CONV.addressToPublicAddress(Constants.DEFAULT_EC_ADDRESS_TESTNET));
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
            log.warn("Node {} was enabled, but no factomd URL was provided, Skipping node", nr);
            return Optional.empty();
        }

        String id = environment.get(Env.NETWORK_ID.key(nr));
        if (StringUtils.isEmpty(id)) {
            log.warn("Using 'mainnet' for node {}, since no network id was provided", nr);
            id = Constants.MAINNET_KEY;
        }


        final var envMode = environment.get(Env.MODE.key(nr));
        final var signingMode = SigningMode.fromModeString(envMode).toString();
        IdentityClient.Builder clientBuilder = new IdentityClient.Builder()
                .networkName(id)
                .property(constructPropertyKey(id, RpcSettings.SubSystem.FACTOMD, Constants.URL_KEY), factomdUrl)
                .property(constructPropertyKey(id, RpcSettings.SubSystem.WALLETD, Constants.SIGNING_MODE_KEY),
                        signingMode)
                .autoRegister(true);

        final String ecAddress = environment.get(Env.EC_ADDRESS.key(nr));
        if (StringUtils.isNotEmpty(ecAddress)) {
            clientBuilder.property(constructPropertyKey(id, RpcSettings.SubSystem.WALLETD, Constants.EC_ADDRESS_KEY), ecAddress);
        }
        log.info("Configured {} client using '{}', signing-mode '{}' and EC-address '{}'", id, factomdUrl, signingMode, StringUtils.isNotEmpty(ecAddress) ? ecAddress : "<none>");

        return Optional.of(clientBuilder.build());
    }

    private Map<String, String> toMap(Properties properties) {
        final Map<String, String> map = new HashMap<>();
        properties.forEach((key, value) -> map.put((String) key, (String) value));
        return map;
    }

    private String constructPropertyKey(String networkId, RpcSettings.SubSystem subsystem, String key) {
        return String.format("%s.%s.%s", networkId, subsystem.configKey(), key);
    }

    public enum Env {
        ENABLED, FACTOMD_URL, WALLETD_URL, NETWORK_ID, MODE, EC_ADDRESS;

        public String key(int id) {
            if (id < 1 || id > 9) {
                throw new RuntimeException("Invalid value for id specified " + id + " for creating environment key " + name());
            }
            return String.format("NODE%d_%s", id, name());
        }
    }
}
