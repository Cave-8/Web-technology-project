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

@WebServlet("/GoToPlaylist")
public class GoToPlaylist extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEng;
	// blockSize is defined in requirements
	private final int blockSize = 5;

	public GoToPlaylist() {
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
		int idPlaylist;
		int currentBlockIndex = 0;
		String error = "";

		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		// Check for session error
		if (session.isNew() || session == null || user == null) {
			response.sendRedirect("/TIWpureHTML/index.html");
			return;
		}

		PlaylistDAO pDAO = new PlaylistDAO(connection);
		TrackDAO tDAO = new TrackDAO(connection);

		try {
			idPlaylist = Integer.parseInt(request.getParameter("idPlaylist"));

			if (!pDAO.playerCanAccessPlaylist(idPlaylist, user.getIdUser())) {
				error += "Errore nel campo idPlaylist";
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
				return;
			}
		} catch (NumberFormatException e) {
			error += "idPlaylist deve essere un numero valido";
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
			return;
		} catch (SQLException e) {
			error += "Errore nella comunicazione nel database";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
			return;
		}

		try {
			currentBlockIndex = Integer.parseInt(request.getParameter("currentBlockIndex"));
		} catch (NumberFormatException e) {
			error += "currentBlockIndex deve essere un numero valido";
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
			return;
		}
		
		try {
			// Every track in playlist
			ArrayList<Track> tracksInPlaylist = tDAO.getTracksInPlaylist(idPlaylist, user.getIdUser());
			// Every track not in playlist
			ArrayList<Track> tracksNotInPlaylist = tDAO.getTracksNotInPlaylist(idPlaylist, user.getIdUser());

			Playlist playlist = new Playlist();
			playlist.setIdPlaylist(idPlaylist);
			playlist.setPlaylistName(pDAO.getPlaylistNameById(idPlaylist));

			// Creation of current block
			ArrayList<Track> visualizedTracks = new ArrayList<Track>();
			boolean noMoreTracks = true;

			if (currentBlockIndex < 0 || currentBlockIndex > (int) (tracksInPlaylist.size()-1) / blockSize) {
				error += "Il numero di tracce in playlist non è compatibile con currentBlockIndex";
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
				return;
			}

			////////////////////////
			// BLOCK CONSTRUCTION //
			////////////////////////
			
			// Every block has 5 tracks
			for (int i = currentBlockIndex * blockSize; i < Math.min((currentBlockIndex + 1) * blockSize,
					tracksInPlaylist.size()); i++) {
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

				// Same for MP3
				file = new File(currentTrack.getAudioTrack());
				byte[] audioToEncode = FileUtils.readFileToByteArray(file);
				String encodedAudio = Base64.getEncoder().encodeToString(audioToEncode);
				// Reformat audio path for visualization
				currentTrack.setAudioTrack("data:audio/wav;base64," + encodedAudio);

				visualizedTracks.add(tracksInPlaylist.get(i));
				noMoreTracks = false;
				if (i == tracksInPlaylist.size() - 1)
					noMoreTracks = true;
			}

			// If noMoreTracks == true then nextBlockIndex = -1 (used in Web page to make
			// next button appear, same condition for previous button)
			int nextBlockIndex = noMoreTracks ? -1 : currentBlockIndex + 1;
			int previousBlockIndex = currentBlockIndex - 1;

			String path = "/WEB-INF/playlist";
			ServletContext servletContext = getServletContext();
			final WebContext context = new WebContext(request, response, servletContext, request.getLocale());

			// Context setup
			context.setVariable("user", user);
			context.setVariable("playlist", playlist);
			context.setVariable("tracksInPlaylist", tracksInPlaylist);
			context.setVariable("tracksNotInPlaylist", tracksNotInPlaylist);
			context.setVariable("visualizedTracks", visualizedTracks);
			context.setVariable("nextBlockIndex", nextBlockIndex);
			context.setVariable("previousBlockIndex", previousBlockIndex);
			context.setVariable("currentBlockIndex", currentBlockIndex);

			templateEng.process(path, context, response.getWriter());

		} catch (SQLException e) {
			error += "Errore nella comunicazione con il database";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
			return;
		}

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
