import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class GameServer {
    private ObjectOutputStream output;   // output stream to client
    private ObjectInputStream input;     // input stream from client
    private ServerSocket server;         // server socket
    private Socket connection;           // connection to client

    private Set<String> userNames = new HashSet<>();               // Usernames of online clients.
    private Set<String> teamA = new HashSet<>();                   // Set of Team A
    private Set<String> teamB = new HashSet<>();                   // Set of Team B
    private Set<ClientThread> clientThreads = new HashSet<>();     // Threads of online clients.
    private Set<ClientThread> teamAClientThreads = new HashSet<>();// Splitting client threads
    private Set<ClientThread> teamBClientThreads = new HashSet<>();
    private List<String> words = new ArrayList<>();
    private HashMap<String, List<String>> wordsCombined = new HashMap<>();
    private int evenOrOdd = 0;
    private String nextWord = "WORD_TO_BE_FOUND";

    public static void main(String[] args) {
        new GameServer().runServer();          // run server application
    }

    // set up and run server
    public void runServer() {
        try // set up server to receive connections; process connections
        {
            String line;
            BufferedReader wordLoader = new BufferedReader(new FileReader("words.csv"));
            while ((line = wordLoader.readLine()) != null) {
                String[] data = line.split(",");
                String word = data[0];
                words.add(word);
                List<String> forbidden = new ArrayList<String>();
                for (int i = 1; i < data.length; i++) {
                    forbidden.add(data[i]);
                }
                wordsCombined.put(word, forbidden);
            }
            wordLoader.close();

            System.out.println("Word list loaded");

            server = new ServerSocket(9001, 3); // create ServerSocket

            while (true) {
                try {
                    waitForConnection();     // wait for a connection
                    getStreams();            // get input & output streams
                    //processConnection();     // process connection
                    ClientThread newClient;

                    //Creating teams
                    if (teamAClientThreads.isEmpty()) {
                        newClient = new ClientThread(connection, output, input, this, "Team A");
                        clientThreads.add(newClient);
                        teamAClientThreads.add(newClient);
                    } else if (teamBClientThreads.isEmpty()) {
                        newClient = new ClientThread(connection, output, input, this, "Team B");
                        clientThreads.add(newClient);
                        teamBClientThreads.add(newClient);
                    } else if (teamAClientThreads.size() - teamBClientThreads.size() >= 1) {
                        newClient = new ClientThread(connection, output, input, this, "Team B");
                        clientThreads.add(newClient);
                        teamBClientThreads.add(newClient);
                    } else {
                        newClient = new ClientThread(connection, output, input, this, "Team A");
                        clientThreads.add(newClient);
                        teamAClientThreads.add(newClient);
                    }

                    newClient.start();
                    //printTeams();
                } catch (EOFException eofException) {
                    System.out.println("\nServer terminated connection");
                } finally {
                    //closeConnection(); //  close connection
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // wait for connection to arrive, then display connection info
    private void waitForConnection() throws IOException {
        System.out.println("Waiting for connection\n");
        connection = server.accept(); // allow server to accept connection
        System.out.println("Connection received from: " +
                connection.getInetAddress().getHostName());

    }

    private String printTeams() {
        return "Team A: " + getTeamANames() + "\n" + "Team B: " + getTeamBNames();
    }

    // get streams to send and receive data
    private void getStreams() throws IOException {
        // set up output stream for objects
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush(); // flush output buffer to send header information

        // set up input stream for objects
        input = new ObjectInputStream(connection.getInputStream());

        System.out.println("\nGot I/O streams\n");
    }

    // close streams and socket
    private void closeConnection() {
        System.out.println("\nTerminating connection\n");

        try {
            output.close(); // close output stream
            input.close(); // close input stream
            connection.close(); // close socket
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    void broadcast(String message, ClientThread excludeUser, String team) {
        System.out.println(message);
        if (message.contains("--START")) {
            for (ClientThread aUser : clientThreads) {
                aUser.sendMessage("Game is starting...");
                aUser.sendMessage(" _____     _               _____ _                \n" +
                        "|_   _|_ _| |__   ___   __|_   _(_)_ __ ___   ___ \n" +
                        "  | |/ _` | '_ \\ / _ \\ / _ \\| | | | '_ ` _ \\ / _ \\\n" +
                        "  | | (_| | |_) | (_) | (_) | | | | | | | | |  __/\n" +
                        "  |_|\\__,_|_.__/ \\___/ \\___/|_| |_|_| |_| |_|\\___|");
                aUser.sendMessage(printTeams());
            }
        } else if (message.contains("--NEXT")) {
            String randomWord = getRandomElement(words);
            nextWord = randomWord;
            Random randomInt = new Random();
            if (evenOrOdd % 2 == 0) {
                int randomClient = randomInt.nextInt(teamAClientThreads.size());
                int counter = 0;
                for (ClientThread aUser : teamAClientThreads) {
                    if (counter == randomClient) {
                        aUser.sendMessage("Explain " + " | " + "Word: " + randomWord + " | Forbiden words: " + wordsCombined.get(randomWord));
                        counter += 1;
                    } else {
                        counter += 1;
                    }
                }
                for (ClientThread aUser : teamBClientThreads) {
                    aUser.sendMessage("Team A Describes" + " | " + "Word: " + randomWord + " | Forbiden words: " + wordsCombined.get(randomWord));
                }

            } else {
                int randomClient = randomInt.nextInt(teamBClientThreads.size());
                int counter = 0;
                for (ClientThread aUser : teamBClientThreads) {
                    if (counter == randomClient) {
                        aUser.sendMessage("Explain " + " | " + "Word: " + randomWord + " | Forbiden words: " + wordsCombined.get(randomWord));
                        counter += 1;
                    } else {
                        counter += 1;
                    }
                }
                for (ClientThread aUser : teamAClientThreads) {
                    aUser.sendMessage("Team B Describes" + " | " + "Word: " + randomWord + " | Forbiden words: " + wordsCombined.get(randomWord));
                }

            }
            evenOrOdd += 1;
        } else if (message.contains(nextWord)){
            for (ClientThread aUser : clientThreads) {
                aUser.sendMessage("Found!!!! You can move to next word by typing \"--NEXT\"");
            }
        } else {
            for (ClientThread aUser : clientThreads) {
                if (aUser != excludeUser) {
                    aUser.sendMessage(message);
                }
            }
        }

    }

    void addUserName(String userName) {
        userNames.add(userName);
    }

    void removeUser(String userName, ClientThread aUser) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            clientThreads.remove(aUser);
            System.out.println("The user " + userName + " quitted");
        }
    }

    Set<String> getUserNames() {
        return this.userNames;
    }

    Set<String> getTeamANames() {
        return this.teamA;
    }

    Set<String> getTeamBNames() {
        return this.teamB;
    }

    boolean hasUsers() {
        return !this.userNames.isEmpty();
    }

    boolean teamAHasUsers() {
        return !this.teamA.isEmpty();
    }

    boolean teamBHasUsers() {
        return !this.teamB.isEmpty();
    }

    public void addToTeam(String team, String userName) {
        if (team.equals("Team A")) {
            teamA.add(userName);
        } else {
            teamB.add(userName);
        }
    }

    // Function select an element base on index
    // and return an element
    public String getRandomElement(List<String> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }
}
