package newtworking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ServerServiceProxy {

	private ServerSocket socket;
	private final static Integer PORT = 6066;
	private ArrayList<ServerWorker> clients = new ArrayList<ServerWorker>();

	public ServerServiceProxy() {
		try {
			socket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("Error at setting up server socket");
			e.printStackTrace();
		}
	}

	public void startListeningForClients() {
		while (true) {
			try {
				Socket s = socket.accept();
				ServerWorker w = new ServerWorker(s);
				w.start();

				clients.add(w);

				System.out.println("New client connected, " + s.getPort());
			} catch (IOException e) {
				System.out.println("Error at new client connetion");
				e.printStackTrace();
			}
		}

	}
}
