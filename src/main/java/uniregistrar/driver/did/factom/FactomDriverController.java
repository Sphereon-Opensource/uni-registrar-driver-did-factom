package uniregistrar.driver.did.factom;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uniregistrar.RegistrationException;
import uniregistrar.request.RegisterRequest;
import uniregistrar.state.RegisterState;

@RestController("/1.0")
public class FactomDriverController {
    private final DidFactomDriver didFactomDriver;

    public FactomDriverController(DidFactomDriver didFactomDriver) {
        this.didFactomDriver = didFactomDriver;
    }

    @PostMapping("/register")
    public RegisterState register(@RequestBody RegisterRequest registerRequest) throws RegistrationException {
        return didFactomDriver.register(registerRequest);
    }
}
