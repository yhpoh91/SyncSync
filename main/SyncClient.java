package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.List;

import utils.Constants;
import utils.FileManager;
import utils.Parserializer;
import utils.SocketManager;

public class SyncClient implements Runnable {
	private String destinationIp = "127.0.0.1";
	private int destinationPort = 5700;
	private DataInputStream reader = null;
	private DataOutputStream writer = null;
	private Socket connectionSocket = null;
	private Thread clientThread = null;

	/**
	 * Constructor
	 * 
	 * @param destinationIp
	 *            peer ip
	 * @param destinationPort
	 *            peer port
	 */
	public SyncClient(String destinationIp, int destinationPort) {
		this.destinationIp = destinationIp;
		this.destinationPort = destinationPort;
		this.clientThread = new Thread(this);
	}

	/**
	 * Initiate synchronization with peer
	 */
	public void startSync() {
		System.out.println("Connecting to " + destinationIp + " : " + Integer.toString(destinationPort));

		// Check if it is instantiated properly
		if (this.clientThread == null) {
			return;
		}

		// Check is already started and is alive
		if (this.clientThread.isAlive()) {
			return;
		}

		// Start
		this.clientThread.start();
	}

	@Override
	public void run() {
		List<String> localFileList = FileManager.getInstance().getFileList();
		List<String> remoteFileList = null;
		List<String> missingFileList = null;

		System.out.println("Synchronization has started\n");

		// Setup Connection
		try {
			setupConnection();
		} catch (IOException connectionSetupException) {
			String reason = connectionSetupException.getMessage();
			if (reason != null) {
				System.out.println(reason);
			}
		}

		// Sync
		try {
			// Get Remote File List
			remoteFileList = getRemoteFileList(localFileList);

			// Compute Missing File List
			missingFileList = FileManager.getInstance().getMissingFileList(remoteFileList);

			// Get Missing Files
			exchangeFiles(missingFileList);

		} catch (IOException e) {
			String reason = e.getMessage();
			if (reason != null) {
				System.out.println(reason);
			}
		}

		// Close Connection
		try {
			closeConnection();
		} catch (IOException connectionCloseException) {
			String reason = connectionCloseException.getMessage();
			if (reason != null) {
				System.out.println(reason);
			}
		}

		// Announce Client Ended
		System.out.println("\nSynchronization has ended");
	}

	private void setupConnection() throws IOException {
		// Setup Socket
		connectionSocket = new Socket(destinationIp, destinationPort);

		// Setup Input Stream
		reader = new DataInputStream(connectionSocket.getInputStream());

		// Setup OutputStream
		writer = new DataOutputStream(connectionSocket.getOutputStream());
	}

	private void closeConnection() throws IOException {
		reader.close();
		writer.close();
		connectionSocket.close();
	}

	private List<String> getRemoteFileList(List<String> localFileList) throws IOException {
		List<String> remoteFileList = null;

		// Send Sync Request
		sendSyncCommand(localFileList);

		// Read Sync Response
		remoteFileList = readSyncCommandResponse(remoteFileList);

		return remoteFileList;
	}

	private List<String> readSyncCommandResponse(List<String> remoteFileList)
			throws IOException, UnsupportedEncodingException {
		String serializedRemoteFileList;
		// Read Sync Response Command
		byte syncResponse = reader.readByte();

		// Process Response
		if (syncResponse == Constants.SYNC_OK) {
			int responsePayloadLength = 0;
			byte[] responsePayload = null;

			// Get Payload Length
			responsePayloadLength = reader.readInt();

			// Get Payload
			responsePayload = new byte[responsePayloadLength];
			reader.read(responsePayload, 0, responsePayloadLength);

			// Parse Payload
			serializedRemoteFileList = new String(responsePayload, "UTF-8");
			remoteFileList = Parserializer.parse(serializedRemoteFileList);
		} else {
			// Print Error on Console
			System.out.println("Server is not accepting sync");
		}
		return remoteFileList;
	}

	private void sendSyncCommand(List<String> localFileList) throws IOException {
		// Get Local File List
		String serializedLocalFileList = Parserializer.serialize(localFileList);
		byte[] payload = serializedLocalFileList.getBytes();

		// Send Sync Command
		writer.writeByte(Constants.SYNC);

		// Send Payload Length
		writer.writeInt(payload.length);

		// Send Payload
		writer.write(payload, 0, payload.length);
	}

	private void exchangeFiles(List<String> missingFiles) throws IOException {
		String whatHeWants = null;
		String whatIWant = null;
		SocketManager socketManager = new SocketManager(reader, writer);

		// Check for first
		if (missingFiles.size() > 0) {
			whatIWant = missingFiles.get(0);

		}
		// Send
		socketManager.send(whatIWant, whatHeWants);
		if (missingFiles.size() > 0) {
			System.out.println("Downloading: " + whatIWant + " [" + Integer.toString(1) + "/"
					+ Integer.toString(missingFiles.size()) + "]");
		}
		for (int i = 1; i < missingFiles.size(); i++) {
			String missingFile = missingFiles.get(i);

			if (missingFile == null) {
				continue;
			}

			// Receive
			whatHeWants = socketManager.receive(whatIWant);
			whatIWant = missingFile;

			// Send
			socketManager.send(whatIWant, whatHeWants);
			System.out.println("Downloading: " + whatIWant + " [" + Integer.toString(i + 1) + "/"
					+ Integer.toString(missingFiles.size()) + "]");

		}
		while (true) {
			// Receive
			whatHeWants = socketManager.receive(whatIWant);
			whatIWant = null;
			// Send
			socketManager.send(whatIWant, whatHeWants);

			// Check
			if (whatHeWants == null) {
				break;
			}
		}
	}
}
