//Contains home page builder
var homepageBuilder;
//Contains playlist-track page builder
var playlistBuilder;
//Contains player page builder
var playerBuilder;
//Contains playlists visualized in homepage
var playlists;
//Contains all tracks
var allTracks;
//Contains tracks in visualized playlist
var tracksInPlaylist;
//Contains tracks not in visualized playlist
var tracksNotInPlaylist;
//Contains visualized track
var currentTrack;
//Istantiate controller
var pageOrchestrator = new PageOrchestrator();

/////////////////
//FIRST LOADING//
/////////////////
window.addEventListener("load", () => {
	if (sessionStorage.getItem("user") == null) {
		window.location.href = "login.html";
	} else {
		pageOrchestrator.start();
		pageOrchestrator.showHomepage();
	}
}, false);

///////////////////
//MAIN CONTROLLER//
///////////////////

/**
 * Main Controller
 */
function PageOrchestrator() {

	this.start = function() {

		/////////////////
		//PAGE BUILDERS//
		/////////////////
		homepageBuilder = new Homepage(document.getElementById("Homepage"));
		playlistBuilder = new Playlist(document.getElementById("Playlist"));
		playerBuilder = new Player(document.getElementById("Player"));
		sortingBuilder = new SortingPage(document.getElementById("Sorting"));

		///////////////////////////
		//ELEMENTS OF SINGLE PAGE//
		///////////////////////////

		//Contains playlists visualized in homepage
		playlists = new PlaylistTableBuilder(document.getElementById("PlaylistTable"),
			document.getElementById("PlaylistTableBody"),
			document.getElementById("PlaylistTableError"));
		//Contains all tracks available to users
		allTracks = new TracksCollector(document.getElementById("TracklistContainer"));
		//Contains currently visualized tracks
		tracksInPlaylist = new TracksInPlaylist(document.getElementById("TracksTable"),
			document.getElementById("TracksTableBody"));
		//Contains tracks not in currently selected playlist
		tracksNotInPlaylist = new TracksNotInPlaylist(document.getElementById("TracksNotInPlaylist"), document.getElementById("AddToPlaylistButton"));
		//Contains tracks, its information and its player
		currentTrack = new PlayerTabBuilder(document.getElementById("PlayerBody"), document.getElementById("PlayerHeader"));
		//Currently sorting playlist
		currentSorting = new SortingTableBuilder(document.getElementById("BeforeSorting"), document.getElementById("BeforeSortingHeader"), document.getElementById("BeforeSortingBody"));

		///////////////////////////
		//BACK TO HOMEPAGE BUTTON//
		///////////////////////////
		document.getElementById("HomepageButtonPlaylist").addEventListener("click", () => {
			if (sessionStorage.getItem("user") == null) {
				window.location.href = "login.html";
			} else {
				pageOrchestrator.showHomepage();
			}
		}, false);

		document.getElementById("HomepageButtonSorting").addEventListener("click", () => {
			if (sessionStorage.getItem("user") == null) {
				window.location.href = "login.html";
			} else {
				pageOrchestrator.showHomepage();
			}
		}, false);

		/////////////////
		//LOGOUT BUTTON//
		/////////////////
		document.getElementById("Logout").addEventListener("click", () => {
			window.location.href = "login.html";
			sessionStorage.removeItem("user");
		})
	}

	this.showHomepage = function() {
		playerBuilder.notVisible();
		playlistBuilder.notVisible();
		sortingBuilder.notVisible();
		homepageBuilder.show();
	}

	this.showPlaylistPage = function(idPlaylist) {
		homepageBuilder.notVisible();
		playerBuilder.notVisible();
		sortingBuilder.notVisible();
		playlistBuilder.show(idPlaylist);
	}

	this.showPlayerPage = function(Track, idPlaylist) {
		homepageBuilder.notVisible();
		playlistBuilder.notVisible();
		sortingBuilder.notVisible();
		playerBuilder.show(Track, idPlaylist);
	}

	this.showSortingPage = function(idPlaylist) {
		homepageBuilder.notVisible();
		playlistBuilder.notVisible();
		playerBuilder.notVisible();
		sortingBuilder.show(idPlaylist);
	}
}

