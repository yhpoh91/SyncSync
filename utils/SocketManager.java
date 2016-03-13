package utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class SocketManager {
	private DataInputStream reader = null;
	private DataOutputStream writer = null;

	/**
	 * Constructor
	 * 
	 * @param reader
	 *            input stream
	 * @param writer
	 *            output stream
	 */
	public SocketManager(DataInputStream reader, DataOutputStream writer) {
		this.reader = reader;
		this.writer = writer;
	}

	/**
	 * Send file
	 * 
	 * @param whatIWantToRequest
	 *            file name of file wanted
	 * @param whatHeRequested
	 *            file name of file other party wanted
	 * @throws IOException
	 */
	public void send(String whatIWantToRequest, String whatHeRequested) throws IOException {
		// System.out.println("Send " + whatIWantToRequest + " : " +
		// whatHeRequested);

		// Send Command
		sendCommand(whatIWantToRequest == null);

		// Send What I want
		sendRequest(whatIWantToRequest);

		// Send What he wanted
		sendResponse(whatHeRequested);

		// Send Data that he wanted
		sendResponseData(whatHeRequested);
	}

	/**
	 * Receive file
	 * 
	 * @param whatIRequested
	 *            file name of file wanted
	 * @return
	 * @throws IOException
	 */
	public String receive(String whatIRequested) throws IOException {
		// System.out.println("Receive " + whatIRequested);

		// Read Command
		readCommand();

		// Read What he wants
		String whatHeWants = readRequest();

		// Read What I wanted
		String whatIWantName = readResponse();

		// Read Data that I wanted
		boolean isWhatIWant = (whatIWantName != null && whatIWantName.equals(whatIRequested));
		readResponseData(whatIRequested, isWhatIWant);

		return whatHeWants;
	}

	private void sendCommand(boolean done) throws IOException {
		if (done) {
			writer.writeByte(Constants.DONE);
		} else {
			writer.writeByte(Constants.DATA);
		}
	}

	private byte readCommand() throws IOException {
		return reader.readByte();
	}

	private void sendRequest(String name) throws IOException {
		sendString(name);
	}

	private String readRequest() throws IOException {
		return readString();
	}

	private void sendResponse(String name) throws IOException {
		sendString(name);
	}

	private String readResponse() throws IOException {
		return readString();
	}

	private void sendString(String name) throws IOException {
		byte[] data = new byte[0];
		int length = 0;

		if (name != null) {
			data = name.getBytes();
			length = data.length;
		}

		// Write Length
		writer.writeInt(length);

		// Write Payload
		if (length > 0) {
			writer.write(data, 0, length);
		}
	}

	private String readString() throws IOException {
		String request = null;
		long lastByteTime = System.currentTimeMillis();
		
		// Check timeout
		while (reader.available() == 0) {
			if (System.currentTimeMillis() - lastByteTime > 10000) {
				throw new IOException("\nTimeout while reading");
			}
		}
		lastByteTime = System.currentTimeMillis();

		// Read Length
		int length = reader.readInt();

		// Read Payload
		if (length > 0) {

			// Check timeout
			while (reader.available() == 0) {
				if (System.currentTimeMillis() - lastByteTime > 10000) {
					throw new IOException("\nTimeout while reading");
				}
			}
			lastByteTime = System.currentTimeMillis();
			
			byte[] data = new byte[length];
			reader.read(data, 0, length);
			request = new String(data, "UTF-8");
		}

		return request;
	}

	private void sendResponseData(String name) throws IOException {
		if (name == null) {
			// Write size (0 byte payload length)
			writer.writeLong(0);
			return;
		}

		try {
			// Get file data
			File file = new File(FileManager.getInstance().getPath() + "/" + name);

			// Get input stream
			InputStream fileReader = Files.newInputStream(file.toPath(), StandardOpenOption.READ);

			// Write size
			long fileSize = Files.size(file.toPath());
			writer.writeLong(fileSize);

			// Write data
			byte[] dataBytes = new byte[Constants.chunkLength];
			long dataDone = 0;
			int numberOfBytes = 0;

			// Send while not complete
			while (dataDone < fileSize) {
				// Read
				numberOfBytes = fileReader.read(dataBytes);

				// Update Count
				dataDone += numberOfBytes;

				// Write
				writer.write(dataBytes, 0, numberOfBytes);
			}

			fileReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void readResponseData(String name, boolean needsToSave) throws IOException {
		File file = null;
		OutputStream fileWriter = null;
		if (needsToSave) {
			// Fix path (support for Windows and Mac (possibly Linux, not
			// tested))
			String editedName = name.replace('\\', File.separatorChar);
			editedName = name.replace('/', File.separatorChar);

			file = new File(FileManager.getInstance().getPath(), editedName);

			// Ensure directories in path gets created
			file.getParentFile().mkdirs();

			// Create empty file if not exist
			file.createNewFile();

			// Get output stream
			fileWriter = Files.newOutputStream(file.toPath(), StandardOpenOption.WRITE);
		}

		// Read size
		long fileSize = reader.readLong();

		// Read data
		String percentageString = "";
		byte[] dataBytes = new byte[Constants.chunkLength];
		long dataDone = 0;
		int numberOfBytes = 0;
		long lastByteTime = System.currentTimeMillis();

		// Read while not complete
		while (dataDone < fileSize) {
			// Check timeout
			while (reader.available() == 0) {
				if (System.currentTimeMillis() - lastByteTime > 10000) {
					throw new IOException("\nTimeout while reading");
				}
			}
			lastByteTime = System.currentTimeMillis();

			// Read
			numberOfBytes = reader.read(dataBytes);

			// Update Count
			dataDone += numberOfBytes;

			// Write
			fileWriter.write(dataBytes, 0, numberOfBytes);

			// Delete previous percentage
			for (int j = 0; j < percentageString.length(); j++) {
				System.out.print("\b");
			}

			// Print new percentage
			double percentageDone = ((dataDone * 1.0) / fileSize) * 100;
			percentageString = String.format("%.2f%%", percentageDone);
			System.out.print(percentageString);
		}

		// Delete previous percentage
		for (int j = 0; j < percentageString.length(); j++) {
			System.out.print("\b");
		}

		// Print 100%
		if (fileSize > 0) {
			System.out.println("100.00% (COMPLETED)");
		}

		if (fileWriter != null) {
			fileWriter.close();
		}
	}
}
