-- 创建数据库
create schema milkomeda;
create schema milkomeda_r;

-- 创建表
create table t_order
(
	id bigint not null primary key auto_increment,
	user_id bigint null comment '用户id',
	order_no bigint null comment '订单号',
	product_id bigint null comment '产品id',
	product_name varchar(50) null comment '产品名',
	price bigint null comment '价格（分）',
	create_time datetime default current_timestamp null,
	update_time datetime default current_timestamp null
)
comment '订单表';

