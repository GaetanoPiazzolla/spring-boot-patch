package gae.piaz.jsonpatch.service;

import com.fasterxml.jackson.databind.JsonNode;
import gae.piaz.jsonpatch.controller.dto.BookDTO;
import gae.piaz.jsonpatch.domain.BookEntity;
import gae.piaz.jsonpatch.domain.BookRepository;
import gae.piaz.jsonpatch.service.core.EntityNotFoundException;
import gae.piaz.jsonpatch.service.core.JsonPatchServerError;
import gae.piaz.jsonpatch.service.patch.BookPatchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookPatchService bookPatchService;

    public BookDTO updateBook(Integer id, JsonNode patch) throws JsonPatchServerError {
        BookEntity book = bookRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        book = bookPatchService.updateEntity(book, patch);

        book = bookRepository.save(book);

        return mapEntityToDTO(book);
    }

    private BookDTO mapEntityToDTO(BookEntity entity) {
        return BookDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .authorName(entity.getAuthor().getName())
                .build();
    }
}
