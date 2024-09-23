package gae.piaz.jsonpatch.controller.dto;

import lombok.Builder;

@Builder
public record AuthorDTO(Integer id, String name) {}
