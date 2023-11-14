package it.polimi.tiw.tiwjs.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;

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

import com.google.gson.*;

import it.polimi.tiw.tiwjs.beans.Track;
import it.polimi.tiw.tiwjs.beans.User;
import it.polimi.tiw.tiwjs.dao.TrackDAO;

@WebServlet("/GetTrackInfo")
@MultipartConfig
public class GetTrackInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetTrackInfo() {
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
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
		} else {
			TrackDAO tDAO = new TrackDAO(connection);
			Track Track = new Track();
			int userId = ((User) session.getAttribute("user")).getIdUser();
			try {
				String trackId = request.getParameter("idTrack");
				
				if (trackId == null || trackId.isEmpty())
				{
					response.setStatus(500);
					return;
				}
				
				int idTrack = Integer.parseInt(trackId);
				Track = (Track) tDAO.getTrackFromId(idTrack, userId);

				
				// Reformat image path for visualization
				// Encode in Base64
				int imageTypeSeparator = Track.getImage().indexOf(".");
				String imageType = Track.getImage().substring(imageTypeSeparator,
						Track.getImage().length()) + 1;
				File file = new File(Track.getImage());
				byte[] imageToEncode = FileUtils.readFileToByteArray(file);
				String encodedImage = Base64.getEncoder().encodeToString(imageToEncode);
				// Set as image attribute
				Track.setImage("data:image/" + imageType + ";base64," + encodedImage);
				// Mp3 encoding
				file = new File(Track.getAudioTrack());
				byte[] audioToEncode = FileUtils.readFileToByteArray(file);
				String encodedAudio = Base64.getEncoder().encodeToString(audioToEncode);
				// Set as audio track
				Track.setAudioTrack("data:audio/wav;base64," + encodedAudio);

			} catch (SQLException e) {
				response.setStatus(500);
				return;
			} catch (NumberFormatException e) {
				response.setStatus(500);
				return;
			}

			// Everything went correctly
			response.setStatus(200);

			// Create Answer
			Gson jsonBuilder = new GsonBuilder().create();
			String jsonFile = jsonBuilder.toJson(Track);

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jsonFile);
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
