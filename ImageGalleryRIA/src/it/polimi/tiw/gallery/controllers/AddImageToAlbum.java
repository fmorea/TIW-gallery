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
import it.polimi.tiw.gallery.dao.AlbumHasImageDAO;
import it.polimi.tiw.gallery.utils.Initializer;

@WebServlet("/AddImageToAlbum")
public class AddImageToAlbum extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public AddImageToAlbum() {
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
		
		// Controllo di non nullità dei values dei radioButtons
		
		if (	stringAlbumId == null 			||
				stringAlbumId.isEmpty() 		||
				!isValidNumber(stringAlbumId) 	||
				stringImageId == null			||
				stringImageId.isEmpty()			||
				!isValidNumber(stringImageId)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect selection, please select an image and an album");
			return;
		}
		
		int imageId = Integer.parseInt(stringImageId);
		int albumId = Integer.parseInt(stringAlbumId);
		
		AlbumHasImageDAO albumHasImageDAO = new AlbumHasImageDAO(connection);
		try {
			if (albumHasImageDAO.exist(albumId, imageId)) {
				response.sendRedirect(getServletContext().getContextPath() + "/GoToHome" + "?add=invalid");
			}
			else {
				albumHasImageDAO.addImageToAlbum(imageId, albumId);
				response.sendRedirect(getServletContext().getContextPath() + "/GoToHome" + "?add=valid");
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Database error");
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
