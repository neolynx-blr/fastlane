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
	
drop table if exists inventory_master;
create table inventory_master (

	id bigserial primary key,
	
	name varchar(256) not null,
	-- TODO :: Need to differentiate product versus vendor bar code potentially?
	barcode varchar(32) not null,
	item_code varchar(128) not null,

	tag_line varchar(256),
	benefits varchar(4096), 
	brand_name varchar(256),
	image_json varchar(512),
	how_to_use varchar(4096),
	description varchar(1024),

	vendor_id integer not null,
	version_id bigint not null,
	product_id integer,
	
	-- price = base_price + taxes - discount
	mrp decimal(8, 2),
	price decimal(8, 2),
	base_price decimal(8,2),
	
	tax_json varchar(4096),
	discount_json varchar(4096),
	
	created_on timestamp not null default now(),
	constraint im_item_unique_constraint unique (vendor_id, version_id, item_code),
	constraint im_barcode_unique_constraint unique (vendor_id, version_id, barcode)
);
	
drop table if exists product_master;
create table product_master (
	id serial primary key,
	-- List of vendors who have this product; TODO :: Supports ~400 vendors
	vendor_id varchar(4096) not null,
	barcode varchar(32) not null,
	name varchar(256) not null,
	description varchar(1024),
	tag_line varchar(256),
	image_json varchar(512), 
	constraint pm_uniq_constraint unique (barcode, vendor_id));
	
