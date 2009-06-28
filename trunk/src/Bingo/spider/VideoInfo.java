package Bingo.spider ;

public class VideoInfo {
	
	private String  imgUrl = new String();
	private String  url = new String();
	private String  title = new String();
	private String  keyWord = new String();
	private String  description = new String();
	private String 	source = new String();	// From which 
	
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
		                   +description+"\n ->"
		                   + "From " + source+"\n ->";
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	
	public String toXML(){
		String content = 	"<item><title>" + title + 
		"</title><keyword>" + keyWord +
		"</keyword><description>" + description +
		"</description><imageurl>" + imgUrl +
		"</imageurl><url>" + url +
		"</url></item>";
		return content;
	}
}