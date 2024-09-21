# Spring Boot JSON Patch

This repository demonstrates how to apply 
[JSON Patch](https://datatracker.ietf.org/doc/html/rfc6902) to JPA entities in a Spring Boot Application 
in a generic, reusable, and optimized way.

## Table of Contents
- [Introduction](#introduction)
- [Core Components](#core-components)
- [How to apply JSON Patch to ANY JPA entity](#how-to-apply-json-patch-to-any-jpa-entity)
- [API Documentation](#api-documentation)
- [HTTP Status Responses](#http-status-responses)
- [Concurrency Handling](#concurrency-handling)
- [Typescript Stubs Generation](#typescript-stubs-generation)
- [Stubs Usage Example in Frontend](#stubs-usage-example-in-frontend)
- [Running Locally](#running-locally)
- [Testing](#testing)

## Introduction
This project showcases a method to apply JSON Patch operations to JPA entities in a Spring Boot application. 
The approach is designed to be generic, reusable, and optimized for performance.

The project uses the following technologies and libraries:
- JSON Patch, as implemented by the [com.flipkart.zjsonpatch:zjsonpatch](https://github.com/flipkart-incubator/zjsonpatch) library
- Spring Boot and related libs version 3.3.4
- Swagger and OpenAPI for API documentation
- Lombok

## Core Components
The core components of the project are:
- [JsonPatchService.java](src/main/java/gae/piaz/jsonpatch/service/common/JsonPatchService.java): The service class that applies JSON Patch operations to JPA entities.
- [JsonPatchUpdate.java](src/main/java/gae/piaz/jsonpatch/service/common/JsonPatchUpdate.java): An annotation that documents the API endpoints and specifies the allowed paths and type names for the Stubs.
- [AbstractUpdateService.java](src/main/java/gae/piaz/jsonpatch/service/common/AbstractUpdateService.java): A generic service class that provides the update functionality for JPA entities.

## How to apply JSON Patch to ANY JPA entity
1) You need to define Bean(s) with all the fields that you want to update in the entity, e.g: 
```java
@Builder
public record BookUpdateBean(
        String title,
        AuthorUpdateBean author
) { }
@Builder
public record AuthorUpdateBean(
        Integer id
) { }
```

2) Create a service class that extends the [AbstractUpdateService.java](src/main/java/gae/piaz/jsonpatch/service/common/AbstractUpdateService.java) class and implements all the abstract methods:
```java
protected abstract T findEntityById(Integer entityId);
protected abstract void updateEntityFields(T entity, U updateBean);
protected abstract U mapEntityToBean(T entity);
protected abstract V mapEntityToDTO(T entity);
```
The First parameter of the Abstract class generic is the JPA entity, the second is the Bean class, and the third is the DTO class.

```java
@Service
public class BookService extends AbstractUpdateService<Book, BookUpdateBean, BookDTO> {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Override
    protected Book findEntityById(Integer entityId) {
        return bookRepository.findById(entityId).orElseThrow();
    }

    @Override
    protected void updateEntityFields(Book entity, BookUpdateBean updateBean) {
        entity.setTitle(updateBean.title());
        entity.setIsbn(authorRepository.findById(updateBean.isbn().id()).orElseThrow());
    }

    @Override
    protected BookUpdateBean mapEntityToBean(Book entity) {
        return BookUpdateBean.builder()
                .title(entity.getTitle())
                .isbn(AuthorUpdateBean.builder().id(entity.getIsbn().getId()).build())
                .build();
    }

    @Override
    protected BookDTO mapEntityToDTO(Book entity) {
        return BookDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .isbn(entity.getIsbn().getId())
                .build();
    }
}
```

Call the `updateEntity` method from the service class in the controller:
```java
@PatchMapping(
    path = "/{id}",
    consumes = APPLICATION_JSON_PATCH_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
@JsonPatchUpdate(
    paths = {"title", "author/id", "isbn"},
    schemaName = "BookPatchOpsDTO")
public ResponseEntity<BookDTO> updateBook(
    @PathVariable("id") Integer bookId, @RequestBody JsonNode patch)
        throws JsonPatchServerError {
    UpdateResult<BookDTO> result = bookService.updateEntity(bookId, patch);
    if (!result.updated()) {
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }
return ResponseEntity.ok(result.result());
}
```

## API Documentation
The API documentation is generated using Swagger. To access the Swagger UI, navigate to:
- `http://localhost:8084/swagger-ui/index.html`

For OpenAPI YAML documentation:
- JSON: `http://localhost:8084/openapi/public`
- YAML: `http://localhost:8084/openapi/public/yaml`

The generated documentation can also be found in the `api` folder.

The [JsonPatchUpdate.java](src/main/java/gae/piaz/jsonpatch/annotation/JsonPatchUpdate.java) annotation documents the endpoints.
Using the annotation parameters, we can specify the path allowed and the type name for the Stubs.
E.g:

```java
@PatchMapping(
            path = "/{id}",
            consumes = APPLICATION_JSON_PATCH_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonPatchUpdate(
            paths = {"title", "author/id", "isbn"},
            schemaName = "BookPatchOpsDTO")
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable("id") Integer bookId, @RequestBody JsonNode patch)
            throws JsonPatchServerError {
        UpdateResult<BookDTO> result = bookService.updateEntity(bookId, patch);
        if (!result.updated()) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        return ResponseEntity.ok(result.result());
    }
```

## HTTP Status Responses
The following HTTP status responses are used:
- 200 OK: The update was successful.
- 304 Not Modified: The update was not applied (no changes).
- 400 Bad Request: The JSON Patch operation is invalid. Can happen if the path is not allowed or if the current state does not match the expected state.
- 404 Not Found: The entity with the specified ID was not found.
- 500 Internal Server Error: An unexpected error occurred.

## Concurrency Handling
Concurrency is managed through JSON Patch test operations, ensuring that updates are applied only if the current state matches the expected state.

## Typescript Stubs Generation
To generate TypeScript stubs for the frontend, update the api-docs.yaml file with the current API version and run the following commands:

```bash
curl http://localhost:8084/openapi/public/yaml > ./api/api-docs.yaml
./gradlew openApiGenerate
```

## Stubs Usage Example in Frontend
```typescript
import { VersionPatchOpsDTO } from './api';
import { Configuration, **ControllerApi } from './api';

const patch: *PatchOpsDTO = [
  {
    op: 'replace',
    value: 'New Title',
    path: '/title',
  },
  {
    op: 'replace',
    value: 1,
    path: '/author/id',
  }];

const configuration = new Configuration({
    basePath: backendUrl,
});

http.controllerApi = new **ControllerApi(configuration);

const options = {
    headers: {
         Authorization: ....
    },
};

http.controllerApi.updateVersion(1, patch, options).subscribe(
    (response) => {
        console.log(response);
    },
    (error) => {
        console.error(error);
    }
);
```

## Running Locally
To run the application locally, execute the following command:

```bash
./gradlew bootRun
```

## Testing
A simple test [JsonPatchServiceTest](src/test/java/gae/piaz/jsonpatch/JsonPatchApplicationTests.java) is provided to demonstrate the functionality of the JSON Patch service.

```bash
./gradlew test
```
