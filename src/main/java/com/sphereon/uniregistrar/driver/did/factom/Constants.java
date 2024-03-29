package com.sphereon.uniregistrar.driver.did.factom;

public interface Constants {
    String URL_KEY = "url";
    String FACTOMD_URL_MAINNET = "https://api.factomd.net/v2";
    String FACTOMD_URL_TESTNET = "https://dev.factomd.net/v2";
    String MAINNET_KEY = "mainnet";
    String TESTNET_KEY = "testnet";
    String DEFAULT_EC_ADDRESS_MAINNET = "Es2eBrpwmp7neVkcx7nTkLPfBCWT9oGQuUktJnXAsvY2qmdYwqaK";
    String DEFAULT_EC_ADDRESS_TESTNET = "Es3Y6U6H1Pfg4wYag8VMtRZEGuEJnfkJ2ZuSyCVcQKweB6y4WvGH";

    String SIGNING_MODE_KEY = "signing-mode";
    String EC_ADDRESS_KEY = "ec-address";

    class RequestOptions {
        public static final String NETWORK_NAME = "networkName";
        public static final String DID_VERSION = "didVersion";
    }

    class ResponseKeywords {
        public static final String IDENTIFIER = "identifier";
        public static final String STATE = "state";
    }

    class DidState {
        public static final String PENDING = "pending";
        public static final String ANCHORED = "anchored";
        public static final String NOT_FOUND = "not found";
    }
}
