
create schema milkomeda;
create schema milkomeda_r;
create schema milkomeda_01;

-- for milkomeda,milkomeda_r,milkomeda_01
create table t_order
(
	id bigint not null primary key auto_increment,
	user_id bigint null comment 'user id',
	order_no bigint null comment 'order number',
	product_id bigint null comment 'product id',
	product_name varchar(50) null comment 'product name',
	price bigint null comment 'price',
	create_time datetime default current_timestamp null,
	update_time datetime default current_timestamp null
)
comment 'order table';


-- for milkomeda
create table t_order_001
(
    id bigint not null primary key auto_increment,
    user_id bigint null comment 'user id',
    order_no bigint null comment 'order number',
    product_id bigint null comment 'product id',
    product_name varchar(50) null comment 'product name',
    price bigint null comment 'price',
    create_time datetime default current_timestamp null,
    update_time datetime default current_timestamp null
)
    comment 'order table';