//////////////////////
//HOMEPAGE FUNCTIONS//
//////////////////////

/**
 * Show homepage
 */

function Homepage(homepage) {
	this.homepage = homepage;

	this.notVisible = function() {
		this.homepage.style.display = "none";
	}

	this.show = function() {
		this.homepage.style.display = "";
		allTracks.show();
		playlists.show();
	}
}

/**
 * Collect playlists from DB
 * @param playlistTable is id of <table>
 * @param playlistTableBody is id of <tbody>
 * @param playlistTableError is id of <p error>
 */
function PlaylistTableBuilder(playlistTable, playlistTableBody, playlistTableError) {

	this.playlistTable = playlistTable;
	this.playlistTableBody = playlistTableBody;
	this.playlistTableError = playlistTableError;

	//Reset playlistTable
	this.reset = function() {
		playlistTable.innerHTML = "";
	}

	//Set playlistTable not visible
	this.notVisible = function() {
		playlistTable.style.display = "none";
		playlistTable.innerHTML = "";
	}

	//Called when playlistTable is shown
	this.show = function() {
		//Save object for future calls
		let t = this;

		function callBack(x) {
			if (x.readyState == XMLHttpRequest.DONE) {
				//OK
				if (x.status == 200) {
					//Collect playlists from JSON
					playlistsToShow = JSON.parse(x.responseText);

					if (playlistsToShow.length == 0) {
						document.getElementById("NoPlaylist").textContent = "Non è stata creata alcuna playlist";
						document.getElementById("PlaylistTableHead").style.display = "none";
						return;
					}
					else
						//Refresh
						t.update(playlistsToShow);
				}
				//NOT OK
				else {
					window.sessionStorage.removeItem("user");
					return;
				}
			}
		}

		AJAXcall("GET", 'GetPlaylists', callBack, null);
	}

	this.update = function(p) {
		let t = this;

		//Empty table
		t.playlistTableBody.innerHTML = "";
		//Fill table
		p.forEach(
			function(x) {
				let row = document.createElement("tr");
				let cell = document.createElement("td");
				let link = document.createElement("a");
				let sortingButton = document.createElement("button");

				//Insert link in cell
				cell.appendChild(link);
				//Link link to text
				let playlistName = document.createTextNode(x.playlistName);
				link.appendChild(playlistName);
				//Link link to idPlaylist
				link.setAttribute("idPlaylist", x.idPlaylist);

				//Create link to playlist
				link.addEventListener("click", (e) => {
					console.log("Selected playlist:" + x.idPlaylist);
					pageOrchestrator.showPlaylistPage(x.idPlaylist);
				})

				link.href = "#";
				row.appendChild(cell);

				//Append date
				let date = document.createElement("td");
				date.textContent = x.creationDate;
				row.appendChild(date);

				//Append button
				let buttonCell = document.createElement("td");
				sortingButton.classList.add("btn", "btn-outline-primary");
				sortingButton.innerHTML = "Riordino";
				//Action to button
				sortingButton.addEventListener("click", (e) => {
					console.log("Sorting: " + x.playlistName);
					pageOrchestrator.showSortingPage(x.idPlaylist);
				});
				buttonCell.appendChild(sortingButton);
				row.appendChild(buttonCell);

				this.PlaylistTableBody.appendChild(row);
			}
		);
	}
}

/**
 * Collect all tracks available to users (for playlist creation)
 */
