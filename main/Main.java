package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import utils.Constants;
import utils.FileManager;

public class Main {
	private static SyncServer server = null;
	private static SyncClient client = null;

	/**
	 * Main function
	 * 
	 * @param args
	 *            command line input
	 *            [port number] [path (optional)]
	 */
	public static void main(String[] args) {
		// Default port (unless specified)
		int serverPort = 5700;

		// Get port
		if (args.length > 0) {
			String portString = args[0];
			serverPort = validatePort(portString);
		}

		// Get path (via command line argument)
		if (args.length > 1) {
			String path = args[1];
			FileManager.getInstance().setPath(path);
		}

		// Create server
		server = new SyncServer(serverPort);
		server.startServer();

		// Console input
		InputStreamReader inputStreamReader = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(inputStreamReader);

		// Print current working directory path
		showPath();

		while (true) {
			// Read and process commands
			loop(reader);
		}
	}

	/**
	 * Connect to peer
	 * 
	 * @param ip
	 *            ip of peer
	 * @param port
	 *            port of peer
	 */
	private static void connect(String ip, int port) {
		client = new SyncClient(ip, port);
		client.startSync();
	}

	/**
	 * Prints current working directory path
	 */
	private static void showPath() {
		FileManager fileManager = FileManager.getInstance();
		String path = fileManager.getPath();
		File file = new File(path);
		System.out.println("Current Directory: \"" + file.getAbsolutePath() + "\"");
	}

	/**
	 * Read and process commands
	 * 
	 * @param reader
	 *            input stream
	 */
	private static void loop(BufferedReader reader) {
		try {
			String nextLine = reader.readLine();

			// Check command
			if (nextLine.toLowerCase().equals(Constants.CONNECT)) {
				String ipString = reader.readLine();
				String portString = reader.readLine();

				String ip = null;
				int port = -1;

				// Validate input
				ip = validateIp(ipString);
				port = validatePort(portString);

				// Connect if everything is correct
				if (ip != null && port != -1) {
					connect(ip, port);
				}
			} else if (nextLine.toLowerCase().equals(Constants.SET_PATH)) {
				String path = reader.readLine();
				FileManager.getInstance().setPath(path);
				System.out.println("New path: \"" + path + "\"");
			} else if (nextLine.toLowerCase().equals(Constants.EXIT)) {
				// Quit Program
				System.exit(0);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if port is valid
	 * 
	 * @param portString
	 *            port read from input
	 * @return port number or -1 (if not valid)
	 */
	private static int validatePort(String portString) {
		int port = -1;

		try {
			int item = Integer.parseInt(portString);
			// Check range
			if (item < 1 && item > 65535) {
				System.out.println("Port outside of valid port range (1 - 65535)");
				port = -1;
			} else {
				port = item;
			}
		} catch (NumberFormatException e) {
			System.out.println("Port reading error");
			port = -1;
		}

		return port;
	}

	/**
	 * Check if ip is valid
	 * 
	 * @param ipString
	 *            ip read from input
	 * @return ip address or null (if not valid)
	 */
	private static String validateIp(String ipString) {
		String ip = null;
		boolean isValidIp = true;

		// Check Validity
		String[] hierarchicalList = ipString.split(".");
		// Check number of sections
		if (hierarchicalList.length < 0 || hierarchicalList.length > 4) {
			System.out.println("IP must have 4 sections");
			isValidIp = false;
		} else {
			// Check range
			for (String hierarchicalItem : hierarchicalList) {
				try {
					int item = Integer.parseInt(hierarchicalItem);

					// Check range
					if (item < 0 && item > 255) {
						System.out.println("IP value outside 8 byte range (0 - 255)");
						isValidIp = false;
						break;
					}
				} catch (NumberFormatException e) {
					System.out.println("IP reading error");
					isValidIp = false;
					break;
				}
			}
		}

		if (isValidIp) {
			ip = ipString;
		}

		return ip;
	}
}
