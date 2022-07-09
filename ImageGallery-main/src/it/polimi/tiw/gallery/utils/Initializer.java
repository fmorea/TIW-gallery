package it.polimi.tiw.gallery.utils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import java.sql.Connection;

public class Initializer {

    private Initializer() {}

    public static Connection connectionInit(ServletContext context) throws UnavailableException {
        Connection connection = null;
        connection = ConnectionHandler.getConnection(context);
        return connection;
    }

    public static TemplateEngine templateEngineInit(ServletContext servletContext) {
        ServletContext context = servletContext;
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        TemplateEngine newTemplateEngine = new TemplateEngine();
        newTemplateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        return newTemplateEngine;
    }
}

