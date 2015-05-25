package newtworking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import exceptions.UserAlreadyInUseException;
import model.Message;
import model.User;


public class ServerWorker extends Thread{
	private Socket socket;
	
	private DataOutputStream os;
	private DataInputStream is;


	private ArrayList<User> users = new ArrayList<User>();
	private Map<User,Socket> bagOfClients = new HashMap<User,Socket>();

	

	public ServerWorker(Socket s) {
		setUpServerSocket(s);

	}

	private void setUpServerSocket(Socket s) {
		socket = s;
	
		try {
			socket.setKeepAlive(true);
			System.out.println("Started new client.");
			os = new DataOutputStream(s.getOutputStream());
			is = new DataInputStream(s.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			if (socket.isClosed()) {
				System.out.println("Server:User disconnected.");
				break;
			}
			
			try {
				int action = is.readInt();
				System.out.println("Server:Action to be performed: " + action);

				if (action == -1) {
					logout();
					System.out.println("server:User disconnected");
					socket.close();
					break;
				}

				if (action == 0) {
					// login
					loginAction();
					//should send update for all users
					//returnUsers();
					
				}			

//				
				if (action == 2){
					//receive message
					receiveMessage();
				}
				if (action == 3){
					//return the list of users to the requestor 
					returnUsers();
				}
				
			} catch (IOException e) {
				if (e.getMessage().equals("Connection reset")){
					System.out.println("User disconnected");
					break;
				}
				
				System.out.println("Error at reading action from client (main)");
				e.printStackTrace();
			}

		}
	}


	private void logout() {
		try {
			System.out.println("Server:Performing removing user action");
			byte[] email = new byte[255];
			byte[] name = new byte[255];
			byte[] phone = new byte[255];

			int len = is.readInt();
			is.readFully(name, 0, len);
			String sName = new String(name, "UTF-8");
			sName = sName.substring(0, len);
			
			len = is.readInt();
			is.readFully(email, 0, len);
			String sEmail = new String(email, "UTF-8");
			sEmail = sEmail.substring(0, len);
			
			
			len = is.readInt();
			is.readFully(phone, 0, len);
			String sPhone = new String(phone, "UTF-8");
			sPhone = sPhone.substring(0, len);
			
			User user = new User();
			user.setName(sName);
			user.setEmail(sEmail);
			user.setPhone(sPhone);
			
			removeUser(user);
			
			System.out.println("Email: " + sEmail);
			System.out.println("Name: " + sName);
			System.out.println("Phone: " + sPhone);


		} catch (IOException e) {
			System.out.println("Server:Error at reading action from client");
			e.printStackTrace();
		}	}

	private void receiveMessage() {
		try {
			System.out.println("Server:Performing receive message");
			byte[] sender = new byte[255];
			byte[] receiver = new byte[255];
			byte[] message = new byte[255];

			int len = is.readInt();
			is.readFully(sender, 0, len);
			String sSender = new String(sender, "UTF-8");
			sSender = sSender.substring(0, len);
			
			len = is.readInt();
			is.readFully(receiver, 0, len);
			String sReceiver = new String(receiver, "UTF-8");
			sReceiver = sReceiver.substring(0, len);
			
			len = is.readInt();
			is.readFully(message, 0, len);
			String sMessage = new String(message, "UTF-8");
			sMessage = sMessage.substring(0, len);
			
			Message msg = new Message();
			msg.setSender(sSender);
			msg.setReceiver(sReceiver);
			msg.setMessage(sMessage);
			
			System.out.println("Server:Message recive: "+msg.messageInfo());
			sendMessage(msg);
			
		} catch (IOException e) {
			System.out.println("Server:Error at reading action from client (add new student)");
			e.printStackTrace();
		}
	}
	

	private void loginAction() {
		try {
			System.out.println("Server:Performing adding new user action");
			byte[] email = new byte[255];
			byte[] name = new byte[255];
			byte[] phone = new byte[255];

			int len = is.readInt();
			is.readFully(name, 0, len);
			String sName = new String(name, "UTF-8");
			sName = sName.substring(0, len);
			
			len = is.readInt();
			is.readFully(email, 0, len);
			String sEmail = new String(email, "UTF-8");
			sEmail = sEmail.substring(0, len);
			
			
			len = is.readInt();
			is.readFully(phone, 0, len);
			String sPhone = new String(phone, "UTF-8");
			sPhone = sPhone.substring(0, len);
			
			User user = new User();
			user.setName(sName);
			user.setEmail(sEmail);
			user.setPhone(sPhone);
			
			System.out.println("Email: " + sEmail);
			System.out.println("Name: " + sName);
			System.out.println("Phone: " + sPhone);

			int res = 0;

			try {
				if(users.contains(user)){
					res = 0;
				}
				else {
					res = -1;
					users.add(user);
					bagOfClients.put(user, socket);
				}
			} finally {
				os.writeInt(res);
				os.flush();
			}
		} catch (IOException e) {
			System.out.println("Server:Error at reading action from client");
			e.printStackTrace();
		}
	}

	private void returnUsers() {
		System.out.println("Server:Users nr: " + users.size());
		try {
			os.writeInt(users.size());
			os.flush();
			for (User s : users) {
				os.writeInt(s.getName().length());
				os.flush();
				os.writeBytes(s.getName());
				os.flush();
				os.writeInt(s.getEmail().length());
				os.flush();
				os.writeBytes(s.getEmail());
				os.flush();
				os.writeInt(s.getPhone().length());
				os.flush();
				os.writeBytes(s.getPhone());
			}
			
			System.out.println("Server:Users sent to client from server");
		} catch (Exception e) {

		}
	}
	
	private void sendMessage(Message message) {
		System.out.println("server:Users nr: " + users.size());
		System.out.println("Server:Performing send message:"+message);

		for(User user:users){

			if(message.getReceiver().equals(user.getName())){
				setUpServerSocket(bagOfClients.get(user));//we want to send the info to the right person and for that we need to use the socket he is using 
				try {
					os.writeInt(message.getSender().length());
					os.flush();
					os.writeBytes(message.getSender());
					os.flush();
					os.writeInt(message.getReceiver().length());
					os.flush();
					os.writeBytes(message.getReceiver());
					os.flush();
					os.writeInt(message.getMessage().length());
					os.flush();
					os.writeBytes(message.getMessage());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void removeUser(User user){
		users.remove(user);
		bagOfClients.remove(user);
	}

}
