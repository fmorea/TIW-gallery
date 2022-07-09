package it.polimi.tiw.gallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.gallery.beans.Album;
import it.polimi.tiw.gallery.beans.Image;

public class ImageDAO {
	private Connection connection;
	
	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Image> findAllImagesOfAlbum(int albumId) throws SQLException {
		List<Image> images = new ArrayList<>();
		String query = "SELECT i.id, i.title, i.description, i.date, i.path\r\n"
				+ "				FROM image as i \r\n"
				+ "				JOIN album_has_image as x ON x.image_id = i.id \r\n"
				+ "				JOIN album as a ON a.id = x.album_id \r\n"
				+ "				WHERE a.id = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, albumId);
			try(ResultSet result = pstatement.executeQuery()) {
				while(result.next()) {
					Image image = new Image();
					image.setId(result.getInt("i.id"));
					image.setTitle(result.getString("i.title"));
					image.setDescription(result.getString("i.description"));
					image.setDate(result.getDate("i.date"));
					image.setPath(result.getString("i.path"));
					
					images.add(image);
				}
			}
		}
		return images;
	}
	
	public List<Image> findAllImagesCreatedBy(int creatorId) throws SQLException {
		List<Image> images = new ArrayList<>();
		String query = "SELECT * FROM image WHERE creator = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, creatorId);
			try(ResultSet result = pstatement.executeQuery()) {
				while(result.next()) {
					Image image = new Image();
					image.setId(result.getInt("id"));
					image.setTitle(result.getString("title"));
					image.setDescription(result.getString("description"));
					image.setDate(result.getDate("date"));
					image.setPath(result.getString("path"));
					images.add(image);
				}
			}
		}
		return images;
	}
	
	public Image findImageById(int imageId) throws SQLException {
		Image image = null;
		String query = "SELECT * FROM image WHERE id = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, imageId);
			try(ResultSet result = pstatement.executeQuery()) {
				if(result.next()) {
					image = new Image();
					image.setId(result.getInt("id"));
					image.setTitle(result.getString("title"));
					image.setDescription(result.getString("description"));
					image.setDate(result.getDate("date"));
					image.setPath(result.getString("src"));
				}
			}
		}
		return image;		
	}
	
	public int createImage(String title, String description, String path, Date date, int creatorid) throws SQLException  {
		String query = "INSERT into image (title, description, path, date, creator) VALUES (?, ?, ?, ?, ?)";
		int code = 0;
		PreparedStatement pStatement = null;
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setString(1, title);
			pStatement.setString(2, description);
			pStatement.setString(3, path);
			pStatement.setObject(4, date);
			pStatement.setInt(5, creatorid);
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
	
	public boolean exist(int imageId) throws SQLException {
		String query = "SELECT * FROM image WHERE id = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, imageId);
			
			try(ResultSet result = pstatement.executeQuery()) {
				return result.next();
			}
		}
	}
}
