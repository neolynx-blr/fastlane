drop table if exists inventory.inventory_sync_status;
drop table if exists inventory_master;
drop table if exists vendor_item_differential;
drop table if exists vendor_item_master;
drop table if exists product_master;
drop table if exists discount_type;

create table inventory_sync_status (id serial primary key,vendor_id integer not null,last_sync_version_id integer,last_modified_on timestamp not null);
create table inventory_master (id bigserial primary key,vendor_id int not null,item_code varchar(128),version_id bigint not null,name varchar(256) not null,description varchar(1024),tag_line varchar(256),barcode bigint not null,mrp decimal(8, 2) not null,price decimal(8, 2),image_json varchar(512),discount_type smallint,discount_value decimal(8,2),created_on timestamp not null);
create table vendor_item_differential (id serial primary key,vendor_id integer not null,item_code varchar(128),product_id bigint not null,version_id bigint not null,barcode bigint not null,mrp decimal(8, 2) not null,price decimal(8, 2),image_json varchar(512),discount_type smallint,discount_value decimal(8,2),created_on timestamp not null); 
create table vendor_item_master (id serial primary key,vendor_id integer not null,item_code varchar(128),product_id bigint not null,version_id bigint not null,barcode bigint not null,mrp decimal(8, 2) not null,price decimal(8, 2),image_json varchar(512),discount_type smallint,discount_value decimal(8,2),created_on timestamp not null); 
create table product_master (id serial primary key,barcode bigint not null unique,name varchar(256) not null,description varchar(1024),tag_line varchar(256),vendor_id bigint not null,image_json varchar(512));
create table discount_type (id serial primary key,name varchar(128) not null,description varchar(1024) not null);

insert into discount_type (name, description) values ('Absolute', 'Applicable discount is absolute value over the MRP price');
insert into discount_type (name, description) values ('Percentage', 'Applicable discount is %  value over the MRP price');

insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1234567890, 'Zespri Kiwi - Green', 'Green Kiwifruit has a tangier, tarter flavor. It flesh is bright green, with an edible white to pale green center and tiny black seeds. It contains the potassium of a banana, the vitamin C of two oranges and a large amount fiber as lots of whole grain cereals.', 'Zespri Kiwi - Green, 1 pc', 1, 'http://bigbasket.com/media/uploads/p/l/40024625_1-zespri-kiwi-green.jpg');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1234567891, 'Fresho Apple - Washington', 'Washington Apples are a normal resource of fiber and are fat free. They have cherry to dark red color with red streaks and sometimes have a speckled pattern on its smooth skin. They have anti-oxidants and polynutrients. The apples hold 95 calories. These Washington Apples are crusty, crimson, smooth-skinned, luscious fruits. Washington apples are a natural source of fibre and are fat free. They contain anti-oxidants and polynutrients. Calories = 95 These apples help reduce cholesterol levels and prevent ''heart disease'', ''smoker''s risk'' and aid lung functions.', 'Fresho Apple - Washington, 500 gms (approx. 3-4 pcs)', 1, 'http://bigbasket.com/media/uploads/p/l/10000008_15-fresho-apple-washington.jpg');

insert into vendor_item_master (vendor_id,item_code,product_id,version_id,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values (1, 'I0001', 1, 1, 1234567890, 2.63, 2.47, null, null, null, now());
insert into vendor_item_master (vendor_id,item_code,product_id,version_id,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values (1, 'I0002', 2, 1, 1234567891, 3.63, 3.47, null, null, null, now());