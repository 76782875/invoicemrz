package base;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PDFPageContent implements Iterator<PageRectangle> {
	private List<PageRectangle> wordList = new LinkedList<PageRectangle>();
	int pos = 0, n;
	
	public PDFPageContent (List<PageRectangle> input, int n){
		this.wordList.addAll(input);
		this.n = n;
	}
	
	public List<PageRectangle> getAll(){
		return wordList;
	}
	
	public List<PageRectangle> getByWord(String keyword){
		List<PageRectangle> matches = new LinkedList<PageRectangle>();
		for (PageRectangle word : wordList){
			if (word.getText().equals(keyword)){
				matches.add(word);
			}
		}
		return matches;
	}
	
	public List<PageRectangle> getAllBeforeX(PageRectangle x){
		List<PageRectangle> matches = new LinkedList<PageRectangle>();
		for (PageRectangle word : wordList){
			if (word.equals(x)){
				continue;
			}
			if (word.getRectangle().getWidth()  <  x.getRectangle().getX()){
				matches.add(word);
			}
		}
		return matches;
	}
	
	public List<PageRectangle> getAllAfterX(PageRectangle x){
		List<PageRectangle> matches = new LinkedList<PageRectangle>();
		for (PageRectangle word : wordList){
			if (word.equals(x)){
				continue;
			}
			if (word.getRectangle().getX()  >  x.getRectangle().getWidth()){
				matches.add(word);
			}
		}
		return matches;
	}
	
	public List<PageRectangle> getAllBelowY(PageRectangle y){
		List<PageRectangle> matches = new LinkedList<PageRectangle>();
		for (PageRectangle word : wordList){
			if (word.equals(y)){
				continue;
			}
			if (word.getRectangle().getY()  >  y.getRectangle().getHeight()){
				matches.add(word);
			}
		}
		return matches;
	}
	
	public List<PageRectangle> getAllAboveY(PageRectangle y){
		List<PageRectangle> matches = new LinkedList<PageRectangle>();
		for (PageRectangle word : wordList){
			if (word.equals(y)){
				continue;
			}
			if (word.getRectangle().getHeight()  <  y.getRectangle().getY()){
				matches.add(word);
			}
		}
		return matches;
	}
	
	public double getIntersection(double a1, double a2, double b1, double b2){
		
		List<Double> dots = new ArrayList<Double>();
		dots.add(a1);
		dots.add(a2);
		dots.add(b1);
		dots.add(b2);
		
		Collections.sort(dots);
		
		if (Math.abs(dots.get(0) - dots.get(3)) > Math.abs(a1 - a2) + Math.abs(b1 - b2))
			return 0;
		
		return Math.abs(dots.get(1) - dots.get(2));
	}
	
	public PageRectangle getLeft(PageRectangle rectangle){
		List<PageRectangle> rects = getAllBeforeX(rectangle);
		List<PageRectangle> matches = new LinkedList<PageRectangle>();		

		if (rectangle == null){
			return null;
		}
		
		if (rects == null || rects.isEmpty()){
			return null;
		}
		
		for (PageRectangle word : rects){
			double intersection = getIntersection(word.getRectangle().getY(), word.getRectangle().getHeight(), rectangle.getRectangle().getY(), rectangle.getRectangle().getHeight());
			if (intersection > 0.0){
				matches.add(word);
			}
		}

		if (matches.isEmpty()){
			return null;
		}
		
		Collections.sort(matches, new XComparator());
		return matches.get(matches.size() - 1);
	}
	
	public PageRectangle getRight(PageRectangle rectangle){
		List<PageRectangle> rects = getAllAfterX(rectangle);
		List<PageRectangle> matches = new LinkedList<PageRectangle>();		

		if (rectangle == null){
			return null;
		}
		
		if (rects == null || rects.isEmpty()){
			return null;
		}
		
		for (PageRectangle word : rects){
			double intersection = getIntersection(word.getRectangle().getY(), word.getRectangle().getHeight(), rectangle.getRectangle().getY(), rectangle.getRectangle().getHeight());
			if (intersection > 0.0){
				matches.add(word);
			}
		}

		if (matches.isEmpty()){
			return null;
		}
		
		Collections.sort(matches, new XComparator());
		return matches.get(0);
	}
	
	public PageRectangle getTop(PageRectangle rectangle){
		List<PageRectangle> rects = getAllAboveY(rectangle);
		List<PageRectangle> matches = new LinkedList<PageRectangle>();
		
		if (rectangle == null){
			return null;
		}
		
		if (rects == null || rects.isEmpty()){
			return null;
		}
		
		for (PageRectangle word : rects){
			double intersection = getIntersection(word.getRectangle().getX(), word.getRectangle().getWidth(), rectangle.getRectangle().getX(), rectangle.getRectangle().getWidth());
			if (intersection > 0.0){
				matches.add(word);
			}
		}
		
		if (matches.isEmpty()){
			return null;
		}
		
		Collections.sort(matches, new YComparator());
		return matches.get(matches.size() - 1);
	}
	
	public PageRectangle getBottom(PageRectangle rectangle){
		List<PageRectangle> rects = getAllBelowY(rectangle);
		List<PageRectangle> matches = new LinkedList<PageRectangle>();
		
		if (rectangle == null){
			return null;
		}
		
		if (rects == null || rects.isEmpty()){
			return null;
		}
		
		for (PageRectangle word : rects){
			double intersection = getIntersection(word.getRectangle().getX(), word.getRectangle().getWidth(), rectangle.getRectangle().getX(), rectangle.getRectangle().getWidth());
			if (intersection > 0.0){
				matches.add(word);
			}
		}
		
		if (matches.isEmpty()){
			return null;
		}
		
		Collections.sort(matches, new YComparator());
		return matches.get(0);
	}
	
	public PageRectangle getLeftBottom(PageRectangle rectangle){
		
		if (rectangle == null){
			return null;
		}
		
		List<PageRectangle> rects = getAllBelowY(rectangle);
		List<PageRectangle> left_rects = getAllBeforeX(rectangle);
		
		rects.retainAll(left_rects);
		
		if (rects == null || rects.isEmpty()){
			return null;
		}
		
		PageRectangle closest = null;
		double dist = Double.MAX_VALUE;
		for (PageRectangle rect : rects){
			double distanceFromBottomLeft = Math.sqrt(Math.pow(rectangle.getRectangle().getX() - rect.getRectangle().getWidth(), 2) + Math.pow(rectangle.getRectangle().getY() - rect.getRectangle().getY(), 2));
			if (distanceFromBottomLeft < dist){
				closest = rect;
				dist = distanceFromBottomLeft;
			}
		} 
		
		//Collections.sort(rects, new YComparator());
		//return rects.get(0);
		return closest;
	}
	
	public PageRectangle getRightBottom(PageRectangle rectangle){
		
		if (rectangle == null){
			return null;
		}
		
		List<PageRectangle> rects = getAllBelowY(rectangle);
		List<PageRectangle> right_rects = getAllAfterX(rectangle);
		
		rects.retainAll(right_rects);
		
		if (rects == null || rects.isEmpty()){
			return null;
		}
		
		PageRectangle closest = null;
		double dist = Double.MAX_VALUE;
		for (PageRectangle rect : rects){
			double distanceFromBottomRight = Math.sqrt(Math.pow(rectangle.getRectangle().getWidth() - rect.getRectangle().getX(), 2) + Math.pow(rectangle.getRectangle().getHeight() - rect.getRectangle().getY(), 2));
			if (distanceFromBottomRight < dist){
				closest = rect;
				dist = distanceFromBottomRight;
			}
		} 
		
		//Collections.sort(rects, new YComparator());
		//return rects.get(0);
		return closest;
	}
	
	public PageRectangle getLeftTop(PageRectangle rectangle){
		
		if (rectangle == null){
			return null;
		}
		
		List<PageRectangle> rects = getAllAboveY(rectangle);
		List<PageRectangle> left_rects = getAllBeforeX(rectangle);
		
		rects.retainAll(left_rects);
		
		if (rects == null || rects.isEmpty()){
			return null;
		}
		
		PageRectangle closest = null;
		double dist = Double.MAX_VALUE;
		for (PageRectangle rect : rects){
			double distanceFromTopLeft = Math.sqrt(Math.pow(rectangle.getRectangle().getX() - rect.getRectangle().getWidth(), 2) + Math.pow(rectangle.getRectangle().getY() - rect.getRectangle().getHeight(), 2));
			if (distanceFromTopLeft < dist){
				closest = rect;
				dist = distanceFromTopLeft;
			}
		} 
		
		//Collections.sort(rects, new YComparator());
		//return rects.get(rects.size() - 1);
		return closest;
	}
	
	public PageRectangle getRightTop(PageRectangle rectangle){
		
		if (rectangle == null){
			return null;
		}
		
		List<PageRectangle> rects = getAllAboveY(rectangle);
		List<PageRectangle> right_rects = getAllAfterX(rectangle);
		
		rects.retainAll(right_rects);
		
		if (rects == null || rects.isEmpty()){
			return null;
		}
		
		PageRectangle closest = null;
		double dist = Double.MAX_VALUE;
		for (PageRectangle rect : rects){
			double distanceFromTopRight = Math.sqrt(Math.pow(rectangle.getRectangle().getWidth() - rect.getRectangle().getX(), 2) + Math.pow(rectangle.getRectangle().getY() - rect.getRectangle().getHeight(), 2));
			
			if (distanceFromTopRight < dist){
				closest = rect;
				dist = distanceFromTopRight;
			}
		} 
		
		//Collections.sort(rects, new YComparator());
		//return rects.get(rects.size() - 1);
		
		return closest;
	}
	
	protected String roundVal(Double x) {
		DecimalFormat df = new DecimalFormat("#0.00");
		String dx=df.format(x);

	    return dx;
	}

	@Override
	public boolean hasNext() {
		return pos < wordList.size() - n + 1;
	}

	@Override
	public PageRectangle next() {
		List<PageRectangle> ngramItems = new LinkedList<PageRectangle>();
        for (int i = pos; i < pos + n; i++){
        	ngramItems.add(wordList.get(i));
        }
        pos++;
        
        List<Double> Ys = new ArrayList<Double>();
        for (PageRectangle ngramItem : ngramItems){
        	if (!Ys.contains(ngramItem.getRectangle().getY())){
        		Ys.add(ngramItem.getRectangle().getY());
        	}
        }
        
        PageRectangle rect = null;
        if (Ys.size() == 1){
        	rect = wordList.get(pos);
        	for (int i = pos + 1; i < pos + n; i++){
            	rect = combine(rect, wordList.get(i));
            }
        }
        return rect;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	private PageRectangle combine(PageRectangle a1, PageRectangle a2){
		PageRectangle result = new PageRectangle(a1.getRectangle().createUnion(a2.getRectangle()), (a1.getText().trim() + " " + a2.getText().trim()).trim());
		return result;
	}
}
