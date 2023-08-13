create table sizes
(
    id   bigserial primary key,
    name varchar(30) unique not null
);

insert into sizes (name)
values ('XXS'),
       ('XS'),
       ('S'),
       ('M'),
       ('L'),
       ('XL'),
       ('XXL'),
       ('XXXL');


create table cart_entries_sizes_quantities_prev_states
(
    cart_entry_id bigint references cart_entries (id) not null,
    quantity      int not null,
    size_id       bigint references sizes (id) not null
);


create table sizes_quantities
(
    id               bigserial primary key,
    quantity         integer not null,
    cart_entry_id    bigint references cart_entries (id),
    item_id          bigint references items (id),
    reserve_entry_id bigint references reserve_entries (id),
    size_id          bigint not null references sizes (id)
);


ALTER TABLE reserve_entries
    DROP COLUMN quantity;

ALTER TABLE items
    DROP COLUMN quantity;

ALTER TABLE cart_entries
    DROP COLUMN prev_quantity;

ALTER TABLE cart_entries
    DROP COLUMN quantity;