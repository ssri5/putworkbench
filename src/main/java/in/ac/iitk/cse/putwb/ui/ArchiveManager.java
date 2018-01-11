package in.ac.iitk.cse.putwb.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

/**
 * A utility class for creating and reading archive files 
 * @author Saurabh Srivastava
 *
 */
public abstract class ArchiveManager {
	/**
	 * Extracts contents of a compressed(zip) file to a given (existing) directory
	 * @param extractionDir The directory to extract
	 * @param zipFile The compressed file
	 * @return <code>true</cod> if the extraction succeeded, <code>false</code> otherwise
	 */
	public static boolean extractCompressedFileToDirectory(File extractionDir, File zipFile ) {
		try {
			ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry entry = zin.getNextEntry();
			byte[] buffer = new byte[10240];	// Read 10 KB at a time
			while(entry != null) {
				String fileName = entry.getName();
				File newFile = new File(extractionDir, fileName);
				FileOutputStream fout = new FileOutputStream(newFile);
				
				int len;
				while((len = zin.read(buffer)) > 0) {
					fout.write(buffer, 0, len);
				}
				fout.close();
				entry = zin.getNextEntry();
			}
			zin.closeEntry();	// Close the last entry
			zin.close();
			return true;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Problem in reading file " + zipFile.getName(), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	/**
	 * Saves a set of given files as a compressed(zip) file
	 * @param zipFile The (existing) compressed file
	 * @param files The list of files to add to this compressed file
	 * @return <code>true</cod> if the extraction succeeded, <code>false</code> otherwise
	 */
	public static boolean saveAsCompressedFile(File zipFile, File...files) {
		return saveAsCompressedFile(zipFile, files, new String[files.length]);
	}
	
	/**
	 * Saves a set of given files as a compressed(zip) file	
	 * @param zipFile The (existing) compressed file
	 * @param files The list of files to add to this compressed file
	 * @param entryNames The respective names of the file entries in the compressed file; a <code>null</code> value implies "use file names as entry names"
	 * @return <code>true</cod> if the extraction succeeded, <code>false</code> otherwise
	 * @throws IllegalArgumentException if the size of <code>files</code> and <code>entryNames</code> are different
	 */
	public static boolean saveAsCompressedFile(File zipFile, File[] files, String[] entryNames) throws IllegalArgumentException {
		if(files == null)
			throw new NullPointerException("The file list is null");
		else if(files != null && entryNames == null)
			throw new NullPointerException("The array containing entry names is null");
		else if(files.length != entryNames.length)
			throw new IllegalArgumentException("The list of entry names must be of the same size as the number of files");
		try {
			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
			if(files.length > 0) {
				byte[] buffer = new byte[10240];	// Read 10 KB at a time
				for(int i = 0; i < files.length; i++) {
					File file = files[i];
					if(file.exists()) {
						ZipEntry entry = new ZipEntry(entryNames[i] == null ? file.getName() : entryNames[i]);
						zout.putNextEntry(entry);
						FileInputStream in = new FileInputStream(file);
						int len;
						while ((len = in.read(buffer)) > 0) {
							zout.write(buffer, 0, len);
						}
						in.close();
					}
				}
				zout.closeEntry();	// Close the last entry
				zout.close();
			}
			return true;
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Problem in creating temporary file. Please check if you have sufficient disk space.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Something went wrong while saving the file:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
}
