package it.polimi.tiw.gallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.gallery.beans.Comment;
import it.polimi.tiw.gallery.exceptions.ValidationError;

public class CommentDAO {
	private Connection connection;
	
	public CommentDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Comment> findCommentsByImage(int imageId) throws SQLException {
		List<Comment> comments = new ArrayList<>();
		String query = "SELECT * FROM comment WHERE imageCommented = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, imageId);
			try(ResultSet result = pstatement.executeQuery()) {
				while(result.next()) {
					Comment comment = new Comment();
					comment.setId(result.getInt("id"));
					comment.setText(result.getString("text"));
					comment.setImage(result.getInt("imageCommented"));
					UserDAO userDAO = new UserDAO(connection);
					comment.setUsername(userDAO.findUsernameById(result.getInt("creator")));
					comments.add(comment);
				}
			}
		}
		return comments;
	}
	
	
	
	public int addComment(int creatorId, String text, int imageCommented)  throws SQLException  {
		String query = "INSERT into comment (creator, text, imageCommented) VALUES (?, ?, ?)";
		int code = 0;
		PreparedStatement pStatement = null;
		try {
			pStatement = connection.prepareStatement(query);
			pStatement.setInt(1, creatorId);
			pStatement.setString(2, text);
			pStatement.setInt(3, imageCommented);
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
	
	
}
