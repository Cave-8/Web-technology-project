package it.polimi.tiw.tiwjs.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.tiwjs.beans.Track;
import it.polimi.tiw.tiwjs.beans.User;
import it.polimi.tiw.tiwjs.dao.TrackDAO;
import it.polimi.tiw.tiwjs.beans.Playlist;
import it.polimi.tiw.tiwjs.dao.PlaylistDAO;

@WebServlet("/GetTracksInPlaylist")
@MultipartConfig
public class GetTracksInPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetTracksInPlaylist() {
		super();
	}

	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UnavailableException("Couldn't get DB connection");
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int idPlaylist;
		String error = "";

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		// Check for session error
		if (session.isNew() || session == null || user == null) {
			error = "Session expired!";
			response.setStatus(403);
			response.getWriter().println(error);
			return;
		}

		PlaylistDAO pDAO = new PlaylistDAO(connection);
		TrackDAO tDAO = new TrackDAO(connection);

		try {
			idPlaylist = Integer.parseInt(request.getParameter("idPlaylist"));

			if (!pDAO.playerCanAccessPlaylist(idPlaylist, user.getIdUser())) {
				error += "Errore nell'idPlaylist";
				response.setStatus(505);
				response.getWriter().println(error);
				return;
			}
		} catch (NumberFormatException e) {
			error += "Errore nell'idPlaylist, deve essere un numero";
			response.setStatus(505);
			response.getWriter().println(error);
			return;
		} catch (SQLException e) {
			error += "Errore durante la comunicazione con il DB, riprova";
			response.setStatus(505);
			response.getWriter().println(error);
			return;
		}

		try {
			// Every track in playlist
			ArrayList<Track> tracksInPlaylist = tDAO.getTracksInPlaylist(idPlaylist, user.getIdUser());

			Playlist playlist = pDAO.getPlaylistById(idPlaylist);
			playlist.setIdPlaylist(idPlaylist);

			for (int i = 0; i < tracksInPlaylist.size(); i++) {
				Track currentTrack = tracksInPlaylist.get(i);

				// Reformat image path for visualization
				// Encode in Base64
				int imageTypeSeparator = currentTrack.getImage().indexOf(".");
				String imageType = currentTrack.getImage().substring(imageTypeSeparator,
						currentTrack.getImage().length()) + 1;
				File file = new File(currentTrack.getImage());
				byte[] imageToEncode = FileUtils.readFileToByteArray(file);
				String encodedImage = Base64.getEncoder().encodeToString(imageToEncode);
				// Set as image attribute
				currentTrack.setImage("data:image/" + imageType + ";base64," + encodedImage);
			}

			Gson jsonBuilder = new GsonBuilder().create();
			String jsonFile = null;
			// No sorting
			if (playlist.getSorting() == null) {
				jsonFile = jsonBuilder.toJson(tracksInPlaylist);
			} else {
				// Reading sorting and rearranging tracks
				ArrayList<Track> sortedTracks = new ArrayList<>();
				// Map every track with its idTrack
				ArrayList<Integer> idTracks = (ArrayList<Integer>) tracksInPlaylist.stream().map(x -> x.getIdTrack())
						.collect(Collectors.toList());
				// Splitting sorting in all couple index-idTrack
				String[] indexTrack = playlist.getSorting().split("-");

				for (int i = 0; i < indexTrack.length; i++) {
					// Splitting current couple in index and idTrack
					String[] currentCouple = indexTrack[i].split(":");
					int currentIdTrack = Integer.parseInt(currentCouple[1]);
					// Retrieving index of corresponding idTrack in idTracks
					int indexInTrackList = idTracks.indexOf(currentIdTrack);
					// If indexInTrackList == -1 than corresponding tracks has been removed
					if (indexInTrackList != -1)
						sortedTracks.add(tracksInPlaylist.get(indexInTrackList));
				}
				// Append remaining tracks to end of list
				for (int i = 0; i < tracksInPlaylist.size(); i++) {
					if (!sortedTracks.contains(tracksInPlaylist.get(i)))
						sortedTracks.add(tracksInPlaylist.get(i));
				}

				jsonFile = jsonBuilder.toJson(sortedTracks);
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jsonFile);
		} catch (SQLException e) {
			error += "Error in communicating with DB";
			response.setStatus(505);
			response.getWriter().println(error);
			return;
		}

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
		}
	}
}
