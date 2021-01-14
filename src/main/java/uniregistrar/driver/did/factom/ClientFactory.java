package uniregistrar.driver.did.factom;


import org.blockchain_innovation.factom.client.api.SigningMode;
import org.blockchain_innovation.factom.client.api.ops.StringUtils;
import org.blockchain_innovation.factom.client.impl.FactomdClientImpl;
import org.blockchain_innovation.factom.client.impl.Networks;
import org.blockchain_innovation.factom.client.impl.WalletdClientImpl;
import org.blockchain_innovation.factom.identiy.did.IdentityClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static uniregistrar.driver.did.factom.Constants.MAINNET_KEY;

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

    public Optional<IdentityClient> fromEnvironment(Properties properties, int nr) {
        return fromEnvironment(toMap(properties), nr);
    }


    public Optional<IdentityClient> fromEnvironment(Map<String, String> environment, int nr) {
        String enabled = Optional.ofNullable(environment.get(Env.ENABLED.key(nr))).orElse("false");
        if (!Boolean.parseBoolean(enabled)) {
            return Optional.empty();
        }

        URL factomdUrl, walletdUrl;
        try {
            factomdUrl = new URL(environment.get(Env.FACTOMD_URL.key(nr)));
            walletdUrl = new URL(environment.get(Env.WALLETD_URL.key(nr)));
        } catch (MalformedURLException e){
            return Optional.empty();
        }

        String id = environment.get(Env.NETWORK_ID.key(nr));
        if (StringUtils.isEmpty(id)) {
            id = MAINNET_KEY;
        }

        FactomdClientImpl factomdClient = (FactomdClientImpl) Networks.factomd(Optional.of(id));
        factomdClient.setUrl(factomdUrl);

        String mode = environment.get(Env.MODE.key(nr));
        WalletdClientImpl walletdClient = (WalletdClientImpl) Networks.walletd(Optional.of(id), Optional.of(SigningMode.fromModeString(mode)));
        walletdClient.setUrl(walletdUrl);

        Networks.register(factomdClient);
        Networks.register(walletdClient);

        return Optional.of(new IdentityClient.Builder().networkName(id).build());
    }

    private Map<String, String> toMap(Properties properties) {
        Map<String, String> map = new HashMap<>();
        properties.forEach((key, value) -> map.put((String) key, (String) value));
        return map;
    }
}
