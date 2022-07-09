package it.polimi.tiw.gallery.controllers;


import it.polimi.tiw.gallery.utils.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.gallery.beans.Album;
import it.polimi.tiw.gallery.beans.User;
import it.polimi.tiw.gallery.dao.AlbumDAO;
import it.polimi.tiw.gallery.dao.ImageDAO;



import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/UploadNewImage")
public class UploadNewImage extends HttpServlet {
	private  static final long serialVersionUID = 1L;
    private  Connection connection;
    private TemplateEngine templateEngine;
    private  final String SAVE_DIR = "img";

    @Override
    public void init() throws UnavailableException {
		this.connection =  Initializer.connectionInit(getServletContext());
		this.templateEngine = Initializer.templateEngineInit(getServletContext());
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User u = (User) session.getAttribute("user");
        Map<String, String> fieldMapValue = handleRequest(request);
        String title;
        String description;
        String path;
        String stringDate;
        
        title = fieldMapValue.get("imagetitle");
        description = fieldMapValue.get("imagedescription");
        path = fieldMapValue.get("path");
        stringDate = fieldMapValue.get("imagedate");
        
        // Controllo di non nullità dei parametri inseriti
     		if (	title == null 			||
     				title.isEmpty() 		||
     				description == null 	||
     				description.isEmpty()	||
     				path == null		||
     				path.isEmpty()		||
     				stringDate == null		||
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
             
             int userid = u.getId();
             ImageDAO imageDAO = new ImageDAO(connection);
             try {
				imageDAO.createImage(title, description, path, date, userid);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Error creating image entry in the database");
				return;
			}
     		
    		response.sendRedirect(getServletContext().getContextPath() + "/GoToHome" + "?upload=valid");
			

    }

    protected Map<String, String> handleRequest(HttpServletRequest request) {
        HashMap<String, String> fieldMapValue = new HashMap<>();
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        File file;
        String profilePicturePath = getServletContext().getRealPath("") + File.separator + SAVE_DIR + File.separator;
        File uploadDir = new File(profilePicturePath);
        if(!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        try {
            List<FileItem> list = upload.parseRequest(new ServletRequestContext(request));
            for (FileItem item: list){
                if(item.isFormField()) {
                    fieldMapValue.put(item.getFieldName(), item.getString());
                }
                else {
                    String fileName = item.getName();  // Let's say /pippo/pluto/topolino.jpg   (from client pc)
                    String realFilePath;	// for example /Applications/Tomcat/wtpwebapps/ImageGallery/img/topolino.jpg
                    
                    // the code removes the path before the original file name
                    // with substring method is selected "topolino.jpg"
                    // and the server images path is concatenated
                    
                    int lastOccurrenceOfSlash = fileName.lastIndexOf('\\');
                    if ( lastOccurrenceOfSlash >= 0) {
                    	realFilePath = profilePicturePath + fileName.substring(lastOccurrenceOfSlash);
                    } else {
                    	realFilePath = profilePicturePath + fileName.substring(lastOccurrenceOfSlash + 1);
                    }
                    
					file = new File(realFilePath);
                    item.write(file);
                    fieldMapValue.put("path", fileName);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return fieldMapValue;
    }
}