function TracksCollector(trackDiv) {
	let userTracks;

	this.reset = function() {
		trackDiv.innerHTML = "";
	}

	this.notVisible = function() {
		trackDiv.style.display = "none";
		trackDiv.innerHTML = "";
	}

	this.update = function() {
		//Empty list
		trackDiv.innerHTML = "";
		//Populate list
		userTracks.forEach(
			function(x) {
				let label = document.createElement("label");
				let checkbox = document.createElement("input");
				let br = document.createElement("br");

				//Setup input type
				checkbox.type = "checkbox";
				//Setup input name for playlist creation
				checkbox.name = "trackSelection";
				//Setup input value
				checkbox.value = (x.idTrack);
				checkbox.checked = false;

				trackDiv.appendChild(label);
				trackDiv.appendChild(checkbox);
				trackDiv.appendChild(br);
				label.appendChild(document.createTextNode((x.title + " - " + x.author)));
				//Used to separate from text value with css
				label.classList.add("labelForCheckbox");
			}
		);
	}

	this.show = function() {
		let t = this;

		function callBack(x) {
			if (x.readyState == XMLHttpRequest.DONE) {
				//OK
				if (x.status == 200) {
					//Collect tracks from JSON
					userTracks = JSON.parse(x.responseText);
					//Refresh
					t.update();
				}
				//NOT OK
				else {
					window.sessionStorage.removeItem("user");
					return;
				}
			}
		}
		AJAXcall("GET", 'GetTracks', callBack);
	}
}

//////////////////////////
//PLAYLIST PAGE FUNCTION//
//////////////////////////

/**
 * Show playlist tracklist
 */
function Playlist(playlist) {
	this.playlist = playlist;

	this.notVisible = function() {
		this.playlist.style.display = "none";
		tracksInPlaylist.reset();
		tracksNotInPlaylist.reset();
	}

	this.show = function(idPlaylist) {
		this.playlist.style.display = "";
		tracksInPlaylist.show(idPlaylist);
		tracksNotInPlaylist.show(idPlaylist);
	}
}

/**
 * This function takes tracks from DB and arrange them in group of 5
 * @param tracksTable is id of <table>
 * @param tracksTableBody is id of <tbody>
 * @param tracksTableError is id of <p error>
 */
