package it.polimi.tiw.gallery.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Connection;

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

import it.polimi.tiw.gallery.beans.User;
import it.polimi.tiw.gallery.dao.UserDAO;
import it.polimi.tiw.gallery.utils.ConnectionHandler;
import it.polimi.tiw.gallery.utils.Initializer;


@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CheckLogin() {
		super();
	}

	public void init() throws ServletException {
    	this.templateEngine = Initializer.templateEngineInit(getServletContext());
		this.connection =  Initializer.connectionInit(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Stringa utilizzata per capire se l'utente proviene dalla form di registrazione
		String userCameFromRegistration = StringEscapeUtils.escapeJava(request.getParameter("registration"));
		
		// Se l'utente proviene dalla pagina di registrazione
		if (userCameFromRegistration != null) {
			
		// Se l'utente ha fatto correttamente la registrazione deve loggarsi
		if(userCameFromRegistration.equals("valid")) {
			String templatePath = "index.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("alert", "Registration Successful, now you can Log In");
			templateEngine.process(templatePath, ctx, response.getWriter());
		}
		// Se l'utente ha scelto uno username già esistente deve loggarsi
		else if(userCameFromRegistration.equals("invalid")) {
			String templatePath = "index.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("alert", "The username you chose already exists, please choose another one.");
			templateEngine.process(templatePath, ctx, response.getWriter());
		}
		}
		// altrimenti l'utente proviene dalla form di login 
		else {
		String identifier = request.getParameter("identifier");
		String pwd = request.getParameter("pwd");
		
		if (identifier == null || identifier.isEmpty() || pwd == null || pwd.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters for the log in");
			return;
		}
		
		UserDAO usr = new UserDAO(connection);
		User u = null;
		try {
			u = usr.checkCredentials(identifier, pwd);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database credential checking");
			return;
		}
		String path = getServletContext().getContextPath();
		if (u == null) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("alert", "Wrong combination of user/email and password");
			path = "/index.html";
			templateEngine.process(path, ctx, response.getWriter());
		} else {
			request.getSession().setAttribute("user", u);
			String target ="/GoToHome" ;
			path = path + target;
			response.sendRedirect(path);
		}
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
}


