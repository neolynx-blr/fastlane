drop table if exists account;
create table account (
	id serial primary key,
	user_name varchar(128) not null,
	password_hash varchar(1024) not null,
	role varchar(64) not null);
	
insert into account (user_name, password_hash, role) values ('nitesh', '1000:d6b9d42984c26f228dae1699fbed0290ffe525239799b1de:2cdd7136d9c7fc61df699d4e8bea34c3e523fa46c89f6f2e', 'Administrator');
insert into account (user_name, password_hash, role) values ('analyst', '1000:c7354b5296f99dac0a7288b1aac6ba08bf0a486920f7c157:4069108b52212a169b7fd432b3e75fe89fcbb06dc0b34a99', 'Analyst');
insert into account (user_name, password_hash, role) values ('vendor', '1000:18ee47af9a316a27155876219f73078e1797e9f7632277b9:6d67b8018cf99d8d51142161fe4b430194437c510ff56a4e', 'Vendor');

drop table if exists tax_type;
create table tax_type (
	id serial primary key,
	name varchar(256) not null,
	description varchar(1024) not null,
	is_percentage boolean not null,
	is_item_level boolean not null,
	is_transaction_level boolean not null);
	
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (1, 'VAT' , 'Applicable VAT', 'f', 't', 'f');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (2, 'Sales Tax' , 'Sales Tax', 'f', 't', 'f');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (3, 'Service Tax' , 'Service Tax', 'f', 't', 'f');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (4, 'VAT' , 'Applicable VAT', 'f', 'f', 't');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (5, 'Sales Tax' , 'Sales Tax', 'f', 'f', 't');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (6, 'Service Tax' , 'Service Tax', 'f', 'f', 't');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (7, 'VAT' , 'Applicable VAT', 't', 't', 'f');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (8, 'Sales Tax' , 'Sales Tax', 't', 't', 'f');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (9, 'Service Tax' , 'Service Tax', 't', 't', 'f');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (10, 'VAT' , 'Applicable VAT', 't', 'f', 't');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (11, 'Sales Tax' , 'Sales Tax', 't', 'f', 't');
insert into tax_type (id, name, description, is_percentage, is_item_level, is_transaction_level) values (12, 'Service Tax' , 'Service Tax', 't', 'f', 't');

drop table if exists discount_type;
create table discount_type (
	id serial primary key,
	name varchar(256) not null,
	description varchar(1024) not null,
	is_item_level boolean not null,
	is_item_aggregate_level boolean not null,
	is_transaction_level boolean not null);
	
insert into discount_type (id, name, description, is_item_level, is_item_aggregate_level, is_transaction_level) 
	values (1, 'Item level absolute amount off', 'Applicable discount is absolute amount over the MRP price on given item. X amount off on the item.', 't', 'f', 'f');
insert into discount_type (id, name, description, is_item_level, is_item_aggregate_level, is_transaction_level) 
	values (2, 'Item level percentage off on MRP', 'Applicable discount is %  value over the MRP price on given item. X% off of the MRP value for the item.', 't', 'f', 'f');
insert into discount_type (id, name, description, is_item_level, is_item_aggregate_level, is_transaction_level) 
	values (3, 'Item level additional free count(s)', 'Applicable discount is free items for the given item. X instances of same or different items free with purchase of this item.', 't', 'f', 'f');
insert into discount_type (id, name, description, is_item_level, is_item_aggregate_level, is_transaction_level) 
	values (4, 'Item aggregate level absolute amount off', 'Applicable discount is absolute amount over the MRP price on sum total of items of given item code.', 'f', 't', 'f');
insert into discount_type (id, name, description, is_item_level, is_item_aggregate_level, is_transaction_level) 
	values (5, 'Item aggregate level percentage off on MRP', 'Applicable discount is % amount over the MRP price on sum total of items of given item code.', 'f', 't', 'f');
insert into discount_type (id, name, description, is_item_level, is_item_aggregate_level, is_transaction_level) 
	values (6, 'Item aggregate level additional free count(s)', 'Applicable discount is free items, same as this or different, for the given count/amount of items of this item-type', 'f', 't', 'f');
insert into discount_type (id, name, description, is_item_level, is_item_aggregate_level, is_transaction_level) 
	values (7, 'Item aggregate level absolute amount off', 'Applicable discount is absolute amount over the total bill amount for the transaction.', 'f', 'f', 't');
insert into discount_type (id, name, description, is_item_level, is_item_aggregate_level, is_transaction_level) 
	values (8, 'Item aggregate level percentage off on MRP', 'Applicable discount is % amount over the total bill amount for the transaction.', 'f', 'f', 't');
