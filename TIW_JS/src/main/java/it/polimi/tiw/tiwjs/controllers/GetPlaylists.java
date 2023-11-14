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

import com.google.gson.*;

import it.polimi.tiw.tiwjs.beans.User;
import it.polimi.tiw.tiwjs.beans.Playlist;
import it.polimi.tiw.tiwjs.dao.PlaylistDAO;

@WebServlet("/GetPlaylists")
@MultipartConfig
public class GetPlaylists extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetPlaylists() {
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
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
				String error = "Session expired!";
				response.setStatus(403);
				response.getWriter().println(error);
				return;
		} else {
			PlaylistDAO pDAO = new PlaylistDAO(connection);
			ArrayList<Playlist> PlaylistList;
			int userId = ((User) session.getAttribute("user")).getIdUser();
			try {
				PlaylistList = (ArrayList<Playlist>) pDAO.getPlaylistsByUser(userId);
			} catch (SQLException e) {
				response.setStatus(500);
				return;
			}
			
			//Everything went correctly
			response.setStatus(200);
			
			//Create Answer
			Gson jsonBuilder = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
			String jsonFile = jsonBuilder.toJson(PlaylistList);
			
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jsonFile);
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
