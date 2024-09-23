package gae.piaz.jsonpatch.service.patch;

import gae.piaz.jsonpatch.domain.AuthorEntity;
import gae.piaz.jsonpatch.domain.BookEntity;
import gae.piaz.jsonpatch.service.core.AbstractPatchService;
import gae.piaz.jsonpatch.service.core.JsonPatchService;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import org.springframework.stereotype.Service;

@Service
public class AuthorPatchService
        extends AbstractPatchService<AuthorEntity, AuthorPatchService.AuthorEntityUpdateBean> {

    public AuthorPatchService(JsonPatchService jsonPatchService) {
        super(jsonPatchService);
    }

    @Override
    protected void updateEntityFields(AuthorEntity entity, AuthorEntityUpdateBean updateBean) {
        entity.setName(updateBean.name());

        if (updateBean.books() != null) {

            // add new items, and eventually update existing ones
            for (BookUpdateBean bookUpdateBean : updateBean.books()) {
                BookEntity bookEntity =
                        entity.getBooks().stream()
                                .filter(b -> b.getId().equals(bookUpdateBean.id()))
                                .findFirst()
                                .orElse(null);
                if (bookEntity == null) {
                    bookEntity = new BookEntity();
                    bookEntity.setAuthor(entity);
                    entity.getBooks().add(bookEntity);
                }
                bookEntity.setTitle(bookUpdateBean.title());
                bookEntity.setIsbn(bookUpdateBean.isbn());
            }

            Iterator<BookEntity> iterator = entity.getBooks().iterator();
            while (iterator.hasNext()) {
                BookEntity bookEntity = iterator.next();
                if (updateBean.books().stream().noneMatch(b -> b.id().equals(bookEntity.getId()))) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    protected AuthorEntityUpdateBean mapEntityToBean(AuthorEntity entity) {
        Set<BookUpdateBean> bookUpdateBeans =
                entity.getBooks().stream()
                        .map(
                                book ->
                                        BookUpdateBean.builder()
                                                .id(book.getId())
                                                .title(book.getTitle())
                                                .isbn(book.getIsbn())
                                                .build())
                        .collect(Collectors.toSet());
        return AuthorEntityUpdateBean.builder()
                .name(entity.getName())
                .books(bookUpdateBeans)
                .build();
    }

    @Builder
    public record AuthorEntityUpdateBean(String name, Set<BookUpdateBean> books) {}

    @Builder
    public record BookUpdateBean(Integer id, String title, String isbn) {}
}
