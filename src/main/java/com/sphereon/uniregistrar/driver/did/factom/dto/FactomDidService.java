package com.sphereon.uniregistrar.driver.did.factom.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.net.URL;

@ApiModel
public class FactomDidService {
    @ApiModelProperty(
            value = "Service identifier extension",
            example = "cr-0"
    )
    private final String serviceIdentifier;
    @ApiModelProperty(
            value = "Service type",
            example = "CredentialRepository"
    )
    private final String type;
    @ApiModelProperty(
            value = "Service endpoint",
            example = "https://repository.example.com/service/8377464"
    )
    private final URL serviceEndpoint;
    @ApiModelProperty(
            value = "Priority requirement for updates",
            example = "1"
    )
    private final int priorityRequirement;

    private FactomDidService(String serviceIdentifier, String type, URL serviceEndpoint, int priorityRequirement) {
        this.serviceIdentifier = serviceIdentifier;
        this.type = type;
        this.serviceEndpoint = serviceEndpoint;
        this.priorityRequirement = priorityRequirement;
    }

    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    public String getType() {
        return type;
    }

    public URL getServiceEndpoint() {
        return serviceEndpoint;
    }

    public int getPriorityRequirement() {
        return priorityRequirement;
    }
}
