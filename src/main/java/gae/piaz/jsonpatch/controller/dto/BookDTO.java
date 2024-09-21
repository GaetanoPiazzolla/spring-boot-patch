package gae.piaz.jsonpatch.controller.dto;

import lombok.Builder;

@Builder
public record BookDTO(Integer id, String title, String author) {}
