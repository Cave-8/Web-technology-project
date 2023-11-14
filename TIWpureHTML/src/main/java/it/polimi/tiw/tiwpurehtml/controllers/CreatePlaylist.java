package it.polimi.tiw.tiwpurehtml.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.tiwpurehtml.beans.Track;
import it.polimi.tiw.tiwpurehtml.beans.User;
import it.polimi.tiw.tiwpurehtml.dao.PlaylistDAO;
import it.polimi.tiw.tiwpurehtml.dao.TrackDAO;

@WebServlet("/CreatePlaylist")
public class CreatePlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

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
			throw new UnavailableException("Couldn't get db connection");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);

		////////////////////////
		// SESSION-USER CHECK //
		////////////////////////

		if (session == null) {
			response.sendRedirect("/TIWpureHTML/index.html");
			return;
		}

		User userCreator = (User) session.getAttribute("user");

		if (userCreator == null) {
			response.sendRedirect("/TIWpureHTML/index.html");
			return;
		}

		// Error is used to collect all mistakes done by user in playlist creation
		String error = "";

		String playlistName = request.getParameter("Title");
		java.sql.Date creationDate = new java.sql.Date(Calendar.getInstance().getTime().getTime());
		int idUser = userCreator.getIdUser();

		//////////////////////
		// PARAMETERS CHECK //
		//////////////////////

		if (playlistName == null || playlistName.equals("") || creationDate == null) {
			error += "Per favore riempi tutti i campi richiesti";
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
			return;
		}

		PlaylistDAO pDAO = new PlaylistDAO(connection);
		TrackDAO tDAO = new TrackDAO(connection);
		ArrayList<Track> tracksToBeAdded = new ArrayList<>();
		ArrayList<Track> allTracks = new ArrayList<>();
		int result = 0;
		int idPlaylist = 0;
		
		try {
			//Multiple queries, begin transaction
			connection.setAutoCommit(false);
			
			result = pDAO.createPlaylist(playlistName, creationDate, idUser);
			allTracks = (ArrayList<Track>) tDAO.getTracksByUser(idUser);

			// Bind selected field to field in HTML page
			for (int i = 0; i < allTracks.size(); i++) {
				boolean selected = false;
				if (request.getParameter(Integer.toString(allTracks.get(i).getIdTrack())) != null)
					selected = (request.getParameter(Integer.toString(allTracks.get(i).getIdTrack()))).equals("on")
							? true
							: false;
				allTracks.get(i).setSelected(selected);
			}

			// Filter only selected tracks
			tracksToBeAdded = (ArrayList<Track>) allTracks.stream().filter(t -> t.getSelected() == true)
					.collect(Collectors.toList());

			// Check if at least one song has been selected
			if (tracksToBeAdded.isEmpty()) {
				error += "Per favore seleziona almeno una traccia";
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
				return;
			}

			// Result is 0 only if playlist creation was successful
			if (result != 0){
				error += "Hai già creato una playlist con questo titolo";
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
				return;
			}
						
			// Add track checked during creation
			idPlaylist = pDAO.getIdPlaylistFromPlaylistName(playlistName, idUser);
			for (int i = 0; i < tracksToBeAdded.size(); i++)
				tDAO.addTrackToPlaylist(idPlaylist, tracksToBeAdded.get(i).getIdTrack(), userCreator.getIdUser());
			
			// Reset selected value for future use
			for (int i = 0; i < allTracks.size(); i++)
				allTracks.get(i).setSelected(false);	

			// Commit changes
			connection.commit();
			
			String path;
			path = getServletContext().getContextPath() + "/homepage";
			response.sendRedirect(path);
		} catch (SQLException e) {
			try {
				connection.rollback();
				error += "Errore nell'inserimento, verifica che il titolo della playlist sia unico";
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
				return;
			} catch (SQLException e2) {
				error += "Errore nel database";
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
				return;
			}
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
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
