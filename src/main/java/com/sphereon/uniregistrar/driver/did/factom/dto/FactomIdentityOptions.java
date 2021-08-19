package com.sphereon.uniregistrar.driver.did.factom.dto;

import com.sphereon.factom.identity.did.DIDVersion;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.blockchain_innovation.factom.client.api.errors.FactomRuntimeException;
import org.factomprotocol.identity.did.model.CreateIdentityRequest;
import org.factomprotocol.identity.did.model.FactomKey;

import java.util.Arrays;
import java.util.List;

import static com.sphereon.factom.identity.did.DIDVersion.FACTOM_IDENTITY_CHAIN;

@ApiModel
public class FactomIdentityOptions extends CreateIdentityRequest {
    @ApiModelProperty(
            value = "On-chain Factom DID Version. Only FACTOM_IDENTITY_CHAIN allowed",
            name = "didVersion",
            dataType = "String",
            example = "FACTOM_IDENTITY_CHAIN"
    )
    private final DIDVersion didVersion = FACTOM_IDENTITY_CHAIN;

    @ApiModelProperty(
            value = "The Factom network on which to create the DID entry.",
            name = "networkName",
            example = "testnet"
    )
    private final String networkName;

    public FactomIdentityOptions(DIDVersion didVersion, String networkName, List<FactomKey> keys, String... tags) {
        super();
        assertValid(didVersion, keys, tags);
        this.networkName = networkName;
        keys(keys);
        Arrays.stream(tags).sequential().forEach(tag -> addTagsItem(tag));
    }

    private void assertValid(DIDVersion didVersion, List<FactomKey> factomKeys, String[] externalIds) {
        if (didVersion != null && didVersion != FACTOM_IDENTITY_CHAIN) {
            throw new FactomRuntimeException.AssertionException("Only Factom identity chains allowed for this request");
            // We allow to pass it in to not have to change the contract in the future.
        } else if (factomKeys == null || factomKeys.size() == 0) {
            throw new FactomRuntimeException.AssertionException("Cannot create a Factom Identity without keys/idpubs!");
        } else if (externalIds == null || externalIds.length == 0) {
            throw new FactomRuntimeException.AssertionException("Cannot create a Factom Identity without external Ids");
        }
    }

    public DIDVersion getDidVersion() {
        return didVersion;
    }

    public List<String> getExternalIds() {
        return getTags();
    }

    public String getNetworkName() {
        return networkName;
    }
}
