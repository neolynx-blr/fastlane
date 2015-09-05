drop table if exists inventory.inventory_sync_status;
drop table if exists inventory_master;
drop table if exists vendor_item_differential;
drop table if exists vendor_item_master;
drop table if exists product_master;
drop table if exists discount_type;

create table inventory_sync_status (id serial primary key,vendor_id integer not null,last_sync_version_id integer,last_modified_on timestamp not null  default now());
create table inventory_master (id bigserial primary key,vendor_id int not null,item_code varchar(128),version_id bigint not null,name varchar(256) not null,description varchar(1024),tag_line varchar(256),barcode bigint not null,mrp decimal(8, 2) not null,price decimal(8, 2),image_json varchar(512),discount_type smallint,discount_value decimal(8,2),created_on timestamp not null default now());
create table vendor_item_differential (id serial primary key,vendor_id integer not null,item_code varchar(128),product_id bigint not null,version_id bigint not null,barcode bigint not null,mrp decimal(8, 2) not null,price decimal(8, 2),image_json varchar(512),discount_type smallint,discount_value decimal(8,2),created_on timestamp not null default now()); 
create table vendor_item_master (id serial primary key,vendor_id integer not null,item_code varchar(128),product_id bigint not null,version_id bigint not null,barcode bigint not null,mrp decimal(8, 2) not null,price decimal(8, 2),image_json varchar(512),discount_type smallint,discount_value decimal(8,2),created_on timestamp not null default now()); 
create table product_master (id serial primary key,barcode bigint not null,name varchar(256) not null,description varchar(1024),tag_line varchar(256),vendor_id varchar(4096) not null,image_json varchar(512), constraint u_constraint unique (barcode, vendor_id));
create table discount_type (id serial primary key,name varchar(128) not null,description varchar(1024) not null);

insert into discount_type (name, description) values ('Absolute', 'Applicable discount is absolute value over the MRP price');
insert into discount_type (name, description) values ('Percentage', 'Applicable discount is %  value over the MRP price');

