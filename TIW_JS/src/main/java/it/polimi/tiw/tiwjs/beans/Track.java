package it.polimi.tiw.tiwjs.beans;

public class Track {
	private int idTrack;
	private String title;
	private String author;
	private String genre;
	private String album;
	private int year;
	private String image;
	private String audioTrack;
	//Foreign key to link with users
	private int userCreator;
	//Used during playlist creation if true then the track is inserted inside playlist (DEFAULT: false)
	private boolean selected = false;
	
	public int getIdTrack() {
		return idTrack;
	}
	public void setIdTrack(int idTrack) {
		this.idTrack = idTrack;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public int getUserCreator() {
		return userCreator;
	}
	public void setUserCreator(int userCreator) {
		this.userCreator = userCreator;
	}
	public String getAudioTrack() {
		return audioTrack;
	}
	public void setAudioTrack(String audioTrack) {
		this.audioTrack = audioTrack;
	}
	public boolean getSelected() {
		return selected;
	}
	public void setSelected(boolean value) {
		selected = value;
	}
	
}
