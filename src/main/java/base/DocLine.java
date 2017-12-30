package base;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

public class DocLine {
	private String line = "";
	private DocParagraph paragraph;
	private Rectangle2D box = null;
	private double confidence = 0.0;
	private List<DocWord> words = new LinkedList<DocWord>();
	
	public String getText() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public DocParagraph getParagraph() {
		return paragraph;
	}
	public void setParagraph(DocParagraph paragraph) {
		this.paragraph = paragraph;
	}
	public Rectangle2D getBox() {
		return box;
	}
	public void setBox(Rectangle2D box) {
		this.box = box;
	}
	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	public List<DocWord> getWords() {
		return words;
	}
	public void setWord(DocWord word) {
		this.words.add(word);
	}	
}
