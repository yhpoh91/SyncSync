package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
	private String directoryPath = "";
	private static FileManager fileManager = null;

	/**
	 * Constructor
	 */
	private FileManager() {
		// Empty Constructor
	}

	/**
	 * Get current working directory path
	 * 
	 * @return current working directory path
	 */
	public String getPath() {
		return this.directoryPath;
	}

	/**
	 * Set current working directory path
	 * 
	 * @param path
	 *            new current working directory path
	 */
	public void setPath(String path) {
		this.directoryPath = path;
	}

	/**
	 * Get existing instance (create new instance if not exist)
	 * 
	 * @return instance of file manager
	 */
	public static FileManager getInstance() {
		// Create if not exist
		if (fileManager == null) {
			fileManager = new FileManager();
		}

		return fileManager;
	}

	/**
	 * Gets list of file in current working directory
	 * 
	 * @return list of file names
	 */
	public List<String> getFileList() {
		return getFileList(null, null);
	}

	private List<String> getFileList(File file, List<String> fileList) {
		// Initialize file list
		if (fileList == null) {
			fileList = new ArrayList<>();
		}

		// Initialize file
		if (file == null) {
			file = new File(directoryPath);
		}

		// Read file list
		File[] files = file.listFiles();

		// Check for null file list
		if (files == null) {
			return fileList;
		}

		// Check and add to list
		for (File f : files) {
			if (f.isDirectory())
				// Look within folder
				getFileList(f, fileList);
			if (f.isFile()) {
				// Add relative path
				File rootSharedFile = new File(directoryPath);
				String path = f.getAbsolutePath().replace(rootSharedFile.getAbsolutePath(), "");
				path = path.substring(1);
				fileList.add(path);
			}
		}

		return fileList;
	}

	/**
	 * Get missing file list
	 * 
	 * @param targetFileList
	 *            list of file names to be compared against
	 * @return list of missing file names
	 */
	public List<String> getMissingFileList(List<String> targetFileList) {
		List<String> fileList = getFileList();
		List<String> missingFileList = new ArrayList<>();

		// Check for missing files
		for (String fileName : targetFileList) {
			boolean hasFile = fileList.contains(fileName);

			if (!hasFile) {
				// Missing
				missingFileList.add(fileName);
			}
		}

		return missingFileList;
	}
}
