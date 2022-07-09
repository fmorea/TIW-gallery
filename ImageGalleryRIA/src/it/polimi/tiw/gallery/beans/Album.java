package it.polimi.tiw.gallery.beans;

import java.util.Date;

public class Album {
	private int id;
	private String title;
	private Date date;
	private int creatorId;
	private String creator;
	
	public int getId() { 
		return id; 
		}
	public void setId(int id) { 
		this.id = id; 
		}
	
	public String getTitle() { 
		return title; 
		}
	public void setTitle(String title) { 
		this.title = title; 
		}
	
	public Date getDate() { 
		return date; 
		}
	public void setDate(Date date) { 
		this.date = date; 
		}
	public int getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}
	public String getCreator() { 
		return creator; 
		}
	public void setCreator(String creator) { 
		this.creator = creator; 
		}
	
	
}
