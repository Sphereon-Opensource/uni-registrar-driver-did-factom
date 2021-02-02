package com.sphereon.uniregistrar.driver.did.factom.model;

import java.util.regex.Pattern;

public class JobMetadata {
    private final String network;
    private final String chainId;
    private final String entryHash;


    public JobMetadata(String network, String chainId, String entryHash) {
        this.network = network;
        this.chainId = chainId;
        this.entryHash = entryHash;
    }

    public String getNetwork() {
        return network;
    }

    public String getChainId() {
        return chainId;
    }

    public String getEntryHash() {
        return entryHash;
    }

    public String getId() {
        return String.format("%s.%s.%s", network, chainId, entryHash);
    }

    public static JobMetadata from(String jobId) {
        if (!jobId.contains(".")) {
            throw new RuntimeException("Invalid job id: " + jobId);
        }
        String[] parts = jobId.split(Pattern.quote("."));
        if (parts.length < 3) {
            throw new RuntimeException("Invalid job id: " + jobId);
        }
        return new JobMetadata(parts[0], parts[1], parts[2]);
    }
}
