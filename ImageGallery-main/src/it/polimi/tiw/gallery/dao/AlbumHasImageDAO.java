package it.polimi.tiw.gallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.gallery.beans.AlbumHasImage;

public class AlbumHasImageDAO {
private Connection connection;
	
	public AlbumHasImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<AlbumHasImage> findImagesOfAlbum(int albumId) throws SQLException{
		List<AlbumHasImage> albumHasImages = new ArrayList<>();
		String query = "SELECT * FROM album_has_image WHERE album_id = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, albumId);
			try(ResultSet result = pstatement.executeQuery()) {
				while(result.next()) {
					AlbumHasImage albumHasImage = new AlbumHasImage();
					albumHasImage.setAlbumId(result.getInt("album_id"));
					albumHasImage.setImageId(result.getInt("image_id"));				
					albumHasImages.add(albumHasImage);
				}
			}
		}
		return albumHasImages;
	}
	
	public void addImageToAlbum(int imageId, int albumId) throws SQLException {
		String query = "INSERT into album_has_image (album_id, image_id) VALUES(?, ?)";
		connection.setAutoCommit(false);
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, albumId);
			pstatement.setInt(2, imageId);
			pstatement.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}
	
	public void removeImageFromAlbum(int imageId, int albumId) throws SQLException {
		String query = "DELETE FROM album_has_image WHERE album_id = ? AND image_id = ?";
		connection.setAutoCommit(false);
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, albumId);
			pstatement.setInt(2, imageId);
			pstatement.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}
	
	public void removeAllImageOfAlbum(int albumId) throws SQLException {
		String query = "DELETE FROM album_has_image WHERE album_id = ?";
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
	
	public boolean exist(int albumId, int imageId ) throws SQLException {
		String query = "SELECT * FROM album_has_image WHERE album_id = ? AND image_id = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, albumId);
			pstatement.setInt(2, imageId);
			
			try(ResultSet result = pstatement.executeQuery()) {
				return result.next();
			}
		}
	}
}
