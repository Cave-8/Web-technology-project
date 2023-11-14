document.getElementById("CreateTrackButton").addEventListener("click", (e) => {
	console.log("Received track creation request!");

	//Get create playlist form
	let form = document.getElementById("CreateTrackForm");

	if (form.checkValidity()) {
		//Take values in field
		let title = document.getElementById("Title").value;
		let author = document.getElementById("Author").value;
		let year = document.getElementById("Year").value;
		let album = document.getElementById("Album").value;
		let genre = document.getElementById("Genre").value;

		//Check values
		if (isNaN(year)) {
			document.getElementById("CreateTrackError").textContent = "L'anno inserito non è un numero!";
			return;
		}
		if (year < 0 || year > (new Date().getFullYear())) {
			document.getElementById("CreateTrackError").textContent = "L'anno non è valido!";
			return;
		}
		if (title.length > 255 || author.length > 255 || album.length > 255) {
			document.getElementById("CreateTrackError").textContent = "Ricontrolla la lunghezza dei valori";
			return;
		}
		if (title == null || author == null || year == null || album == null || genre == null) {
			document.getElementById("CreateTrackError").textContent = "Per favore inserisci un contenuto per ogni campo";
			return;
		}

		function callBack(x) {
			if (x.readyState == XMLHttpRequest.DONE) {
				switch (x.status) {
					case 200:
						console.log("Status of request:" + x.status);
						console.log("Track created successfully");
						document.getElementById("CreateTrackError").innerHTML = "";
						allTracks.show();
						break;

					case 403:
						//HTTP forbidden
						window.sessionStorage.removeItem("user");
						break;

					default:
						document.getElementById("CreateTrackError").textContent = x.responseText;
				}
			}
		}

		//Create track
		AJAXFormcall("POST", "CreateTrack", callBack, form);

		form.reset();
	}
	else
		form.reportValidity();
})