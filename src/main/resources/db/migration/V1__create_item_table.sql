-- V1__initial_schema.sql

-- Items table
CREATE TABLE items (
    uuid uuid NOT NULL,
    description varchar(1000),
    image_urls jsonb NOT NULL,
    name varchar(255) NOT NULL,
    price numeric(10,2) NOT NULL,
    quantity integer NOT NULL,
    status varchar(30) NOT NULL,
    CONSTRAINT items_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::varchar, 'INACTIVE'::varchar, 'OUT_OF_STOCK'::varchar])::text[])))
);

ALTER TABLE ONLY items ADD CONSTRAINT items_pkey PRIMARY KEY (uuid);

-- Orders table

CREATE TABLE orders (
    uuid uuid NOT NULL,
    address text NOT NULL,
    city varchar(50) NOT NULL,
    country varchar(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    customer_notes text,
    email varchar(255) NOT NULL,
    first_name varchar(100) NOT NULL,
    last_name varchar(100) NOT NULL,
    order_number varchar(50) NOT NULL,
    phone varchar(20),
    postal_code varchar(20) NOT NULL,
    status varchar(30) NOT NULL,
    total_amount numeric(10,2) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT orders_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::varchar, 'CONFIRMED'::varchar, 'PROCESSING'::varchar, 'SHIPPED'::varchar, 'DELIVERED'::varchar, 'CANCELLED'::varchar, 'REFUNDED'::varchar])::text[])))
);

ALTER TABLE ONLY orders ADD CONSTRAINT orders_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY orders ADD CONSTRAINT uk_order_number UNIQUE (order_number);


-- Order_items table
CREATE TABLE order_items (
    uuid uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    price numeric(10,2) NOT NULL,
    quantity integer NOT NULL,
    item_uuid uuid NOT NULL,
    order_uuid uuid NOT NULL
);

ALTER TABLE ONLY order_items ADD CONSTRAINT order_items_pkey PRIMARY KEY (uuid);
-- Foreign keys
ALTER TABLE ONLY order_items
    ADD CONSTRAINT fk_order_item_item FOREIGN KEY (item_uuid) REFERENCES items(uuid);

ALTER TABLE ONLY order_items
    ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_uuid) REFERENCES orders(uuid);


-- Order status history table
CREATE TABLE order_status_history (
    uuid uuid NOT NULL,
    changed_by varchar(100),
    created_at timestamp(6) without time zone NOT NULL,
    new_status varchar(30) NOT NULL,
    notes text,
    old_status varchar(30),
    order_uuid uuid NOT NULL,
    CONSTRAINT order_status_history_new_status_check CHECK (((new_status)::text = ANY ((ARRAY['PENDING'::varchar, 'CONFIRMED'::varchar, 'PROCESSING'::varchar, 'SHIPPED'::varchar, 'DELIVERED'::varchar, 'CANCELLED'::varchar, 'REFUNDED'::varchar])::text[]))),
    CONSTRAINT order_status_history_old_status_check CHECK (((old_status)::text = ANY ((ARRAY['PENDING'::varchar, 'CONFIRMED'::varchar, 'PROCESSING'::varchar, 'SHIPPED'::varchar, 'DELIVERED'::varchar, 'CANCELLED'::varchar, 'REFUNDED'::varchar])::text[])))
);

ALTER TABLE ONLY order_status_history ADD CONSTRAINT order_status_history_pkey PRIMARY KEY (uuid);
-- Foreign key
ALTER TABLE ONLY order_status_history ADD CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_uuid) REFERENCES orders(uuid);
