
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


create table job_inspection
(
    id bigint(32) not null,
    topic varchar(32) not null,
    application_name varchar(20) not null,
    queue_type tinyint(1) not null comment '0: DelayQueue, 1: ReadyQueue, 2: NoneQueue, 3: DeadQueue',
    bucket_index tinyint(1) default -1 null,
    had_retry_count tinyint(1) default 0 null,
    need_re_push tinyint(1) default 0 null comment '0: No, 1: Yes',
    execution_time datetime default null null comment 'time of next execution',
    push_time datetime default now() null,
    update_time datetime default now() null,
    constraint job_inspection_pk
        primary key (id)
) comment 'job状态描述表';

create index job_inspection_push_time_index
    on job_inspection (push_time);

create index job_inspection_update_time_index
    on job_inspection (update_time desc);


