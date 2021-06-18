import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Client {
    private ObjectOutputStream output;   // output stream to server
    private ObjectInputStream input;     // input stream from server
    private Socket client;               // socket to communicate with server
    private String userName;
    private final String regex = "\\@(\\w+)";
    private final Pattern pattern = Pattern.compile(regex);


    // connect to server and process messages from server
    public void runClient() {
        try // connect to server, get streams, process connection
        {
            connectToServer(); // create a Socket to make connection
            getStreams(); // get the input and output streams
            processConnection(); // process connection
        } catch (EOFException eofException) {
            System.out.println("\nClient terminated connection");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeConnection(); // close connection
        }
    }

    // connect to server
    private void connectToServer() throws IOException {
        System.out.println("Attempting connection\n");

        // create Socket to make connection to server
        client = new Socket("127.0.0.1", 9001);

        // display connection information
        System.out.println("Connected to: " +
                client.getInetAddress().getHostName());
    }

    // get streams to send and receive data
    private void getStreams() throws IOException {
        // set up output stream for objects
        output = new ObjectOutputStream(client.getOutputStream());
        output.flush(); // flush output buffer to send header information

        // set up input stream for objects
        input = new ObjectInputStream(client.getInputStream());

        System.out.println("\nGot I/O streams\n");
    }

    // process connection with server
    private void processConnection() throws IOException {
        String message = "";
        Scanner sc = null;
        try // read message and display it
        {
            message = (String) input.readObject(); // read new message containing connected users.
            System.out.println("\n" + message); // display message

            sc = new Scanner(System.in);
            System.out.print("Enter your name: ");
            this.userName = sc.nextLine();
            sendData(this.userName);
        } catch (ClassNotFoundException classNotFoundException) {
            System.out.println("\nUnknown object type received");
        }

        Thread readMessage = new Thread() {
            public void run() {

                while (true) {
                    String message = "";
                    try {
                        message = (String) input.readObject(); // read new message containing connected users.
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (message.length() > 0) {
                        ArrayList usernames = new ArrayList();
                        Matcher matcher = pattern.matcher(message);
                        while (matcher.find()){
                            usernames.add(matcher.group());
                        }

                        if (usernames.size() > 0) {
                            String userWithAt = "@" + userName;
                            if (usernames.contains(userWithAt)){
                                System.out.println("\n" + message); // display message
                            }
                        } else {
                            System.out.println("\n" + message); // display message
                        }
                    }
                }
            }
        };

        readMessage.start();

        //sendMessage("[" + userName + "]: ");


        do // process messages sent from server
        {
            String clientMessage = sc.nextLine();
            sendData("[" + this.userName + "]: " + clientMessage);
        } while (!message.equals("SERVER>>> TERMINATE"));
    }

    // close streams and socket
    private void closeConnection() {
        System.out.println("\nClosing connection");

        try {
            output.close(); // close output stream
            input.close(); // close input stream
            client.close(); // close socket
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // send message to server
    private void sendData(String message) {
        try // send object to server
        {
            output.writeObject(message);
            output.flush(); // flush data to output
        } catch (IOException ioException) {
            System.out.println("\nError writing object");
        }

    }


    public static void main(String[] args) {
        new Client().runClient(); // use args to connect
    }
} 

