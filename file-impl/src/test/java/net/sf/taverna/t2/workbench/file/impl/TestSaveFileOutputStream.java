package net.sf.taverna.t2.workbench.file.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

import net.sf.taverna.t2.lang.ui.FileTools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;

public class TestSaveFileOutputStream {

	private File folder;

	@Test
	public void alternativeTemp() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");
		File tmpFile = File.createTempFile("other", "tmp");
		SafeFileOutputStream out = new SafeFileOutputStream(file, tmpFile);		
		assertFalse(file.exists());
		assertEquals(0, folder.listFiles().length);
		out.write("Hello there\n".getBytes("ASCII"));
		out.flush();
		assertEquals(0, folder.listFiles().length);
		assertEquals("Hello there\n", FileUtils.readFileToString(tmpFile, "ASCII"));		
		out.close();
		assertEquals(1, folder.listFiles().length);
		assertTrue(file.exists());		
	}

	@Test
	public void alternativeTempRollBack() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");
		File tmpFile = File.createTempFile("other", "tmp");
		SafeFileOutputStream out = new SafeFileOutputStream(file, tmpFile);		
		assertFalse(file.exists());
		assertEquals(0, folder.listFiles().length);
		out.write("Hello there\n".getBytes("ASCII"));
		out.flush();
		assertEquals(0, folder.listFiles().length);
		assertEquals("Hello there\n", FileUtils.readFileToString(tmpFile, "ASCII"));		
		out.rollback();
		assertFalse(tmpFile.exists());
		out.close();
		assertEquals(0, folder.listFiles().length);
	}

	@Test
	public void createAfter() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");

		assertEquals(0, folder.listFiles().length);

				
		SafeFileOutputStream out = new SafeFileOutputStream(file);		
		assertFalse(file.exists());
		assertEquals(1, folder.listFiles().length);
		out.write("Safe".getBytes("ASCII"));
		out.flush();

		FileUtils.writeStringToFile(file, "Other", "ASCII");
		assertEquals(2, folder.listFiles().length);
		assertTrue(file.exists());		

		out.close();
		assertEquals(1, folder.listFiles().length);
		assertTrue(file.exists());		
		assertEquals("Safe", FileUtils.readFileToString(file, "ASCII"));
	}

	
	@Test
	public void createAfterRollBack() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");

		assertEquals(0, folder.listFiles().length);

				
		SafeFileOutputStream out = new SafeFileOutputStream(file);		
		assertFalse(file.exists());
		assertEquals(1, folder.listFiles().length);
		out.write("Safe".getBytes("ASCII"));
		out.flush();

		FileUtils.writeStringToFile(file, "Other", "ASCII");
		assertEquals(2, folder.listFiles().length);
		assertTrue(file.exists());		

		out.rollback();
		assertEquals(1, folder.listFiles().length);
		assertTrue(file.exists());		
		assertEquals("Other", FileUtils.readFileToString(file, "ASCII"));
		out.close();
		
		assertEquals(1, folder.listFiles().length);
		assertEquals("Other", FileUtils.readFileToString(file, "ASCII"));
		
		
	}

	@Test
	public void deleteBeforeClose() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");
		FileUtils.writeStringToFile(file, "Initial", "ASCII");
		assertEquals(1, folder.listFiles().length);
		assertTrue(file.exists());
				
		FileOutputStream out = new SafeFileOutputStream(file);		
		assertTrue(file.exists());
		assertEquals(2, folder.listFiles().length);
		out.write("Different".getBytes("ASCII"));
		out.flush();
		
		assertEquals("Initial", FileUtils.readFileToString(file, "ASCII"));
		
		file.delete();
		assertEquals(1, folder.listFiles().length);
		out.close();
		assertFalse(file.exists());		
		assertEquals(0, folder.listFiles().length);

	}

	
	@Test
	public void deleteFolderBeforeClose() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");
		FileUtils.writeStringToFile(file, "Initial", "ASCII");
		assertEquals(1, folder.listFiles().length);
		assertTrue(file.exists());
				
		FileOutputStream out = new SafeFileOutputStream(file);		
		assertTrue(file.exists());
		assertEquals(2, folder.listFiles().length);
		out.write("Different".getBytes("ASCII"));
		out.flush();
		
		assertEquals("Initial", FileUtils.readFileToString(file, "ASCII"));

		FileUtils.cleanDirectory(folder);
		folder.delete();
		assertFalse(folder.exists());
		out.close();
		assertFalse(folder.exists());
	}
	
	@Before
	public void makeTempFolder() throws IOException {
		folder = File.createTempFile("taverna", "test");
		folder.delete();
		folder.mkdir();
	}
	
	@Test
	public void overwrite() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");

		// Give it some initial content
		FileUtils.writeStringToFile(file, "Initial", "ASCII");
		assertEquals(1, folder.listFiles().length);
		assertTrue(file.exists());
				
		SafeFileOutputStream out = new SafeFileOutputStream(file);		
		assertTrue(file.exists());
		assertEquals(2, folder.listFiles().length);
		out.write("Different".getBytes("ASCII"));
		out.flush();
		
		assertEquals("Initial", FileUtils.readFileToString(file, "ASCII"));
		
		out.close();
		assertEquals(1, folder.listFiles().length);
		assertTrue(file.exists());		
		assertEquals("Different", FileUtils.readFileToString(file, "ASCII"));
	}
	

	@Test
	public void rollback() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");
		SafeFileOutputStream out = new SafeFileOutputStream(file);		
		assertFalse(file.exists());
		assertEquals(1, folder.listFiles().length);
		out.write("Hello there\n".getBytes("ASCII"));		
		out.rollback();
		assertEquals(0, folder.listFiles().length);
		assertFalse(file.exists());		
		out.close();
		assertEquals(0, folder.listFiles().length);
		assertFalse(file.exists());		

	}
	
	@Test
	public void safeFileOutputStream() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");
		SafeFileOutputStream out = new SafeFileOutputStream(file);
		
		assertFalse(file.exists());
		assertEquals(1, folder.listFiles().length);
		out.write("Hello there\n".getBytes("ASCII"));

		out.close();
		assertEquals(1, folder.listFiles().length);
		assertTrue(file.exists());		
	}	

	@Test(expected=IOException.class)
	public void wrongPermissionsBeforeClose() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");
		SafeFileOutputStream out = new SafeFileOutputStream(file);
		
		assertFalse(file.exists());
		assertEquals(1, folder.listFiles().length);
		out.write("Hello there\n".getBytes("ASCII"));
		
		folder.setWritable(false);		
		out.close();
	}
	

	@Test(expected=IOException.class)
	public void wrongPermissionsOnCreate() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");
		folder.setWritable(false);		
		SafeFileOutputStream out = new SafeFileOutputStream(file);		
	}	

	@Test(expected=IOException.class)
	public void wrongPermissionsOnCreateWithTemp() throws Exception {
		assertEquals(0, folder.listFiles().length);
		File file = new File(folder, "file.txt");
		File tmpFile = File.createTempFile("other", "tmp");
		folder.setWritable(false);		
		SafeFileOutputStream out = new SafeFileOutputStream(file, tmpFile);		
	}


	
}
