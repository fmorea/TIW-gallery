package it.polimi.tiw.gallery.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.Connection;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import it.polimi.tiw.gallery.dao.UserDAO;
import it.polimi.tiw.gallery.exceptions.ValidationError;
import it.polimi.tiw.gallery.utils.ConnectionHandler;
import it.polimi.tiw.gallery.utils.Initializer;


@WebServlet("/Registration")
public class Registration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public Registration() {
		super();
	}

	public void init() throws ServletException {
		this.templateEngine = Initializer.templateEngineInit(getServletContext());
		this.connection =  Initializer.connectionInit(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String newUsername = request.getParameter("new_username");
		String newPassword = request.getParameter("new_password");
		String newPasswordRepetition = request.getParameter("new_password_bis");
		String newEmail = request.getParameter("new_email");
		
		
		// Controlli necessari in caso di "malicious client", situazioni che non si dovrebbero verificare normalmente
		// poiché controllate anche client side
		
		// Controlli di non nullità
		if (	newUsername == null  						||
				newUsername.isEmpty()  						|| 
				newPassword == null 						||
				newPassword.isEmpty() 						||
				newPasswordRepetition == null 				||
				newPasswordRepetition.isEmpty() 			||
				newEmail == null 							||
				newEmail.isEmpty() 							){
			
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect parameters: empty field");
			return;
		}
		
		if (
				!newPassword.equals(newPasswordRepetition)  &&
				!isValidEmail(newEmail)){
			
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect parameters: password and repeat password do not match and the inserted mail is not valid");
			return;
		}
		
		
		
		if (
				!newPassword.equals(newPasswordRepetition) 
				){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect parameters: password and repeat password do not match");
				return;
			}
		
		if (
				!isValidEmail(newEmail)
				){
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect parameters: email not valid");
				return;
			}		
		
		// Controllo che può essere fatto solo server side
		// Controllo di unicità dello username
		
		UserDAO userDAO= new UserDAO(connection);
		
		try {
			if (userDAO.existUserWithNicknameEqualsTo(newUsername)) {
				// Lo username non è unico
				response.sendRedirect(getServletContext().getContextPath() + "/CheckLogin" + "?registration=invalid");
			}
			else {
				// Lo username è unico, posso registrare i dati dell'utente nel database
				userDAO.register(newUsername, newEmail, newPassword);
				response.sendRedirect(getServletContext().getContextPath() + "/CheckLogin" + "?registration=valid");
			}
		} catch (SQLException | IOException | ValidationError e) {
			e.printStackTrace();
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
	
	public boolean isValidEmail(String email) {
		String  emailRegexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" 
		        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
		
		Pattern emailPattern = Pattern.compile(emailRegexPattern);
		
		if (email == null) {
            return false;
        }

        Matcher matcher = emailPattern.matcher(email);

        return matcher.matches();
	}
}



