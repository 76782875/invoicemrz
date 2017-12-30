package base;

import java.awt.geom.Rectangle2D;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PDFTextCoordinates extends PDFTextStripper {
	
	public List<PageRectangle> wordList = new ArrayList<PageRectangle>();
	
	public PageRectangle tWord = null;
	public boolean is1stChar = true;
	public boolean lineMatch;
	public int pageNo = 1;
	public double lastYVal;
	public double lastXVal;
	public double characterWidth = 0;
	public double currentWordGapWidth = 0;

	public PDFTextCoordinates()
	        throws IOException {
	    super.setSortByPosition(true);
	}
	
	public List<PageRectangle> getWordList(){
		return wordList();
	}
	
	
	
	public List<PageRectangle> wordList(){
		return null;
	}
	
	public List<PageRectangle> lineList(){
		return null;
	}
	
	public List<PageRectangle> paragraphList(){
		return null;
	}
	
	public DocPage getTIFFContents(int page_num, String str) throws Exception{
		Document doc = Jsoup.parse(str);
		
		DocPage pageContent;
		pageContent = new DocPage();
		pageContent.setPageNum(page_num);
		
        Elements paragraphs = doc.getElementsByAttributeValue("class", "ocr_par");
        
        for (Element paragraph : paragraphs){
        	String text = paragraph.text();
        	text = text.replaceAll("�(?=.*[\\p{P}])[^ ]", "fi");
        	text = text.replaceAll("’", "'");
        	text = text.replaceAll("�(?=.*[^\\w])[^ ]", "'").replaceAll("�", "\"");
        	
        	if (text.isEmpty()){
        		continue;
        	}
        	
        	DocParagraph wcClaimParagraph = new DocParagraph();
        	wcClaimParagraph.setText(text);
        	
        	double confidence = getConfidence(paragraph);
        	wcClaimParagraph.setConfidence(confidence);
        	
        	if (paragraph.hasAttr("title")){
        		String title = paragraph.attr("title");
        		String[] params = title.split("\\s+");
        		if (params.length == 5){
        			try{
        				wcClaimParagraph.setBox(Integer.valueOf(params[1]), Integer.valueOf(params[2]), Integer.valueOf(params[3]), Integer.valueOf(params[4]));
        			}catch(NumberFormatException ex){
        				//System.out.println("bbox error");
        			}
        		}
        	}
        	if (paragraph.hasAttr("id")){
        		String title = paragraph.attr("id");
        		String[] params = title.split("_");
        		if (params.length == 3){
        			try{
        				wcClaimParagraph.setParagraphNum(Integer.valueOf(params[2]));
        			}catch(NumberFormatException ex){
        				//System.out.println("par_id error");
        			}
        		}
        	}
        	List<DocLine> lines = getLinesAndWords(paragraph);
        	for (DocLine line : lines){
        		wcClaimParagraph.setLine(line);
        	}
        	pageContent.setParagraph(wcClaimParagraph);
        }
		
        return pageContent;
	}
	
	public List<DocPage> getContents(PDDocument document) throws Exception{
		
		if (document == null){
			return null;
		}
		
		List<DocPage> contentss = new ArrayList<DocPage>();
		
        List<PDPage> allPages = document.getDocumentCatalog().getAllPages();

        if (allPages.isEmpty()){
        	return null;
        }
        
        PDPage fpage = (PDPage) allPages.get(0);
        PDResources resources = fpage.getResources();
        
        String text = "";
        int numImages = 0;
        if (resources != null){
	        Map pageImages = resources.getImages();
	        
	        if (pageImages != null) {
	        	numImages += pageImages.size();
	        }
        }
        
        PDFTextStripper pdfStripper = new PDFTextStripper();
		pdfStripper.setEndPage(1);
		text = pdfStripper.getText(document).trim();
		
		if (text.isEmpty()){
			return null;
		}
        
        for (int i = 0; i < allPages.size(); i++) {
            PDPage page = (PDPage) allPages.get(i);
            PDStream contents = page.getContents();

            if (contents != null) {
                processStream(page, page.findResources(), page.getContents().getStream());
                close();
            }
            pageNo += 1;
            PDFPageContent content = new PDFPageContent(wordList, 4);
            
            
            DocPage p = new DocPage();
            p.setPageNum(i);
            p.setConfidence(1.0);
            p.setText(text);
            
            List<DocLine> lineList = new ArrayList<DocLine>();
            for (PageRectangle record : wordList){
            	DocLine line = new DocLine();
            	line.setBox(record.getRectangle());
            	line.setConfidence(1.0);
            	line.setLine(record.getText());
            	
            	DocWord word = new DocWord();
            	word.setBox(record.getRectangle());
            	word.setConfidence(1.0);
            	word.setLine(line);
            	word.setWord(record.getText());
            	
            	line.setWord(word);
            	lineList.add(line);
            }
            
			boolean bChange = true;
			while (bChange){
				bChange = false;
				for (DocLine record : lineList){
					
					if (record.getText().isEmpty()){
						continue;
					}
					
					double closestDist = Double.MAX_VALUE;
					DocLine closestRect = null;
					bChange = false;
					for (DocLine recordA : lineList){
						
						if (recordA.getText().isEmpty()){
							continue;
						}
						
						double dist = Double.MAX_VALUE;
						if (record != recordA && DistanceUtils.isSameLine(record, recordA, "application/pdf")){
							dist = DistanceUtils.distance(record, recordA);
							if (dist < closestDist){
								closestRect = recordA;
								closestDist = dist;
							}
						}
					}
					
					if (closestRect != null){
						lineList.add(DistanceUtils.union(record, closestRect));
						lineList.remove(record);
						lineList.remove(closestRect);
						bChange = true;
						break;
					}
				}
			}
			
			int paragraphNum = 1;
			for (DocLine line :lineList){
				DocParagraph paragraph = new DocParagraph();
				paragraph.setParagraphNum(paragraphNum);
				paragraph.setBox(line.getBox().getX(), line.getBox().getY(), line.getBox().getWidth(), line.getBox().getHeight());
				paragraph.setText(line.getText());
				paragraph.setPage(p);
				paragraph.setLine(line);
				paragraphNum++;
				p.setParagraph(paragraph);
			}
			
			wordList.clear();
            contentss.add(p);
        }
        
        return contentss;
	}
	
	public List<DocPage> getContents(InputStream in) throws Exception{
		PDDocument document = null;
		
		try {
	    	
	    	if (in == null){
	    		return null;
	    	}
	        
	        document = PDDocument.load(in);
	        return getContents(document);
	    } finally {
	        if (document != null) {
	            document.close();
	        }
	    }
	}
		
	public static void main(String[] args)
	        throws Exception {
	    PDDocument document = null;
	    
	    try {
	        File input = new File("C:\\Users\\u392576\\Downloads\\201509232b2194fc-7af0-44b2-bc0c-949f9ab515e2.pdf");
	        //File input = new File("C:\\Users\\u392576\\Downloads\\send\\pdfs\\hertz-native\\22429892_Hertz Invoice A62383780.PDF");
	        document = PDDocument.load(input);
	        
	        PDFTextCoordinates printer = new PDFTextCoordinates();
	        List allPages = document.getDocumentCatalog().getAllPages();

	        for (int i = 0; i < allPages.size(); i++) {
	            PDPage page = (PDPage) allPages.get(i);
	            PDStream contents = page.getContents();

	            if (contents != null) {
	                printer.processStream(page, page.findResources(), page.getContents().getStream());
	            }
	            printer.pageNo += 1;
	        }
	        printer.close();
	        
	        File file = new File ("coordinates.txt");
	        PrintWriter printWriter = new PrintWriter (file);

	        for (PageRectangle rect: printer.getWordList()){
	        	printWriter.println(rect.toString());
	        }
	        
	        printWriter.close ();
	    } finally {
	        if (document != null) {
	            document.close();
	        }
	    }
	}

	@Override
	protected void processTextPosition(TextPosition text) {
	    String tChar = text.getCharacter();
	    char c = tChar.charAt(0);
	    
	    if (tWord == null){
	    	Rectangle2D r = new Rectangle2D.Double(0,0,0,0);
	    	tWord = new PageRectangle(r, "word");
	    	tWord.setText("");
	    }
	    
	    if (tChar.equals("N")){
	    	tChar = "N";
	    }
	    
	    lineMatch = matchCharLine(text);
	    
	    if (!(Character.isWhitespace(c) || Character.isSpaceChar(c) || Character.isISOControl(c) || (currentWordGapWidth > text.getWidthOfSpace()))) {
	        if ((!is1stChar) && (lineMatch == true)
	        		) {
	            appendChar(tChar);
	            characterWidth = roundVal(Float.valueOf(text.getWidthDirAdj()));
	            lastXVal = roundVal(Float.valueOf(text.getXDirAdj() + text.getWidthDirAdj()));
	            lastYVal = roundVal(Float.valueOf(text.getYDirAdj()));
	        } else if (is1stChar == true) {
	            setWordCoord(text, tChar);
	            lastXVal = roundVal(Float.valueOf(text.getXDirAdj() + text.getWidthDirAdj()));
	            lastYVal = roundVal(Float.valueOf(text.getYDirAdj()));
	        }
	    } 
	    else {
	        endWord();
            lastXVal = roundVal(Float.valueOf(text.getXDirAdj() + text.getWidthDirAdj()));
            lastYVal = roundVal(Float.valueOf(text.getYDirAdj()));
            if (!(Character.isWhitespace(c) || Character.isSpaceChar(c) || Character.isISOControl(c))){
            	setWordCoord(text, tChar);
	            lastXVal = roundVal(Float.valueOf(text.getXDirAdj() + text.getWidthDirAdj()));
	            lastYVal = roundVal(Float.valueOf(text.getYDirAdj()));
    	    }
	    }
	}

	protected void appendChar(String tChar) {
	    tWord.setText(tWord.getText() + tChar);
	    is1stChar = false;
	}

	protected void setWordCoord(TextPosition text, String tChar) {
		
		if (tWord == null){
			Rectangle2D r = new Rectangle2D.Double(0,0,0,0);
	    	tWord = new PageRectangle(r, "word");
	    	tWord.setText("");
	    }
		
		Rectangle2D rectangle = new Rectangle2D.Double(roundVal(Float.valueOf(text.getXDirAdj())), roundVal(Float.valueOf(text.getYDirAdj())), roundVal(Float.valueOf(text.getWidthDirAdj())).intValue(), roundVal(Float.valueOf(text.getHeightDir())));
	    tWord.setRectangle(rectangle);
	    tWord.setText(tWord.getText() + tChar);
	    is1stChar = false;
	    currentWordGapWidth = 0;
	}

	protected void endWord() {
		
		if (tWord == null){
			return;
		}
		
		if ((tWord.getText() != null) && !tWord.getText().isEmpty()){
			tWord.setText(tWord.getText().replaceAll("[^\\x00-\\x7F]", "").trim());
		}
		
	    if ((tWord.getText() != null) && !tWord.getText().isEmpty()){
	    	double x1 = tWord.getRectangle().getX();
		    double y1 = tWord.getRectangle().getY();
		    double x2 = lastXVal + tWord.getRectangle().getWidth();
		    double y2 = lastYVal + tWord.getRectangle().getHeight();
		    tWord.setRectangle(new Rectangle2D.Double(x1, y1, x2, y2));
	    	wordList.add(tWord);
	    }
	    
	    tWord = null;
	    is1stChar = true;
	    currentWordGapWidth = 0;
	}

	protected boolean matchCharLine(TextPosition text) {
	    Double yVal = roundVal(Float.valueOf(text.getYDirAdj()));
	    Double xVal = roundVal(Float.valueOf(text.getXDirAdj() + text.getWidthDirAdj()));
	    if ((yVal.doubleValue() == lastYVal) && lastXVal < xVal) {
	    	currentWordGapWidth = roundVal(Float.valueOf(text.getXDirAdj())) - lastXVal;
	        return true;
	    }
	    endWord();
	    lastYVal = yVal.doubleValue();
	    lastXVal = xVal.doubleValue();
	    
	    return false;
	}

	protected Double roundVal(Float yVal) {
	    DecimalFormat rounded = new DecimalFormat("0.0'0'");
	    Double yValDub = new Double(rounded.format(yVal));
	    return yValDub;
	}	
	
	public void close(){
		endWord();
	}	
	
	public double getConfidence(Element paragraph){
        Elements words = paragraph.getElementsByAttributeValue("class", "ocrx_word");
        
        int sum = 0;
        for (Element word : words){
        	if (word.hasAttr("title")){
        		String title = word.attr("title");
        		String[] params = title.split("x_wconf");
        		if (params.length == 2){
        			int confidence = Integer.valueOf(params[1].trim());
        			sum += confidence;
        		}
        	}
        }
        
        return (double)sum/(double)words.size();
	}
	
	public List<DocLine> getLinesAndWords(Element paragraph){
		Elements lines = paragraph.getElementsByAttributeValue("class", "ocr_line");
		List<DocLine> lns = new LinkedList<DocLine>();
		for (Element line : lines){
        	DocLine ln = new DocLine();
        	String text = line.text();
        	text = text.replaceAll("�(?=.*[\\p{P}])[^ ]", "fi");
        	text = text.replaceAll("’", "'");
        	text = text.replaceAll("�(?=.*[^\\w])[^ ]", "'").replaceAll("�", "\"");
        	ln.setLine(text);
        	
        	double confidence = getConfidence(line);
        	ln.setConfidence(confidence);
        	
        	if (line.hasAttr("title")){
        		String title = line.attr("title");
        		String[] params1 = title.split(";");
        		String[] params = params1[0].split("\\s+");
        		
        		if (params.length == 5){
        			try{
        				ln.setBox(new Rectangle2D.Double(Double.valueOf(params[1]), Double.valueOf(params[2]), Double.valueOf(params[3]), Double.valueOf(params[4])));
        			}catch(NumberFormatException ex){
        				System.out.println("bbox error");
        			}
        		}
        	}
        	
	        Elements words = line.getElementsByAttributeValue("class", "ocrx_word");
	        for (Element word : words){
	        	DocWord w = new DocWord();
	        	w.setLine(ln);
	        	text = word.text();
	        	text = text.replaceAll("�(?=.*[\\p{P}])[^ ]", "fi");
	        	text = text.replaceAll("’", "'");
	        	text = text.replaceAll("�(?=.*[^\\w])[^ ]", "'").replaceAll("�", "\"");
	        	w.setWord(text);
	        	if (word.hasAttr("title")){
	        		String title = word.attr("title");
	        		String[] params = title.split("x_wconf");
	        		if (params.length == 2){
	        			confidence = Integer.valueOf(params[1].trim());
	        			w.setConfidence(confidence);
	        		}
	        		String[] params1 = title.split(";");
	        		String[] params2 = params1[0].split("\\s+");
	        		
	        		if (params2.length == 5){
	        			try{
	        				w.setBox(new Rectangle2D.Double(Double.valueOf(params2[1]), Double.valueOf(params2[2]), Double.valueOf(params2[3]), Double.valueOf(params2[4])));
	        			}catch(NumberFormatException ex){
	        				//System.out.println("bbox error");
	        			}
	        		}
	        	}
	        	
	        	ln.setWord(w);
	        }
	        lns.add(ln);
        }
		
        return lns;
	}
}
