package gae.piaz.jsonpatch.service.jsonpatch;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        reason = "Server error while patching entity",
        code = HttpStatus.INTERNAL_SERVER_ERROR)
public class JsonPatchServerError extends Throwable {
    public JsonPatchServerError(String errorMessage, IllegalArgumentException e) {
        super(errorMessage, e);
    }
}
