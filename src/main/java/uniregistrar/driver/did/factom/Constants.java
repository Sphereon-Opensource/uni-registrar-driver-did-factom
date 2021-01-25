package uniregistrar.driver.did.factom;

public interface Constants {
    String DID_FACTOM_METHOD_PATTERN = "^did:factom:.+";
    String URL_KEY = "url";
    String FACTOMD_URL_MAINNET = "https://api.factomd.net/v2";
    String FACTOMD_URL_TESTNET = "https://dev.factomd.net/v2";
    String MAINNET_KEY = "mainnet";
    String TESTNET_KEY = "testnet";
    String SIGNING_MODE_KEY = "signing-mode";
    String EC_ADDRESS_KEY = "ec-address";

    class DidState{
        public static final String PENDING = "pending";
        public static final String FINISHED = "finished";
        public static final String NOT_FOUND = "not found";
    }
}
