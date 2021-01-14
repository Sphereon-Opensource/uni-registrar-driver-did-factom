package uniregistrar.driver.did.factom;

import org.blockchain_innovation.factom.identiy.did.request.CreateFactomDidRequest;
import uniregistrar.request.RegisterRequest;

import java.util.Map;

public class FactomRegisterRequest {
    private CreateFactomDidRequest createRequest;

    private FactomRegisterRequest(CreateFactomDidRequest createRequest) {
        this.createRequest = createRequest;
    }

    public CreateFactomDidRequest getCreateRequest(){
        return this.createRequest;
    }

    public static FactomRegisterRequest from(RegisterRequest registerRequest) {
        Map<String, Object> requestValues = registerRequest.getOptions();
        return new FactomRegisterRequest(new CreateFactomDidRequest.Builder().build());
    }
}
