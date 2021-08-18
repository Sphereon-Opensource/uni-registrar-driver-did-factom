package com.sphereon.uniregistrar.driver.did.factom;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uniregistrar.RegistrationException;
import uniregistrar.request.CreateRequest;
import uniregistrar.state.CreateState;

@RestController("Factom DID registrar")
@RequestMapping(value = "/1.0", name = "Factom DID registrar v1.0")
public class FactomDriverController {
    private final DidFactomDriver didFactomDriver;

    public FactomDriverController(DidFactomDriver didFactomDriver) {
        this.didFactomDriver = didFactomDriver;
    }

    @Operation(summary = "Create DID", operationId = "createDID", description = "Create a new DID", tags = "Registrar")
    @PostMapping(value = "/create")
    public CreateState create(@RequestBody CreateRequest createRequest) throws RegistrationException {
        return didFactomDriver.create(createRequest);
    }
}
