package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import utils.Constants;
import utils.FileManager;
import utils.Parserializer;
import utils.SocketManager;

public class SyncServer implements Runnable {
	private int port = 5700;
	private DataInputStream reader = null;
	private DataOutputStream writer = null;
	private Socket connectionSocket = null;
	private ServerSocket serverSocket = null;
	private Thread serverThread = null;

	/**
	 * Constructor
	 * 
	 * @param port
	 *            server port
	 */
	public SyncServer(int port) {
		this.port = port;
	}

	/**
	 * Start server
	 */
	public void startServer() {
		System.out.println("Running on port " + Integer.toString(port));

		this.serverThread = new Thread(this);
		this.serverThread.start();
	}

	@Override
	public void run() {
		List<String> remoteFileList = null;
		List<String> missingFileList = null;

		// Setup Server
		try {
			setupServer();

			while (true) {
				try {
					// Get Connection
					setupConnection();
					System.out.println("Connection from client established");

					// Sync
					System.out.println("Synchronization has started\n");

					// Get Remote Files
					remoteFileList = getRemoteFileList();

					// Compute Missing File List
					missingFileList = FileManager.getInstance().getMissingFileList(remoteFileList);

					// Get Missing Files
					exchangeFiles(missingFileList);



				} catch (IOException exchangeException) {
					String reason = exchangeException.getMessage();
					if (reason != null) {
						System.out.println(reason);
					}
				} finally {
					// Announce Client Ended
					System.out.println("\nSynchronization has ended");

					// Close Connection
					closeConnection();
				}
			}

		} catch (IOException serverSetupException) {
			String reason = serverSetupException.getMessage();
			if (reason != null) {
				System.out.println(reason);
			}
		}

		// Close Server
		try {
			closeServer();
		} catch (IOException serverCloseException) {
			String reason = serverCloseException.getMessage();
			if (reason != null) {
				System.out.println(reason);
			}
		}
	}

	private void setupServer() throws IOException {
		serverSocket = new ServerSocket(port);
	}

	private void closeServer() throws IOException {
		// Check for null server socket
		if (serverSocket == null) {
			return;
		}

		// Check closed server socket
		if (serverSocket.isClosed()) {
			return;
		}

		// Close socket
		serverSocket.close();
	}

	private void setupConnection() throws IOException {
		// Check for null server socket
		if (serverSocket == null) {
			return;
		}

		// Check server socket status
		if (serverSocket.isClosed()) {
			return;
		}

		// Wait for connection
		connectionSocket = serverSocket.accept();

		// Setup Input Stream
		reader = new DataInputStream(connectionSocket.getInputStream());

		// Setup OutputStream
		writer = new DataOutputStream(connectionSocket.getOutputStream());
	}

	private void closeConnection() throws IOException {
		// Check connection socket for null
		if (connectionSocket == null) {
			return;
		}

		// Check for closed connection
		if (connectionSocket.isClosed()) {
			return;
		}

		// Close socket
		connectionSocket.close();
	}

	private List<String> getRemoteFileList() throws IOException {
		List<String> remoteFileList = null;
		List<String> localFileList = FileManager.getInstance().getFileList();

		// Wait for sync request
		byte command = reader.readByte();
		if (command == Constants.SYNC) {
			remoteFileList = readSyncCommand();

			sendSyncOK(localFileList);
		} else {
			System.out.println("Error: Command out of order");
			writer.writeByte(Constants.SYNC_NOT_OK);
		}

		return remoteFileList;
	}

	private List<String> readSyncCommand() throws IOException, UnsupportedEncodingException {
		List<String> remoteFileList;
		byte[] payload = null;
		// Sync
		// Get Payload length
		int payloadLength = reader.readInt();

		// Get Payload
		payload = new byte[payloadLength];
		reader.read(payload, 0, payloadLength);

		// Parse Payload
		String serializedRemoteFileList = new String(payload, "UTF-8");
		remoteFileList = Parserializer.parse(serializedRemoteFileList);
		return remoteFileList;
	}

	private void sendSyncOK(List<String> localFileList) throws IOException {
		// Send File List
		String serializedLocalFileList = Parserializer.serialize(localFileList);
		byte[] responsePayload = serializedLocalFileList.getBytes();

		// Sync OK Response
		writer.writeByte(Constants.SYNC_OK);

		// Send Payload Length
		writer.writeInt(responsePayload.length);

		// Send Payload
		writer.write(responsePayload, 0, responsePayload.length);
	}

	private void exchangeFiles(List<String> missingFileList) throws IOException {
		String whatHeWants = null;
		String whatIWant = null;
		SocketManager socketManager = new SocketManager(reader, writer);

		for (int i = 0; i < missingFileList.size(); i++) {
			String missingFile = missingFileList.get(i);

			if (missingFile == null) {
				continue;
			}

			// Receive
			whatHeWants = socketManager.receive(whatIWant);
			whatIWant = missingFile;

			// Send
			socketManager.send(whatIWant, whatHeWants);

			System.out.println("Downloading: " + whatIWant + " [" + Integer.toString(i + 1) + "/"
					+ Integer.toString(missingFileList.size()) + "]");

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
