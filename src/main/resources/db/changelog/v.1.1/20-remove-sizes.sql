ALTER TABLE cart_entries
    ADD COLUMN
        quantity INT NOT NULL DEFAULT 0;

ALTER TABLE cart_entries
    ADD COLUMN
        prev_quantity INT;

ALTER TABLE items
    ADD COLUMN
        quantity INT NOT NULL DEFAULT 0;

ALTER TABLE reserve_entries
    ADD COLUMN
        quantity INT NOT NULL DEFAULT 0;


DROP TABLE sizes_quantities;
DROP TABLE cart_entries_sizes_quantities_prev_states;
DROP TABLE sizes;