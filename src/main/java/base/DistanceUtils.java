package base;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.PageRectangle;

public class DistanceUtils {
    public static boolean isSameLine(DocLine rec1, DocLine rec2, String mimetype){
    	
    	List<Double> xs = new ArrayList<Double>();
    	List<Double> ys = new ArrayList<Double>();
    	
    	Rectangle2D rect1 = rec1.getBox();
    	Rectangle2D rect2 = rec2.getBox();
    	
    	xs.add(rect1.getX());
    	xs.add(rect1.getWidth());
    	xs.add(rect2.getX());
    	xs.add(rect2.getWidth());
    	
    	ys.add(rect1.getY());
    	ys.add(rect1.getHeight());
    	ys.add(rect2.getY());
    	ys.add(rect2.getHeight());
    	
    	double xSide = Math.abs(Collections.min(xs) - Collections.max(xs));
    	double ySide = Math.abs(Collections.min(ys) - Collections.max(ys));
    	
    	double y1 = Math.abs(rect1.getY() - rect1.getHeight());
    	double y2 = Math.abs(rect2.getY() - rect2.getHeight());
    	
    	double x1 = Math.abs(rect1.getX() - rect1.getWidth());
    	double x2 = Math.abs(rect2.getX() - rect2.getWidth());
    	
    	if (mimetype.equals("application/pdf") && (ySide < y1 + y2 /*&& xSide > x1 + x2*/)){
    		return true;
    	}else if (!mimetype.equals("application/pdf") && (ySide < y1 + y2 && xSide > x1 + x2)){
    		return true;
    	}
    	
    	return false;
    }
    
    public static DocLine union(DocLine rec1, DocLine rec2){
    	List<Double> xs = new ArrayList<Double>();
    	List<Double> ys = new ArrayList<Double>();
    	
    	Rectangle2D rect1 = rec1.getBox();
    	Rectangle2D rect2 = rec2.getBox();
    	
    	xs.add(rect1.getX());
    	xs.add(rect1.getWidth());
    	xs.add(rect2.getX());
    	xs.add(rect2.getWidth());
    	
    	ys.add(rect1.getY());
    	ys.add(rect1.getHeight());
    	ys.add(rect2.getY());
    	ys.add(rect2.getHeight());
    	
    	Rectangle2D rect = new Rectangle2D.Double();
    	rect.setRect(Collections.min(xs), Collections.min(ys), Collections.max(xs),  Collections.max(ys));
    	
    	DocLine newRecord = new DocLine();
    	newRecord.setBox(rect);
    	
    	if (rect1.getX() < rect2.getX()){
    		newRecord.setLine(rec1.getText() + " " + rec2.getText());
    	}else{
    		newRecord.setLine(rec2.getText() + " " + rec1.getText());
    	}
    	
    	for (DocWord word : rec1.getWords()){
    		word.setLine(newRecord);
    		newRecord.setWord(word);
    	}

    	for (DocWord word : rec2.getWords()){
    		word.setLine(newRecord);
    		newRecord.setWord(word);
    	}
    	
    	return newRecord;
    }
    
    public static int median(int[] recordLengths){
		int median = -1;
		Arrays.sort(recordLengths);
		int middle = ((recordLengths.length) / 2);
		if(recordLengths.length % 2 == 0){
		 int medianA = recordLengths[middle];
		 int medianB = recordLengths[middle-1];
		 median = (medianA + medianB) / 2;
		} else{
		 median = recordLengths[middle + 1];
		}
		return median;
    }
    
    public static int bestMax(int[] recordLengths){
		Arrays.sort(recordLengths);
		Map<Integer, Integer> lengths = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < recordLengths.length; i++){
			lengths.put(recordLengths[i], 0);
		}
		
		for (int i = 0; i < recordLengths.length; i++){
			lengths.put(recordLengths[i], lengths.get(recordLengths[i]) + 1);
		}
		
		int max_entry = 0;
		for (Map.Entry<Integer, Integer> entry : lengths.entrySet()){
			if (entry.getValue() >= 5 && entry.getKey() >= max_entry){
				max_entry = entry.getKey();
			}
		}
		
		return max_entry;
    }
    
    public static double median(List<Double> records){
		double median = -1;
		
		double[] recordLengths = new double[records.size()];
		
		int index = 0;
		for (Double record : records){
			recordLengths[index] = record;
			index++;		
		}
		
		Arrays.sort(recordLengths);
		int middle = ((recordLengths.length) / 2);
		if(recordLengths.length % 2 == 0){
		 double medianA = recordLengths[middle];
		 double medianB = recordLengths[middle-1];
		 median = (medianA + medianB) / 2;
		} else{
			if (recordLengths.length == 1){
				median = recordLengths[0];
			}else{
				median = recordLengths[middle + 1];
			}
		}
		return median;
    }
    
    public static double distance(DocLine rec1, DocLine rec2){
    	
    	Rectangle2D rect1 = rec1.getBox();
    	Rectangle2D rect2 = rec2.getBox();
    	
    	List<Double> xs = new ArrayList<Double>();
    	List<Double> ys = new ArrayList<Double>();
    	
    	xs.add(rect1.getX());
    	xs.add(rect1.getWidth());
    	xs.add(rect2.getX());
    	xs.add(rect2.getWidth());
    	
    	ys.add(rect1.getY());
    	ys.add(rect1.getHeight());
    	ys.add(rect2.getY());
    	ys.add(rect2.getHeight());
    	
    	double xSide = Math.abs(Collections.min(xs) - Collections.max(xs));
    	double ySide = Math.abs(Collections.min(ys) - Collections.max(ys));
    	
    	double sUnion = xSide * ySide;
    	double s1 = Math.abs(rect1.getWidth() - rect1.getX()) * Math.abs(rect1.getHeight() - rect1.getY());
    	double s2 = Math.abs(rect2.getWidth() - rect2.getX()) * Math.abs(rect2.getHeight() - rect2.getY());
    	
    	double s12 = s1 + s2;
    	
    	/*
    	if (rect1.intersects(rect2)){
    		Rectangle2D dest = new Rectangle2D.Double();
    		Rectangle2D.intersect(rect1, rect2, dest);
    		double sU = Math.abs(dest.getWidth() - dest.getX()) * Math.abs(dest.getHeight() - dest.getY());
    		s12 = s12 - sU;
    	}
    	*/
    	double result = sUnion / s12; 	
    	return result;
    }
}
