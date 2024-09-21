package gae.piaz.jsonpatch.service;

import gae.piaz.jsonpatch.controller.dto.BookDTO;
import gae.piaz.jsonpatch.domain.AuthorEntity;
import gae.piaz.jsonpatch.domain.BookEntity;
import gae.piaz.jsonpatch.service.jsonpatch.AbstractUpdateService;
import gae.piaz.jsonpatch.service.jsonpatch.JsonPatchService;
import lombok.Builder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class BookUpdateService
        extends AbstractUpdateService<BookEntity, BookUpdateService.BookEntityUpdateBean, BookDTO> {

    public BookUpdateService(
            JpaRepository<BookEntity, Integer> repository, JsonPatchService jsonPatchService) {
        super(repository, jsonPatchService);
    }

    @Override
    protected BookEntity findEntityById(Integer entityId) {
        return repository.findById(entityId).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    protected void updateEntityFields(BookEntity entity, BookEntityUpdateBean updateBean) {
        entity.setTitle(updateBean.title());
        entity.setIsbn(updateBean.isbn());
        entity.setAuthor(new AuthorEntity());
        entity.getAuthor().setId(updateBean.author().id());
    }

    @Override
    protected BookEntityUpdateBean mapEntityToBean(BookEntity entity) {
        return BookEntityUpdateBean.builder()
                .title(entity.getTitle())
                .author(new AuthorUpdateBean(entity.getAuthor().getId()))
                .isbn(entity.getIsbn())
                .build();
    }

    @Override
    protected BookDTO mapEntityToDTO(BookEntity entity) {
        return BookDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor().getName())
                .build();
    }

    @Builder
    public record BookEntityUpdateBean(String title, AuthorUpdateBean author, String isbn) {}

    @Builder
    public record AuthorUpdateBean(Integer id) {}
}
