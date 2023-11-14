package it.polimi.tiw.tiwjs.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

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
import it.polimi.tiw.tiwjs.beans.Playlist;
import it.polimi.tiw.tiwjs.dao.PlaylistDAO;

@WebServlet("/GetTracksNotInPlaylist")
@MultipartConfig
public class GetTracksNotInPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetTracksNotInPlaylist() {
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
			ArrayList<Track> tracksNotInPlaylist = tDAO.getTracksNotInPlaylist(idPlaylist, user.getIdUser());
						
			Playlist playlist = pDAO.getPlaylistById(idPlaylist);
			playlist.setIdPlaylist(idPlaylist);
						
			Gson jsonBuilder = new GsonBuilder().create();
			String jsonFile = jsonBuilder.toJson(tracksNotInPlaylist);
					
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

	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		doGet(request , response);
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
