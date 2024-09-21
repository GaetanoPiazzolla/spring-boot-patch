package gae.piaz.jsonpatch.service.jsonpatch;

import lombok.Builder;

@Builder
public record UpdateResult<T>(T result, boolean updated) {}
