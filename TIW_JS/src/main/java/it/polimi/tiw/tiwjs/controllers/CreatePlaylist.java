package it.polimi.tiw.tiwjs.controllers;

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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.tiwjs.beans.Track;
import it.polimi.tiw.tiwjs.beans.User;
import it.polimi.tiw.tiwjs.dao.TrackDAO;
import it.polimi.tiw.tiwjs.dao.PlaylistDAO;

@WebServlet("/CreatePlaylist")
@MultipartConfig
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

		String error;
		
		if (session == null) {
			error = "Session expired!";
			response.setStatus(403);
			response.getWriter().println(error);
			return;
		}

		User userCreator = (User) session.getAttribute("user");

		if (userCreator == null) {
			error = "Session expired!";
			response.setStatus(403);
			response.getWriter().println(error);
			return;
		}

		// Error is used to collect all mistakes done by user in playlist creation
		error = "";

		String playlistName = request.getParameter("Title");
		java.sql.Date creationDate = new java.sql.Date(Calendar.getInstance().getTime().getTime());
		int idUser = userCreator.getIdUser();

		///////////////////////////
		//Emptiness or null check//
		///////////////////////////
		if (playlistName == null || playlistName.equals("") || creationDate == null) {
			error += "Per favore, compila correttamente l'ordinamento";
			response.sendError(505, error);
			response.getWriter().println(error);
			return;
		}

		PlaylistDAO pDAO = new PlaylistDAO(connection);
		TrackDAO tDAO = new TrackDAO(connection);
		ArrayList<Track> tracksToBeAdded = new ArrayList<>();
		ArrayList<Track> allTracks = new ArrayList<>();
		
		try {
			//Multiple queries, rollback if even one fails
			connection.setAutoCommit(false);
		
			int result = pDAO.createPlaylist(playlistName, creationDate, idUser);
			allTracks = (ArrayList<Track>) tDAO.getTracksByUser(idUser);
			
			if (result !=  0) {
				error += "Hai già creato una playlist con questo titolo";
				Gson jsonBuilder = new GsonBuilder().create();
				String jsonFile = jsonBuilder.toJson(error);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");

				response.setStatus(400);
				response.getWriter().write(jsonFile);
				
				return;
			}

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

			if (tracksToBeAdded.isEmpty())
			{
				error += "Per favore, seleziona almeno un brano";
				Gson jsonBuilder = new GsonBuilder().create();
				String jsonFile = jsonBuilder.toJson(error);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");

				response.setStatus(400);
				response.getWriter().write(jsonFile);
				
				return;
			}
			
			
			int idP = pDAO.getIdPlaylistFromPlaylistName(playlistName, idUser);
			// Add track checked during creation
			for (int i = 0; i < tracksToBeAdded.size(); i++)
				tDAO.addTrackToPlaylist(idP, tracksToBeAdded.get(i).getIdTrack(), userCreator.getIdUser());

			// Reset selected value for future use
			for (int i = 0; i < allTracks.size(); i++)
				allTracks.get(i).setSelected(false);
			
			connection.commit();
			
		} catch (SQLException e) {
			try {
				connection.rollback();
				
				error += "Verifica che il titolo della playlist sia unico";
				Gson jsonBuilder = new GsonBuilder().create();
				String jsonFile = jsonBuilder.toJson(error);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");

				response.setStatus(400);
				response.getWriter().write(jsonFile);
				
				return;
			} catch (SQLException e1) {
				e1.printStackTrace();
				error += "Problema con il DB, riprova";
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
				response.getWriter().println(error);
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