function TracksInPlaylist(tracksTable, tracksTableBody, tracksTableError) {
	this.tracksTable = tracksTable;
	this.tracksTableBody = tracksTableBody;
	this.tracksTableError = tracksTableError;

	//Current idPlaylist
	this.idPlaylist = null;
	//Current array of tracks
	this.tracks = null;
	//Current blockSectionIndex
	this.currentBlockIndex = 0;

	this.reset = function() {
		document.getElementById("TracksTableBody").innerHTML = "";
		document.getElementById("TracksTableError").innerHTML = "";
		document.getElementById("NoTracks").innerHTML = "";
	}
	
	this.notVisible = function() {
		document.getElementById("TracksTableBody").innerHTML = "";
		document.getElementById("TracksTableError").innerHTML = "";
		document.getElementById("NoTracks").innerHTML = "";	
	}

	this.show = function(idPlaylist) {
		this.idPlaylist = idPlaylist;
		let t = this;

		function callBack(x) {
			if (x.readyState == XMLHttpRequest.DONE) {
				switch (x.status) {
					//OK
					case 200:
						tracks = JSON.parse(x.responseText);

						if (tracks.length == 0) {
							document.getElementById("NoTracks").style.display = "";
							document.getElementById("NoTracks").textContent = "La playlist non contiene ancora brani musicali";
							document.getElementById("TracksTable").style.display = "none";
							t.update(0, idPlaylist);
							return;
						}
						else {
							document.getElementById("NoTracks").style.display = "none";
							document.getElementById("TracksTable").style.display = "";
							t.update(0, idPlaylist);
						}
						break;
					//Forbidden HTTP
					case 403:
						window.sessionStorage.removeItem("user");
						break;
					//NOT OK
					default:
						t.tracksTableError.textContent = x.responseText;
						break;
				}
			}
		}

		AJAXcall("GET", "GetTracksInPlaylist?idPlaylist=" + this.idPlaylist, callBack);
	}

	this.update = function(currentBlockIndex, idPlaylist) {
		this.tracksTableBody.innerHTML = "";
		tracksTableBody.innerHTML = "";
		//True if there are more tracks to show
		let hasNext = false;
		//True if there are tracks before to show
		let hasBefore = false;

		if (currentBlockIndex < 0) {
			currentBlockIndex = 0;
		}
		//Cast to Int (avoid any possible float)
		currentBlockIndex = Math.trunc(currentBlockIndex);

		//There are more tracks to show
		if (currentBlockIndex * 5 + 5 < tracks.length) {
			hasNext = true;
		}
		//Current block is not the first
		if (currentBlockIndex != 0) {
			hasBefore = true;
		}

		let tracksToShow;
		if (hasNext == false) {
			tracksToShow = tracks.slice(currentBlockIndex * 5, tracks.length);
		}
		else if (hasNext == true) {
			tracksToShow = tracks.slice(currentBlockIndex * 5, currentBlockIndex * 5 + 5);
		}

		//////////////////
		//TABLE BUILDING//
		//////////////////

		let row = document.createElement("tr");

		//Previous tracks button
		if (hasBefore) {
			let beforeButton = document.createElement("button");
			beforeButton.classList.add("btn", "btn-outline-secondary");
			beforeButton.value = "Precedenti";
			beforeButton.innerHTML = "Precedenti";
			row.appendChild(document.createElement("br"));
			row.appendChild(beforeButton);

			beforeButton.addEventListener("click", (e) => {
				tracksInPlaylist.update(currentBlockIndex - 1, idPlaylist)
			})
		}

		tracksToShow.forEach(function(tracks) {
			let currentCell = document.createElement("td");
			//Link to song
			let title = document.createElement("a");
			//Album image
			let image = document.createElement("img");
			//Year
			let yearP = document.createElement("p");
			let button = document.createElement("button");
			let br = document.createElement("br");

			//Define title
			currentCell.appendChild(title);
			title.appendChild(document.createTextNode(tracks.title));
			title.setAttribute("idTrack", tracks.idTrack);
			title.title = tracks.title;
			title.href = "#";

			title.addEventListener("click", (e) => {
				console.log("Selected track: " + tracks.title);
				pageOrchestrator.showPlayerPage(tracks, idPlaylist);
			})

			//Add spacing
			currentCell.appendChild(br);

			//Add image
			currentCell.appendChild(image)
			image.src = tracks.image;
			image.width = 120;
			image.height = 120;

			//Define year
			currentCell.appendChild(yearP);
			yearP.innerHTML = tracks.year;

			//Add class to button
			button.classList.add("btn", "btn-primary");
			button.value = "Rimuovi dalla playlist";
			button.innerHTML = "Remove";
			//Remove from playlist button
			button.addEventListener("click", (e) => {

				function callBack(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						//Cleanup
						playlistBuilder.notVisible();
						//Refresh
						playlistBuilder.show(idPlaylist);
					}
				}

				AJAXcall("POST", "RemoveFromPlaylist?idTrack=" + tracks.idTrack + "&idPlaylist=" + idPlaylist, callBack);
			});

			currentCell.appendChild(button);
			row.appendChild(currentCell);
		})
		//Next button
		if (hasNext) {
			let nextButton = document.createElement("button");
			nextButton.classList.add("btn", "btn-outline-secondary");
			nextButton.value = "Successivi";
			nextButton.innerHTML = "Successivi";
			row.appendChild(document.createElement("br"));
			row.appendChild(nextButton);

			nextButton.addEventListener("click", (e) => {
				tracksInPlaylist.update(currentBlockIndex + 1, idPlaylist)
			})
		}

		tracksTableBody.appendChild(row);
	}
}
/**
 * Collects tracks not in playlist
 * @param tracksToAdd is dropdown list containing all tracks currently not in playlist
 * @param button is button activating add to playlist
 */