insert into vendor_item_master (vendor_id,item_code,product_id,version_id,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values (1, 'I0011', 1, 1440654001000, 1234567890, 2.63, 2.47, null, null, null, now());
insert into vendor_item_master (vendor_id,item_code,product_id,version_id,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values (1, 'I0012', 2, 1440654001001, 1234567891, 3.63, 3.47, null, null, null, now());

insert into vendor_item_master (vendor_id,item_code,product_id,version_id,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values (1, 'I0001', 3, 21, 1234, 2.63, 2.47, null, null, null, now());
insert into vendor_item_master (vendor_id,item_code,product_id,version_id,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values (1, 'I0004', 6, 4, 1237, 3.63, 3.47, null, null, null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0001',1,'X','X-Description','X-Tagline', 1234, 1.2, 1.0, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0001',11,'X','X-Description','X-Tagline', 1234, 1.3, 1.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0001',21,'X','X-Description','X-TaglineChange', 1234, 1.3, 1.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0001',31,'X','X-DescriptionChange','X-TaglineChange', 1234, 1.3, 1.1, null, null, null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0002',2,'Y','Y-Description','Y-Tagline', 1235, 2.3, 2.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0002',12,'Y','Y-Description','Y-TaglineChange', 1235, 2.3, 2.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0002',22,'Y','Y-Description','Y-TaglineChangeAgain', 1235, 2.3, 2.1, null, null, null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0003',3,'Z','Z-Description','Z-Tagline', 1236, 3.3, 3.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0003',13,'Z','Z-DescriptionChange','Z-TaglineCHange', 1236, 3.4, 3.3, null, null, null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0004',4,'A','A-Description','A-Tagline', 1237, 3.3, 3.1, null, null, null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0005',31,'B','B-Description','B-Tagline', 1238, 4.3, 4.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0006',22,'C','C-Description','C-Tagline', 1239, 5.3, 5.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0007',13,'D','D-Description','D-Tagline', 1240, 6.3, 6.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(1,'I0008',4,'E','E-Description','E-Tagline', 1241, 7.3, 7.1, null, null, null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0001',1,'X','X-Description','X-Tagline', 1234, 1.2, 1.0, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0001',11,'X','X-DescriptionChange','X-TaglineChange', 1234, 1.3, 1.1, null, null, null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0002',2,'Y','Y-Description','Y-Tagline', 1235, 2.3, 2.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0002',12,'Y','Y-DescriptionChange','Y-TaglineChange', 1235, 2.3, 2.1, null, null, null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0003',3,'Z','Z-Description','Z-Tagline', 1236, 3.3, 3.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0003',13,'Z','Z-DescriptionChange','Z-TaglineCHange', 1236, 3.4, 3.3, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0003',23,'Z','Z-DescriptionChangeAgain','Z-TaglineCHangeAgain', 1236, 3.5, 3.4, null, null, null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0004',4,'A','A-Description','A-Tagline', 1237, 3.3, 3.1, null, null, null, now());

insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0005',11,'B','B-Description','B-Tagline', 1238, 4.3, 4.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0006',22,'C','C-Description','C-Tagline', 1239, 5.3, 5.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0007',23,'D','D-Description','D-Tagline', 1240, 6.3, 6.1, null, null, null, now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,discount_type,discount_value,created_on) values(2,'U0008',4,'E','E-Description','E-Tagline', 1241, 7.3, 7.1, null, null, null, now());

insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1234567890, 'Zespri Kiwi - Green', 'Green Kiwifruit has a tangier, tarter flavor. It flesh is bright green, with an edible white to pale green center and tiny black seeds. It contains the potassium of a banana, the vitamin C of two oranges and a large amount fiber as lots of whole grain cereals.', 'Zespri Kiwi - Green, 1 pc', 1, 'http://bigbasket.com/media/uploads/p/l/40024625_1-zespri-kiwi-green.jpg');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1234567891, 'Fresho Apple - Washington', 'Washington Apples are a normal resource of fiber and are fat free. They have cherry to dark red color with red streaks and sometimes have a speckled pattern on its smooth skin. They have anti-oxidants and polynutrients. The apples hold 95 calories. These Washington Apples are crusty, crimson, smooth-skinned, luscious fruits. Washington apples are a natural source of fibre and are fat free. They contain anti-oxidants and polynutrients. Calories = 95 These apples help reduce cholesterol levels and prevent ''heart disease'', ''smoker''s risk'' and aid lung functions.', 'Fresho Apple - Washington, 500 gms (approx. 3-4 pcs)', 1, 'http://bigbasket.com/media/uploads/p/l/10000008_15-fresho-apple-washington.jpg');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1234, 'X-Name', 'X-Description', 'X-Tagline', '1,2', 'X-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1235, 'Y-Name', 'Y-Description', 'Y-Tagline', '1,2', 'Y-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1236, 'Z-Name', 'Z-Description', 'Z-Tagline', '1,2', 'Z-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1237, 'A-Name', 'A-Description', 'A-Tagline', '1,2', 'A-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1238, 'B-Name', 'B-Description', 'B-Tagline', '1,2', 'B-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1239, 'C-Name', 'C-Description', 'C-Tagline', '1,2', 'C-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1240, 'D-Name', 'D-Description', 'D-Tagline', '1,2', 'D-ImageJSON');
insert into product_master (barcode,name,description,tag_line,vendor_id,image_json) values (1241, 'E-Name', 'E-Description', 'E-Tagline', '1,2', 'E-ImageJSON');


select im.*
from inventory_master im
inner join
    (select barcode, vendor_id, max(version_id) version_id from inventory_master group by barcode, vendor_id) in_inner 
on im.barcode = in_inner.barcode 
and im.version_id = in_inner.version_id
and im.vendor_id = in_inner.vendor_id
and im.version_id > (select coalesce (max(version_id), 0) from vendor_item_master where barcode = im.barcode and vendor_id = im.vendor_id)
order by im.vendor_id;