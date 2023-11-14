package it.polimi.tiw.tiwpurehtml.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.tiwpurehtml.beans.User;

public class UserDAO {

	private Connection con;

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	/**
	 * Check if user is registered in DB
	 * 
	 * @param username
	 * @param password
	 * @return user from DB
	 * @throws SQLException
	 */
	public User checkUser(String username, String password) throws SQLException {
		User user = null;
		// BINARY avoid case insensitivity
		String query = "SELECT * FROM user WHERE username = BINARY ? and password = BINARY ?";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			try (ResultSet result = pstatement.executeQuery()) {
				while (result.next()) {
					user = new User();
					user.setIdUser(result.getInt("idUser"));
					user.setUsername(result.getString("username"));
					user.setPassword(result.getString("password"));
				}
			}
		}
		return user;
	}
}
