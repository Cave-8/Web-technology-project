package it.polimi.tiw.tiwpurehtml.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.tiwpurehtml.beans.Playlist;
import it.polimi.tiw.tiwpurehtml.beans.Track;
import it.polimi.tiw.tiwpurehtml.beans.User;
import it.polimi.tiw.tiwpurehtml.dao.PlaylistDAO;
import it.polimi.tiw.tiwpurehtml.dao.TrackDAO;

@WebServlet("/GoToTrack")
public class GoToTrack extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEng;

	public GoToTrack() {
		super();
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEng = new TemplateEngine();
		this.templateEng.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
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
		int idTrack = 0;
		int idPlaylist = 0;
		ArrayList<Track> availableTracks = new ArrayList<>();
		ArrayList<Playlist> availablePlaylists = new ArrayList<>();
		String error = "";

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		// Check for session error
		if (session.isNew() || session == null || user == null) {
			response.sendRedirect("/TIWpureHTML/index.html");
			return;
		}

		TrackDAO tDAO = new TrackDAO(connection);
		PlaylistDAO pDAO = new PlaylistDAO(connection);
		try {
			availableTracks = (ArrayList<Track>) tDAO.getTracksByUser(user.getIdUser());
			availablePlaylists = (ArrayList<Playlist>) pDAO.getPlaylistsByUser(user.getIdUser());
			idTrack = Integer.parseInt(request.getParameter("idTrack"));
			idPlaylist = Integer.parseInt(request.getParameter("idPlaylist"));

		} catch (SQLException e) {
			error += "Errore nella comunicazione con il database";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
			return;
		} catch (NumberFormatException e) {
			error += "Errore nei parametri, idTrack/idPlaylist deve essere un numero valido";
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
			return;
		}

		int idT = idTrack;
		int idP = idPlaylist;

		// Reject if user can't play the track
		if (!availableTracks.stream().filter(t -> t.getIdTrack() == idT).findFirst().isPresent()) {
			error += "Errore nella traccia visualizzata";
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
			return;
		}
		// Reject if user can't access playlist
		if (!availablePlaylists.stream().filter(p -> p.getIdPlaylist() == idP).findFirst().isPresent()) {
			error += "Errore nella playlist scelta";
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
			return;
		}

		// Get currentTrack data
		Track currentTrack = availableTracks.stream().filter(t -> t.getIdTrack() == idT).findAny().get();
		// Preventing manipulation, avoid user access to Track that he can visualize but isn't in current playlist
		try {
			if ((!tDAO.getTracksInPlaylist(idP, user.getIdUser()).stream().filter(t -> t.getIdTrack() == currentTrack.getIdTrack()).findFirst().isPresent())) {
				error += "La traccia non è nella playlist scelta";
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
				return;
			}
		} catch (SQLException e) {
			error += "Errore nella comunicazione con il database";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
			return;
		}
		
		// Reformat image path for visualization
		// Get image type (imageTypeSeparator -> index for .)
		// Reformat image path for visualization
		// Encode in Base64
		int imageTypeSeparator = currentTrack.getImage().indexOf(".");
		String imageType = currentTrack.getImage().substring(imageTypeSeparator, currentTrack.getImage().length()) + 1;
		File file = new File(currentTrack.getImage());
		byte[] imageToEncode = FileUtils.readFileToByteArray(file);
		String encodedImage = Base64.getEncoder().encodeToString(imageToEncode);
		// Set as image attribute
		currentTrack.setImage("data:image/" + imageType + ";base64," + encodedImage);

		// Same for MP3
		file = new File(currentTrack.getAudioTrack());
		byte[] audioToEncode = FileUtils.readFileToByteArray(file);
		String encodedAudio = Base64.getEncoder().encodeToString(audioToEncode);
		// Reformat audio path for visualization
		currentTrack.setAudioTrack("data:audio/wav;base64," + encodedAudio);

		String path = "/WEB-INF/player";
		ServletContext servletContext = getServletContext();
		final WebContext context = new WebContext(request, response, servletContext, request.getLocale());

		context.setVariable("user", user);
		context.setVariable("idPlaylist", idPlaylist);
		context.setVariable("track", currentTrack);
		templateEng.process(path, context, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
