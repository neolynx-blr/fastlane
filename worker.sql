/curator/worker/session/init/{vendor-id}/{user-id}/{passwd}
    - Returns: If success, session-id. Appropriate error otherwise

Request: vendor-id, user-id, password
Response:
Success:
	- Return: session-id
Failure:
	- Error code (Invalid user-password, Not valid for vendor, Already working, Status is terminated)  
  
/curator/worker/session/{id}/terminate
/curator/worker/session/{id}/pause
    - Returns: Basic success/failure indication
    - Error code in case of failures
    
/curator/worker/session/{id}/quota/pull
    - Return set of optimized carts and their items on which worked should working working
    - <Cart>
    	- User Detail
    	- Fetch <Items>
    	- Not to fetch yet <Items>
    	- Status (Open, Closed)
    	- If status = closed
    		- Pending time to delivery
    		- Deliver Now? 
    - For every item that supports multiple MRPs, 
    	- Beside other things, send selected MRP if item supports multiple MRP
    
/curator/worker/session/{id}/quota/push
	- <Cart>
		- Did fetch <Items>
		- Didn't fetch <Items> {Can be inferred}
		- Did I deliver?
	- Response on success would be as if next pull request is made. Call "pause" before this, if no more work desired
    - For every item that supports multiple MRPs,
    	- Send the updated MRP if any

/curator/worker/session/{id}/order/{id}/update (this will include the order closure as well)
    - Returns: For success, simple confirmation
    - On delivery of cart, user want's to edit items (+ or -)
    - User is not allowed to edit a completed order, but can change the order via worker's app
    - Once updated by worked, post review, update cart details on user's app & allow for payment
    - Update on user's app may be pulled from server or pushed from worker app via QR code  

/curator/worker/session/{id}/order/{id}/status/{id}
    - Returns: Basic success/failure indication
    - Request
    	- 
    - Indicates that the cart is delivered; User enabled to make payment
    - Indicate cart if paid for, user exited
    
    
    
    
    
    
    
    
    
    
	
	
drop table if exists worker_role_type;
create table worker_role_type (
	id serial primary key,
	name varchar(256) not null,
	description varchar(1024) not null);
	
insert into worker_role_type (id, name, description) values (1, 'Admin' , 'All possible permissions');
insert into worker_role_type (id, name, description) values (2, 'Fetch' , 'Responsible for fetching items and preparing the cart');
insert into worker_role_type (id, name, description) values (3, 'Deliver' , 'Responsible for delivering the items to the user once cart is complete');
insert into worker_role_type (id, name, description) values (4, 'Fetch & Deliver' , 'Responsible for fetching items, preparing cart & delivering to the user');
insert into worker_role_type (id, name, description) values (5, 'Doorman' , 'Responsible for checking payment confirmation before user exits');
	
drop table if exists worker_account;
create table worker_account (
	id serial primary key,
	vendor_id integer not null,
	user_id varchar(128) not null,
	password_hash varchar(1024) not null,
	role_id integer not null);

insert into worker_account (id, user_id, password_hash, role_id) values (1, 281, 'Nitesh', 'Passwd', 1);

drop table if exists worker_session;
create table worker_session (
	id serial primary key,
	worker_id integer not null,
	session_id varchar(64) not null,
	completed_cart_id_csv varchar(8192),
	start_time timestamp not null default now(),
	end_time timestamp);

drop table if exists worker_session;
create table worker_session (
	id serial primary key,
	worker_id integer not null,
	session_id varchar(64) not null,
	active_cart_id_csv varchar(8192));
	



drop table if exists worker_session_history;
create table worker_session_history (
	id serial primary key,
	worker_id integer not null,
	session_id varchar(64) not null,
	cart_id_csv varchar(8192),
	start_time timestamp not null,
	end_time timestamp not null);
	


	



	
/curator/worker/session/init/{vendor-id}/{user-id}/{passwd}
	- Returns: If success, session-id. Appropriate error otherwise
	
/curator/worker/session/{id}/terminate
/curator/worker/session/{id}/pause
	- Returns: Basic success/failure indication
	
/curator/worker/session/{id}/quota/pull
	- Return set of optimized carts and their items on which worked should working working
	
/curator/worker/session/{id}/quota/push
	- Returns: Indicates the items picked and not
	- Ensure that in case of multi MRP item, right MRP is picked in the order
	- Return success/failure indication

/curator/worker/session/{id}/order/{id}/update (this will include the order closure as well)
	- Returns: For success, simple confirmation
	- On delivery of cart, user want's to edit items (+ or -)
	- User is not allowed to edit a completed order, but can change the order via worker's app
	- Once updated by worked, post review, update cart details on user's app & allow for payment
	- Update on user's app may be pulled from server or pushed from worker app via QR code  

/curator/worker/session/{id}/order/{id}/status/{id}
	- Returns: Basic success/failure indication
	- Indicates that the cart is delivered; User enabled to make payment
	- Indicate cart if paid for, user exited




    