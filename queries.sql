
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

