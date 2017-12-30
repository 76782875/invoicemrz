package base;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

public class DocPage {
	private String text = "";
	private int pageNum = 0;
	private Rectangle2D box = null;
	private double confidence = 0.0; 
	private List<DocParagraph> paragraphs = new LinkedList<DocParagraph>();
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getPageNum() {
		return pageNum;
	}
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	public Rectangle2D getBox() {
		return box;
	}
	public void setBox(double x, double y, double w, double h) {
		this.box = new Rectangle2D.Double(x, y, w, h);
	}
	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}	
	public List<DocParagraph> getLines() {
		return paragraphs;
	}
	public void setParagraph(DocParagraph line) {
		this.paragraphs.add(line);
	}
}