insert into discount_type (id, name, description, is_item_level, is_item_aggregate_level, is_transaction_level) 
	values (9, 'Item aggregate level additional free count(s)', 'Applicable discount is free items, of one kind, for the given count/amount of items in the bill or overall bill', 'f', 'f', 't');
	
drop table if exists product_master;
create table product_master (
	id serial primary key,

	name varchar(512) not null,
	barcode varchar(32) not null,

	tag_line varchar(1024),
	description varchar(8192),
	image_json varchar(4096), 

	-- List of vendors who have this product; TODO :: Supports ~400 vendors
	vendor_id varchar(4096) not null,

	constraint pm_uniq_constraint unique (barcode, vendor_id));
	
drop table if exists vendor_item_master;
create table vendor_item_master (
	id serial primary key,
	
	name varchar(512) not null,
	barcode varchar(32) not null,
	item_code varchar(128) not null,

	tag_line varchar(1024),
	benefits varchar(8192), 
	brand_name varchar(512),
	image_json varchar(4096),
	how_to_use varchar(8192),
	description varchar(8192),
	
	category_id varchar(16),
	category_text varchar(512),
	
	-- Captures any additional information specific to vendor like weight, similar items etc, 
	info_json varchar(2048),

	vendor_id integer not null,
	version_id bigint not null,
	product_id integer not null,
	
	-- price = base_price + taxes - discount
	mrp decimal(8, 2),
	price decimal(8, 2),
	base_price decimal(8,2),
	
	tax_json varchar(4096),
	discount_json varchar(4096),
	
	created_on timestamp not null default now(), 
	constraint vim_unique_constraint unique (barcode, vendor_id)
);
		 
drop table if exists vendor_item_history;
create table vendor_item_history (
	id serial primary key,
	
	name varchar(512) not null,
	barcode varchar(32) not null,
	item_code varchar(128) not null,

	tag_line varchar(1024),
	benefits varchar(8192), 
	brand_name varchar(512),
	image_json varchar(4096),
	how_to_use varchar(8192),
	description varchar(8192),
	
	category_id varchar(16),
	category_text varchar(512),
	
	-- Captures any additional information specific to vendor like weight, similar items etc, 
	info_json varchar(2048),

	vendor_id integer not null,
	version_id bigint not null,
	product_id integer not null,
	
	-- price = base_price + taxes - discount
	mrp decimal(8, 2),
	price decimal(8, 2),
	base_price decimal(8,2),
	
	tax_json varchar(4096),
	discount_json varchar(4096),
	
	constraint vih_unique_constraint unique (barcode, vendor_id, version_id)
);
	 
drop table if exists user_detail;
create table user_detail (
	id serial primary key,
	first_name varchar(128),
	last_name varchar(256),
	screen_name varchar(64),
	email varchar(256) unique,
	registered_phone varchar(16) unique,
	other_phone varchar(128),
	device_id varchar(2048));
	
drop table if exists user_location_detail;
create table user_location_detail (
	id serial primary key,
	user_id integer not null,
	address_tag varchar(64),
	address varchar(2048) not null,
	google_map_url varchar(512),
	latitude real,
	longitude real);
	
drop table if exists order_status;
create table order_status (
	id smallint primary key,
	name varchar(32) not null,
	description varchar(256));
	
insert into order_status (id, name, description) values (1, 'Created', 'Order details have been submitted on server side and is ready for payment.');
insert into order_status (id, name, description) values (2, 'Updated', 'Order details have been updated since initial submission and is ready for payment.');
insert into order_status (id, name, description) values (3, 'Pending Pickup', 'Order has been paid for and awaiting in-store pickup.');
insert into order_status (id, name, description) values (4, 'Pending Delivery', 'Order has been paid for and awaiting delivery.');
insert into order_status (id, name, description) values (5, 'Picked, Pending Delivery', 'Order has been paid for, partially picked up and awaiting delivery.');	
insert into order_status (id, name, description) values (6, 'Completed', 'Order is complete.');

drop table if exists delivery_mode;
create table delivery_mode (
	id smallint primary key,
	name varchar(16) not null,
	description varchar(128));
	
insert into delivery_mode (id, name, description) values (1, 'In-Store Pickup', 'Order will be picked up in store.');
insert into delivery_mode (id, name, description) values (2, 'Partial Delivery', 'Order will be partially picked up and partially delivered.');
insert into delivery_mode (id, name, description) values (3, 'Delivery', 'Order will be delivered.');
	
