import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.IOException;
 

public class ClientThread extends Thread {
    private Socket connection;
    private GameServer server;
    private ObjectOutputStream output;   // output stream to client
    private ObjectInputStream input;     // input stream from client
    private String team;
    
    public ClientThread(Socket connection, ObjectOutputStream output, ObjectInputStream input, GameServer server, String team) {
        this.connection = connection;
        this.output = output;
        this.input  = input;
        this.server = server;
        this.team = team;
    }

    public String getTeam() {
        return team;
    }

    public void run() {
        try {
            sendConnectedUsers();               // sends a list of online users to the newly connected client.

            String userName = (String) input.readObject(); // read the username of the newly connected client
            server.addUserName(userName);
            server.addToTeam(team, userName);
 
            String serverMessage = "New user connected: " + userName;
            server.broadcast(serverMessage, this, this.getTeam());       // announce the newly connected client to the online clients.
 
            String clientMessage;
 
            do {
                clientMessage = (String) input.readObject();
                
                server.broadcast(clientMessage, this, this.getTeam());
 
            } while (!clientMessage.equals("bye"));
 
            server.removeUser(userName, this);
            connection.close();
 
            serverMessage = userName + " has quitted.";
            server.broadcast(serverMessage, this, this.getTeam());
 
        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
        catch (ClassNotFoundException classNotFoundException) 
        {
            System.out.println("\nUnknown object type received");
        } 
    }
 
    /**
     * Sends a list of online users to the newly connected user.
     */
    void sendConnectedUsers() {
        if (server.hasUsers()) {
            sendMessage("Connected users: " + server.getUserNames());
        } else {
            sendMessage("No other users connected");
        }
        /*if (server.teamAHasUsers() && server.teamBHasUsers()){
            sendMessage("Team A: " + server.getTeamANames());
            sendMessage("Team B: " + server.getTeamBNames());
        }*/
    }
 
    /**
     * Sends a message to the client.
     */
    void sendMessage(String message) {
        try // send object to client
        {
            output.writeObject(message);
            output.flush(); // flush output to client
        } 
        catch (IOException ioException) 
        {
            System.out.println("\nError writing object");
        }
    }
}