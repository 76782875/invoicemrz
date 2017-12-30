package base;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import java.util.LinkedList;
import java.util.List;

public class DocParagraph {
	private String text = "";
	private int paragraphNum = 0;
	private Rectangle2D box = null;
	private double confidence = 0.0; 
	private DocPage page;
	private List<DocLine> lines = new LinkedList<DocLine>();
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getParagraphNum() {
		return paragraphNum;
	}
	public void setParagraphNum(int paragraphNum) {
		this.paragraphNum = paragraphNum;
	}
	public DocPage getPage() {
		return page;
	}
	public void setPage(DocPage page) {
		this.page = page;
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
	public List<DocLine> getLines() {
		return lines;
	}
	public void setLine(DocLine line) {
		this.lines.add(line);
	}	
}
