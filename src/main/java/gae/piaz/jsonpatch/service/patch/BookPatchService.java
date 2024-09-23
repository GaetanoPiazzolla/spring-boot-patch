package gae.piaz.jsonpatch.service.patch;

import gae.piaz.jsonpatch.domain.AuthorEntity;
import gae.piaz.jsonpatch.domain.BookEntity;
import gae.piaz.jsonpatch.service.core.AbstractPatchService;
import gae.piaz.jsonpatch.service.core.JsonPatchService;
import lombok.Builder;
import org.springframework.stereotype.Service;

@Service
public class BookPatchService
        extends AbstractPatchService<BookEntity, BookPatchService.BookEntityUpdateBean> {

    public BookPatchService(JsonPatchService jsonPatchService) {
        super(jsonPatchService);
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

    @Builder
    public record BookEntityUpdateBean(String title, AuthorUpdateBean author, String isbn) {}

    @Builder
    public record AuthorUpdateBean(Integer id) {}
}
