package org.geogebra.common.gui.view.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoText;
import org.junit.Before;
import org.junit.Test;

public class TableValuesInputProcessorTest extends BaseUnitTest {

	private TableValuesInputProcessor processor;
	private GeoList list;

	@Before
	public void setUp() {
		TableValues view = new TableValuesView(getKernel());
		getKernel().attach(view);
		processor = new TableValuesInputProcessor(getConstruction());
		list = new GeoList(getConstruction());
	}

	@Test
	public void testValidInput() {
		processor.processInput("1", list, 0);
		processor.processInput("2", list, 1);
		processor.processInput("99.9", list, 2);
		processor.processInput("0.01", list, 3);
		processor.processInput("", list, 4);
		processor.processInput("20e-2", list, 6);
		processor.processInput("10", list, 10);
		assertThat(list.size(), is(11));
		assertValue("1", 0);
		assertValue("2", 1);
		assertValue("99.9", 2);
		assertValue("0.01", 3);
		assertEmptyInput(4);
		assertEmptyInput(5);
		assertValue("0.2", 6);
		assertValue("10", 10);
	}

	private void assertValue(String value, int index) {
		GeoElement element = list.get(index);
		assertTrue(element instanceof GeoNumeric);
		assertThat(element.toString(StringTemplate.defaultTemplate), is(value));
	}

	private void assertEmptyInput(int index) {
		GeoElement element = list.get(index);
		assertTrue(element instanceof GeoText);
		assertThat(((GeoText) element).getTextString(), is(""));
	}

	@Test
	public void testInvalidInputWithComma() {
		processor.processInput("10,2", list, 0);
		assertEmptyInput("10,2");
	}

	private void assertEmptyInput(String input) {
		GeoElement element = list.get(0);
		assertTrue(element instanceof GeoText);
		assertEquals(input, ((GeoText) element).getTextString());

	}

	@Test
	public void testInvalidInputWithOperators() {
		processor.processInput("10 + 2", list, 0);
		assertEmptyInput("10 + 2");
	}

	@Test
	public void testInvalidInputWithLetters() {
		processor.processInput("a", list, 0);
		assertEmptyInput("a");
	}

	@Test
	public void testClearValuesFromColumn() throws InvalidInputException {
		processor.processInput("0", null, 0);
		GeoList column = (GeoList) view.getEvaluatable(1);
		processor.processInput("1", column, 1);
		processor.processInput("2", column, 2);
		assertEquals(3, model.getRowCount());
		assertEquals(2, model.getColumnCount());

		processor.processInput("", column, 0);
		// emptying any row above the last row shouldn't reduce the row count
		assertEquals(3, model.getRowCount());
		assertEquals(2, model.getColumnCount());

		processor.processInput("", column, 2);
		// emptying last row should reduce the row count
		assertEquals(2, model.getRowCount());
		assertEquals(2, model.getColumnCount());

		processor.processInput("", column, 1);
		// emptying last row should remove all the empty rows on the bottom fo the table
		assertEquals(0, model.getRowCount());
		assertEquals(1, model.getColumnCount());
	}

	@Test
	public void testClearRowsAndColumns() throws InvalidInputException {
		processor.processInput("1", null, 0);
		processor.processInput("2", null, 1);
		processor.processInput("3", null, 2);
		assertEquals(4, model.getColumnCount());
		assertEquals(3, model.getRowCount());

		processor.processInput("", (GeoList) view.getEvaluatable(3), 2);
		assertEquals(3, model.getColumnCount());
		assertEquals(2, model.getRowCount());

		processor.processInput("", (GeoList) view.getEvaluatable(1), 0);
		assertEquals(2, model.getColumnCount());
		assertEquals(2, model.getRowCount());

		processor.processInput("", (GeoList) view.getEvaluatable(1), 1);
		assertEquals(1, model.getColumnCount());
		assertEquals(0, model.getRowCount());
	}

	@Test
	public void testClearLastRow() throws InvalidInputException {
		processor.processInput("1", null, 0);
		GeoList c1 = (GeoList) view.getEvaluatable(1);
		processor.processInput("1", c1, 1);

		processor.processInput("2", null, 0);
		GeoList c2 = (GeoList) view.getEvaluatable(2);
		processor.processInput("2", c2, 1);

		processor.processInput("3", null, 0);

		assertEquals(2, model.getRowCount());
		assertEquals(4, model.getColumnCount());

		processor.processInput("", c1, 1);
		assertEquals(2, model.getRowCount());
		assertEquals(4, model.getColumnCount());

		processor.processInput("", c2, 1);
		assertEquals(1, model.getRowCount());
		assertEquals(4, model.getColumnCount());
	}
}
