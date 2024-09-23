insert into authors (id, name, email) values (1, 'John Doe', 'john.doe@stuff.com');
insert into authors (id, name, email) values (2, 'Jane Doe', 'jane.doe@some.com');

insert into books (id, title, author_id, isbn) values (1, 'Java 101', 1, '123456');
insert into books (id, title, author_id, isbn) values (2, 'Java 102', 1, '123457');
insert into books (id, title, author_id, isbn) values (3, 'Java 103', 1, '123458');

insert into books (id, title, author_id, isbn) values (4, 'Java 104', 2, '123459');
insert into books (id, title, author_id, isbn) values (5, 'Java 105', 2, '123460');
insert into books (id, title, author_id, isbn) values (6, 'Java 106', 2, '123461');
insert into books (id, title, author_id, isbn) values (7, 'Java 107', 2, '123462');

ALTER TABLE books ALTER COLUMN id RESTART WITH 8;
ALTER TABLE authors ALTER COLUMN id RESTART WITH 3;
