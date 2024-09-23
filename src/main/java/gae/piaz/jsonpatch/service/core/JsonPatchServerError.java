package gae.piaz.jsonpatch.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * JsonPatchServerError should be a checked exception since it represents server-side errors, which
 * might be recoverable and should be explicitly handled.
 */
@ResponseStatus(
        reason = "Server error while patching entity",
        code = HttpStatus.INTERNAL_SERVER_ERROR)
public class JsonPatchServerError extends Throwable {
    public JsonPatchServerError(String errorMessage, IllegalArgumentException e) {
        super(errorMessage, e);
    }
}
