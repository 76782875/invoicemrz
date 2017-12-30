package base;

import java.awt.geom.Rectangle2D;

public class PageRectangle {
	private Rectangle2D rectangle;
	private String type;
	private String text;
	
	public PageRectangle(Rectangle2D rect, String type){
		this.rectangle = rect;
		this.type = type;
	}

	public Rectangle2D getRectangle() {
		return rectangle;
	}

	public void setRectangle(Rectangle2D rectangle) {
		this.rectangle = rectangle;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String toString(){
		return /*" ," + */ rectangle.getX() + "," + rectangle.getWidth() + ","+ rectangle.getY()  + "," + rectangle.getHeight() + "," + text.replaceAll(",", "");
	}
}
