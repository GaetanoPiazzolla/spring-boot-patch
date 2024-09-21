package gae.piaz.jsonpatch.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Generic service to update an entity using a JsonPatch
 *
 * @param <T> Entity type
 * @param <U> Update bean type
 * @param <V> DTO type
 */
@RequiredArgsConstructor
public abstract class AbstractUpdateService<T, U, V> {

    protected final JpaRepository<T, Integer> repository;
    protected final JsonPatchService jsonPatchService;

    public UpdateResult<V> updateEntity(Integer entityId, JsonNode patch)
            throws JsonPatchServerError {
        T entity = findEntityById(entityId);
        U updateBean = mapEntityToBean(entity);
        PatchResults<U> result = applyPatchToBean(patch, updateBean);

        if (!result.isUpdated()) {
            return buildUpdateResult(false, null);
        }

        updateEntityFields(entity, result.body());
        entity = repository.save(entity);

        V dto = mapEntityToDTO(entity);
        return buildUpdateResult(true, dto);
    }

    protected PatchResults<U> applyPatchToBean(JsonNode patch, U updateBean)
            throws JsonPatchServerError {
        return jsonPatchService.applyPatch(patch, updateBean, (Class<U>) updateBean.getClass());
    }

    protected UpdateResult<V> buildUpdateResult(boolean updated, V dto) {
        return UpdateResult.<V>builder().updated(updated).result(dto).build();
    }

    protected abstract T findEntityById(Integer entityId);

    protected abstract void updateEntityFields(T entity, U updateBean);

    protected abstract U mapEntityToBean(T entity);

    protected abstract V mapEntityToDTO(T entity);
}
