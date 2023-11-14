/**
 * Login script
 */
document.getElementById("login").addEventListener('click', (e) => {
	console.log("Received login event");

	//Get login form
	let form = document.getElementById("loginForm");

	//Manage answer
	let callBack = function(x) {
		//When request is done
		if (x.readyState == XMLHttpRequest.DONE) {
			console.log("Result of login request: " + x.status);
			//OK
			if (x.status == 200) {
				sessionStorage.setItem('user', x.responseText);
				window.location.href = "homepage.html"
			}
			//Not OK
			else
				window.alert("Errore negli username/password inseriti");
		}
	}

	//HTML check for parameters
	if (form.checkValidity()) {
		//Method -> POST
		//Servlet called -> Login
		//CallBack function -> redirect to homepage
		//Form sent -> Login form
		AJAXFormcall("POST", 'Login', callBack, form);
	}
	else
		form.reportValidity();
}
)