package base;

import java.util.Comparator;

public class YLineComparator implements Comparator<DocLine>{

	@Override
	public int compare(DocLine o1, DocLine o2) {
		Double d1 = Double.valueOf(o1.getBox().getY());
		Double d2 = Double.valueOf(o2.getBox().getY());
		return d1.compareTo(d2);
	}

}
