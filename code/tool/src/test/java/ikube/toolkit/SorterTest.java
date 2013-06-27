package ikube.toolkit;

import ikube.AbstractTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

public class SorterTest extends AbstractTest {

	class Sortable {

		String workCenter;
		String serviceCode;
		String organization;

		Sortable(String workCenter, String serviceCode, String organization) {
			this.workCenter = workCenter;
			this.serviceCode = serviceCode;
			this.organization = organization;
		}
	}

	@Test
	public void sort() {
		Sortable one = new Sortable("a", "a", "a");
		Sortable two = new Sortable("a", "b", "a");
		Sortable three = new Sortable("a", "c", "b");
		Sortable four = new Sortable("b", "a", "a");
		Sortable five = new Sortable("b", "b", "a");
		Sortable six = new Sortable("b", "a", "b");
		Sortable seven = new Sortable("a", "a", "a");

		Comparator<Sortable> comparator = new Comparator<SorterTest.Sortable>() {
			@Override
			public int compare(Sortable o1, Sortable o2) {
				int result = o1.workCenter.compareTo(o2.workCenter);
				if (result == 0) {
					result = o1.serviceCode.compareTo(o2.serviceCode);
					if (result == 0) {
						result = o1.organization.compareTo(o2.organization);
					}
				}
				return result;
			}
		};

		List<Sortable> list = Arrays.asList(seven, six, five, four, three, two, one);
		Collections.sort(list, comparator);
		for (final Sortable sortable : list) {
			logger.info("Sortable : " + sortable.workCenter + ", " + sortable.serviceCode + ", " + sortable.organization);
		}
	}

}
