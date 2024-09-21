package gae.piaz.jsonpatch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import gae.piaz.jsonpatch.controller.dto.BookDTO;
import gae.piaz.jsonpatch.service.BookUpdateService;
import gae.piaz.jsonpatch.service.common.JsonPatchServerError;
import gae.piaz.jsonpatch.service.common.JsonPatchUpdate;
import gae.piaz.jsonpatch.service.common.UpdateResult;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@AllArgsConstructor
@CrossOrigin
public class BookController {

    private final BookUpdateService bookService;

    public static final String APPLICATION_JSON_PATCH_VALUE = "application/json-patch+json";

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookDTO> getBooks(@PathVariable Integer id) {
        return null;
    }

    @PatchMapping(
            path = "/{id}",
            consumes = APPLICATION_JSON_PATCH_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonPatchUpdate(
            paths = {"title", "author/id", "isbn"},
            schemaName = "VersionPatchOpsDTO")
    public ResponseEntity<BookDTO> updateVersion(
            @PathVariable("id") Integer bookId, @RequestBody JsonNode patch)
            throws JsonPatchServerError {
        UpdateResult<BookDTO> result = bookService.updateEntity(bookId, patch);
        if (!result.updated()) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        return ResponseEntity.ok(result.result());
    }
}
