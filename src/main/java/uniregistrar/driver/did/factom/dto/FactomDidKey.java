package uniregistrar.driver.did.factom.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.factomprotocol.identity.did.model.KeyPurpose;
import org.factomprotocol.identity.did.model.KeyType;

import java.util.List;

@ApiModel
public class FactomDidKey {
    @ApiModelProperty(
            value = "Key type: [ Ed25519VerificationKey, ECDSASecp256k1VerificationKey, RSAVerificationKey ]",
            name = "type",
            example = "RSAVerificationKey"
    )
    private final KeyType type;
    @ApiModelProperty(
            value = "keyIdentifier extension",
            name="keyIdentifier",
            example = "verification-key-0"
    )
    private final String keyIdentifier;
    @ApiModelProperty(
            value = "Base 58 encoding of the public key",
            name = "publicKeyBase58",
            example = "H3C2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV"
    )
    private final String publicKeyBase58;
    @ApiModelProperty(
            value = "Priority ranking for management keys.",
            name="priority",
            example = "0",
            dataType = "int"
    )
    private final int priority;
    @ApiModelProperty(
            value = "Priority requirement for updating key.",
            name="priorityRequirement",
            example = "1",
            dataType = "int"
    )
    private final int priorityRequirement;
    @ApiModelProperty(
            value = "bip44 path derivation",
            required = false,
            example = "m / 44' / 1' / 1' / 1 / 1"
    )
    private final String bip44;
    @ApiModelProperty(
            value = "Key purpose array (for DID keys)",
            name="keyPurpose",
            example = "[\"publicKey\", \"authentication\"]"
    )
    private final List<KeyPurpose> purpose;

    private FactomDidKey(KeyType type,
                         String keyIdentifier,
                         String publicKeyBase58,
                         int priority,
                         List<KeyPurpose> purpose,
                         int priorityRequirement,
                         String bip44) {
        this.type = type;
        this.keyIdentifier = keyIdentifier;
        this.publicKeyBase58 = publicKeyBase58;
        this.priority = priority;
        this.priorityRequirement = priorityRequirement;
        this.purpose = purpose;
        this.bip44 = bip44;
    }

    public int getPriority() {
        return priority;
    }

    public KeyType getType() {
        return type;
    }

    public String getKeyIdentifier() {
        return keyIdentifier;
    }

    public String getPublicKeyBase58() {
        return publicKeyBase58;
    }

    public int getPriorityRequirement() {
        return priorityRequirement;
    }

    public String getBip44() {
        return bip44;
    }

    public List<KeyPurpose> getPurpose() {
        return purpose;
    }
}
