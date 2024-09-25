# Spring Boot JSON Patch

This repository demonstrates how to apply 
[JSON Patch](https://datatracker.ietf.org/doc/html/rfc6902) to JPA entities in a Spring Boot Application.
The approach is designed to be generic, reusable, and optimized. I hope.

## Table of Contents
- [Tech Used](#tech-used)
- [Core Components](#core-components)
- [How to apply JSON Patch to ANY JPA entity](#how-to-apply-json-patch-to-any-jpa-entity)
- [API Documentation](#api-documentation)
- [Concurrency Handling](#concurrency-handling)
- [Typescript Stubs Generation](#typescript-stubs-generation)
- [Stubs Usage Example in Frontend](#stubs-usage-example-in-frontend)
- [Testing](#testing)
- [Not so Frequently Asked Questions (NSFAQ)](#not-so-frequently-asked-questions-nsfaq)

## Tech Used

The project uses the following technologies and libraries:
- Java 22
- JSON Patch, as implemented by the [com.flipkart.zjsonpatch:zjsonpatch](https://github.com/flipkart-incubator/zjsonpatch) library
- Spring Boot and related libs version 3.3.4
- Swagger and OpenAPI for API documentation 3.0.1
- H2 Database created automatically and automatically populated with data for testing purposes
- Lombok

## Core Components
The core components of the project are:
- [JsonPatchService.java](src/main/java/gae/piaz/jsonpatch/service/core/JsonPatchService.java): The service class that applies JSON Patch operations to JPA entities.
- [JsonPatchUpdate.java](src/main/java/gae/piaz/jsonpatch/service/core/JsonPatchUpdate.java): An annotation that documents the API endpoints and specifies the allowed paths in the endpoint description.
- [AbstractPatchService.java](src/main/java/gae/piaz/jsonpatch/service/core/AbstractPatchService.java): A generic service class that provides the patch functionality for JPA entities.
- [OpenApiConfiguration.java](src/main/java/gae/piaz/jsonpatch/config/OpenApiConfiguration.java): A configuration class that generates OpenAPI documentation with the allowed paths specified in the JSON Patch operation.

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
(in future releases those beans could be auto-generated... stay tuned)

2) Create a service class that extends the [AbstractPatchService.java](src/main/java/gae/piaz/jsonpatch/service/core/AbstractPatchService.java) class and implements all the abstract methods:
The First parameter of the Abstract class generic is the JPA entity, the second is the Bean class.

```java
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
```

The [BookService.java](src/main/java/gae/piaz/jsonpatch/service/BookService.java) class demonstrates how to use the [BookPatchService.java](src/main/java/gae/piaz/jsonpatch/service/patch/BookPatchService.java) class.

```java
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
```

The controller class [BookController.java](src/main/java/gae/piaz/jsonpatch/controller/BookController.java) demonstrates how to use the [BookService.java](src/main/java/gae/piaz/jsonpatch/service/BookService.java) class.String

```java
@RestController
@RequestMapping("/api/v1/books")
@AllArgsConstructor
@CrossOrigin
public class BookController {

    private final BookService bookService;

    @PatchMapping(
            path = "/{id}",
            consumes = APPLICATION_JSON_PATCH_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonPatchUpdate(allowedPaths = {"title", "author/id", "isbn"})
    public ResponseEntity<BookDTO> updateBook(
            @PathVariable("id") Integer bookId, @RequestBody JsonNode patch)
            throws JsonPatchServerError {
        return ResponseEntity.ok(bookService.updateBook(bookId, patch));
    }
}
```

## API Documentation
The API documentation is generated using Swagger. To access the Swagger UI, navigate to:
- `http://localhost:8084/swagger-ui/index.html`

For OpenAPI YAML documentation:
- JSON: `http://localhost:8084/openapi/public`
- YAML: `http://localhost:8084/openapi/public/yaml`

The generated documentation example can be found committed in the [api-docs.yaml](api/api-docs.yaml) file.

The [JsonPatchUpdate.java](src/main/java/gae/piaz/jsonpatch/service/core/JsonPatchUpdate.java) annotation documents the endpoints.
Using the annotation parameters, we can specify the path allowed in the JSON Patch operation.

E.g:

```java
@JsonPatchUpdate(
    allowedPaths = {
        "name -> change the name of the author",
        "books/- -> add and remove items from the books of this author",
        "books/-/title -> update an author's book title",
        "books/-/isbn -> update an author's book isbn"
    })
```

Thanks to the custom [OpenApiConfiguration.java](src/main/java/gae/piaz/jsonpatch/config/OpenApiConfiguration.java) class, the Swagger UI displays the allowed paths in the JSON Patch operation:

![swagger-example.png](swagger-example.png)

## Concurrency Handling
Concurrency is managed through [JSON Patch test](https://jsonpatch.com/#test) operations, ensuring that updates are applied only if the current state matches the expected state.

## Typescript Stubs Generation
To generate TypeScript stubs for the frontend, update the api-docs.yaml file with the current API version and run the following commands:
(Or you can use the openapi-generator-cli to generate the stubs, I prefer this way because it's easier)

```bash
curl http://localhost:8084/openapi/public/yaml > ./api/api-docs.yaml
./gradlew openApiGenerate
```

## Stubs Usage Example in Frontend

In the [frontend](frontend) directory, the generated TypeScript stubs are used to demonstrate how to apply JSON Patch operations to a JPA entity in a frontend application:

```typescript
import {JsonPatchItem, JsonPatchOps} from './api';
import { Configuration, BookControllerApi } from './api';

const patch: JsonPatchItem[] = [
    {
        op: JsonPatchOps.Replace,
        value: 'New Title',
        path: '/title',
    },
    {
        op: JsonPatchOps.Replace,
        value: 1,
        path: '/author/id',
    }];

const configuration = new Configuration({
    basePath: "http://localhost:8083",
});

const controllerApi = new BookControllerApi(configuration);

const options = {
    headers: {
        Authorization: 'Bearer YOUR_TOKEN_HERE'
    },
};

controllerApi.updateBook(1, patch, options)
    .then((response) => {
        console.log(response.data);
    })
    .catch((error) => {
        console.error(error);
    });
```

## Testing
A simple test [JsonPatchServiceTest](src/test/java/gae/piaz/jsonpatch/JsonPatchApplicationTests.java) 
is provided to demonstrate the functionality of the JSON Patch service.

Otherwise, you can start the application locally:

```bash
./gradlew test
```

And then run the Typescript script in the frontend directory:

```bash
cd frontend
npm install
npm start
```

## Not so Frequently Asked Questions (NSFAQ)

**Why patching beans instead of using the entity directly?**
- The entity is a JPA managed object, and it's not a good practice to update it directly.
- The entity can have fields that should not be updated, e.g: id, created_at, updated_at, etc.
- The entity can have fields that should be updated only by specific roles, e.g: admin, owner, etc.
- Serializing the entity to JSON can cause issues with lazy loading and circular references.

**Is it a good practice to use the entity directly in the controller instead of DTO?**
- No.

**Why not calling the class DTOs instead of Beans for applying json Patch?**
-The DTOs are used to transfer data between the frontend and backend, while the Beans are used to update the entity.

**Why documenting the allowed paths in the annotation?**
- To provide a clear and concise documentation of the API endpoints.
- To ensure that the frontend developers know which paths and operations are allowed in the JSON Patch operation, even if Patch are not restricted in the stubs generation.

**Is it a good practice to add items to a JPA entity @OneToMany collection using JSON Patch?**
- I did this for the Author -> N BOOK in this example [AuthorPatchService.java](src/main/java/gae/piaz/jsonpatch/service/patch/AuthorPatchService.java), but I think it should be way easier to build a separate endpoint to add and remove items to a collection.
The mapping to bean and back, the persist and the cascade are not that easy to handle.