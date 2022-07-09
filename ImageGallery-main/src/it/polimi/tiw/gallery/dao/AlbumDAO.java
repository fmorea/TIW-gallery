package it.polimi.tiw.gallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.gallery.beans.Album;

public class AlbumDAO {
	private Connection connection;
	
	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Album> findAlbumsListCreatedBy(int creatorId) throws SQLException {
		List<Album> albums = new ArrayList<>();
		String query = "SELECT * FROM album WHERE creator = ? ORDER BY date DESC";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, creatorId);
			try(ResultSet result = pstatement.executeQuery()) {
				while(result.next()) {
					Album album = new Album();
					album.setId(result.getInt("id"));
					album.setTitle(result.getString("title"));
					album.setDate(result.getDate("date"));
					album.setCreatorId(result.getInt("creator"));
					UserDAO userDAO = new UserDAO(connection);
					album.setCreator(userDAO.findUsernameById(result.getInt("creator")));
					albums.add(album);
				}
			}
		}
		return albums;
	}
	
	public List<Album> findAlbumsListNotCreatedBy(int creatorId) throws SQLException {
		List<Album> albums = new ArrayList<>();
		String query = "SELECT * FROM album WHERE creator <> ? ORDER BY date DESC";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, creatorId);
			try(ResultSet result = pstatement.executeQuery()) {
				while(result.next()) {
					Album album = new Album();
					album.setId(result.getInt("id"));
					album.setTitle(result.getString("title"));
					album.setDate(result.getDate("date"));
					album.setCreatorId(result.getInt("creator"));
					UserDAO userDAO = new UserDAO(connection);
					album.setCreator(userDAO.findUsernameById(result.getInt("creator")));
					albums.add(album);
				}
			}
		}
		return albums;
	}
	
	public Album findAlbumById(int albumId) throws SQLException {
		Album album = null;
		String query = "SELECT * FROM album WHERE id = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, albumId);
			try(ResultSet result = pstatement.executeQuery()) {
				if(result.next()) {
					album = new Album();
					album.setId(result.getInt("id"));
					album.setTitle(result.getString("title"));
					album.setDate(result.getDate("date"));
					album.setCreatorId(result.getInt("creator"));
					UserDAO userDAO = new UserDAO(connection);
					album.setCreator(userDAO.findUsernameById(result.getInt("creator")));
				}
			}
		}
		return album;		
	}
	
	public void removeAlbum(int albumId) throws SQLException {
		String query = "DELETE FROM album WHERE id = ?";
		connection.setAutoCommit(false);
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, albumId);
			pstatement.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}
	
	public int createAlbum(String title, Date date, int creatorid) throws SQLException  {
		String query = "INSERT into album (title, date, creator) VALUES (?, ?, ?)";
		int code = 0;
		PreparedStatement pStatement = null;
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, title);
			pStatement.setObject(2, date);
			pStatement.setInt(3, creatorid);
			code = pStatement.executeUpdate();
		}catch(SQLException e) {
			throw new SQLException(e);
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {

			}
		}
		return code;
	}
	
	public boolean existAlbumWith(String title, int creator) throws SQLException {
		String query = "SELECT * FROM album WHERE title = ? AND creator = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, title);
			pstatement.setInt(2, creator);
			
			try(ResultSet result = pstatement.executeQuery()) {
				return result.next();
			}
		}
	}
}