function TracksNotInPlaylist(tracksToAdd, button) {
	this.tracksToAdd = tracksToAdd;
	this.button = button;
	let tracksToBeAdded;

	this.reset = function() {
		document.getElementById("TracksNotInPlaylist").innerHTML = "";
		document.getElementById("NoTracksToAdd").innerHTML = "";
		//Listener cleanup
		document.getElementById("AddToPlaylist").innerHTML = "";
	}
	
	this.notVisible = function() {
		document.getElementById("TracksTableBody").innerHTML = "";
		document.getElementById("TracksTableError").innerHTML = "";
		document.getElementById("NoTracks").innerHTML = "";	
	}

	this.show = function(idPlaylist) {
		let t = this;
		this.idPlaylist = idPlaylist;

		function callBack(x) {
			if (x.readyState == XMLHttpRequest.DONE) {
				switch (x.status) {
					//OK
					case 200:
						tracksToBeAdded = JSON.parse(x.responseText);
						t.update(idPlaylist);
						break;
					//Forbidden HTTP
					case 403:
						window.sessionStorage.removeItem("user");
						break;
				}
			}
		}

		AJAXcall("GET", "GetTracksNotInPlaylist?idPlaylist=" + this.idPlaylist, callBack);
	}

	this.update = function(idPlaylist) {
		this.idPlaylist = idPlaylist;

		let select = document.getElementById("TracksNotInPlaylist");

		if (tracksToBeAdded.length == 0) {
			//Hide buttons and tracklist
			document.getElementById("NoTracksToAdd").style.display = "";
			document.getElementById("NoTracksToAdd").textContent = "Non ci sono brani da aggiungere alla playlist";
			document.getElementById("TracksNotInPlaylist").style.display = "none";
		}
		else {
			document.getElementById("NoTracksToAdd").style.display = "none";
			document.getElementById("TracksNotInPlaylist").innerHTML = "";
			document.getElementById("TracksNotInPlaylist").style.display = "";
			tracksToBeAdded.forEach(function(x) {
				//Create option containing current track
				let currentOption = document.createElement("option");
				let text = document.createTextNode(x.title + " - " + x.author);

				//Define option
				currentOption.appendChild(text);
				currentOption.value = x.idTrack;
				//Append option
				select.appendChild(currentOption);
			}
			)

			let addToPlaylistButton = document.createElement("button");
			addToPlaylistButton.classList.add("btn", "btn-primary");
			addToPlaylistButton.innerHTML = "Add to playlist";

			document.getElementById("AddToPlaylist").appendChild(addToPlaylistButton);

			//Add to playlist button
			addToPlaylistButton.addEventListener("click", (e) => {
				let selected = document.getElementById("TracksNotInPlaylist");
				let selectedIdTrack = selected.value;

				function callBack(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						//Cleanup
						playlistBuilder.notVisible();
						//Refresh
						playlistBuilder.show(idPlaylist);
					}
				}

				AJAXcall("POST", "AddToPlaylist?TitleToBeAdded=" + selectedIdTrack + "&idPlaylist=" + idPlaylist, callBack);
			}
			)
		}
	}
}

/////////////////////////
//PLAYER PAGE FUNCTIONS//
/////////////////////////

function Player(Player) {
	this.player = Player;

	this.notVisible = function() {
		this.player.style.display = "none";
		currentTrack.notVisible();
	}

	this.show = function(Track, idPlaylist) {
		this.player.style.display = "";
		currentTrack.notVisible();
		currentTrack.show(Track, idPlaylist);
	}
}

/**
 * Build player for selected track
 * @param playerBody is table body for player
 * @param playerHeader is table header for player
 */
