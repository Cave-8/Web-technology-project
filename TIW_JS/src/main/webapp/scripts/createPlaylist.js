/**
 * Create a new playlist and insert selected tracks
 */
document.getElementById("CreatePlaylistButton").addEventListener("click", (e) => {

	//Get create playlist form
	let form = document.getElementById("CreatePlaylistForm");

	if (form.checkValidity()) {
		let playlistName = document.getElementById("PlaylistName").value;
		//Take all checked tracks
		let selectedTracks = document.getElementsByName("trackSelection");

		//Create Form to send
		let FormToSend = new FormData(form);
		//Append all selected tracks
		for (let i = 0; i < selectedTracks.length; i++) {
			if (selectedTracks[i].checked)
				//"On"" is HTML value for checked check box
				FormToSend.append(selectedTracks[i].value, "on");
			//Reset checkbox
			selectedTracks[i].checked = false;	
		}
		
		if (playlistName == null) {
			document.getElementById("CreatePlaylistError").textContent = "Per favore inserisci un titolo";
			return;
		}
		if (selectedTracks.length < 1) {
			document.getElementById("CreatePlaylistError").textContent = "Per favore seleziona almeno una traccia";
			return;
		}

		function callBack(x) {
			if (x.readyState == XMLHttpRequest.DONE) {
				switch (x.status) {
					//OK
					case 200:
						//Show updated list
						console.log("Received playlist creation request!");
						document.getElementById("CreatePlaylistError").innerHTML = "";
						playlists.show();
						break;
					case 403:
						//HTTP forbidden
						window.sessionStorage.removeItem("user");
						break;
					//NOT OK
					default:
						let error = JSON.parse(x.responseText);
						document.getElementById("CreatePlaylistError").textContent = error;
						break;
				}
			}
		}
		
		//Create playlist
		AJAXFormDatacall("POST", "CreatePlaylist", callBack, FormToSend);
		form.reset();
	}
	else
		form.reportValidity();
})