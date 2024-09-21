package gae.piaz.jsonpatch.service.jsonpatch;

/**
 * A simple record to hold the body of the patch operation. It contains the updated body object and
 * a boolean flag indicating if the body was updated.
 */
public record PatchResults<T>(T body, boolean isUpdated) {}
