package org.apache.taverna.workbench.views.results.saveactions;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static java.lang.Math.max;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_NONE;
import static org.apache.poi.ss.usermodel.CellStyle.BORDER_THIN;
import static org.apache.poi.ss.usermodel.CellStyle.SOLID_FOREGROUND;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.saveIcon;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.swing.AbstractAction;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Stores the entire map of result objects to disk as a single XML data document.
 *
 * @author Tom Oinn
 */
public class SaveAllResultsAsExcel extends SaveAllResultsSPI {
	private static final long serialVersionUID = -2759817859804112070L;

	HSSFWorkbook wb = null;
	HSSFSheet sheet = null;
	HSSFCellStyle headingStyle = null;
	HSSFCellStyle[] styles = null;

	public SaveAllResultsAsExcel() {
		super();
		putValue(NAME, "Save as Excel");
		putValue(SMALL_ICON, saveIcon);
	}

	@Override
	public AbstractAction getAction() {
		return new SaveAllResultsAsExcel();
	}

	@Override
	protected void saveData(File f) throws IOException {
		try {
			generateSheet();
		} catch (IntrospectionException e) {
			throw new IOException("failed to generate excel sheet model", e);
		}
		saveSheet(f);
	}

	/**
	 * Generate the Excel sheet from the DataThing's in the map. All of the
	 * results are shown in the same spreadsheet, but in different columns. Flat
	 * lists are shown vertically, 2d lists as a matrix, and deeper lists are
	 * flattened to 2d.
	 * 
	 * @throws IntrospectionException
	 */
	void generateSheet() throws IntrospectionException {
		wb = new HSSFWorkbook();
		setStyles();
		sheet = wb.createSheet("Workflow results");
		sheet.setDisplayGridlines(false);
		int currentCol = 0;

		for (String portName : chosenReferences.keySet()) {
			logger.debug("Output for : " + portName);
			Object v = getObjectForName(portName);			
			if (! isTextual(v).orElse(false)) {
				logger.debug("Ignoring non-textual port " + portName);
				continue;
			}
			getCell(currentCol, 0).setCellValue(portName);
			getCell(currentCol, 0).setCellStyle(headingStyle);
			int numCols = 1;
			int numRows = 1;
			int currentRow = 0;
			
			Collection rows; 
			if (v instanceof Collection) { 
				rows = (Collection) v;
			} else {
				// Not a list, single value. Wrap it!
				rows = Arrays.asList(v);
			}
			/*
			 * If we only have one row, we'll show each value on a new row
			 * instead
			 */
			boolean isFlat = rows.size() == 1;
			for (Object row : rows) {
				/*
				 * Even increase first time, as we don't want to overwrite our
				 * header
				 */
				currentRow++;
				if (! (row instanceof Collection)) {
					// Wrap it for the iterator
					row = Arrays.asList(row);
				}
				
				int columnOffset = -1;
				for (Object containedValue : (Collection)row) {
					if (!isFlat) {
						columnOffset++;
						numCols = Math.max(numCols, columnOffset + 1);
					}
					logger.debug("Storing in cell " + (currentCol + columnOffset) + " "
							+ currentRow + ": " + containedValue);
					HSSFCell cell = getCell(currentCol + columnOffset, currentRow);
					if (containedValue instanceof String) {
						cell.setCellValue(containedValue.toString());						
					}
					if (isFlat)
						currentRow++;
				}
			}
			numRows = max(numRows, currentRow);

			// Set the styles
			for (int x = currentCol; x < currentCol + numCols; x++)
				for (int y = 1; y < numRows + 1; y++)
					setStyle(currentCol, x, y);
			sheet.setColumnWidth(currentCol + numCols, 200);
			currentCol += numCols + 1;
		}
	}

