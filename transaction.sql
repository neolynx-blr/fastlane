/curator/cart/{user-id}/{vendor-id}/init
Returns: 
	- If all is fine, simply return cart-id (globally unique)
	- Else, return error code (unknown user-id or vendor-id or existing initialized cart for this combination)

/curator/cart/{id}/seen/{barcode}
/curator/cart/{id}/reset/{barcode}
/curator/cart/{id}/set/{barcode}/{count}
Returns: 
	- Basic success/failure indication
	- Response can indicate item count etc just for verification on app side, in case any inconsistency got introduced
	
/curator/cart/{id}/refresh
	- In case some inconsistency is found, just send the whole cart to the server side to being both in sync

/curator/cart/{id}/order 

/curator/cart/{id}/update

/curator/cart/{id}/status/{id} 



