package gae.piaz.jsonpatch.service.common;

import lombok.Builder;

@Builder
public record UpdateResult<T>(T result, boolean updated) {}
