package gae.piaz.jsonpatch;

import static gae.piaz.jsonpatch.controller.BookController.APPLICATION_JSON_PATCH_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import gae.piaz.jsonpatch.controller.dto.BookDTO;
import gae.piaz.jsonpatch.domain.BookEntity;
import gae.piaz.jsonpatch.domain.BookRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class JsonPatchApplicationTests {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private BookRepository bookRepository;

    @Test
    void updateBook_correct_200() throws Exception {
        int bookId = 2;

        String patch =
                """
                [
                    { "op": "test", "path": "/title", "value": "Java 102" },
                    { "op": "replace", "path": "/title", "value": "updated" },
                    { "op": "test", "path": "/author/id", "value": 1 },
                    { "op": "replace", "path": "/author/id", "value": 2 }
                ]
                """;
        MvcResult resp =
                mockMvc.perform(
                                patch("/api/v1/books/" + bookId)
                                        .contentType(APPLICATION_JSON_PATCH_VALUE)
                                        .header("Content-Type", APPLICATION_JSON_PATCH_VALUE)
                                        .content(patch))
                        .andExpect(status().isOk())
                        .andReturn();

        BookDTO book =
                objectMapper.readValue(resp.getResponse().getContentAsString(), BookDTO.class);

        assertEquals("updated", book.title());
        Optional<BookEntity> bookEntity = bookRepository.findById(bookId);
        assertTrue(bookEntity.isPresent());
        assertEquals("updated", bookEntity.get().getTitle());
        assertEquals(2, bookEntity.get().getAuthor().getId());
    }

    @Test
    void updateBook_onlyTest_304() throws Exception {
        int bookId = 1;

        String patch =
                """
                [
                    { "op": "test", "path": "/title", "value": "Java 101" }
                ]
                """;
        mockMvc.perform(
                        patch("/api/v1/books/" + bookId)
                                .contentType(APPLICATION_JSON_PATCH_VALUE)
                                .header("Content-Type", APPLICATION_JSON_PATCH_VALUE)
                                .content(patch))
                .andExpect(status().isNotModified());
    }

    @Test
    void updateBook_wrongPath_400() throws Exception {
        int bookId = 1;

        String patch =
                """
                [
                    { "op": "replace", "path": "/id", "value": "2" }
                ]
                """;
        mockMvc.perform(
                        patch("/api/v1/books/" + bookId)
                                .contentType(APPLICATION_JSON_PATCH_VALUE)
                                .header("Content-Type", APPLICATION_JSON_PATCH_VALUE)
                                .content(patch))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBook_wrongId_404() throws Exception {
        int bookId = 10;

        String patch =
                """
                [
                    { "op": "replace", "path": "/id", "value": "2" }
                ]
                """;
        mockMvc.perform(
                        patch("/api/v1/books/" + bookId)
                                .contentType(APPLICATION_JSON_PATCH_VALUE)
                                .header("Content-Type", APPLICATION_JSON_PATCH_VALUE)
                                .content(patch))
                .andExpect(status().isNotFound());
    }
}