function PlayerTabBuilder(playerBody, playerHeader) {
	let visualizedTrack;

	this.reset = function() {
		playerBody.innerHTML = "";
		playerHeader.innerHTML = "";
		document.getElementById("GoBackButtonDiv").innerHTML = "";
	}

	this.notVisible = function() {
		playerBody.innerHTML = "";
		playerHeader.innerHTML = "";
		document.getElementById("GoBackButtonDiv").innerHTML = "";
	}

	this.update = function() {
		let t = this;

		//Header
		let titleH = document.createElement("th");
		let authorH = document.createElement("th");
		let yearH = document.createElement("th");
		let genreH = document.createElement("th");
		let albumH = document.createElement("th");
		let imageH = document.createElement("th");
		let playerH = document.createElement("th");

		titleH.textContent = "Titolo";
		authorH.textContent = "Autore";
		yearH.textContent = "Anno";
		genreH.textContent = "Genere";
		albumH.textContent = "Album";
		imageH.textContent = "Album cover";
		playerH.textContent = "Player";

		playerHeader.appendChild(titleH);
		playerHeader.appendChild(authorH);
		playerHeader.appendChild(yearH);
		playerHeader.appendChild(genreH);
		playerHeader.appendChild(albumH);
		playerHeader.appendChild(imageH);
		playerHeader.appendChild(playerH);

		//Body
		let row = document.createElement("tr");
		let title = document.createElement("td");
		let author = document.createElement("td");
		let genre = document.createElement("td");
		let album = document.createElement("td");
		let year = document.createElement("td");
		let imageContainer = document.createElement("td");
		let image = document.createElement("img");
		let playerContainer = document.createElement("td");
		let audio = document.createElement("audio");
		let audioSource = document.createElement("source");

		title.textContent = visualizedTrack.title;
		author.textContent = visualizedTrack.author;
		year.textContent = visualizedTrack.year;
		genre.textContent = visualizedTrack.genre;
		album.textContent = visualizedTrack.album;

		image.src = visualizedTrack.image;
		image.width = 120;
		image.height = 120;
		imageContainer.appendChild(image);

		audioSource.src = visualizedTrack.audioTrack;
		audioSource.type = "audio/mpeg";
		audio.controls = true;
		audio.appendChild(audioSource);
		playerContainer.appendChild(audio);

		row.appendChild(title);
		row.appendChild(author);
		row.appendChild(year);
		row.appendChild(genre);
		row.appendChild(album);
		row.appendChild(image);
		row.appendChild(playerContainer);

		playerBody.appendChild(row);
	}

	this.show = function(Track, idPlaylist) {
		let t = this;		
		function callBack(x) {
			if (x.readyState == XMLHttpRequest.DONE) {
				switch (x.status) {
					//OK
					case 200:
						visualizedTrack = JSON.parse(x.responseText);
						t.update()
						break;
					//Forbidden HTTP
					case 403:
						window.sessionStorage.removeItem("user");
						break;
				}
			}
		}
		AJAXcall("GET", "GetTrackInfo?idTrack=" + Track.idTrack, callBack);
		
				
		//////////////////
		//GO BACK BUTTON//
		//////////////////
		let goBackButton = document.createElement("button");
		goBackButton.classList.add("btn", "btn-primary");
		goBackButton.innerHTML = "Go Back"
		//Insert element in div
		document.getElementById("GoBackButtonDiv").appendChild(goBackButton);
		
		goBackButton.addEventListener("click", () => {
			if (sessionStorage.getItem("user") == null) {
				window.location.href = "login.html";
			} else {
				console.log("Selected playlist:" + idPlaylist);
				pageOrchestrator.showPlaylistPage(idPlaylist);
			}
		}, false);	
	}
}

////////////////
//SORTING PAGE//
////////////////

function SortingPage(Sorting) {
	this.sorting = Sorting;

	this.notVisible = function() {
		this.sorting.style.display = "none";
		currentSorting.notVisible();
	}

	this.show = function(idPlaylist) {
		this.sorting.style.display = "";
		currentSorting.show(idPlaylist);
	}
}

/**
 * Build table for sorting
 * @param idPlaylist is id of sorted playlist
 */
