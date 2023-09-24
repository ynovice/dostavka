alter table orders
    add column address_id bigint references addresses (id) not null