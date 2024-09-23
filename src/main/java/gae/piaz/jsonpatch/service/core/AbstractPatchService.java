package gae.piaz.jsonpatch.service.core;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

/**
 * Generic service to update an entity using a JsonPatch
 *
 * @param <EN> Entity type
 * @param <UB> Update bean type
 */
@RequiredArgsConstructor
public abstract class AbstractPatchService<EN, UB> {

    protected final JsonPatchService jsonPatchService;

    /**
     * The flow of the updateEntity method is as follows:
     *
     * <ul>
     *   <li>Map the entity to an update bean.
     *   <li>Apply the patch to the update bean.
     *   <li>Update the entity fields with the update bean.
     *   <li>Returns a result with updated entity and updated set to true.
     * </ul>
     *
     * @param entity The entity to update
     * @param patch JSON patch to apply to the entity
     * @throws JsonPatchServerError if the patch cannot be applied to the update bean, internal
     *     error
     */
    public EN updateEntity(EN entity, JsonNode patch) throws JsonPatchServerError {
        UB updateBean = mapEntityToBean(entity);

        UB updateBeanResult = applyPatchToBean(patch, updateBean);

        updateEntityFields(entity, updateBeanResult);

        return entity;
    }

    protected UB applyPatchToBean(JsonNode patch, UB updateBean) throws JsonPatchServerError {
        return jsonPatchService.applyPatch(patch, updateBean, (Class<UB>) updateBean.getClass());
    }

    protected abstract void updateEntityFields(EN entity, UB updateBean);

    protected abstract UB mapEntityToBean(EN entity);
}
