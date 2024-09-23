package gae.piaz.jsonpatch.service.core;

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
     * Applies a JSON Patch to a body object. The body object is converted to a JsonNode, the patch
     * is applied, and the body is converted back to the body object. If the patch cannot be
     * applied, for example when a "test" operation has failed, the method will throw a
     * JsonPatchClientError.
     *
     * @throws JsonPatchClientError if the patch cannot be applied to the body object, typically
     *     because a "test" operation has failed or the patch is invalid
     * @throws JsonPatchServerError if the body object cannot be converted to a JsonNode or the
     *     JsonNode cannot be converted back to the body object
     */
    public <T> T applyPatch(JsonNode patch, T target, Class<T> targetClass)
            throws JsonPatchClientError, JsonPatchServerError {

        JsonNode targetNode = convertToJsonNode(target, targetClass);

        JsonNode patchedNode = applyJsonPatch(patch, targetNode, targetClass);

        return convertToBean(patchedNode, targetClass);
    }

    /**
     * Converts the target object to a JsonNode. This can fail if the input has recursive
     * relationships.
     */
    private <T> JsonNode convertToJsonNode(T target, Class<T> targetClass)
            throws JsonPatchServerError {
        try {
            return objectMapper.convertValue(target, JsonNode.class);
        } catch (IllegalArgumentException e) {
            String errorMessage =
                    String.format(
                            "Failed to convert BEAN of type %s to JSON Node",
                            targetClass.getSimpleName());
            log.error(errorMessage, e);
            throw new JsonPatchServerError(errorMessage, e);
        }
    }

    /**
     * Applies the JSON Patch to the target JsonNode. Logs and throws a client error if the patch
     * application fails, typically due to concurrency issues.
     */
    private JsonNode applyJsonPatch(JsonNode patch, JsonNode targetNode, Class<?> targetClass)
            throws JsonPatchClientError {

        if (isTestOperationOnly(patch)) {
            log.info("No operations to apply in the JSON Patch");
            throw new JsonPatchNoOpError();
        }

        try {
            return JsonPatch.apply(patch, targetNode);
        } catch (JsonPatchApplicationException e) {
            String errorMessage =
                    String.format(
                            "Failed to apply JSON Patch to BEAN of type %s. Patch: %s",
                            targetClass.getSimpleName(), patch);
            log.error(errorMessage, e);
            throw new JsonPatchClientError(errorMessage, e);
        }
    }

    /** Checks if all operations in the patch are "test" operations. */
    private boolean isTestOperationOnly(JsonNode patch) {
        List<JsonNode> operations = patch.findValues("op");
        return operations.stream().allMatch(jsonNode -> jsonNode.asText().equals("test"));
    }

    /**
     * Converts the patched JsonNode back to the target object. Logs and throws a server error if
     * the conversion fails.
     */
    private <T> T convertToBean(JsonNode patchedNode, Class<T> targetClass)
            throws JsonPatchServerError {
        try {
            return objectMapper.convertValue(patchedNode, targetClass);
        } catch (IllegalArgumentException e) {
            String errorMessage =
                    String.format(
                            "Failed to convert JSON Node to BEAN of type %s",
                            targetClass.getSimpleName());
            log.error(errorMessage, e);
            throw new JsonPatchServerError(errorMessage, e);
        }
    }
}