drop table if exists vendor_user_order_mapper;
create table vendor_user_order_mapper (
	id bigserial primary key,
	vendor_id integer not null,
	user_id integer not null,
	order_id varchar(16) not null unique,
	status smallint not null,
	last_modified_on timestamp not null);
		
drop table if exists order_detail;
create table order_detail (
	id bigserial primary key,
	order_id varchar(16) not null unique,
	status integer,
	
	item_list varchar(2048),
	item_list_delivery varchar(2048),
	delivery_address_id integer,
	delivery_mode integer,
	
	vendor_id integer not null,
	server_data_version_id bigint not null,
	device_data_version_id bigint not null,
	
	net_amount real not null,
	tax_amount real,
	taxable_amount real,
	discount_amount real,
	
	created_on timestamp not null,  
	last_modified_on timestamp not null
);

drop table if exists vendor_detail;
create table vendor_detail (
	id serial primary key,
	vendor_id integer not null unique,
	name varchar(512) not null,
	abbr char(3) not null,
	tin varchar(32) not null,
	tagline varchar(512));
	
drop table if exists vendor_location_detail;
create table vendor_location_detail (
	id serial primary key,
	vendor_id integer not null unique,
	address varchar(2048) not null,
	google_map_url varchar(512),
	latitude real,
	longitude real);
	
drop table if exists vendor_contact_detail;
create table vendor_contact_detail (
	id serial primary key,
	vendor_id integer not null unique,
	phone varchar(128),
	mobile varchar(64));
	
drop table if exists vendor_information_cache;
create table vendor_information_cache (
	id serial primary key,
	vendor_id integer not null unique,
	-- vendor_details table
	basic_details varchar(2048),
	-- location and contact details
	contact_details varchar(2048),
	-- product count, last update etc.
	inventory_details varchar(2048)
);


insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values ('1234567890', 'Zespri Kiwi - Green', 'Green Kiwifruit has a tangier, tarter flavor. It flesh is bright green, with an edible white to pale green center and tiny black seeds. It contains the potassium of a banana, the vitamin C of two oranges and a large amount fiber as lots of whole grain cereals.', 'Zespri Kiwi - Green, 1 pc', 1, 'http://bigbasket.com/media/uploads/p/l/40024625_1-zespri-kiwi-green.jpg');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values ('1234567891', 'Fresho Apple - Washington', 'Washington Apples are a normal resource of fiber and are fat free. They have cherry to dark red color with red streaks and sometimes have a speckled pattern on its smooth skin. They have anti-oxidants and polynutrients. The apples hold 95 calories. These Washington Apples are crusty, crimson, smooth-skinned, luscious fruits. Washington apples are a natural source of fibre and are fat free. They contain anti-oxidants and polynutrients. Calories = 95 These apples help reduce cholesterol levels and prevent ''heart disease'', ''smoker''s risk'' and aid lung functions.', 'Fresho Apple - Washington, 500 gms (approx. 3-4 pcs)', 1, 'http://bigbasket.com/media/uploads/p/l/10000008_15-fresho-apple-washington.jpg');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values ('1234', 'X-Name', 'X-Description', 'X-Tagline', '1,2', 'X-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values ('1235', 'Y-Name', 'Y-Description', 'Y-Tagline', '1,2', 'Y-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values ('1236', 'Z-Name', 'Z-Description', 'Z-Tagline', '1,2', 'Z-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values ('1237', 'A-Name', 'A-Description', 'A-Tagline', '1,2', 'A-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values ('1238', 'B-Name', 'B-Description', 'B-Tagline', '1,2', 'B-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values ('1239', 'C-Name', 'C-Description', 'C-Tagline', '1,2', 'C-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values ('1240', 'D-Name', 'D-Description', 'D-Tagline', '1,2', 'D-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values ('1241', 'E-Name', 'E-Description', 'E-Tagline', '1,2', 'E-ImageJSON');

insert into vendor_item_master (vendor_id,item_code,product_id,version_id,barcode,mrp,price,image_json,name,description,tag_line,created_on) values (1, 'I0011', 1, 1440654001000, '1234567890', 2.63, 2.47,  null, 'Zespri Kiwi - Green', 'Green Kiwifruit has a tangier, tarter flavor. It flesh is bright green, with an edible white to pale green center and tiny black seeds. It contains the potassium of a banana, the vitamin C of two oranges and a large amount fiber as lots of whole grain cereals.', 'Zespri Kiwi - Green, 1 pc', now());
insert into vendor_item_master (vendor_id,item_code,product_id,version_id,barcode,mrp,price,image_json,name,description,tag_line,created_on) values (1, 'I0012', 2, 1440654001001, '1234567891', 3.63, 3.47,  null, 'Fresho Apple - Washington', 'Washington Apples are a normal resource of fiber and are fat free. They have cherry to dark red color with red streaks and sometimes have a speckled pattern on its smooth skin. They have anti-oxidants and polynutrients. The apples hold 95 calories. These Washington Apples are crusty, crimson, smooth-skinned, luscious fruits. Washington apples are a natural source of fibre and are fat free. They contain anti-oxidants and polynutrients. Calories = 95 These apples help reduce cholesterol levels and prevent ''heart disease'', ''smoker''s risk'' and aid lung functions.', 'Fresho Apple - Washington, 500 gms (approx. 3-4 pcs)', now());

insert into vendor_item_master (vendor_id,item_code,product_id,version_id,barcode,mrp,price,image_json,name,description,tag_line,created_on) values (1, 'I0001', 3, 21, '1234', 2.63, 2.47,  null,'X-Name', 'X-Description', 'X-Tagline', now());
insert into vendor_item_master (vendor_id,item_code,product_id,version_id,barcode,mrp,price,image_json,name,description,tag_line,created_on) values (1, 'I0004', 6, 3, '1237', 3.63, 3.47,  null, 'A-Name', 'A-Description', 'A-Tagline',now());


select im.name, im.barcode, im.item_code, im.vendor_id, im.version_id
from inventory_master im
inner join
    (select barcode, vendor_id, min(version_id) version_id from inventory_master group by barcode, vendor_id) in_inner 
on im.barcode = in_inner.barcode 
and im.version_id = in_inner.version_id
and im.vendor_id = in_inner.vendor_id
and im.version_id > (select coalesce (max(version_id), 0) from vendor_item_master where barcode = im.barcode and vendor_id = im.vendor_id)
order by im.id limit 5;

select im.id, im.name, im.barcode, im.item_code, im.vendor_id, im.version_id
from inventory_master im
inner join
    (	select barcode, vendor_id, version_id from inventory_master im_inner 
    	where version_id > (select coalesce (max(version_id), 0) from vendor_item_master where vendor_id = im_inner.vendor_id and barcode = im_inner.barcode) 
    ) in_inner 
on im.barcode = in_inner.barcode 
and im.version_id = in_inner.version_id
and im.vendor_id = in_inner.vendor_id order by im.id;

select vih.*
from vendor_item_history vih
inner join
    (	select barcode, vendor_id, version_id from vendor_item_history vih_inner 
    	where version_id >= (select max(version_id) from vendor_item_history where vendor_id = vih_inner.vendor_id and barcode = vih_inner.barcode and version_id <= 9) 
    ) in_inner 
on vih.barcode = in_inner.barcode 
and vih.version_id = in_inner.version_id
and vih.vendor_id = in_inner.vendor_id 
and vih.vendor_id = 289
and vih.version_id <= 9
order by vih.id;

select vvd.vendor_id, vvd.latest_synced_version_id, vvd.valid_version_ids, vim_inner.max_version_id, vvdf.version_id, vvdf.delta_item_codes
from vendor_version_detail vvd 
right join
	(select vendor_id, max(version_id) max_version_id from vendor_item_master group by vendor_id) vim_inner on vvd.vendor_id = vim_inner.vendor_id, 
	vendor_version_differential vvdf
where vvdf.vendor_id = vvd.vendor_id
and vvd.latest_synced_version_id < vim_inner.max_version_id;

select vvd.*, vvdf.*
from vendor_version_differential vvdf, vendor_version_detail vvd 
where vvd.vendor_id = vvdf.vendor_id;

select vim.* from vendor_item_master vim
where vendor_id = 1
and version_id between 1 and 5;

select * from vendor_item_master vim
where vendor_id = 1
and version_id = 2;

select vvd_inner.*, vvdf.* from 
(select vvd.* from vendor_version_detail vvd where vvd.latest_synced_version_id < (select max(version_id) from vendor_item_master vim where vendor_id = vvd.vendor_id)) vvd_inner 
left outer join vendor_version_differential vvdf on vvdf.vendor_id = vvd_inner.vendor_id;

select vvdf.*, vim_inner.latestVersionId from vendor_version_differential vvdf, (select vendor_id, max(version_id) latestVersionId from vendor_item_master group by vendor_id) vim_inner 
where vvdf.last_synced_version_id < (select max(version_id) from vendor_item_master vim where vendor_id = vvdf.vendor_id)
and vim_inner.vendor_id = vvdf.vendor_id;

