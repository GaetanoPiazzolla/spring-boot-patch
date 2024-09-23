package gae.piaz.jsonpatch.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * It makes sense for JsonPatchClientError to be a runtime exception since it represents client-side
 * errors, which are typically not recoverable and do not need to be explicitly handled.
 */
@ResponseStatus(reason = "Client error while patching entity", code = HttpStatus.BAD_REQUEST)
public class JsonPatchClientError extends RuntimeException {
    public JsonPatchClientError(String message, Throwable cause) {
        super(message, cause);
    }
}