	void setStyle(int currentCol, int column, int row) {
		if (!hasValue(column, row))
			return;
		HSSFCell cell = getCell(column, row);
		int n = 0, s = 0, w = 0, e = 0;
		if (row < 2 || !hasValue(column, row - 1))
			n = 1;
		if (column == currentCol || !hasValue(column - 1, row))
			w = 1;
		if (!hasValue(column, row + 1))
			s = 1;
		if (!hasValue(column + 1, row))
			e = 1;
		int index = n + 2 * s + 4 * e + 8 * w;
		cell.setCellStyle(styles[index]);
	}

	void setStyles() {
		headingStyle = wb.createCellStyle();
		headingStyle.setBorderTop(BORDER_THIN);
		headingStyle.setBorderBottom(BORDER_THIN);
		headingStyle.setBorderLeft(BORDER_THIN);
		headingStyle.setBorderRight(BORDER_THIN);
		headingStyle.setFillBackgroundColor(HSSFColor.LIGHT_YELLOW.index);
		headingStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		headingStyle.setFillPattern(SOLID_FOREGROUND);
		styles = new HSSFCellStyle[16];
		for (int n = 0; n < 2; n++)
			for (int s = 0; s < 2; s++)
				for (int e = 0; e < 2; e++)
					for (int w = 0; w < 2; w++) {
						int index = n + 2 * s + 4 * e + 8 * w;
						styles[index] = wb.createCellStyle();
						styles[index].setBorderTop(n == 1 ? BORDER_THIN
								: BORDER_NONE);
						styles[index].setBorderBottom(s == 1 ? BORDER_THIN
								: BORDER_NONE);
						styles[index].setBorderRight(e == 1 ? BORDER_THIN
								: BORDER_NONE);
						styles[index].setBorderLeft(w == 1 ? BORDER_THIN
								: BORDER_NONE);
						styles[index].setFillBackgroundColor(HSSFColor.GOLD.index);
						styles[index].setFillForegroundColor(HSSFColor.GOLD.index);
						styles[index].setFillPattern(SOLID_FOREGROUND);
					}
	}

	/**
	 * Check if o is a String or contains elements that satisfy isTextual(o)
	 * <p>
	 * Traverse down the Collection o if possible, and check the tree of collection at the deepest
	 * level.
	 * </p>
	 *
	 * @param o
	 *            Object to check
	 * @return 	true if o is a String or is a Collection that contains a string at the deepest level.
	 *         false if o is not a String or Collection, or if it is a collection that contains
	 *         non-strings.
	 *         Optional.empty() if o is a Collection, but it is empty or contains nothing but Collections.
	 */
	Optional<Boolean> isTextual(Object o) {
		if (o instanceof String)
			// We dug down and found a string. Hurray!
			return Optional.of(true);
		if (o instanceof Collection) {
			for (Object child : (Collection<?>) o) {
				Optional<Boolean> isTxt = isTextual(child);
				if (isTxt.isPresent()) { 
					return isTxt;
				}
			}
			/*
			 * We looped through and found just empty collections (or we are an
			 * empty collection), we don't know.
			 */
			return Optional.empty();
		}
		// No, sorry mate.. o was neither a String or Collection
		return Optional.of(false);
	}

	/**
	 * Get a cell at the given coordinates, create it if needed.
	 *
	 * @param column
	 * @param row
	 * @return
	 */
	HSSFCell getCell(int column, int row) {
		HSSFRow srow = sheet.getRow(row);
		if (srow == null)
			srow = sheet.createRow(row);
		HSSFCell scell = srow.getCell(column);
		if (scell == null)
			scell = srow.createCell(column);
		return scell;
	}

	/**
	 * Check if a cell has a value.
	 *
	 * @param column
	 * @param row
	 * @return
	 */
	boolean hasValue(int column, int row) {
		HSSFRow srow = sheet.getRow(row);
		if (srow == null)
			return false;
		HSSFCell scell = srow.getCell(column);
		if (scell == null)
			return false;
		return true;
	}

	/**
	 * Save the generated worksheet to a file
	 *
	 * @param file
	 *            to save to
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	void saveSheet(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		wb.write(fos);
		fos.close();
	}

	@Override
	protected String getFilter() {
		return "xls";
	}
}
