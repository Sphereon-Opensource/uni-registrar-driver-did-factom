package com.sphereon.uniregistrar.driver.did.factom.dto;

import com.sphereon.factom.identity.did.DIDVersion;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.factomprotocol.identity.did.model.DidKey;
import org.factomprotocol.identity.did.model.ManagementKey;
import org.factomprotocol.identity.did.model.Service;

import java.util.List;

@ApiModel
public class FactomDidOptions {
    @ApiModelProperty(
            value = "On-chain Factom DID Version",
            name = "didVersion",
            dataType = "String",
            example = "FACTOM_V1_JSON"
    )
    private final DIDVersion didVersion;
    @ApiModelProperty
    private final List<ManagementKey> managementKeys;
    @ApiModelProperty
    private final List<DidKey> didKeys;
    @ApiModelProperty
    private final List<Service> services;
    @ApiModelProperty(
            value = "External ids that form the basis of the chainId.",
            name = "tags",
            example = "[\"test\", \"identity\", \"abc\"]"
    )
    private final String[] tags;
    @ApiModelProperty(
            value = "A unique string used to form the DID URI",
            name = "nonce",
            example = "5589f60f-f28b-44ec-9abd-d0896e3eb627"
    )
    private final String nonce;
    @ApiModelProperty(
            value = "The Factom network on which to create the DID entry.",
            name = "networkName",
            example = "testnet"
    )
    private final String networkName;

    private FactomDidOptions(DIDVersion didVersion, String networkName, List<ManagementKey> managementKeys, List<DidKey> didKeys, List<Service> services, String nonce, String... tags) {
        this.didVersion = didVersion;
        this.networkName = networkName;
        this.managementKeys = managementKeys;
        this.didKeys = didKeys;
        this.services = services;
        this.nonce = nonce;
        this.tags = tags;
    }

    public DIDVersion getDidVersion() {
        return didVersion;
    }

    public List<ManagementKey> getManagementKeys() {
        return managementKeys;
    }

    public List<DidKey> getDidKeys() {
        return didKeys;
    }

    public List<Service> getServices() {
        return services;
    }

    public String[] getTags() {
        return tags;
    }

    public String getNonce() {
        return nonce;
    }

    public String getNetworkName() {
        return networkName;
    }
}
