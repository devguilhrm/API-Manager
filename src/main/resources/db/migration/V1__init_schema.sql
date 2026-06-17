create table users (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    version bigint,
    name varchar(255) not null,
    email varchar(255) not null unique,
    password varchar(255) not null,
    role varchar(30) not null check (role in ('MANAGER', 'SELLER')),
    enabled boolean not null
);

create table products (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    version bigint,
    name varchar(255) not null,
    description varchar(1000),
    price numeric(14, 2) not null check (price >= 0),
    stock_quantity integer not null check (stock_quantity >= 0),
    active boolean not null
);

create table clients (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    version bigint,
    name varchar(255) not null,
    email varchar(255) not null unique,
    phone varchar(255) not null,
    address varchar(500),
    seller_id uuid not null references users(id)
);

create table sales (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    version bigint,
    client_id uuid not null references clients(id),
    seller_id uuid not null references users(id),
    status varchar(30) not null check (status in ('PENDING', 'COMPLETED', 'CANCELLED')),
    payment_method varchar(30) not null check (payment_method in ('CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'BOLETO', 'CASH')),
    discount numeric(14, 2) not null check (discount >= 0),
    total_amount numeric(14, 2) not null check (total_amount >= 0),
    cancel_reason varchar(500)
);

create table sale_items (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    version bigint,
    sale_id uuid not null references sales(id),
    product_id uuid not null references products(id),
    product_name varchar(255) not null,
    quantity integer not null check (quantity > 0),
    unit_price numeric(14, 2) not null check (unit_price >= 0),
    total_price numeric(14, 2) not null check (total_price >= 0)
);

create table sale_status_history (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    version bigint,
    sale_id uuid not null references sales(id),
    previous_status varchar(30) check (previous_status in ('PENDING', 'COMPLETED', 'CANCELLED')),
    new_status varchar(30) not null check (new_status in ('PENDING', 'COMPLETED', 'CANCELLED')),
    reason varchar(500),
    changed_by_user_id uuid not null references users(id)
);

create table refresh_tokens (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    version bigint,
    token varchar(120) not null unique,
    user_id uuid not null references users(id),
    expires_at timestamp with time zone not null,
    revoked boolean not null
);

create index idx_clients_seller_id on clients(seller_id);
create index idx_sales_seller_id on sales(seller_id);
create index idx_sales_status on sales(status);
create index idx_sales_created_at on sales(created_at);
create index idx_sale_items_sale_id on sale_items(sale_id);
create index idx_sale_status_history_sale_id on sale_status_history(sale_id);
create index idx_sale_status_history_changed_by_user_id on sale_status_history(changed_by_user_id);
create index idx_refresh_tokens_user_id on refresh_tokens(user_id);
create index idx_refresh_tokens_revoked_expires_at on refresh_tokens(revoked, expires_at);
