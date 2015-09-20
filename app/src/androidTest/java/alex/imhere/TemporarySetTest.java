package alex.imhere;

import android.util.Pair;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import static junit.framework.Assert.*;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import alex.imhere.container.TemporarySet;

@RunWith(RobolectricTestRunner.class)
public class TemporarySetTest {
	List<Pair<Integer, DateTime>> elementsReadonly;
	final int elementsCount = 10;

	@Before
	public void setUp() {
		List<Pair<Integer, DateTime>> array = new ArrayList<>();
		DateTime date = new DateTime();
		for (int i = 0; i < elementsCount; ++i) {
			array.add(Pair.create(i, date.plusSeconds(i)));
		}

		this.elementsReadonly = Collections.unmodifiableList(array);
	}

	@After
	public void tearDown() {

	}

	public void addToSetElementByIndex(TemporarySet<Integer> set, int index) {
		set.add(elementsReadonly.get(index).first, elementsReadonly.get(index).second);
	}

	@Test
	public void test_isSortedByDates() throws Exception {
		TemporarySet<Integer> set = new TemporarySet<>(); // TODO: 20.09.2015 stub inner Timer
		addToSetElementByIndex(set, 4);
		addToSetElementByIndex(set, 0);
		addToSetElementByIndex(set, 7);

		assertEquals(set.getNextElementToDie().intValue(), 0);
		set.killNextElementToDie();

		assertEquals(set.getNextElementToDie().intValue(), 4);
		set.killNextElementToDie();

		assertEquals(set.getNextElementToDie().intValue(), 7);
		set.killNextElementToDie();

		throw new Exception("well, failed");
	}

	@Test
	public void test_removeNotExistedElements() throws Exception {
		TemporarySet<Integer> set = new TemporarySet<>();
		addToSetElementByIndex(set, 4);
		addToSetElementByIndex(set, 6);
		addToSetElementByIndex(set, 7);

		assertFalse(set.remove(0));
		assertFalse(set.remove(9));
	}

	@Test
	public void test_removeInEmpty() throws Exception {
		TemporarySet<Integer> set = new TemporarySet<>();

		assertTrue(set.size() == 0);
		assertFalse(set.killNextElementToDie());
		assertFalse(set.remove(7));
	}

	@Test
	public void test_add() throws Exception {
		TemporarySet<Integer> set = new TemporarySet<>();

		addToSetElementByIndex(set, 7);
		assertEquals(set.getNextElementToDie().intValue(), 7);

		addToSetElementByIndex(set, 3);
		assertEquals(set.getNextElementToDie().intValue(), 3);

		addToSetElementByIndex(set, 1);
		assertEquals(set.getNextElementToDie().intValue(), 1);
		assertEquals(set.size(), 3);
	}

	@Test
	public void test_remove() throws Exception {
		TemporarySet<Integer> set = new TemporarySet<>();

		addToSetElementByIndex(set, 3);
		addToSetElementByIndex(set, 7);
		addToSetElementByIndex(set, 9);

		assertEquals(set.getNextElementToDie().intValue(), 3);
		set.killNextElementToDie();
		assertEquals(set.size(), 2);

		assertEquals(set.getNextElementToDie().intValue(), 7);
		set.killNextElementToDie();
		assertEquals(set.size(), 1);

		assertEquals(set.getNextElementToDie().intValue(), 9);
		set.killNextElementToDie();
		assertEquals(set.size(), 0);
	}
}
