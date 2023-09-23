create table colors
(
    id   bigserial primary key,
    name varchar(30) unique not null
);

insert into colors (name)
    values ('чёрный'),
           ('белый'),
           ('зелёный'),
           ('жёлтый'),
           ('розовый'),
           ('красный'),
           ('бежевый'),
           ('коричневый'),
           ('оранжевый'),
           ('фиолетовый'),
           ('серый'),
           ('хаки'),
           ('голубой');

create table items_colors
(
    item_id  bigint references items (id) not null,
    color_id bigint references colors (id) not null
);