package be.csmmi.zombiegame.rendering;

public class Cam {
	private String url;
	private String name;
	
	public Cam(String name, String url) {
		this.url = url;
		this.name = name;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getName() {
		return name;
	}
}
