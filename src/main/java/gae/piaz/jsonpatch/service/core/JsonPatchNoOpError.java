package gae.piaz.jsonpatch.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "No operation error while patching entity", code = HttpStatus.NOT_MODIFIED)
public class JsonPatchNoOpError extends RuntimeException {}
