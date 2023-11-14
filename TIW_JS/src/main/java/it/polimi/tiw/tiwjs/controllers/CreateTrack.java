package it.polimi.tiw.tiwjs.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import it.polimi.tiw.tiwjs.beans.User;
import it.polimi.tiw.tiwjs.dao.TrackDAO;

@WebServlet("/CreateTrack")
@MultipartConfig
public class CreateTrack extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

	// Setup of folder for image and audio track
	private String imageDefaultPath;
	private String audioDefaultPath;

	// Used to check if album cover was already added
	boolean newImage = true;

	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			imageDefaultPath = context.getInitParameter("imageDefaultPath");
			audioDefaultPath = context.getInitParameter("audioDefaultPath");
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

		if (session == null) {
			String error = "Session expired!";
			response.setStatus(403);
			response.getWriter().println(error);
			return;
		}

		User userCreator = (User) session.getAttribute("user");

		if (userCreator == null) {
				String error = "Session expired!";
				response.setStatus(403);
				response.getWriter().println(error);
				return;
		}

		// Error is used to collect all mistakes done by user in track creation
		String error = "";

		String title = request.getParameter("Title");
		String author = request.getParameter("Author");
		String genre = request.getParameter("Genre");
		String album = request.getParameter("Album");
		String year = request.getParameter("Year");
		Part image = request.getPart("Image");
		Part audio = request.getPart("AudioTrack");

		///////////////////////////
		// Emptiness or null check//
		///////////////////////////
		if (title == null || title.equals("") || author == null || author.equals("") || genre == null
				|| genre.equals("") || album == null || album.equals("") || year == null || year.equals("")
				|| image == null || image.getSize() <= 0 || audio == null || audio.getSize() <= 0) {
			error += "Per favore, compila correttamente l'ordinamento";
			response.setStatus(505);
			response.getWriter().println(error);
			return;
		}

		TrackDAO tDAO = new TrackDAO(connection);

		////////////////////
		// Bad format check//
		////////////////////
		int yearInt = 0;
		int currentYear = 0;

		// Year must be > 0, lower than current year
		try {
			yearInt = Integer.parseInt(year);
			currentYear = Calendar.getInstance().get(Calendar.YEAR);

			if (yearInt > currentYear || yearInt < 0)
			{
				error += "Anno non valido";
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(error);
				return;
			}
		} catch (NumberFormatException e) {
			error += "L'anno deve essere un numero";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}

		// Check limit for String
		if (title.length() > 255) {
			error += "Titolo troppo lungo";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}
		if (author.length() > 255) {
			error += "Nome autore troppo lungo";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}
		if (genre.length() > 255) {
			error += "Genere troppo lungo";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}
		if (album.length() > 255) {
			error += "Nome album troppo lungo";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}
		
		//Replace . with • for safety in URL
		title = title.replace(".", "•");
		author = author.replace(".", "•");
		album = album.replace(".", "•");
		
		String imageType = image.getContentType();
		String audioType = audio.getContentType();

		if (!imageType.startsWith("image")) {
			error += "Per favore, esegui l'upload di un'immagine";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}

		if (!audioType.startsWith("audio")) {
			error += "Per favore, esegui l'upload di un file audio";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}

		// Redirect to error
		if (!error.equals("")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}

		boolean result;
		try {
			result = tDAO.checkForDuplicates(title, author, genre, yearInt, album, userCreator.getIdUser());
		} catch (SQLException e1) {
			error += "Errore, verifica che il brano non sia duplicato";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}
		
		if (result) {
			error += "Errore, verifica che il brano non sia duplicato";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}
		
		//////////////////////////////
		// Audio and image management//
		//////////////////////////////
		String imageTypeOfFile = image.getContentType().replace("image/", ".");

		String imagePath = imageDefaultPath + "\\" + userCreator.getIdUser() + "_" + author + "_" + album
				+ imageTypeOfFile;
		String audioPath = audioDefaultPath + "\\" + userCreator.getIdUser() + "_" + album + "_" + title + ".mp3";
		
		if (imagePath.length() > 255 || audioPath.length() > 255) {
			error += "Per favore ricontrolla il nome del file";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}

		File imageFile = new File(imagePath);
		// Flag if album images was already inserted -> avoid double insertion
		boolean newImage = true;

		File audioFile = new File(audioPath);
		
		if (imageFile.exists())
			newImage = false;
		
		if (audioFile.exists()) {
			error += "Hai già caricato questa traccia";
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
			response.getWriter().println(error);
			return;
		}
		
		if (newImage == true) {
			try (InputStream imageStream = image.getInputStream()) {
				Files.copy(imageStream, imageFile.toPath());
			} catch (Exception e) {
				error += "Errore durante l'upload, riprova";
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(error);
				return;
			}
		}
		
		try (InputStream audioStream = audio.getInputStream()) {
			Files.copy(audioStream, audioFile.toPath());
		} catch (Exception e) {
			error += "Errore durante l'upload, riprova";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}

		///////////////////////////
		/// Begin database update///
		///////////////////////////
		try {
			tDAO.createTrack(title, author, genre, album, yearInt, imagePath, audioPath, userCreator.getIdUser());
			
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (SQLException e) {

			File fileToBeDeleted;
			// Delete image only if is new
			if (!newImage) {
				fileToBeDeleted = new File(imagePath);
				fileToBeDeleted.delete();
			}

			fileToBeDeleted = new File(audioPath);
			fileToBeDeleted.delete();

			error = "Bad database insertion input";
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(error);
			return;
		}
	}

	public void doGet(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		doPost(request , response);
	}
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
}
