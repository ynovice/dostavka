create table addresses (
    id              bigserial primary key,
    lat             double precision not null,
    lon             double precision not null,
    representation  varchar(1000) not null,
    created_at      timestamp(6) not null,
    user_id         bigint references users (id)
);