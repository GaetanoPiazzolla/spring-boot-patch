package gae.piaz.jsonpatch;

import static gae.piaz.jsonpatch.config.Constants.APPLICATION_JSON_PATCH_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import gae.piaz.jsonpatch.controller.dto.AuthorDTO;
import gae.piaz.jsonpatch.domain.AuthorEntity;
import gae.piaz.jsonpatch.domain.AuthorRepository;
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
public class AuthorControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private AuthorRepository authorRepository;

    @Test
    void updateAuthor_correct_200() throws Exception {
        int authorId = 1;
        Optional<AuthorEntity> authorEntity = authorRepository.findById(authorId);
        assertTrue(authorEntity.isPresent());
        assertEquals("John Doe", authorEntity.get().getName());

        String patch =
                """
                        [
                            { "op": "test", "path": "/name", "value": "John Doe" },
                            { "op": "replace", "path": "/name", "value": "Jane Doe" }
                        ]
                        """;
        MvcResult resp =
                mockMvc.perform(
                                patch("/api/v1/authors/" + authorId)
                                        .header("Content-Type", APPLICATION_JSON_PATCH_VALUE)
                                        .content(patch))
                        .andExpect(status().isOk())
                        .andReturn();

        AuthorDTO author =
                objectMapper.readValue(resp.getResponse().getContentAsString(), AuthorDTO.class);

        assertEquals("Jane Doe", author.name());
        authorEntity = authorRepository.findById(authorId);
        assertTrue(authorEntity.isPresent());
        assertEquals("Jane Doe", authorEntity.get().getName());
    }

    @Test
    void updateAuthor_onlyTest_304() throws Exception {
        int authorId = 1;

        String patch =
                """
                        [
                            { "op": "test", "path": "/name", "value": "John Doe" }
                        ]
                        """;
        mockMvc.perform(
                        patch("/api/v1/authors/" + authorId)
                                .header("Content-Type", APPLICATION_JSON_PATCH_VALUE)
                                .content(patch))
                .andExpect(status().isNotModified());
    }

    @Test
    void updateAuthor_wrongPath_400() throws Exception {
        int authorId = 1;

        String patch =
                """
                        [
                            { "op": "replace", "path": "/id", "value": "2" }
                        ]
                        """;
        mockMvc.perform(
                        patch("/api/v1/authors/" + authorId)
                                .header("Content-Type", APPLICATION_JSON_PATCH_VALUE)
                                .content(patch))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAuthor_wrongId_404() throws Exception {
        int authorId = 10;

        String patch =
                """
                        [
                            { "op": "replace", "path": "/id", "value": "2" }
                        ]
                        """;
        mockMvc.perform(
                        patch("/api/v1/authors/" + authorId)
                                .header("Content-Type", APPLICATION_JSON_PATCH_VALUE)
                                .content(patch))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPatch_removeBook_200() throws Exception {
        Integer authorId = 1;
        AuthorEntity updatedAuthor = authorRepository.findById(authorId).orElseThrow();
        assertThat(updatedAuthor.getBooks()).hasSize(3);

        String jsonPatch =
                """
                [
                    {
                        "op": "remove",
                        "path": "/books/0"
                    }
                ]
                """;
        mockMvc.perform(
                        patch("/api/v1/authors/" + authorId)
                                .contentType("application/json-patch+json")
                                .content(jsonPatch))
                .andExpect(status().isOk());

        updatedAuthor = authorRepository.findById(authorId).orElseThrow();
        assertThat(updatedAuthor.getBooks()).hasSize(2);
    }

    @Test
    public void testPatch_addBook_200() throws Exception {
        Integer authorId = 2;
        AuthorEntity updatedAuthor = authorRepository.findById(authorId).orElseThrow();
        assertThat(updatedAuthor.getBooks()).hasSize(4);

        String jsonPatch =
                """
                [
                    {
                        "op": "add",
                        "path": "/books/-",
                        "value": {
                            "title": "New Book",
                            "isbn": "123-4567890123"
                        }
                    }
                ]
                """;
        mockMvc.perform(
                        patch("/api/v1/authors/" + authorId)
                                .contentType("application/json-patch+json")
                                .content(jsonPatch))
                .andExpect(status().isOk());

        updatedAuthor = authorRepository.findById(authorId).orElseThrow();
        assertThat(updatedAuthor.getBooks()).hasSize(5);
    }
}
