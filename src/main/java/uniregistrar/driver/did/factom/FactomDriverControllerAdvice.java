package uniregistrar.driver.did.factom;

import com.sphereon.ms.rest.response.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uniregistrar.RegistrationException;

@ControllerAdvice
public class FactomDriverControllerAdvice {
    private static final Logger log = LoggerFactory.getLogger(FactomDriverControllerAdvice.class);

    @ResponseBody
    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse registrationException(RegistrationException registrationException) {
        log.warn(registrationException.getMessage());
        return new ErrorResponse(registrationException);
    }
}
