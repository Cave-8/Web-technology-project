package it.polimi.tiw.tiwpurehtml.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.tiwpurehtml.beans.User;
import it.polimi.tiw.tiwpurehtml.dao.TrackDAO;

@WebServlet("/AddToPlaylist")
public class AddToPlaylist extends HttpServlet {
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
		int idTrack = 0;
		int idPlaylist = 0;
		String error = "";

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

		/////////////////////
		// ADD TO PLAYLIST //
		/////////////////////

		try {
			String idTrackString = request.getParameter("TitleToBeAdded");
			String idPlaylistString = request.getParameter("idPlaylist");

			if (idTrackString == null || idTrackString == "" || idPlaylistString == null || idPlaylistString == "") {
				error += "Per favore, riempi i campi richiesti";
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
				return;
			}

			idTrack = Integer.parseInt(idTrackString);
			idPlaylist = Integer.parseInt(idPlaylistString);

		} catch (NumberFormatException e) {
			error += "Errore nell'idTrack o nell'idPlaylist, devono essere numeri";
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
			return;
		}

		try {
			TrackDAO tDAO = new TrackDAO(connection);
			tDAO.addTrackToPlaylist(idPlaylist, idTrack, userCreator.getIdUser());
		} catch (SQLException e) {
			e.printStackTrace();
			error += "Errore durante l'inserimento, riprova";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
			return;
		}

		// Redirect to playlist page
		String path = getServletContext().getContextPath() + "/GoToPlaylist?" + "idPlaylist=" + idPlaylist + "&"
				+ "currentBlockIndex=" + 0;
		response.sendRedirect(path);
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
