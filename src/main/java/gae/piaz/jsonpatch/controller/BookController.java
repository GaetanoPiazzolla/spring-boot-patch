package gae.piaz.jsonpatch.controller;

import static gae.piaz.jsonpatch.config.Constants.APPLICATION_JSON_PATCH_VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import gae.piaz.jsonpatch.controller.dto.BookDTO;
import gae.piaz.jsonpatch.service.BookService;
import gae.piaz.jsonpatch.service.core.JsonPatchServerError;
import gae.piaz.jsonpatch.service.core.JsonPatchUpdate;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@AllArgsConstructor
@CrossOrigin
public class BookController {

    private final BookService bookService;

    @PatchMapping(
            path = "/{id}",
            consumes = APPLICATION_JSON_PATCH_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonPatchUpdate(allowedPaths = {"title", "author/id", "isbn"})
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable("id") Integer bookId, @RequestBody JsonNode patch)
            throws JsonPatchServerError {
        return ResponseEntity.ok(bookService.updateBook(bookId, patch));
    }
}
