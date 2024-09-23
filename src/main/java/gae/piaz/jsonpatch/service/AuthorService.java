package gae.piaz.jsonpatch.service;

import com.fasterxml.jackson.databind.JsonNode;
import gae.piaz.jsonpatch.controller.dto.AuthorDTO;
import gae.piaz.jsonpatch.domain.AuthorEntity;
import gae.piaz.jsonpatch.domain.AuthorRepository;
import gae.piaz.jsonpatch.service.core.EntityNotFoundException;
import gae.piaz.jsonpatch.service.core.JsonPatchServerError;
import gae.piaz.jsonpatch.service.patch.AuthorPatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorPatchService authorPatchService;

    public AuthorDTO updateAuthor(Integer id, JsonNode patch) throws JsonPatchServerError {
        AuthorEntity author =
                authorRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        author = authorPatchService.updateEntity(author, patch);

        author = authorRepository.save(author);

        return mapEntityToDTO(author);
    }

    private AuthorDTO mapEntityToDTO(AuthorEntity entity) {
        return AuthorDTO.builder().name(entity.getName()).id(entity.getId()).build();
    }
}
