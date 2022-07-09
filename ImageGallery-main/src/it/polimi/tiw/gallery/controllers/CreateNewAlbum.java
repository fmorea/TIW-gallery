package it.polimi.tiw.gallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.gallery.beans.User;
import it.polimi.tiw.gallery.dao.AlbumDAO;
import it.polimi.tiw.gallery.utils.ConnectionHandler;
import it.polimi.tiw.gallery.utils.Initializer;

@WebServlet("/CreateNewAlbum")
public class CreateNewAlbum extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CreateNewAlbum() {
		super();
	}

	public void init() throws ServletException {
		this.templateEngine = Initializer.templateEngineInit(getServletContext());
		this.connection =  Initializer.connectionInit(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession  s = request.getSession();
		User u = (User) s.getAttribute("user");
		String title = request.getParameter("title");
		String stringDate = request.getParameter("date");
		
		// Controllo di non nullità dei parametri inseriti
		if (	title == null 		||
				title.isEmpty() 	||
				stringDate == null	||
				stringDate.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name of the album missing ");
			return;
		}
		
		
		Date date = null;
		// La data deve essere valida
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(stringDate);
			
		}catch(ParseException e1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Date format");
		}
		
		
		// La data non deve essere nel futuro
		Date today = new Date();
        if(date.after(today)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Date cannot be in the future!");
			return;
        }
        
        
        // Load user id from the session and create the album
		int userid = u.getId();
		AlbumDAO aDao = new AlbumDAO(connection);
		
		// Cannot exist a duplicated album (same title and author)
		try {
			if(aDao.existAlbumWith(title, userid)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Album already existing");
				return;
			}
		} catch (SQLException | IOException e1) {
			e1.printStackTrace();
		}
		
		// Create the album
		try {
			System.out.println(title + date + userid);
			aDao.createAlbum(title, date, userid);
		}catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Error creating the album in the DB");
			return;
		}
		
		String path = getServletContext().getContextPath() + "/GoToHome"; 
		response.sendRedirect(path);
	}
	
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
			}
	}

}

