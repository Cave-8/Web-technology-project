package it.polimi.tiw.tiwjs.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.tiwjs.beans.Playlist;

public class PlaylistDAO {
	private Connection con;

	public PlaylistDAO(Connection connection) {
		this.con = connection;
	}

	/**
	 * Sorting only for JS version
	 * 
	 * @param idPlaylist
	 * @return playlistName from playlistId
	 * @throws SQLException
	 */
	public Playlist getPlaylistById(int idPlaylist) throws SQLException {
		String query = "SELECT * FROM playlist WHERE idPlaylist = ?";
		String playlistName = "";
		String sorting = "";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, idPlaylist);

			try (ResultSet result = pstatement.executeQuery()) {
				if (result.next()) {
					playlistName = result.getString("playlistName");
					sorting = result.getString("sorting");
				}
			}
		}
		Playlist toBeRet = new Playlist();
		toBeRet.setPlaylistName(playlistName);
		toBeRet.setSorting(sorting);
		return toBeRet;
	}

	/**
	 * 
	 * @param playlistName
	 * @param idUser
	 * @return idPlaylist from PlaylistName and its creator
	 * @throws SQLException
	 */
	public int getIdPlaylistFromPlaylistName(String playlistName, int idUser) throws SQLException {
		String query = "SELECT idPlaylist FROM playlist WHERE playlistName = ? AND idUser = ?";
		int idPlaylist = 0;

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, playlistName);
			pstatement.setInt(2, idUser);

			try (ResultSet result = pstatement.executeQuery()) {
				if (result.next()) {
					idPlaylist = result.getInt("idPlaylist");
				}
			}
		}
		return idPlaylist;
	}

	/**
	 * Given idUser returns all playlist created by him
	 * 
	 * @param idUser
	 * @return a list of all playlists created by user with userId
	 * @throws SQLException
	 */
	public List<Playlist> getPlaylistsByUser(int idUser) throws SQLException {
		List<Playlist> PlaylistList = new ArrayList<Playlist>();
		String query = "SELECT * FROM playlist WHERE idUser = ? ORDER BY creationDate DESC";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, idUser);

			try (ResultSet result = pstatement.executeQuery()) {
				// Iterate between all founded playlists and for each one of them build an
				// object
				while (result.next()) {
					Playlist playlist = new Playlist();
					playlist.setIdPlaylist(result.getInt("idPlaylist"));
					playlist.setPlaylistName(result.getString("playlistName"));
					playlist.setCreationDate(result.getDate("creationDate"));
					playlist.setIdUser(result.getInt("idUser"));

					// Add playlist to PlaylistList
					PlaylistList.add(playlist);
				}
			}
		}
		return PlaylistList;
	}

	/**
	 * Check if user can access to playlist
	 * 
	 * @param idPlaylist
	 * @param idUser
	 * @return true if player can access to specified playlist, false if not
	 * @throws SQLException
	 */
	public boolean playerCanAccessPlaylist(int idPlaylist, int idUser) throws SQLException {
		String query = "SELECT * FROM playlist WHERE idPlaylist = ? and idUser = ?";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, idPlaylist);
			pstatement.setInt(2, idUser);

			try (ResultSet result = pstatement.executeQuery()) {
				if (result.next())
					return true;
				else
					return false;
			}
		}
	}

	/**
	 * Check for duplicates during playlist creation
	 * 
	 * @param playlistName
	 * @param idUser
	 * @return true if there is already a duplicate
	 * @throws SQLException
	 */
	public boolean checkForDuplicates(String playlistName, int idUser) throws SQLException {
		String query = "SELECT * FROM playlist WHERE playlistName = ? AND idUser = ?";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, playlistName);
			pstatement.setInt(2, idUser);

			try (ResultSet result = pstatement.executeQuery()) {
				if (result.next())
					return true;
				else
					return false;
			}
		}
	}

	/**
	 * Create a new playlist from passed parameter
	 * 
	 * @param playlistName
	 * @param creationDate
	 * @param idUser
	 * @return 0 if everything went correctly, -1 if name is a duplicate
	 * @throws SQLException
	 */
	public int createPlaylist(String playlistName, Date creationDate, int idUser) throws SQLException {
		String query = "INSERT INTO playlist (playlistName, creationDate, idUser) VALUES (?, ?, ?)";
		int code = 0;
		
		if (checkForDuplicates(playlistName, idUser))
			return -1;

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, playlistName);
			pstatement.setDate(2, creationDate);
			pstatement.setInt(3, idUser);
			pstatement.executeUpdate();
			code = 0;
		}
		return code;
	}

	/**
	 * Set sorting for playlist -> JS version only
	 * 
	 * @param String     for sorting
	 * @param idPlaylist
	 * @param idUser
	 */
	public void setSorting(String sorting, int idPlaylist, int idUser) throws SQLException {
		String query = "UPDATE playlist SET sorting = ? WHERE idPlaylist = ? AND idUser = ?";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, sorting);
			pstatement.setInt(2, idPlaylist);
			pstatement.setInt(3, idUser);
			pstatement.executeUpdate();
		}
	}

	/**
	 * Reset sorting for playlist -> JS version only
	 * 
	 * @param idPlaylist
	 * @param idUser
	 */
	public void resetSorting(int idPlaylist, int idUser) throws SQLException {
		String query = "UPDATE playlist SET sorting = NULL WHERE idPlaylist = ? AND idUser = ?";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, idPlaylist);
			pstatement.setInt(2, idUser);
			pstatement.executeUpdate();
		}
	}
}