drop table if exists vendor_item_master;
create table vendor_item_master (
	id serial primary key,
	
	name varchar(256) not null,
	barcode varchar(32) not null,
	item_code varchar(128) not null,

	tag_line varchar(256),
	benefits varchar(4096), 
	brand_name varchar(256),
	image_json varchar(512),
	how_to_use varchar(4096),
	description varchar(1024),

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
	
	name varchar(256) not null,
	barcode varchar(32) not null,
	item_code varchar(128) not null,

	tag_line varchar(256),
	benefits varchar(4096), 
	brand_name varchar(256),
	image_json varchar(512),
	how_to_use varchar(4096),
	description varchar(1024),

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
	 
drop table if exists vendor_version_detail;
create table vendor_version_detail (
	id serial primary key,
	vendor_id integer not null unique,
	latest_synced_version_id bigint,
	valid_version_ids varchar(16184),
	last_modified_on timestamp not null);
	
drop table if exists vendor_version_differential;
create table vendor_version_differential (
	id serial primary key,
	
	vendor_id integer not null,
	version_id bigint not null,
	last_synced_version_id bigint not null,
	is_this_latest_version boolean not null,
	
	delta_item_codes varchar(64736),
	differential_data varchar(10000000),
	price_differential_data varchar(10000000),
	
	is_valid boolean not null default 't',
	last_modified_on timestamp not null,
	constraint vvd_unique_constraint unique (vendor_id, version_id));

-- Vendor registration should include this
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (1, 0, now());
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (2, 0, now());
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (71, 0, now());
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (89, 0, now());
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (281, 0, now());
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (289, 0, now());

insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (1, 0, 0, 't', now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (2, 0, 0, 't', now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (71, 0, 0, 't', now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (89, 0, 0, 't', now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (281, 0, 0, 't', now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (289, 0, 0, 't', now());
	
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
	name varchar(16) not null,
	description varchar(128));
	
insert into order_status (id, name, description) values (1, 'Created', 'Order details have been submitted on server side and is ready for payment.');
insert into order_status (id, name, description) values (2, 'Updated', 'Order details have been updated since initial submission and is ready for payment.');
insert into order_status (id, name, description) values (3, 'Set For Delivery', 'Order has been paid for and ready for delivery.');
insert into order_status (id, name, description) values (4, 'Completed', 'Order has been paid for and is either picked-up or delivered.');	

	
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
	status varchar(32) not null,
	
	item_list varchar(2048),
	item_list_delivery varchar(2048),
	delivery_address_id integer,
	
	vendor_id integer not null,
	generated_barcode varchar(32),
	server_data_version_id bigint not null,
	device_data_version_id bigint not null,
	
	net_amount real not null,
	tax_amount real,
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

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0001',1,'X','X-Description','X-Tagline', '1234', 1.2, 1.0,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0001',11,'X','X-Description','X-Tagline', '1234', 1.3, 1.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0001',21,'X','X-Description','X-TaglineChange', '1234', 1.3, 1.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0001',31,'X','X-DescriptionChange','X-TaglineChange', '1234', 1.3, 1.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0002',2,'Y','Y-Description','Y-Tagline', '1235', 2.3, 2.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0002',12,'Y','Y-Description','Y-TaglineChange', '1235', 2.3, 2.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0002',22,'Y','Y-Description','Y-TaglineChangeAgain', '1235', 2.3, 2.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0003',3,'Z','Z-Description','Z-Tagline', '1236', 3.3, 3.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0003',13,'Z','Z-DescriptionChange','Z-TaglineCHange', '1236', 3.4, 3.3,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0004',4,'A','A-Description','A-Tagline', '1237', 3.3, 3.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0005',31,'B','B-Description','B-Tagline', '1238', 4.3, 4.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0006',22,'C','C-Description','C-Tagline', '1239', 5.3, 5.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0007',13,'D','D-Description','D-Tagline', '1240', 6.3, 6.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(1,'I0008',4,'E','E-Description','E-Tagline', '1241', 7.3, 7.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0001',1,'X','X-Description','X-Tagline', '1234', 1.2, 1.0,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0001',11,'X','X-DescriptionChange','X-TaglineChange', '1234', 1.3, 1.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0002',2,'Y','Y-Description','Y-Tagline', '1235', 2.3, 2.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0002',12,'Y','Y-DescriptionChange','Y-TaglineChange', '1235', 2.3, 2.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0003',3,'Z','Z-Description','Z-Tagline', '1236', 3.3, 3.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0003',13,'Z','Z-DescriptionChange','Z-TaglineCHange', '1236', 3.4, 3.3,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0003',23,'Z','Z-DescriptionChangeAgain','Z-TaglineCHangeAgain', '1236', 3.5, 3.4,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0004',4,'A','A-Description','A-Tagline', '1237', 3.3, 3.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0005',11,'B','B-Description','B-Tagline', '1238', 4.3, 4.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0006',22,'C','C-Description','C-Tagline', '1239', 5.3, 5.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0007',23,'D','D-Description','D-Tagline', '1240', 6.3, 6.1,  null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values(2,'U0008',4,'E','E-Description','E-Tagline', '1241', 7.3, 7.1,  null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(281,'I0001', 1,'SENSODYNE TOOTHPASTE - FRESH GEL (FOR SENSITIVE TEETH)','Sensodyne Toothpaste - Fresh Gel (for Sensitive Teeth), 80 gm','', '9876543210', 100, 95.0, 'http://bigbasket.com/media/uploads/p/l/286939_1-sensodyne-toothpaste-fresh-gel-for-sensitive-teeth.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(281,'I0002', 1,'SENSODYNE TOOTHBRUSH - ULTRA SENSITIVE (SOFT)','Sensodyne Toothbrush - Ultra Sensitive (Soft), 1 nos Pouch','X-Tagline', '9876543211', 50, 47.50, 'http://bigbasket.com/media/uploads/p/l/100517749_1-sensodyne-toothbrush-ultra-sensitive-soft.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(281,'I0003', 1,'AASHIRVAAD ATTA - WHOLE WHEAT','Aashirvaad Atta - Whole Wheat, 10 kg Pouch','X-Tagline', '9876543212', 400, 345.0, 'http://bigbasket.com/media/uploads/p/l/126906_2-aashirvaad-atta-whole-wheat.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(281,'I0004', 1,'NANDINI PURE GHEE','Nandini Pure Ghee, 1 lt Pouch','A taste of purity, Nandini Ghee prepared from pure butter', '9876543213', 385, 358.0, 'http://bigbasket.com/media/uploads/p/l/213273_1-nandini-pure-ghee.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(281,'I0005', 1,'FRESHO POMEGRANATE - KESAR','Fresho Pomegranate - Kesar, 4 pcs ( approx. 800 to 1000 gm )','Pomegranate is a reddish-pink colored fruit packed up with countless seeds which are encompassed by juice-filled sacs.', '9876543214', 139, 125.0, 'http://bigbasket.com/media/uploads/p/l/10000269_10-fresho-pomegranate-kesar.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(281,'I0006', 1,'FRESHO CHILLI GREEN BIG - GRADE A','Fresho Chilli Green Big - Grade A , 100 gm','Green chillies have an attractive fresh flavor and a sharp bite.', '9876543215', 3.0, 3.0, 'http://bigbasket.com/media/uploads/p/l/10000081_14-fresho-chilli-green-big-grade-a.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(281,'I0007', 1,'SUGAR','BB Royal Sugar, 1 kg Pouch','Sugar is completed from organic sugar cane, the grains are light cream highlighted.', '9876543216', 45, 49, 'http://bigbasket.com/media/uploads/p/l/10000447_6-bb-royal-sugar.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(281,'I0008', 1,'BEST SPECIAL RICE','Best Special Rice, 1 kg Pouch','Recommended By India Culinary Forum', '9876543217', 160, 150, 'http://bigbasket.com/media/uploads/p/l/20004911_2-best-special-rice.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(281,'I0009', 1,'FORTUNE MUSTARD OIL - (KACHI GHANI)','','', '9876543218', 152, 142, 'http://bigbasket.com/media/uploads/p/l/276756_3-fortune-mustard-oil-kachi-ghani.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(281,'I0010', 1,'FORTUNE RICE BRAN OIL - HEALTH','Fortune Rice Bran Oil - Health, 2 ltr Can','', '9876543219', 230, 230, 'http://bigbasket.com/media/uploads/p/l/40006905_2-fortune-rice-bran-oil-health.jpg',  now());

select im.id
from inventory_master im
inner join
    (select barcode, vendor_id, max(version_id) version_id from inventory_master group by barcode, vendor_id) in_inner 
on im.barcode = in_inner.barcode 
and im.version_id = in_inner.version_id
and im.vendor_id = in_inner.vendor_id
and im.version_id > (select coalesce (max(version_id), 0) from vendor_item_master where barcode = im.barcode and vendor_id = im.vendor_id)
order by im.vendor_id limit 5;


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

