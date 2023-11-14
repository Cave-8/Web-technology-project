/**
 * Asynchronous call to server
 * @param method can be GET or POST
 * @param servlet is URL of called servelt
 * @param callBack is function to call when answer arrives
 * @param object is body containing results
 */
function AJAXcall (method, servlet, callBack, objectToSend) {
	let x = new XMLHttpRequest;
	
	//Create request, by default asynch flag is true
	x.open(method, servlet);
	
	//Event handler
	x.onreadystatechange = function eventHandler() {
		callBack(x);
	}
	
	//By default for GET request body is ignored
	x.send(JSON.stringify(objectToSend));		
}

/**
 * Asynchronous call to server when form is compiled
 * @param method can be GET or POST
 * @param servlet is URL of called servelt
 * @param callBack is function to call when answer arrives
 * @param form is compiled form
 * @param reset to reset form when true (always if not specified)
 */
function AJAXFormcall (method, servlet, callBack, form, reset = true) {
	let x = new XMLHttpRequest;
	
	//Create request, by default asynch flag is true
	x.open(method, servlet);
	
	//Event handler
	x.onreadystatechange = function eventHandler() {
		callBack(x);
	}
	
	//Send form
	if (form !== null)
		x.send(new FormData(form));
	else
		x.send(null);
	
	//Empty form fields	
	if (form !== null && reset === true)
		form.reset();		
}

/**
 * Asynchronous call to server when form is compiled
 * @param method can be GET or POST
 * @param servlet is URL of called servelt
 * @param callBack is function to call when answer arrives
 * @param formData is form in FormData format (useful for Playlist Creation)
 * @param reset to reset form when true (always if not specified)
 */
function AJAXFormDatacall (method, servlet, callBack, formData, reset = true) {
	let x = new XMLHttpRequest;
	
	//Create request, by default asynch flag is true
	x.open(method, servlet);
	
	//Event handler
	x.onreadystatechange = function eventHandler() {
		callBack(x);
	}
	
	//Send form
	if (formData !== null)
		x.send(formData);
	else
		x.send(null);
}