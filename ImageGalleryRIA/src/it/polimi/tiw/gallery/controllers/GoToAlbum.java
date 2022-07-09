package it.polimi.tiw.gallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.gallery.beans.Album;
import it.polimi.tiw.gallery.beans.Comment;
import it.polimi.tiw.gallery.beans.Image;
import it.polimi.tiw.gallery.dao.AlbumDAO;
import it.polimi.tiw.gallery.dao.CommentDAO;
import it.polimi.tiw.gallery.dao.ImageDAO;
import it.polimi.tiw.gallery.utils.ConnectionHandler;
import it.polimi.tiw.gallery.utils.Initializer;

@WebServlet("/GoToAlbum")
public class GoToAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
	
	private final int pageSize = 5;
	
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
		Integer albumId = null;
		Integer imageId = null;
		try {
			// ottengo l'id dell'album dalla get
			albumId = Integer.parseInt(request.getParameter("album"));
			// Se l'id dell'immagine è nullo vorrà dire che il client sta richiedendo la prima immagine dell'album
			String imageIdString = StringEscapeUtils.escapeJava(request.getParameter("image"));
			if(imageIdString != null)
				imageId = Integer.parseInt(imageIdString);
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}		

		// Recupera l'album e tutte le sue immagini
		AlbumDAO aDAO = new AlbumDAO(connection);
		ImageDAO iDAO = new ImageDAO(connection);
		Album album = null;
		List<Image> images = null;
		try {
			album = aDAO.findAlbumById(albumId);
			images = iDAO.findAllImagesOfAlbum(albumId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover the album");
			return;
		}

		List<Image> pageImages = null;
		Image prevImage = null;
		Image nextImage = null;		
		Image activeImage = null;
		List<Comment> comments = null;		
		
		
		// se c'è almeno una immagine
		if(images.size() > 0) {
			// se l'immagine (passata con il suo id nella GET http) non è nulla
			if(imageId != null) {
				final Integer id = imageId;
				// L'immagine attiva è quella che ha come id quello scelto dall'utente
				activeImage = images.stream().filter(image -> image.getId() == id).collect(Collectors.toList()).get(0);
			}
			if(activeImage == null)
				// Altrimenti è la prima immagine dell'album
				activeImage = images.get(0);
			
			// Recupera tutti i commenti relativi a quell'immagine	
			CommentDAO cDAO = new CommentDAO(connection);
			try {
				comments = cDAO.findCommentsByImage(activeImage.getId());
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error fetching comments");
				return;
			}
			
			// Seleziona il blocco di immagini al quale l'immagine attiva appartiene.
			int imageIndex = images.indexOf(activeImage);
			int lowerBound = (int)Math.floorDiv(imageIndex, pageSize) * pageSize;
			int upperBound = Math.min(images.size(), lowerBound + pageSize);
			pageImages = images.subList(lowerBound, upperBound);	
			if(lowerBound > 0)
				prevImage = images.get(imageIndex - pageSize);
			if(upperBound < images.size())
				nextImage = images.get(Math.min(images.size() - 1, imageIndex + pageSize));
		}
		
		String path = "/WEB-INF/album.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("album", album);
		ctx.setVariable("images", pageImages);
		ctx.setVariable("prev", prevImage);	
		ctx.setVariable("next", nextImage);			
		ctx.setVariable("active", activeImage);	
		ctx.setVariable("comments", comments);	
		templateEngine.process(path, ctx, response.getWriter());
	}
}
