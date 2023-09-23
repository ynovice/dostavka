alter table orders rename to reserves;

alter table order_entries
    rename column order_id to reserve_id;
alter table order_entries rename to reserve_entries;
