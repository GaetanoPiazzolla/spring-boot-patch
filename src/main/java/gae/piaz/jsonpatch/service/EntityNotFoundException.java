package gae.piaz.jsonpatch.service;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Entity not found", code = org.springframework.http.HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {}
