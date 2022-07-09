package it.polimi.tiw.gallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.gallery.beans.Album;
import it.polimi.tiw.gallery.beans.Image;
import it.polimi.tiw.gallery.beans.User;
import it.polimi.tiw.gallery.dao.AlbumDAO;
import it.polimi.tiw.gallery.dao.ImageDAO;
import it.polimi.tiw.gallery.utils.ConnectionHandler;
import it.polimi.tiw.gallery.utils.Initializer;

@WebServlet("/GoToHome")
public class GoToHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
		
	public void init() throws ServletException {
		this.templateEngine = Initializer.templateEngineInit(getServletContext());
		this.connection =  Initializer.connectionInit(getServletContext());
	}
	
	public void destroy() {		
		try {
			ConnectionHandler.closeConnection(connection);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		HttpSession session = request.getSession();
		
		// Parametri che ottengo dalla richiesta GET per printare un messaggio di ALERT all'utente
		String upload = StringEscapeUtils.escapeJava(request.getParameter("upload"));
		String add = StringEscapeUtils.escapeJava(request.getParameter("add"));
		String remove = StringEscapeUtils.escapeJava(request.getParameter("remove"));
		
		// Recupera tutti gli album dal database
		AlbumDAO aDAO = new AlbumDAO(connection);
		List<Album> myAlbums = null;
		List<Album> othersAlbums = null;
		
		ImageDAO iDAO = new ImageDAO(connection);
		List<Image> myImages = null;
		
		try {
			myAlbums = aDAO.findAlbumsListCreatedBy(((User)(session.getAttribute("user"))).getId());
			othersAlbums = aDAO.findAlbumsListNotCreatedBy(((User)(session.getAttribute("user"))).getId());
			myImages = iDAO.findAllImagesCreatedBy(((User)(session.getAttribute("user"))).getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover some data from the database");
			return;
		}
		String templatePath = "/WEB-INF/home.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("myAlbums", myAlbums);
		ctx.setVariable("othersAlbums", othersAlbums);
		ctx.setVariable("myImages", myImages);
		if(upload != null && upload.equals("valid")) {
			ctx.setVariable("alert", "Image uploaded Successfully");
		}
		if(add != null && add.equals("valid")) {
			ctx.setVariable("alert", "Image added to the specified Album");
		}
		if(add != null && add.equals("invalid")) {
			ctx.setVariable("alert", "ALERT: Image was already into the specified album");
		}
		if(remove != null && remove.equals("valid")) {
			ctx.setVariable("alert", "Album removed successfully");
		}
		templateEngine.process(templatePath, ctx, response.getWriter());
	
		
	}	
}
