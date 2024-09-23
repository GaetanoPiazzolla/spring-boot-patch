package gae.piaz.jsonpatch.controller;

import static gae.piaz.jsonpatch.config.Constants.APPLICATION_JSON_PATCH_VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import gae.piaz.jsonpatch.controller.dto.AuthorDTO;
import gae.piaz.jsonpatch.service.AuthorService;
import gae.piaz.jsonpatch.service.core.JsonPatchServerError;
import gae.piaz.jsonpatch.service.core.JsonPatchUpdate;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/authors")
@AllArgsConstructor
@CrossOrigin
public class AuthorController {

    private final AuthorService authorService;

    @PatchMapping(
            path = "/{id}",
            consumes = APPLICATION_JSON_PATCH_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonPatchUpdate(
            allowedPaths = {
                "name -> change the name of the author",
                "books/- -> add and remove items from the books of this author",
                "books/-/title -> update an author's book title",
                "books/-/isbn -> update an author's book isbn"
            })
    public ResponseEntity<AuthorDTO> updateAuthor(
            @PathVariable("id") Integer authorId, @RequestBody JsonNode patch)
            throws JsonPatchServerError {
        return ResponseEntity.ok(authorService.updateAuthor(authorId, patch));
    }
}
