package Bingo.spider ;

public class VideoInfo {
	
	private String  imgUrl;
	private String  url;
	private String  title;
	private String  keyWord;
	private String  description;
	
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getKeyWord() {
		return keyWord;
	}
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString(){
		return  url + "\n ->" +imgUrl+"\n ->"
		                   +title+"\n ->"
		                   +keyWord+"\n ->"
		                   +description+"\n ->";
	}
}