function SortingTableBuilder(table, header, body) {
	let tracks;
	let confirmSorting = document.getElementById("ConfirmSorting");
	let resetSorting = document.getElementById("ResetSorting");

	this.notVisible = function() {
		header.style.display = "none";
		body.style.display = "none";

		header.innerHTML = "";
		body.innerHTML = "";
		confirmSorting.innerHTML = "";
		resetSorting.innerHTML = "";
	}

	this.reset = function() {
		header.innerHTML = "";
		body.innerHTML = "";
		confirmSorting.innerHTML = "";
		resetSorting.innerHTML = "";
	}

	this.show = function(idPlaylist) {
		this.idPlaylist = idPlaylist;
		let t = this;

		header.style.display = "";
		body.style.display = "";

		function callBack(x) {
			if (x.readyState == XMLHttpRequest.DONE) {
				switch (x.status) {
					//OK
					case 200:
						tracks = JSON.parse(x.responseText);

						if (tracks.length !== 0) {
							t.update(idPlaylist, false);
						}
						else
							t.update(idPlaylist, true);
						break;
					//Forbidden HTTP
					case 403:
						window.sessionStorage.removeItem("user");
						break;
					//NOT OK
					default:
						t.tracksTableError.textContent = x.responseText;
						break;
				}
			}
		}

		AJAXcall("GET", "GetTracksInPlaylist?idPlaylist=" + idPlaylist, callBack);
	}

	this.update = function(idPlaylist, empty) {
		let t = this;
		//Playlist has some tracks
		if (empty !== true) {
			document.getElementById("NoTracksToBeSorted").style.display = "none";
			//Header
			let idH = document.createElement("th");
			let titleH = document.createElement("th");
			let albumH = document.createElement("th");
			let authorH = document.createElement("th");

			idH.textContent = "ID"
			titleH.textContent = "Titolo";
			albumH.textContent = "Album";
			authorH.textContent = "Autore"

			header.appendChild(idH);
			header.appendChild(titleH);
			header.appendChild(albumH);
			header.appendChild(authorH);

			tracks.forEach(
				function(x) {
					//Body
					let row = document.createElement("tr");
					let idTrack = document.createElement("td");
					let title = document.createElement("td");
					let album = document.createElement("td");
					let author = document.createElement("td");

					idTrack.textContent = x.idTrack;
					title.textContent = x.title;
					author.textContent = x.author;
					album.textContent = x.album;

					row.appendChild(idTrack);
					row.appendChild(title);
					row.appendChild(album);
					row.appendChild(author);

					row.setAttribute("draggable", "true");
					row.setAttribute("ondragstart", "dragit(event)");
					row.setAttribute("ondragover", "dragover(event)");

					body.appendChild(row);
				}
			)

			//Create confirm sorting button
			let button = document.createElement("button");
			button.classList.add("btn", "btn-success");
			button.innerText = "Conferma ordinamento";
			confirmSorting.appendChild(button);

			//Array containing sorted idTracks
			let sortedTracks = [];

			//Add action to button
			button.addEventListener("click", (e) => {
				//Collecting sorted tracks
				for (let row of document.getElementById("BeforeSorting").rows) {
					//Currently read idTrack
					let readIdTrack;
					for (let cell of row.cells) {
						readIdTrack = cell.innerText;
						break;
					}
					sortedTracks.push(readIdTrack);
				}

				//String containing newSort
				let newSort;
				let index = 0;
				newSort = "{";
				sortedTracks.forEach(
					function(x) {
						if (index !== 0)
							newSort += ","
						//Add element to string in order
						newSort += '"' + index + '"' + ":" + x;
						index++;
					});
				newSort += "}";

				let JSONSort = JSON.parse(newSort);

				function callBack(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						//OK
						switch (x.status) {
							case 200:
								window.alert("Ordinamento confermato, ritorna all'homepage o esegui nuove modifiche!");
								return;
							//NOT OK
							case 403:
								window.sessionStorage.removeItem("user");
								return;
							default:
								document.getElementById("SortingError").innerHTML = x.responseText;
						}
					}
				}
				//Using regex because replace() only replace first occurance -> replace all 
				let JSONSafeForHTTPReq = JSON.stringify(JSONSort).replace(/"|{|}/g, "");
				JSONSafeForHTTPReq = JSONSafeForHTTPReq.replace(/,/g, "-");

				AJAXcall("POST", "AddSortingToPlaylist?sorting=" + JSONSafeForHTTPReq + "&idPlaylist=" + idPlaylist, callBack);

				//Empty array
				sortedTracks = [];
			});

			//Create reset sorting button
			let reset = document.createElement("button");
			reset.classList.add("btn", "btn-danger");
			reset.innerText = "Reset ordinamento";
			resetSorting.appendChild(reset);

			reset.addEventListener("click", (e) => {

				function callBack(x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						switch (x.status) {
							case 200:
								window.alert("Reset eseguito!");
								t.reset();
								t.show(idPlaylist);
								return;
							case 403:
								window.sessionStorage.removeItem("user");
								return;
							default:
								document.getElementById("SortingError").innerHTML = x.responseText;
								return;
						}
					}
				}

				AJAXcall("POST", "ResetSortingToPlaylist?idPlaylist=" + idPlaylist, callBack);
			});
		}
		else {
			document.getElementById("NoTracksToBeSorted").style.display = "";
		}
	}
}