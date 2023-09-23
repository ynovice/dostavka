alter table reserves rename to orders;

alter table reserve_entries
    rename column reserve_id to order_id;
alter table reserve_entries rename to order_entries;
