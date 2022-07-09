package it.polimi.tiw.gallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;

import it.polimi.tiw.gallery.beans.AlbumHasImage;
import it.polimi.tiw.gallery.beans.User;
import it.polimi.tiw.gallery.dao.AlbumHasImageDAO;
import it.polimi.tiw.gallery.dao.CommentDAO;
import it.polimi.tiw.gallery.utils.Initializer;

@WebServlet("/AddComment")
public class AddComment extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public AddComment() {
		super();
	}

	public void init() throws ServletException {
		this.templateEngine = Initializer.templateEngineInit(getServletContext());
		this.connection =  Initializer.connectionInit(getServletContext());
	} 
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws IOException{
		String stringImageId = request.getParameter("image");
		String stringAlbumId = request.getParameter("album");
		String text = request.getParameter("text");
		
		// Controllo di non nullità dei values dei radioButtons
		
		if (	stringAlbumId == null 			||
				stringAlbumId.isEmpty() 		||
				!isValidNumber(stringAlbumId) 	||
				stringImageId == null			||
				stringImageId.isEmpty()			||
				!isValidNumber(stringImageId)	||
				text == null					||
				text.isEmpty()					) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The forms sent contains invalid data");
			return;
		}
		
		int imageId = Integer.parseInt(stringImageId);
		int albumId = Integer.parseInt(stringAlbumId);
		
		AlbumHasImageDAO albumHasImageDAO = new AlbumHasImageDAO(connection);
		CommentDAO cDAO = new CommentDAO(connection);
		try {
			if (albumHasImageDAO.exist(albumId, imageId)) {
				// Abbiamo appurato che l'immagine che l'utente desidera commentare esiste veramente
				// ed è proprio in quell'album
				try {
					cDAO.addComment(((User) request.getSession().getAttribute("user")).getId(), text, imageId);
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("Not possible to create comment");
					return;
				} 
				response.sendRedirect(getServletContext().getContextPath() + "/GoToAlbum" + "?album="+stringAlbumId+"&image="+stringImageId);
				
			}
			else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hidden forms modified with incorrect data");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Database error");
			return;
		}
}
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
		}
	}
	
	public boolean isValidNumber(String number) {
		String NUMERO_REGEX ="[0-9]+";
	    Pattern NUMERO_PATTERN = Pattern.compile(NUMERO_REGEX);
	    
	    if (number == null) {
            return false;
        }

        Matcher matcher = NUMERO_PATTERN.matcher(number);

        return matcher.matches();
	}
	
}
