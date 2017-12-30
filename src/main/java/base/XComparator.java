package base;

import java.util.Comparator;

public class XComparator implements Comparator<PageRectangle>{
	@Override
	public int compare(PageRectangle o1, PageRectangle o2) {
		Double d1 = Double.valueOf(o1.getRectangle().getX());
		Double d2 = Double.valueOf(o2.getRectangle().getX());
		return d1.compareTo(d2);
	}
}
