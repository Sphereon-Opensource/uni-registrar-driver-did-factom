package com.sphereon.uniregistrar.driver.did.factom;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uniregistrar.RegistrationException;
import uniregistrar.request.CreateRequest;
import uniregistrar.state.CreateState;

@RestController
@RequestMapping("/1.0")
public class FactomDriverController {
    private final DidFactomDriver didFactomDriver;

    public FactomDriverController(DidFactomDriver didFactomDriver) {
        this.didFactomDriver = didFactomDriver;
    }

    @PostMapping("/create")
    public CreateState register(@RequestBody CreateRequest createRequest) throws RegistrationException {
        return didFactomDriver.create(createRequest);
    }
}
