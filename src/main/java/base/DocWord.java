package base;

import java.awt.geom.Rectangle2D;

public class DocWord {
	private String word = "";
	private DocLine line;
	private Rectangle2D box = null;
	private double confidence = 0.0;
	public String getText() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public DocLine getLine() {
		return line;
	}
	public void setLine(DocLine line) {
		this.line = line;
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
}
