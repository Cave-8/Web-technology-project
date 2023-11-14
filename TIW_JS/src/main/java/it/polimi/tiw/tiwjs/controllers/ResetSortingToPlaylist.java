package it.polimi.tiw.tiwjs.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.tiwjs.beans.User;
import it.polimi.tiw.tiwjs.dao.PlaylistDAO;

@WebServlet("/ResetSortingToPlaylist")
@MultipartConfig
public class ResetSortingToPlaylist extends HttpServlet {
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
		int idPlaylist = 0;
		String error = "";

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

		try {
			String idPlaylistString = request.getParameter("idPlaylist");

			if (idPlaylistString == null || idPlaylistString == "") {
				error += "Per favore, riempi tutti i campi";
				response.sendError(505, error);
				return;
			}

			idPlaylist = Integer.parseInt(idPlaylistString);

		} catch (NumberFormatException e) {
			error += "Errore nell'idPlaylist, deve essere un numero";
			response.sendError(505, error);
			return;
		}

		try {
			PlaylistDAO pDAO = new PlaylistDAO(connection);
			pDAO.resetSorting(idPlaylist, userCreator.getIdUser());
		} catch (SQLException e) {
			error += "Errore durante il reset, riprova";
			response.sendError(505, error);
			return;
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
