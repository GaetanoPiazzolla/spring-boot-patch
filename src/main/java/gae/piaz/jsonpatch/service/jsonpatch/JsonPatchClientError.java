package gae.piaz.jsonpatch.service.jsonpatch;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * TODO Article : Yes, it makes sense for JsonPatchClientError to be a runtime exception since it
 * represents client-side errors, which are typically not recoverable and do not need to be
 * explicitly handled. On the other hand, JsonPatchServerError should be a checked exception since
 * it represents server-side errors, which might be recoverable and should be explicitly handled.
 */
@ResponseStatus(reason = "Client error while patching entity", code = HttpStatus.BAD_REQUEST)
public class JsonPatchClientError extends RuntimeException {
    public JsonPatchClientError(String message, Throwable cause) {
        super(message, cause);
    }
}
