package com.sphereon.uniregistrar.driver.did.factom.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class FactomRegisterIdentityRequest {
    @ApiModelProperty(
            value = "options for creating a new identity",
            name = "options"
    )
    private final FactomIdentityOptions options;
    @ApiModelProperty(
            value = "jobId corresponding to previously created register requests.",
            name = "jobId",
            dataType = "String",
            example = "testnet.15425a19e1daf8079e87c75fa32646ad51db7d878367733403479161bb94690f.ef03f2722cf0eae024d01cc353bd100aa918813904b4b62587a6c92a893f334e"
    )
    private String jobId;

    public FactomRegisterIdentityRequest(String jobId, FactomIdentityOptions options) {
        this.jobId = jobId;
        this.options = options;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public FactomIdentityOptions getOptions() {
        return options;
    }
}
