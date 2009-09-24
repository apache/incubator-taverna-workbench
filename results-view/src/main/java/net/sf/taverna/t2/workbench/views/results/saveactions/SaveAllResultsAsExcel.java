/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.views.results.saveactions;

import java.awt.event.ActionEvent;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.views.results.ResultsUtils;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.baclava.iterator.BaclavaIterator;

/**
 * Stores the entire map of result objects to disk
 * as a single XML data document.
 * 
 * @author Tom Oinn
 */
public class SaveAllResultsAsExcel extends AbstractAction implements SaveAllResultsSPI {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2759817859804112070L;

	private static Logger logger = Logger.getLogger(SaveAllResultsAsExcel.class);

	private Map<String, T2Reference> resultReferencesMap = null;

	private InvocationContext context = null;
	
    HSSFWorkbook wb = null;
    HSSFSheet sheet = null;
    HSSFCellStyle headingStyle = null;
    HSSFCellStyle[] styles = null;
	
	public SaveAllResultsAsExcel(){
		super();
		putValue(NAME, "Save as Excel");
		putValue(SMALL_ICON, WorkbenchIcons.xmlNodeIcon);
	}
	
	public AbstractAction getAction() {
		return new SaveAllResultsAsExcel();
	}
	
	// Must be called before actionPerformed()
	public void setInvocationContext(InvocationContext context) {
		this.context = context;
	}
	
	// Must be called before actionPerformed()
	public void setResultReferencesMap(Map<String, T2Reference> resultReferencesMap) {
		this.resultReferencesMap = resultReferencesMap;
	}

