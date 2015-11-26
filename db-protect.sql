drop table if exists inventory_master;
create table inventory_master (

	id bigserial primary key,
	
	name varchar(512) not null,
	-- TODO :: Need to differentiate product versus vendor bar code potentially?
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
(381,'I0001', 1,'SENSODYNE TOOTHPASTE - FRESH GEL (FOR SENSITIVE TEETH)','Sensodyne Toothpaste - Fresh Gel (for Sensitive Teeth), 80 gm','', '9876543210', 100, 95.0, 'http://bigbasket.com/media/uploads/p/l/286939_1-sensodyne-toothpaste-fresh-gel-for-sensitive-teeth.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(381,'I0002', 1,'SENSODYNE TOOTHBRUSH - ULTRA SENSITIVE (SOFT)','Sensodyne Toothbrush - Ultra Sensitive (Soft), 1 nos Pouch','X-Tagline', '9876543211', 50, 47.50, 'http://bigbasket.com/media/uploads/p/l/100517749_1-sensodyne-toothbrush-ultra-sensitive-soft.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(381,'I0003', 1,'AASHIRVAAD ATTA - WHOLE WHEAT','Aashirvaad Atta - Whole Wheat, 10 kg Pouch','X-Tagline', '9876543212', 400, 345.0, 'http://bigbasket.com/media/uploads/p/l/126906_2-aashirvaad-atta-whole-wheat.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(381,'I0004', 1,'NANDINI PURE GHEE','Nandini Pure Ghee, 1 lt Pouch','A taste of purity, Nandini Ghee prepared from pure butter', '9876543213', 385, 358.0, 'http://bigbasket.com/media/uploads/p/l/213273_1-nandini-pure-ghee.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(381,'I0005', 1,'FRESHO POMEGRANATE - KESAR','Fresho Pomegranate - Kesar, 4 pcs ( approx. 800 to 1000 gm )','Pomegranate is a reddish-pink colored fruit packed up with countless seeds which are encompassed by juice-filled sacs.', '9876543214', 139, 125.0, 'http://bigbasket.com/media/uploads/p/l/10000269_10-fresho-pomegranate-kesar.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(381,'I0006', 1,'FRESHO CHILLI GREEN BIG - GRADE A','Fresho Chilli Green Big - Grade A , 100 gm','Green chillies have an attractive fresh flavor and a sharp bite.', '9876543215', 3.0, 3.0, 'http://bigbasket.com/media/uploads/p/l/10000081_14-fresho-chilli-green-big-grade-a.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(381,'I0007', 1,'SUGAR','BB Royal Sugar, 1 kg Pouch','Sugar is completed from organic sugar cane, the grains are light cream highlighted.', '9876543216', 45, 49, 'http://bigbasket.com/media/uploads/p/l/10000447_6-bb-royal-sugar.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(381,'I0008', 1,'BEST SPECIAL RICE','Best Special Rice, 1 kg Pouch','Recommended By India Culinary Forum', '9876543217', 160, 150, 'http://bigbasket.com/media/uploads/p/l/20004911_2-best-special-rice.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(381,'I0009', 1,'FORTUNE MUSTARD OIL - (KACHI GHANI)','','', '9876543218', 152, 142, 'http://bigbasket.com/media/uploads/p/l/276756_3-fortune-mustard-oil-kachi-ghani.jpg',  now());
insert into inventory_master (vendor_id,item_code,version_id,name,description,tag_line,barcode,mrp,price,image_json,created_on) values 
(381,'I0010', 1,'FORTUNE RICE BRAN OIL - HEALTH','Fortune Rice Bran Oil - Health, 2 ltr Can','', '9876543219', 230, 230, 'http://bigbasket.com/media/uploads/p/l/40006905_2-fortune-rice-bran-oil-health.jpg',  now());


drop table if exists vendor_version_detail;
create table vendor_version_detail (
	id serial primary key,
	vendor_id integer not null unique,
	latest_synced_version_id bigint,
	valid_version_ids varchar(16184),
	current_inventory varchar(10000000),
	last_modified_on timestamp not null);
	
drop table if exists vendor_version_differential;
create table vendor_version_differential (
	id serial primary key,
	
	vendor_id integer not null,
	version_id bigint not null,
	is_valid boolean not null default 't',
	last_synced_version_id bigint not null,
	is_this_latest_version boolean not null,
	
	delta_item_codes varchar(64736),
	differential_data varchar(10000000),
	price_differential_data varchar(10000000),
	
	last_modified_on timestamp not null,
	constraint vvd_unique_constraint unique (vendor_id, version_id));

-- Vendor registration should include this

-- Following ID is being hardcoded for Amazon, make sure this is always part of the DB setup
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (281, 0, now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (281, 0, 0, 't', now());

insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (1, 0, now());
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (2, 0, now());
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (71, 0, now());
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (89, 0, now());
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (289, 0, now());
insert into vendor_version_detail (vendor_id, latest_synced_version_id, last_modified_on) values (381, 0, now());

insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (1, 0, 0, 't', now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (2, 0, 0, 't', now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (71, 0, 0, 't', now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (89, 0, 0, 't', now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (381, 0, 0, 't', now());
insert into vendor_version_differential (vendor_id, version_id, last_synced_version_id, is_this_latest_version, last_modified_on) values (289, 0, 0, 't', now());

