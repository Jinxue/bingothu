package Bingo.search;

public class SearchResultBean {
	private String url;
	private String title;
	private String description;
	private String imageFileName;
	/**
	 * @param url the URL to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the URL
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param imageFileName the imageFileName to set
	 */
	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}
	/**
	 * @return the imageFileName
	 */
	public String getImageFileName() {
		return imageFileName;
	}
}
