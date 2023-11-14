package it.polimi.tiw.tiwjs.beans;

import java.sql.Date;

public class Playlist {
	private String sorting;
	private int idPlaylist;
	private String playlistName;
	private Date creationDate;
	private int idUser;
	
	//JS version
	public String getSorting() {
		return sorting;
	}
	
	public void setSorting(String sorting) {
		this.sorting = sorting;
	}
	
	public int getIdPlaylist() {
		return idPlaylist;
	}
	public void setIdPlaylist(int idPlaylist) {
		this.idPlaylist = idPlaylist;
	}
	
	public String getPlaylistName() {
		return playlistName;
	}
	public void setPlaylistName(String playlistName) {
		this.playlistName = playlistName;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public int getIdUser() {
		return idUser;
	}
	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

}