	/**
     * Shows a standard save dialog and dumps the entire result
     * set to the specified XML file.
     */
	public void actionPerformed(ActionEvent e) {
		
		JFileChooser fc = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[]{"xls"}));
		fc.setCurrentDirectory(new File(curDir));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			int returnVal = fc.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				prefs.put("currentDir", fc.getCurrentDirectory().toString());
				File file = fc.getSelectedFile();

				// If the user did not use the .xml extension for the file - append it to the file name now
				if (!file.getName().toLowerCase().endsWith(".xls")) {
					String newFileName = file.getName() + ".xls";
					file = new File(file.getParentFile(), newFileName);
				}

				final File finalFile = file;
				
				if (file.exists()){ // File already exists
					// Ask the user if they want to overwrite the file
					String msg = file.getAbsolutePath() + " already exists. Do you want to overwrite it?";
					int ret = JOptionPane.showConfirmDialog(
							null, msg, "File already exists",
							JOptionPane.YES_NO_OPTION);
					
					if (ret == JOptionPane.YES_OPTION) {
						// Do this in separate thread to avoid hanging UI
						new Thread("SaveAllResultsAsExcel: Saving results to " + finalFile){
							public void run(){
								try {
									synchronized(resultReferencesMap){
										saveData(finalFile);
									}
								} catch (Exception ex) {
									JOptionPane.showMessageDialog(null, "Problem saving result data", "Save Result Error",
											JOptionPane.ERROR_MESSAGE);
									logger.error("SaveAllResultsAsXML Error: Problem saving result data", ex);
								}	
							}
						}.start();
					}
					else{
						tryAgain = true;
					}
				}
				else{ // File does not already exist
					
					// Do this in separate thread to avoid hanging UI
					new Thread("SaveAllResultsAsExcel: Saving results to " + finalFile){
						public void run(){
							try {
								synchronized(resultReferencesMap){
									saveData(finalFile);
								}
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, "Problem saving result data", "Save Result Error",
										JOptionPane.ERROR_MESSAGE);
								logger.error("SaveAllResultsAsXML Error: Problem saving result data", ex);
							}
						}
					}.start();
				}
			}
		}  
	}		
	
	
	/**
	 * Converts a T2References pointing to results to 
	 * a list of (lists of ...) dereferenced result objects.
	 */
	Object convertReferencesToObjects(T2Reference reference) throws Exception{				

			if (reference.getReferenceType() == T2ReferenceType.ReferenceSet){
				// Dereference the object
				Object dataValue;
				try{
					dataValue = context.getReferenceService().renderIdentifier(reference, Object.class, context);
				}
				catch(ReferenceServiceException rse){
					String message = "Problem rendering T2Reference in convertReferencesToObjects().";
					logger.error("SaveAllResultsAsXML Error: "+ message, rse);
					throw new Exception(message);
				}
				return dataValue;
			}
			else if (reference.getReferenceType() == T2ReferenceType.ErrorDocument){
				// Dereference the ErrorDocument and convert it to some string representation
				ErrorDocument errorDocument = (ErrorDocument)context.getReferenceService().resolveIdentifier(reference, null, context);
				String errorString = ResultsUtils.buildErrorDocumentString(errorDocument, context);
				return errorString;
			}
			else { // it is an IdentifiedList<T2Reference> - go recursively
				IdentifiedList<T2Reference> identifiedList = context
				.getReferenceService().getListService().getList(reference);
				List<Object> list = new ArrayList<Object>();
				
				for (int j=0; j<identifiedList.size(); j++){
					T2Reference ref = identifiedList.get(j);
					list.add(convertReferencesToObjects(ref));
				}
				return list;
			}	
	}
	

		/**
	 * Returns a map of port names to DataThings from a map of port names to a 
	 * list of (lists of ...) result objects.
	 */
	Map<String, DataThing> bakeDataThingMap(Map<String, Object> resultMap){
		
		Map<String, DataThing> dataThingMap = new HashMap<String, DataThing>();
		for (Iterator<String> i = resultMap.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
			dataThingMap.put(portName, DataThingFactory.bake(resultMap.get(portName)));
		}
		return dataThingMap;
	}
	
	private void saveData(File f) throws Exception {
	    generateSheet();
        saveSheet(f);		
	}
	
    /**
     * Generate the Excel sheet from the DataThing's in the map.
     * 
     * All of the results are shown in the same spreadsheet, but in 
     * different columns. Flat lists are shown vertically, 2d lists
     * as a matrix, and deeper lists are flattened to 2d. 
     * @throws Exception 
     */
    void generateSheet() throws Exception {
            wb = new HSSFWorkbook();
            setStyles();
            sheet = wb.createSheet("Workflow results");
            sheet.setDisplayGridlines(false);
            int currentCol = 0;
            
    		// Build the DataThing map from the resultReferencesMap
    		// First convert map of references to objects into a map of real result objects
    		Map<String, Object> resultMap = new HashMap<String, Object>();
    		for (Iterator<String> i = resultReferencesMap.keySet().iterator(); i.hasNext();) {
    			String portName = (String) i.next();
    			T2Reference reference = resultReferencesMap.get(portName);
    			Object obj = convertReferencesToObjects(reference);
    			resultMap.put(portName, obj);
    		}
    		Map<String, DataThing> map = bakeDataThingMap(resultMap);

            for (String resultName : map.keySet()) {
                    logger.debug("Output for : " + resultName);
                    DataThing resultValue = map.get(resultName);
                    // Check whether there's a textual type
                    Boolean textualType = isTextual(resultValue.getDataObject());
                    if (textualType == null || !textualType) { 
                            continue;
                    }
                    logger.debug("Output is textual");
                    getCell(currentCol, 0).setCellValue(resultName);
                    getCell(currentCol, 0).setCellStyle(headingStyle);
                    int numCols = 1;
                    int numRows = 1;
                    int currentRow = 0;
                    BaclavaIterator rows;
                    try {
                            rows = resultValue.iterator("l('')");
                    } catch (IntrospectionException ex) {
                            // Not a list, single value. We'll fake the iterator
                            DataThing fakeValues = new DataThing(Arrays.asList(resultValue.getDataObject()));
                            rows = fakeValues.iterator("l('')");
                    }
                    // If we only have one row, we'll show each value on a new
                    // row instead
                    boolean isFlat = rows.size() == 1;
                    while (rows.hasNext()) {
                            DataThing row = (DataThing) rows.next();
                            // Even increase first time, as we don't want to overwrite our header
                            currentRow++;
                            BaclavaIterator bi = row.iterator("''");
                            while (bi.hasNext()) {
                                    DataThing containedThing = (DataThing) bi.next();
                                    String containedValue = (String) containedThing.getDataObject();
                                    int columnOffset = 0;
                                    int[] location = bi.getCurrentLocation();
                                    if (!isFlat && location.length > 0) {
                                            columnOffset = location[location.length-1];
                                            numCols = Math.max(numCols, columnOffset + 1);
                                    }
                                    logger.debug("Storing in cell " + (currentCol+columnOffset) + 
                                                           " " + currentRow + ": " + containedValue);
                                    getCell(currentCol + columnOffset, currentRow).setCellValue(containedValue);
                                    if (isFlat) {
                                            currentRow++;
                                    }
                            }
                    }
                    numRows = Math.max(numRows, currentRow);

                    // Set the styles
                    for (int x = currentCol; x < currentCol + numCols; x++) {
                            for (int y = 1; y < numRows + 1; y++) {
                                    setStyle(currentCol, x, y);
                            }
                    }
                    sheet.setColumnWidth((short) (currentCol + numCols), (short) 200);
                    currentCol += numCols + 1;
            }
    }
    
    void setStyle(int currentCol, int column, int row) {
        if (!hasValue(column, row)) {
                return;
        }
        HSSFCell cell = getCell(column, row);
        int n=0, s=0, w=0, e=0;
        if (row < 2 || !hasValue(column, row - 1)) {
                n = 1;
        }
        if (column == currentCol || !hasValue(column - 1, row)) {
                w = 1;
        }
        if (!hasValue(column, row + 1)) {
                s = 1;
        }
        if (!hasValue(column + 1, row)) {
                e = 1;
        }
        int index = n + 2 * s + 4 * e + 8 * w;
        cell.setCellStyle(styles[index]);
}

    void setStyles() {
        headingStyle = wb.createCellStyle();
        headingStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        headingStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        headingStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        headingStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        headingStyle.setFillBackgroundColor(HSSFColor.LIGHT_YELLOW.index);
        headingStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
        headingStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        styles = new HSSFCellStyle[16];
        for (int n = 0; n < 2; n++) {
                for (int s = 0; s < 2; s++) {
                        for (int e = 0; e < 2; e++) {
                                for (int w = 0; w < 2; w++) {
                                        int index = n + 2*s + 4*e + 8*w;
                                        styles[index] = wb.createCellStyle();
                                        if (n == 1) {
                                                styles[index].setBorderTop(HSSFCellStyle.BORDER_THIN);
                                        } else {
                                                styles[index].setBorderTop(HSSFCellStyle.BORDER_NONE);
                                        }
                                        if (s == 1) {
                                                styles[index].setBorderBottom(HSSFCellStyle.BORDER_THIN);
                                        } else {
                                                styles[index].setBorderBottom(HSSFCellStyle.BORDER_NONE);
                                        }
                                        if (e == 1) {
                                                styles[index].setBorderRight(HSSFCellStyle.BORDER_THIN);
                                        } else {
                                                styles[index].setBorderRight(HSSFCellStyle.BORDER_NONE);
                                        }
                                        if (w == 1) {
                                                styles[index].setBorderLeft(HSSFCellStyle.BORDER_THIN);
                                        } else {
                                                styles[index].setBorderLeft(HSSFCellStyle.BORDER_NONE);
                                        }
                                        styles[index].setFillBackgroundColor(HSSFColor.GOLD.index);
                                        styles[index].setFillForegroundColor(HSSFColor.GOLD.index);
                                        styles[index].setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                                }
                        }
                }
        }
}

    /**
     * Check if o is a String or contains elements that satisfy isTextual(o)
     * <p>
     * Traverse down the Collection o if possible, and check the tree of collection at the deepest level.
     * </p>
     * 
     * @param o Object to check
     * @return true if o is a String or is a Collection that contains a string at the deepest level. 
     * false if o is not a String or Collection, or if it is a collection that contains non-strings.
     * null if o is a Collection, but it is empty or contains nothing but Collections.    
     * 
     */
    Boolean isTextual(Object o) {
            if (o instanceof String) {
                    // We dug down and found a string. Hurray!
                    return true;
            } 
            if (o instanceof Collection) {
                    for (Object child : (Collection) o) {
                            Boolean isTxt = isTextual(child);
                            if (isTxt == null) {
                                    // Unknown, try next one
                                    continue;
                            }
                            return isTxt;
                    }
                    // We looped through and found just empty collections 
                    // (or we are an empty collection), we don't know
                    return null;
            }
            // No, sorry mate.. o was neither a String or Collection
            return false;
    }

    /**
     * Get a cell at the given coordinates, create it if needed.
     * 
     * @param column
     * @param row
     * @return
     */
    HSSFCell getCell(int column, int row) {
            HSSFRow srow = sheet.getRow((short) row);
            if (srow == null) {
                    srow = sheet.createRow((short) row);
            }
            HSSFCell scell = srow.getCell((short) column);
            if (scell == null) {
                    scell = srow.createCell((short) column);
            }
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
            HSSFRow srow = sheet.getRow((short) row);
            if (srow == null) {
                    return false;
            }
            HSSFCell scell = srow.getCell((short) column);
            if (scell == null) {
                    return false;
            }
            return true;
    }

    /**
     * Save the generated worksheet to a file 
     * 
     * @param file to save to
     * @throws FileNotFoundException
     * @throws IOException
     */
    void saveSheet(File file) throws FileNotFoundException, IOException {
            FileOutputStream fos = new FileOutputStream(file);
            wb.write(fos);
            fos.close();
    }

}

