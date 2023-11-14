package it.polimi.tiw.tiwjs.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.tiwjs.beans.Track;

public class TrackDAO {
	private Connection con;

	public TrackDAO(Connection connection) {
		this.con = connection;
	}

	/**
	 * 
	 * @param idUser is the idUser of the user
	 * @return a list containing all tracks uploaded by the user
	 * @throws SQLException
	 */
	public List<Track> getTracksByUser(int idUser) throws SQLException {
		List<Track> trackList = new ArrayList<Track>();
		String query = "SELECT * FROM track WHERE userCreator = ? ORDER BY title";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, idUser);
			try (ResultSet result = pstatement.executeQuery()) {
				// Iterate between all founded tracks and for each one of them build an object
				while (result.next()) {
					Track track = new Track();
					track.setIdTrack(result.getInt("idTrack"));
					track.setTitle(result.getString("title"));
					track.setAuthor(result.getString("author"));
					track.setGenre(result.getString("genre"));
					track.setAlbum(result.getString("album"));
					track.setYear(result.getInt("year"));
					track.setUserCreator(result.getInt("userCreator"));
					track.setImage(result.getString("image"));
					track.setAudioTrack(result.getString("audioTrack"));

					// Add track to trackList
					trackList.add(track);
				}
			}
		}
		return trackList;
	}

	/**
	 * 
	 * @param title       it's the title of the track
	 * @param author      it's the author of the track
	 * @param genre       it's the genre of the track
	 * @param album       it's the track's album
	 * @param year        it's the release year of the track
	 * @param image       it's the picture path of the album
	 * @param audioTrack  it's the audioTrack path
	 * @param userCreator it's the user that created the track
	 * @return 0 if everything was done correctly
	 * @throws SQLException
	 */
	public int createTrack(String title, String author, String genre, String album, int year, String image,
			String audio, int userCreator) throws SQLException {
		String query = "INSERT into track (title, author, genre, album, year, image, audioTrack, userCreator) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

		int code = 0;
		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, title);
			pstatement.setString(2, author);
			pstatement.setString(3, genre);
			pstatement.setString(4, album);
			pstatement.setInt(5, year);
			pstatement.setString(6, image);
			pstatement.setString(7, audio);
			pstatement.setInt(8, userCreator);
			code = pstatement.executeUpdate();
		}
		return code;
	}

	/**
	 * 
	 * @param idPlaylist
	 * @param idUser
	 * @return a list of tracks currently not in specified playlist
	 * @throws SQLException
	 */
	public ArrayList<Track> getTracksNotInPlaylist(int idPlaylist, int idUser) throws SQLException {
		String query = "SELECT * FROM track WHERE idTrack NOT IN (SELECT idTrack FROM track NATURAL JOIN bindingtp NATURAL JOIN playlist WHERE idPlaylist = ? AND userCreator = ?) AND userCreator = ? ORDER BY year DESC";
		ArrayList<Track> trackList = new ArrayList<Track>();

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, idPlaylist);
			pstatement.setInt(2, idUser);
			pstatement.setInt(3, idUser);
			try (ResultSet result = pstatement.executeQuery()) {
				while (result.next()) {
					Track track = new Track();
					track.setIdTrack(result.getInt("idTrack"));
					track.setTitle(result.getString("title"));
					track.setAuthor(result.getString("author"));
					track.setGenre(result.getString("genre"));
					track.setAlbum(result.getString("album"));
					track.setYear(result.getInt("year"));
					track.setUserCreator(result.getInt("userCreator"));
					track.setImage(result.getString("image"));
					track.setAudioTrack(result.getString("audioTrack"));

					trackList.add(track);
				}
			}
		}
		return trackList;
	}

	/**
	 * 
	 * @param idPlaylist
	 * @param idUser
	 * @return a list of tracks currently in specified playlist
	 * @throws SQLException
	 */
	public ArrayList<Track> getTracksInPlaylist(int idPlaylist, int idUser) throws SQLException {
		String query = "SELECT * FROM track NATURAL JOIN bindingtp NATURAL JOIN playlist WHERE idPlaylist = ? AND userCreator = ? ORDER BY year DESC";
		ArrayList<Track> trackList = new ArrayList<Track>();

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, idPlaylist);
			pstatement.setInt(2, idUser);
			try (ResultSet result = pstatement.executeQuery()) {
				while (result.next()) {
					Track track = new Track();
					track.setIdTrack(result.getInt("idTrack"));
					track.setTitle(result.getString("title"));
					track.setAuthor(result.getString("author"));
					track.setGenre(result.getString("genre"));
					track.setAlbum(result.getString("album"));
					track.setYear(result.getInt("year"));
					track.setUserCreator(result.getInt("userCreator"));
					track.setImage(result.getString("image"));
					track.setAudioTrack(result.getString("audioTrack"));

					trackList.add(track);
				}
			}
		}
		return trackList;
	}

	/**
	 * Check for duplicates during track creation
	 * 
	 * @param title
	 * @param author
	 * @param genre
	 * @param year
	 * @param album
	 * @param idUser
	 * @return true if there isn't any duplicates
	 * @throws SQLException
	 */
	public boolean checkForDuplicates(String title, String author, String genre, int year, String album, int idUser) throws SQLException {
		String query = "SELECT * FROM track WHERE title = ? AND author = ? AND genre = ? AND year = ? AND album = ? AND userCreator = ?";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, title);
			pstatement.setString(2, author);
			pstatement.setString(3, genre);
			pstatement.setInt(4, year);
			pstatement.setString(5, album);
			pstatement.setInt(6, idUser);

			try (ResultSet result = pstatement.executeQuery()) {
				if (result.next())
					return true;
				else
					return false;
			}
		}
	}
	
	/**
	 * Add binding idTrack and idPlaylist
	 * 
	 * @param idPlaylist
	 * @param idTrack
	 * @param idUser
	 */
	public void addTrackToPlaylist(int idPlaylist, int idTrack, int idUser) throws SQLException {
		String query = "INSERT INTO bindingtp (idTrack, idPlaylist) VALUES (?, ?)";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, idTrack);
			pstatement.setInt(2, idPlaylist);
			pstatement.executeUpdate();
		}
	}

	/**
	 * Remove track from playlist
	 * 
	 * @param idPlaylist
	 * @param idTrack
	 * @param idUser
	 */
	public void removeTrackFromPlaylist(int idPlaylist, int idTrack, int idUser) throws SQLException {
		String query = "DELETE FROM bindingtp WHERE idTrack = ? and idPlaylist = ?";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, idTrack);
			pstatement.setInt(2, idPlaylist);
			pstatement.executeUpdate();
		}
	}

	/**
	 * Get track details from idUser
	 * 
	 * @param idTrack
	 * @param idUser
	 * @return
	 */
	public Track getTrackFromId(int idTrack, int idUser) throws SQLException {
		String query = "SELECT * FROM track WHERE idTrack = ? and userCreator = ?";
		Track track = new Track();

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, idTrack);
			pstatement.setInt(2, idUser);

			try (ResultSet result = pstatement.executeQuery()) {
				if (result.next()) {
					track.setIdTrack(result.getInt("idTrack"));
					track.setTitle(result.getString("title"));
					track.setAuthor(result.getString("author"));
					track.setGenre(result.getString("genre"));
					track.setAlbum(result.getString("album"));
					track.setYear(result.getInt("year"));
					track.setUserCreator(result.getInt("userCreator"));
					track.setImage(result.getString("image"));
					track.setAudioTrack(result.getString("audioTrack"));
				} else
					return null;

			}
		}
		return track;
	}
}
