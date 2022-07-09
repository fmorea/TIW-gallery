package it.polimi.tiw.gallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.gallery.beans.User;
import it.polimi.tiw.gallery.exceptions.ValidationError;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Controlla che esista nel database l'utente, di cui viene fornito un identificatore e la password
	 * @param identifier Email o Nickname
	 * @param password La password ricevuta da form
	 * @return
	 * @throws SQLException
	 */
	
	public User checkCredentials(String identifier, String password) throws SQLException {
		User user = null;
		String query = "SELECT id, username FROM user WHERE (username = ? OR email = ?) AND password = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, identifier);
			pstatement.setString(2, identifier);
			pstatement.setString(3, password);
			try(ResultSet result = pstatement.executeQuery()) {
				if(result.next()) {
					user = new User();
					user.setId(result.getInt("id"));
					user.setUser(result.getString("username"));
				}
			}
		}
		return user;
	}
	
	public void register(String username, String email, String password) throws SQLException, ValidationError {
		if(username == null || username.equals(""))
			throw new ValidationError("Username non valido");
		if(email == null || email.equals(""))
			throw new ValidationError("Email non valida");
		if(password == null || password.equals(""))
			throw new ValidationError("Password non valida");
		
		String query = "INSERT into user (username, email, password) VALUES(?, ?, ?)";
		connection.setAutoCommit(false);
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			pstatement.setString(3, password);
			pstatement.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}
	
	public String findUsernameById(int userId) throws SQLException {
		String query = "SELECT username FROM user WHERE id = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, userId);
			try(ResultSet result = pstatement.executeQuery()) {
				if(result.next()) {
					return result.getString("username");
				}
			}
		}
		return null;
	}
	
	public boolean existUserWithNicknameEqualsTo(String username) throws SQLException {
		String query = "SELECT username FROM user WHERE username = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, username);
			try(ResultSet result = pstatement.executeQuery()) {
				return result.next();
			}
		}
	}
}
