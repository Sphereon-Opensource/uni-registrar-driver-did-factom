package uniregistrar.driver.did.factom;

import com.google.gson.Gson;
import com.sphereon.factom.identity.did.request.CreateFactomDidRequest;
import uniregistrar.request.RegisterRequest;

public class FactomRegisterRequest {
    private final CreateFactomDidRequest createRequest;
    private FactomRegisterRequest(CreateFactomDidRequest createRequest) {
        this.createRequest = createRequest;
    }

    public CreateFactomDidRequest getCreateRequest(){
        return this.createRequest;
    }

    public static FactomRegisterRequest from(RegisterRequest registerRequest) {
        Gson gson = new Gson();
        CreateFactomDidRequest factomDidRequest = gson.fromJson(
                gson.toJsonTree(registerRequest.getOptions()),
                CreateFactomDidRequest.class);
        return new FactomRegisterRequest(factomDidRequest);
    }
}
