package gae.piaz.jsonpatch.config;

import gae.piaz.jsonpatch.service.core.JsonPatchUpdate;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@AllArgsConstructor
@Slf4j
public class OpenApiConfiguration {

    private final RequestMappingHandlerMapping handlerMapping;

    static {
        io.swagger.v3.core.jackson.ModelResolver.enumsAsRef = true;
    }

    @Bean
    public GroupedOpenApi publicApi(OpenApiCustomizer customizer) {
        return GroupedOpenApi.builder().group("public").addOpenApiCustomizer(customizer).build();
    }

    @Bean
    public OpenApiCustomizer customOpenApi() {
        return openApi -> {
            initialize(openApi);

            openApi.info(
                    new Info()
                            .title("Sia Modulith API")
                            .version("1.0")
                            .description(
                                    "This is the API description for the awesome brand new FAST SiA Modulith backend."));

            Map<RequestMappingInfo, HandlerMethod> handlerMethods =
                    handlerMapping.getHandlerMethods();

            // create a new schema for JsonPatch operations and JsonPatch values
            createJsonPatchSchema(openApi);

            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                RequestMappingInfo requestMappingInfo = entry.getKey();
                PatternsRequestCondition patternsCondition =
                        requestMappingInfo.getPatternsCondition();
                String pattern = getEndpointPattern(patternsCondition, requestMappingInfo);
                if (pattern == null) {
                    continue;
                }
                log.info("Processing pattern: {}", pattern);
                JsonPatchUpdate annotation =
                        entry.getValue().getMethodAnnotation(JsonPatchUpdate.class);
                if (annotation != null) {
                    customizeJsonPatch(openApi, annotation, pattern);
                }
            }
        };
    }

    private void createJsonPatchSchema(OpenAPI openApi) {
        Schema<?> jsonPatchOps =
                new StringSchema()
                        ._enum(
                                Stream.of("add", "remove", "replace", "move", "copy", "test")
                                        .toList());

        Schema<?> jsonPatchValue =
                new Schema<>()
                        .anyOf(
                                List.of(
                                        new StringSchema(),
                                        new NumberSchema(),
                                        new BooleanSchema(),
                                        new ObjectSchema()));

        openApi.getComponents().getSchemas().putIfAbsent("JsonPatchOps", jsonPatchOps);
        openApi.getComponents().getSchemas().putIfAbsent("JsonPatchValue", jsonPatchValue);
    }

    private static String getEndpointPattern(
            PatternsRequestCondition patternsCondition, RequestMappingInfo requestMappingInfo) {
        String pattern = null;
        if (patternsCondition != null && !patternsCondition.getPatterns().isEmpty()) {
            // Extract pattern from PatternsRequestCondition
            pattern = patternsCondition.getPatterns().iterator().next();
        } else {
            // Check for PathPatternsCondition (newer style path matching)
            PathPatternsRequestCondition pathPatternsCondition =
                    requestMappingInfo.getPathPatternsCondition();
            if (pathPatternsCondition != null && !pathPatternsCondition.getPatterns().isEmpty()) {
                // Extract pattern from PathPatternsCondition
                pattern = pathPatternsCondition.getPatterns().iterator().next().getPatternString();
            }
        }
        return pattern;
    }

    private static void customizeJsonPatch(
            OpenAPI openApi, JsonPatchUpdate annotation, String pattern) {
        String schemaRef = "#/components/schemas/" + "JsonPatchItem";

        Schema<?> patchDTO =
                new ObjectSchema()
                        .addProperty("op", new Schema<>().$ref("#/components/schemas/JsonPatchOps"))
                        .addProperty("path", new StringSchema())
                        .addProperty(
                                "value",
                                new Schema<>().$ref("#/components/schemas/JsonPatchValue"));

        patchDTO.required(List.of("op", "path"));

        // Add or update the array schema
        openApi.getComponents().getSchemas().putIfAbsent("JsonPatchItem", patchDTO);

        // Ensure the path and request body exist
        if (openApi.getPaths() != null
                && openApi.getPaths().get(pattern) != null
                && openApi.getPaths().get(pattern).getPatch() != null
                && openApi.getPaths().get(pattern).getPatch().getRequestBody() != null) {

            RequestBody requestBody = openApi.getPaths().get(pattern).getPatch().getRequestBody();

            // Update the request body content schema to be an array of the patch schema
            requestBody
                    .getContent()
                    .put(
                            "application/json-patch+json",
                            new io.swagger.v3.oas.models.media.MediaType()
                                    .schema(
                                            new ArraySchema()
                                                    .items(new Schema<>().$ref(schemaRef))));

            requestBody.setRequired(true); // Make the requestBody required

            openApi.getPaths().get(pattern)
                    .getPatch().setDescription("<b>Allowed paths are:</b><br><br>- " + String.join("<br>- ", annotation.allowedPaths()));            // Add or update the responses
            openApi.getPaths()
                    .get(pattern)
                    .getPatch()
                    .getResponses()
                    .put(
                            "200",
                            new io.swagger.v3.oas.models.responses.ApiResponse()
                                    .description("Successful operation"));
            openApi.getPaths()
                    .get(pattern)
                    .getPatch()
                    .getResponses()
                    .put(
                            "304",
                            new io.swagger.v3.oas.models.responses.ApiResponse()
                                    .description("Not modified"));
            openApi.getPaths()
                    .get(pattern)
                    .getPatch()
                    .getResponses()
                    .put(
                            "400",
                            new io.swagger.v3.oas.models.responses.ApiResponse()
                                    .description("Precondition failed"));
            openApi.getPaths()
                    .get(pattern)
                    .getPatch()
                    .getResponses()
                    .put(
                            "404",
                            new io.swagger.v3.oas.models.responses.ApiResponse()
                                    .description("Resource not found"));
            openApi.getPaths()
                    .get(pattern)
                    .getPatch()
                    .getResponses()
                    .put(
                            "500",
                            new io.swagger.v3.oas.models.responses.ApiResponse()
                                    .description("Internal server error"));
        }
    }

    private static void initialize(OpenAPI openApi) {
        // Ensure the schema is added to components
        if (openApi.getComponents() == null) {
            openApi.setComponents(new io.swagger.v3.oas.models.Components());
        }
        if (openApi.getComponents().getSchemas() == null) {
            openApi.getComponents().setSchemas(new HashMap<>());
        }
    }
}
