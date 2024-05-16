/*
 * MAIN SERVER CLASS
 * Running this main method will establish a server that starts accepting connections.
 * The clients will expect port 5128, though the server allows you to specify any port for its socket.
 * 
 * This server sets up three (by default) chat rooms for clients to chat in.
 * 
 * Author: Alex Lewtschuk and Kai Sorensen
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
* Main class for chat server
*/
public class ChatServer extends  Thread {

    private int port; //we use port 5128
    private int db; //debug level
    private ServerSocket servSock;

    private ArrayList<ServerConnection> allClients;
    private ArrayList<Channel> channels;
    private LinkedHashMap<String, ArrayList<ServerConnection>> channelClientMap;
    private ExecutorService pool;
    private Set<String> names;

    //stats
    private int numClients;
    private int numChannels;
    private int numMessagesSent;
    private long timeStart;


    /*
    * Constructor for ChatServer
    */
    public ChatServer(int p, int d) {
        this.port = p; 
        this.db = d;
        this.pool = Executors.newFixedThreadPool(100);
        this.allClients = new ArrayList<ServerConnection>();
        this.channels = new ArrayList<Channel>();
        this.channelClientMap = new LinkedHashMap<String, ArrayList<ServerConnection>>();
        this.names = new HashSet<String>();

        this.numClients = 0;
        this.numMessagesSent = 0;
        this.timeStart = System.currentTimeMillis();

        if(db == 1) System.out.println(this.timeStart);

        //the instantiateChannels() method does all the work for setting up channels
        initiateChannels();

        //create socket
        try {
            this.servSock = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * Runs the server
    */
    public void runServer() {
        System.out.println("\nLAUNCHING SERVER\n");
        pool.execute(new DeathToClients(180000));
        Runtime.getRuntime().addShutdownHook(new DeathToClients(0));

        //accepts clients until pool is full
        while (true) {
            Socket client;
            try {
                client = servSock.accept();
                System.out.println("Client connected."); numClients++;
                ServerConnection sc = new ServerConnection(client);
                allClients.add(sc);
                pool.execute(sc);
            } catch (SocketException e) {
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }

    /*
    * Initiates channels :) :) :) :) I luv channels
    */ 
    private void initiateChannels() {
        String[] channelNames = {"A","B","C"};
        for (int i = 0; i < 3; i++) {
            Channel newChannel = new Channel(channelNames[i]);
            channels.add(newChannel);
            channelClientMap.put(newChannel.getName(), new ArrayList<ServerConnection>());
        }
        this.numChannels = channelNames.length;
    }

    /*
    * Handles cutting off the clients before ending the program
    */
    private void shutdown() {
        try {
            //notify the clients
            for (int i = 0; i < allClients.size(); i++) {
                allClients.get(i).getOOS().writeObject(new CmdMsg("/shut", "Kick rocks because the server is shutting down."));
                allClients.get(i).getOOS().flush();
            }
            //sets the flag to have all the connections gracefully return from their infinite loops and rejoin the main thread
            for (int i = 0; i < allClients.size(); i++) {
                allClients.get(i).setQuitting(true);
                allClients.get(i).join();
            }
            servSock.close(); //goodbye :)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * Gets server stats!!!!!!
    */
    private String getStats() {
        String stats = "";
        stats += "number of clients: " + numClients + "\n";
        stats += "number of channels: " + numChannels + "\n";
        stats += "total messages sent: " + numMessagesSent + "\n";
        return stats;
    }

    /*
    * An inner class is used because we need an object that can be ran as a thread, so this inner class implements Runnable
    * this object also represents clients
    */ 
    private class ServerConnection extends Thread {
        private final Socket clientSocket; //the client connection
        private CmdMsg info; //this is the only object that will be sent to the client, with updated contents each time
        private String name; //client's nickname
        private Channel ch; //channel that the client joined, or null if the client is not in a channel
        private long clientTime; //used to send a nice disconnection message to the client 
        ObjectOutputStream oos; //the object output stream for the client assocaited with this thread

        private boolean chat; //flag for if the client is sending an ordinary chat, so we don't resend it back to the client
        private boolean quitting; //flag for when it's time to disconnect a client from the server

        //constructors
        public ServerConnection(Socket client) {
            this.clientSocket = client;
            this.info = new CmdMsg("/print", "new client received");
            this.name = "client" + Integer.toString(this.hashCode());
            this.chat = false;
            this.quitting = false;
            this.clientTime = System.currentTimeMillis();
            names.add(name);
        }

        /*
        * Getters for ServerConnection
        */
        public CmdMsg getInfo() {
            return this.info;
        }
        
        /*
        * Gets the object output stream for the client assocaited with this thread
        */
        public ObjectOutputStream getOOS() {
            return this.oos;
        }

        /*
        * Used when the server is shutting down or if the client is leaving the server
        */
        public void setQuitting(boolean quit) {
            this.quitting = quit;
        }

        
        /*
        * Runs the thread
        */
        public void run() {
            try {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                //set the instance variable for later retreival 
                this.oos = out;

                while(true) {
                    //if it's time to disconnect the client
                    if(quitting) {
                        String finalStats = "Your final connection duration: " + ((System.currentTimeMillis() - this.clientTime) / 1000.0 / 60.0 + " minutes");
                        info.updateContents("/quit", finalStats);

                        out.writeObject(info);
                        out.flush();

                        this.wait(100);
                        out.close();
                        in.close();
                        clientSocket.close();
                        return;
                    } else {
                        //read in the client's object and handle it
                        handleInfo(in.readObject());
                        //if the object received was not a chat, then immediately send an updated object back
                        if(!chat && !quitting) {
                            out.writeObject(info);
                            out.flush();
                        }
                        chat = false;
                    }
                }
            } catch (IOException e) {
                handleQuit();
            } catch (ClassNotFoundException e) {
                System.out.println("***FATAL ERROR***  object received was not a CmdMsg");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IllegalMonitorStateException e) {/*this is a stupid exception and I don't like it*/}
        }

        /*
        * Handles the recieved info
        */
        private void handleInfo(Object receivedInfo) throws ClassNotFoundException {
            //make sure it's a CmdMsg
            if(!(receivedInfo instanceof CmdMsg)) throw new ClassNotFoundException("server did not reveive a CmdMsg object");
            
            //reformat received Object
            info = (CmdMsg) receivedInfo;
            String command = info.getCommand();
            String message = info.getMessage();
            timeStart = System.currentTimeMillis();

            //depending on the command, we must do different things
            switch (command) {
                case "chat":
                    handleChat(message);
                    break;
                case "/connect":
                    handleConnect(message);
                    break;
                case "/nick":
                    handleNick(message);
                    break;
                case "/list":
                    handleList();
                    break;
                case "/join":
                    handleJoin(message);
                    break;
                case "/leave":
                    handleLeave();
                    break;
                case "/quit":
                    handleQuit();
                    break;
                case "/stats":
                    handleStats();
                    break;
                default:
                    handleError();
                    break;
            }
        }

        //COMMAND HANDLING METHODS
        //these methods update the "info" instance variable of this inner class, preparing it to be sent out

        /*
        * Called when the client is in a channel and sends a chat
        */
        private void handleChat(String message) {
            //this if statement makes sure the client is in a channel first
            if(!(ch == null)) {
                if(db == 1) System.out.println("Client \"" + this.name + "\" sent a chat");
                this.info.updateContents("/print", this.name + ": " + message);
                chat = true;
                numMessagesSent++;
                ch.sendChat(this);
            } else {
                this.info.updateContents("/print", "You must join a channel to send chats.");
            }
        }
        
        /*
        * Called when the client initially connects
        */
        private void handleConnect(String message) {
            info.updateContents("/print", "\nYou are now connected to AMIT CHAT.\n");
        }
        
        /*
        * Called when the client is setting a new nickname
        */
        private void handleNick(String message) {
            String newName = message.trim(); //no spaces on the ends
            //condition: if it's a unique nickname
            if(!(names.contains(message))) {
                if(db == 1) System.out.println("Client \"" + this.name + "\" is changing its name to " + newName);
                names.remove(this.name);
                this.name = newName;
                names.add(this.name);
                info.updateContents("/print", "Nickname successfully updated: " + newName);
            } else {
                info.updateContents("/print", "\""+ newName +"\" is already taken!");
                if(db == 1) System.out.println("Client \"" + this.name + "\" tried a name that was taken! " + newName);
            }
        }
        
        /* 
        * Called when the client wants the channel list, gives formatted output
        */
        private void handleList() {
            if(db == 1) System.out.println("Client \"" + this.name + "\" is asking for the channel list");
            String message = "";
            for(String name : channelClientMap.keySet()) message += "Channel " + name + ": " + channelClientMap.get(name).size() + " chatters\n";
            info.updateContents("/print", message);
        }  
        
        /*
        * Called when the client wants to join a channel
        */
        private void handleJoin(String message) {
            //condition: if the client specified a channel
            if(!(message == null)) {
                message = message.trim().toUpperCase();
                //condition: if the client is already in this channel
                if(!(ch == null) && ch.getName().equals(message)) {
                    info.updateContents("/print", "You are already in channel \"" + message +"\"");
                } else {
                    //condition: if the specified channel exists
                    if(channelClientMap.keySet().contains(message)) {
                        channelClientMap.get(message).add(this);
                        if(db == 1) System.out.println("Client \"" + this.name + "\" joined channel " + message);
                        //for getting the channel object for the ServerConnection's instance variable ch
                        for(int i = 0; i < channels.size(); i++) {
                            if(channels.get(i).getName().equals(message)) {
                                ch = channels.get(i);
                                break;
                            }
                        }
                        info.updateContents("/print", "You joined channel " + ch.name);
                    } else {
                        info.updateContents("/print", "There is no channel \"" + message + "\". Use /list");
                    }
                }
            } else {
                info.updateContents("/print", "You must specify a channel name. Use /list");
            }
            
        }
        
        /*
        * Called when the client is leaving a channel
        */
        private void handleLeave() {
            //condition: if the client is indeed in a channel
            if(!(ch == null)) {
                channelClientMap.get(ch.name).remove(this);
                if(db == 1) System.out.println("Client \"" + this.name + "\" left channel " + ch.name);
                info.updateContents("/print", "You left channel " + ch.name);
                ch = null;
            } else {
                info.updateContents("/print", "You are not in a channel.");
            }   
        }

        /*
        * Called when the client uses /quit, we completely remove it from the server's data
        */
        private void handleQuit() {
            if(!(ch == null)) channelClientMap.get(ch.name).remove(this);
            allClients.remove(this);
            this.quitting = true; //sets quitting 
            System.out.println("Client \"" + this.name + "\" disconnected");
        }
        
       /*
        * Called when the client wants stats
        */
        private void handleStats() {
            info.updateContents("/print", getStats());
        }

        /*
        * Called if the client sends a command that isn't recognized, though this may also be handled on the client-side
        */
        private void handleError() {
            info.updateContents("/print", "improper command: use /help");
            if (db == 1) System.out.println("Error message triggered by client \"" + this.name);
        }
 
    }//end ServerConnection class

    /*
    * Inner class representing channels
    */
    private class Channel {
        //its name is what identifies it
        private String name;

        /*
        * Constructor
        */
        public Channel(String name) {
            this.name = name;
        }

        /*
        * Gets name
        */
        public String getName() {
            return this.name;
        }

        /*
        * Called to send a chat to the channel
        */
        public void sendChat(ServerConnection sender) {
            CmdMsg toSend = sender.getInfo();
            int j = channelClientMap.get(name).indexOf(sender);

            for(int i = 0; i < channelClientMap.get(name).size(); i++) {
                //send the message to everyone in the channel but the sender
                if(!(i == j)) {
                    try {
                        channelClientMap.get(name).get(i).getOOS().writeObject(toSend);
                    } catch (IOException e) {
                        System.out.println("***FATAL ERROR***  the channel failed to write to the output streams of its clients");
                        e.printStackTrace();
                    }
                }
            }
        }

    } //end Channel class

    /*
    * Shutdown hook
    */
    public class DeathToClients extends Thread {
        private int wait; //how long before the server shuts down, used for the 3 minute wait

        /*
        * Constructor
        */ 
        public DeathToClients(int millis) {
            this.wait = millis;
        }

        /*
        * Runs thread
        */
        public void run() {
            while (true) {
                if(System.currentTimeMillis() - timeStart > this.wait) {
                    System.out.println("Server is shutting down.");
                    shutdown();
                    return;
                }
            }
        }
    } //end DeathToClients class

    /*
    * Main class to run Server
    */
    public static void main(String[]args) {
        int p = -1; //port to be passed into constructor
        int d = -1; //debug level to be passed into constructor
        if(args.length != 4) usage();

        //handle arguments
        for (int i = 0; i < args.length; i++) {
            //looks for the expected arguments, otherwise throws usage()
            switch (args[i]) {
                case "-p":
                    i++;
                    p = Integer.parseInt(args[i]);
                    if(p < 0 || p > 65534) usage(); //expected range
                    break;
                case "-d":
                    i++;
                    d = Integer.parseInt(args[i]);
                    if(d < 0 || d > 1) usage(); //expected range
                    break;
                default:
                    usage();
            }
        }

        //here we will run the server
        ChatServer severus = new ChatServer(p, d);
        severus.runServer();
        
    }//end main

    /*
    * Usage, in case you goofed
    */
    private static void usage() {
        System.out.println("USAGE: java ChatServer -p <port#> -d <debug-level(0|1)>");
        System.exit(-1);
    }

}//end ChatServer class
