package it.polimi.tiw.gallery.exceptions;

public class ValidationError extends Exception {
	private static final long serialVersionUID = 1L;
	
	public ValidationError(String message) {
		super(message);
	}

}
