CREATE TABLE materials
(
    id      BIGSERIAL      PRIMARY KEY,
    name    VARCHAR(30)    UNIQUE NOT NULL
);

INSERT INTO materials (name)
VALUES ('лён'),
       ('полиэстр'),
       ('хлопок'),
       ('шёлк'),
       ('шерсть');

CREATE TABLE items_materials
(
    item_id         BIGINT     REFERENCES items (id) NOT NULL,
    material_id     BIGINT     REFERENCES materials (id) NOT NULL
);