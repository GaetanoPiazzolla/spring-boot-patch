package gae.piaz.jsonpatch.service.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;
import com.flipkart.zjsonpatch.JsonPatchApplicationException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class JsonPatchService {

    private final ObjectMapper objectMapper;

    /**
     * TODO Article: Applies a JSON Patch to a body object. The body object is converted to a
     * JsonNode, the patch is applied, and the body is converted back to the body object. If the
     * patch cannot be applied, for example when a "test" operation has failed, the method will
     * throw a JsonPatchClientError.
     *
     * @throws JsonPatchClientError if the patch cannot be applied to the body object, typically
     *     because a "test" operation has failed or the patch is invalid
     * @throws JsonPatchServerError if the body object cannot be converted to a JsonNode or the
     *     JsonNode cannot be converted back to the body object
     */
    public <T> PatchResults<T> applyPatch(JsonNode patch, T target, Class<T> targetClass)
            throws JsonPatchClientError, JsonPatchServerError {

        JsonNode targetNode;
        JsonNode patchedNode;

        try {
            // TODO Article: this is a very simple way to convert a bean to a JsonNode.
            // It can fail if the input has recursive relationships - that why we use simple beans.
            targetNode = objectMapper.convertValue(target, JsonNode.class);
        } catch (IllegalArgumentException e) {
            // TODO Article: this error depends on the internal implementation of the code, so it
            // should be a 500
            String errorMessage =
                    String.format(
                            "Failed to convert BEAN of type %s to JSON Node",
                            targetClass.getSimpleName());
            log.error(errorMessage, e);
            throw new JsonPatchServerError(errorMessage, e);
        }

        try {
            // TODO Article: magic happens link to the library and the RFC implemented
            patchedNode = JsonPatch.apply(patch, targetNode);
        } catch (JsonPatchApplicationException e) {
            // TODO Article: this happens mostly because of concurrency errors!
            // this error depends on the input, so it should be a 400
            String errorMessage =
                    String.format(
                            "Failed to apply JSON Patch to BEAN of type %s. Patch: %s",
                            targetClass.getSimpleName(), patch);
            log.error(errorMessage, e);
            throw new JsonPatchClientError(errorMessage, e);
        }

        List<JsonNode> operations = patch.findValues("op");
        if (operations.stream().allMatch(jsonNode -> jsonNode.asText().equals("test"))) {
            return new PatchResults<>(target, false);
        }

        try {
            target = objectMapper.convertValue(patchedNode, targetClass);
        } catch (IllegalArgumentException e) {
            String errorMessage =
                    String.format(
                            "Failed to convert JSON Node to BEAN of type %s",
                            targetClass.getSimpleName());
            log.error(errorMessage, e);
            throw new JsonPatchServerError(errorMessage, e);
        }

        return new PatchResults<>(target, true);
    }
}
