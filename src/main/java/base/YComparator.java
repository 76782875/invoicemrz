package base;

import java.util.Comparator;

public class YComparator implements Comparator<PageRectangle>{

	@Override
	public int compare(PageRectangle o1, PageRectangle o2) {
		Double d1 = Double.valueOf(o1.getRectangle().getY());
		Double d2 = Double.valueOf(o2.getRectangle().getY());
		return d1.compareTo(d2);
	}

}
