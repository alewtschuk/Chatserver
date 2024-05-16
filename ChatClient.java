import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
/**
 * This is an implementation of basic IRC Protocol.
 * It is only as complicated as IRC needs to be, but it can be difficult to read.
 * @author Alex Lewtschuk, Kai Sorensen
 */
public class ChatClient {
    //Colors for text:
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    static String lineChar = ANSI_GREEN + ">" + ANSI_RESET;
    static String nickname;
    static String host = "localhost";
    static Socket server;
    static ObjectOutputStream out;
    static ObjectInputStream in;
    static Listener serverListener;
    static boolean listenerEnabled = false;
    static Boolean inConnect;
    static String currentChannel;

    enum NicknameValidation {
        NO_CONNECTION,
        NULL_OR_EMPTY,
        BAD_CHARACTERS,
        SAME_NICKNAME,
        VALID
    }

    

    // + ANSI_PURPLE + 
    // + ANSI_WHITE + 

    public static void main(String[]args) {
        System.out.println(ANSI_PURPLE + "------------------------------------------Chat Client------------------------------------------\n\n");
        // Portrait of our professor (for fun)
        // System.out.println(ANSI_WHITE + "####################################################################################################\n" + //
        //                 "####################################################################################################\n" + //
        //                 "####################################################################################################\n" + //
        //                 "#########################################" + ANSI_PURPLE + "(##(((((((((((((((" + ANSI_WHITE + "#########################################\n" + //
        //                 "######################################" + ANSI_PURPLE + "((((((///////////////(((" + ANSI_WHITE + "######################################\n" + //
        //                 "##############################" + ANSI_PURPLE + "((((//((((//////////********////(((" + ANSI_WHITE + "###################################\n" + //
        //                 "###########################" + ANSI_PURPLE + "(((///*///////////*/****************/(((" + ANSI_WHITE + "#################################\n" + //
        //                 "##########################" + ANSI_PURPLE + "///*/**//////////*//******************///((" + ANSI_WHITE + "###############################\n" + //
        //                 "#########################" + ANSI_PURPLE + "////////////(//////*******,***,,,********//(((" + ANSI_WHITE + "(((((((((####################\n" + //
        //                 "######################(#" + ANSI_PURPLE + "(/////////(((/////*****,,,,,,,,,,,,,,*****///(((("  + ANSI_WHITE + "((((((((((#((##############\n" + //
        //                 "###################((#"  + ANSI_PURPLE + "(((///////((((((/////*****,,,*,,,,,,,,**,***///(/((" + ANSI_WHITE + "(((((((((((((((((##########\n" + //
        //                 "##################(((((" + ANSI_PURPLE + "##((//((((/((((////////*******************/////((" + ANSI_WHITE + "(((((((((((((((((((##(######\n" + //
        //                 "#############((((((((((" + ANSI_PURPLE + "(##(//((((((((///////*/**********,,,**,***///(//(("  + ANSI_WHITE + "(((((((((((((((((((((#(####\n" + //
        //                 "#########((#(((((((((((" + ANSI_PURPLE + "#((((#((((/(((((////******,,,,,,,,**,,***///(((((#("  + ANSI_WHITE + "(((((((((((((((((((((((((#\n" + //
        //                 "#######(##(((((((((((((" + ANSI_PURPLE + "#(((((#(#" + ANSI_BLUE + "%@@@@&&&@@&%##" + ANSI_PURPLE + "((/***///((" + ANSI_BLUE + "#&@@@&&%&&@%" + ANSI_PURPLE + "(((#((("  + ANSI_WHITE + "((((((((((((((((((((((((\n" + //
        //                 "######(((((((((((((((" + ANSI_BLUE + "#&(&%((#####" + ANSI_PURPLE + "%#//((#%((//(" + ANSI_BLUE + "#(((##(((##" + ANSI_PURPLE + "#%%%#(//((#%"+ ANSI_BLUE + "(((#" + ANSI_PURPLE + "((#(("  + ANSI_WHITE + "((((((((((((((((((((((\n" + //
        //                 "###((((((((((((((((" + ANSI_PURPLE + "(%%#%#/#%##" + ANSI_BLUE + "#" + ANSI_PURPLE + "(%%@&/%@@#,/(##" + ANSI_BLUE + "#" + ANSI_PURPLE +"#%(,,/(#/" + ANSI_BLUE + "#" + ANSI_PURPLE + "(#/&@@*,%%##" + ANSI_BLUE + "(" + ANSI_BLUE + "((((#((((" + ANSI_WHITE + "(((((((((((((((((((((\n" + //
        //                 "((#((((((((((((((((" + ANSI_PURPLE + "#%#%%#(###%" + ANSI_BLUE + "%" + ANSI_PURPLE + "((#%%##((((/(((" + ANSI_BLUE + "#" + ANSI_PURPLE + "##**,*(#*"+ ANSI_BLUE + "/" + ANSI_PURPLE + "///(((((((((" + ANSI_BLUE + "(" + ANSI_PURPLE + "(##/,//((" + ANSI_WHITE + "(((((((((((((((((((((\n" + //
        //                 "((((((((((((((((((((" + ANSI_PURPLE + "%#%%%##(#(" + ANSI_BLUE + "(" + ANSI_PURPLE + "#/(///*****//((" + ANSI_BLUE + "#" + ANSI_PURPLE + "((/*****#" + ANSI_BLUE + "*" + ANSI_PURPLE + "*********///" + ANSI_BLUE + "(" + ANSI_PURPLE + "((#**//((" + ANSI_WHITE + "(((((((((((((((((((((\n" + //
        //                 "((((((((((((((((((((" + ANSI_PURPLE + "%%&@%%%##((" + ANSI_BLUE + "(((((########(#" + ANSI_PURPLE + "##(/******/" + ANSI_BLUE + "#########((/" + ANSI_PURPLE + "//(#/*((("  + ANSI_WHITE + "((((((((((((((((((((((\n" + //
        //                 "((((((((((((((((((((" + ANSI_PURPLE + "&%&@%%%%%#((//*******/##(((((/**,*****/*(/*,,,**//((#/*/((" + ANSI_WHITE + "((((((((((((((((((((((\n" + //
        //                 "((((((((((((((((((((" + ANSI_PURPLE + "###%&%%%%%##((/////((#%%%%#((/***//(%#////(//*///((##((((" + ANSI_WHITE + "(((((((((((((((((((((((\n" + //
        //                 "(((((((((((((((((((((" + ANSI_PURPLE + "%%&%%%%&%%%#(((##(###########(((/((((((((((#(((((##%/*/"  + ANSI_WHITE + "((((((((((((((((((((((((\n" + //
        //                 "(((((((((((((((((((((/" + ANSI_PURPLE + "%&&%%%%%%###(%#########((((///**//////(//((((((#(##(" + ANSI_RESET + "////////(//"  + ANSI_WHITE + "(((((((((((((((\n" + //
        //                 "(((((((((((((((((((" + ANSI_RESET + "//////(" + ANSI_PURPLE + "%%%%%%#((#####%%%#(*/(*,,,.,.*.*/&&(%##((((((##" + ANSI_RESET + "///////////////"  + ANSI_WHITE + "((((((((((((\n" + //
        //                 "(((((((((((/(" + ANSI_RESET + "/////////////" + ANSI_PURPLE + "#%%%%%##(/(####((#%%(/(*,*///(/(((/(/(//((((##" + ANSI_RESET + "//////////////////"  + ANSI_WHITE + "(//(((((((\n" + //
        //                 "(((((((((((" + ANSI_RESET + "////////////////" + ANSI_PURPLE + "#%%%%%%#(((##%%#%%#((/((/**((((////(/(((((##" + ANSI_RESET + "//////////////////////"  + ANSI_WHITE + "(((((((\n" + //
        //                 "((((((((" + ANSI_RESET + "///////////////////" + ANSI_PURPLE + "%%%%%%%%##(####%####(#(((((((((/////((####(" + ANSI_RESET + "///////////////////////////" + ANSI_WHITE + "(((\n" + //
        //                 "((((((" + ANSI_RESET + "///////////////////" + ANSI_PURPLE + "#&@%%&%%%%%%%########((((//////((////((####" + ANSI_RESET + "////////////////////////////////\n" + //
        //                 "(((((" + ANSI_RESET + "//////////////////" + ANSI_CYAN + "*" + ANSI_PURPLE + "(&@%%%%%&%%%%%#######(((/(//*/////(((######" + ANSI_RESET + "/////////////////////////////////\n" + //
        //                 "(((" + ANSI_RESET + "/////////////////" + ANSI_CYAN + "*//////" + ANSI_PURPLE + "@%%%&&&&&&%%%%%%###((((/////(((((#######(," + ANSI_RESET + "///////////////////////////////\n" + //
        //                 "/(" + ANSI_RESET + "/////////////////" + ANSI_CYAN + "**/////*/" + ANSI_PURPLE + "&&%%%&&&&&&&%%%%%%######((((#%%%####(#(#*" + ANSI_CYAN + "#(#*,,*****/" + ANSI_RESET + "///////////////////\n" + //
        //                 "" + ANSI_RESET + "//////////////////" + ANSI_CYAN + "******///**" + ANSI_PURPLE + "(&%%%%%&&&&&&&&%%%%%%%%%%%%%#####((((" + ANSI_CYAN + "#&*,,,,*/**/***" + ANSI_RESET + "///////////////////\n" + //
        //                 "" + ANSI_RESET + "/////////////" + ANSI_CYAN + "***/,*,******///*," + ANSI_PURPLE + "#&%%%%%%%%%&&&&&&&&%&&%%%%####(((((" + ANSI_CYAN + "%#/,,,,,,,,,********/**" + ANSI_RESET + "///////////\n" + //
        //                 "" + ANSI_RESET + "////////" + ANSI_CYAN + "*,**,,,*//,,,,,*****///*," + ANSI_PURPLE + "#%#######################(((((((" + ANSI_CYAN + "#&(/,,,,,,,,,,,,,,********" + ANSI_RESET + "/////////\n" + //
        //                 "" + ANSI_RESET + "///" + ANSI_CYAN + "*,**,,,,*,,**,(,,,,,,,,,,***//*," + ANSI_PURPLE + "/%###((#(###(((((((((((((((/" + ANSI_CYAN + "/#&((/,,,,.,,,,,,,,,,,,,,*/****" + ANSI_RESET + "//////\n" + //
        //                 "" + ANSI_CYAN + "**,,,,****,,,,**,,(.*,,,,,,,,,,,***/," + ANSI_PURPLE + "./#((((((/(((/////((((//(" + ANSI_CYAN + "/%%/##/,,,,,,,,,.,,,,,,,,,,**,,,*//*//\n" + //
        //                 "*******,**,,*,,,,,//.,,,,,,,,,,,,,,,,.." + ANSI_PURPLE + "*/(#((((((((((///(//((" + ANSI_CYAN + "/%#(#(#(*.,,,,,,,,,,,,,,,,,,,,,,,,***,,\n" + //
        //                 "****,****,,,,******#.,,,,,,,,,,..,/##%*(%" + ANSI_PURPLE + "@@%,*,(/////////////" + ANSI_CYAN + "%%#(##(/&&@@.,,,,,,,,,,,,,,,,,,,,,,,,,,\n" + //
        //                 "********,,*,,,,***/#####((((#((/*****/(((" + ANSI_PURPLE + "(##*,*,,,//*******" + ANSI_CYAN + "///##/##/**/(#%&&#((#(*,,,,,,,,,,,,,,,,,,\n" + //
        //                 "**,,,,,,,,,,,,,*,************,********,*(,,((" + ANSI_PURPLE + "######/*****" + ANSI_CYAN + "/((#/#(//*,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n" + //
        //                 ",,,,,,,,,,,,,,,,,,,*,,,,,,,,,,,,,,*,***,*,/*((//*//((**(((((*,,**,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n" + //
        //                 "*,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,/(///((/#%.*((/***,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n" + //
        //                 ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,(//" + ANSI_CYAN + "(#%.,..,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n" + //
        //                 ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,(#( &/..,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n" + //
        //                 ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,*&,*@&*.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n" + //
        //                 ",,,,,,*,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,&*..,...,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n" + //
        //                 ",,,,,,,*,,,,,,,,,,,,,,,,,,,,,,*,,,,,,,,,,,,,,,**,,&*..,,..,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n" + //
        //                 ",,,,,,,**,,,,,,,,,,,,,*,,,,,,,,,,,,,,,,,,,,,,,,,,,&*,..,,.,*,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n" + //
        //                 ",,,,,,***,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,&*,,,,,..,*,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,*\n\n");
        System.out.println(ANSI_PURPLE + "------------------------------------------Welcome to Chat--------------------------------------\n\n" + ANSI_RESET);
        runClient();
    }//end main


    /**
     * This method runs a client instance 
     */
    private static void runClient(){

        Scanner reader = new Scanner(System.in);
        CmdMsg cmdMsg;
        try{
            while(true) {
                //INPUT PARSING
                if(listenerEnabled == false){
                    System.out.print(lineChar);
                }
                String inp = reader.nextLine().trim();
                String command = "chat";
                String message = null;
                if (!inp.isEmpty()) { //if the user gave input (didn't just press enter)
                    if (inp.charAt(0) == '/') { //if the input contains a command
                        if(inp.contains(" ")){ //if the input contains a space, it might have an argument
                            int space = inp.indexOf(" ");
                            if (inp.length() < space + 1) { //if it has no argument
                                command = inp.substring(0, space).toLowerCase();
                            } else { //if it has an argument
                                command = inp.substring(0, space).toLowerCase();
                                message = inp.substring(space + 1, inp.length());
                            }
                        } else { //if the input contains no spaces=
                            command = inp;
                        }
                    } else { //if the input is not a command
                        message = inp;
                    }
    
                    //FOR DEBUGGING
                    //System.out.println("\ncommand: " + command);
                    //System.out.println("message: " + message + "\n");

                    //CREATING OBJECT
                    cmdMsg =  new CmdMsg(command, message);
    
                    //COMMAND HANDLING
                    try{
                        switch (command) {
                            case "/connect":
                                handleConnect(cmdMsg);
                                break;
                            case "/nick":
                                handleNick(cmdMsg);
                                break;
                            case "/list":
                                handleList(cmdMsg);
                                break;
                            case "/join":
                                handleJoin(cmdMsg);
                                break;
                            case "/leave":
                                handleLeave(cmdMsg);
                                break;
                            case "/quit":
                                handleQuit(cmdMsg);
                                in = null;
                                break;
                            case "/help":
                                handleHelp();
                                break;
                            case "/stats":
                                handleStats(cmdMsg);
                                break;
                            case "chat":
                                if(in == null){
                                    System.out.println(ANSI_RED + "Please connect to a server first to chat" + ANSI_RESET);
                                    break;
                                } else
                                    handleChat(cmdMsg);
                                    break;
                            default:
                                System.out.println(ANSI_RED + "No such command. Use /help to list all available commands" + ANSI_RESET);
                                if(listenerEnabled == true){
                                    System.out.print(lineChar);
                                }
                                break;
                        }
                    }catch(UnknownHostException u){
                        System.out.println(ANSI_RED + "No server of such name please try again" + ANSI_RESET);
                    }
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            System.err.println(ANSI_RED + "Error in runClient()\nProgram Terminated.\n" + ANSI_RESET);
        } finally{
            reader.close();
        }
    }


    //COMMAND HANDLING METHODS
    /**
     * This method handles when the user tries to connect to the server
     * @param cmdMsg
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    private static void handleConnect(CmdMsg cmdMsg) throws IOException, ClassNotFoundException, InterruptedException{
        try {
            
            if(cmdMsg.getMessage() == null){
                System.out.println(ANSI_RED +"No server name specified. Please connect using /connect <name>\n" + ANSI_RESET);
                return;
            } else if(in != null){
                System.out.println(ANSI_RED +"Already connected to a server.\n" + ANSI_RESET);
                System.out.print(lineChar);
                return;
            }
            server = new Socket(host, Integer.parseInt(cmdMsg.getMessage()));
            out = new ObjectOutputStream(server.getOutputStream());
            in = new ObjectInputStream(server.getInputStream());

            //Start server listener after initial response
            serverListener = new Listener(in);
            serverListener.start(); //Listen as soon as connection is established

            out.writeObject(cmdMsg);
            out.flush();
            //System.out.println("Please set a nickname.");
        } catch (Exception e) {
            System.out.println(ANSI_RED +"No server connection found\n" + ANSI_RESET);
            if(serverListener == null)
                listenerEnabled = false;            
        }
    } 

    /**
     * This method handles when a user tries to update or change thier nickname
     * @param cmdMsg
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleNick(CmdMsg cmdMsg) throws IOException, ClassNotFoundException{ 
        NicknameValidation result = validateNickname(cmdMsg, nickname);
        
        switch (result) { //Switch to handle what type of nickname has been passed, basically a validity check for edge cases
            case NO_CONNECTION:
                System.out.println(ANSI_RED + "Cannot change nickname, not connected to server. Please connect use /connect <name> first\n" + ANSI_RESET);
                break;
            case NULL_OR_EMPTY:
                System.out.println(ANSI_RED + "Nickname would be null. Cannot change, please input /nick <name>\n" + ANSI_RESET);
                System.out.print(lineChar);
                break;
            case BAD_CHARACTERS:
                System.out.println(ANSI_RED + "This has a bad character.");
                System.out.println("Bad characters are tab, newline, /, \\");
                System.out.println("Please input a valid name using /nick <name>\n" + ANSI_RESET);
                break;
            case SAME_NICKNAME:
                System.out.println(ANSI_RED + "Nickname is the same, cannot change\n"  + ANSI_RESET);
                System.out.print(lineChar);
                break;
            case VALID:
                nickname = cmdMsg.getMessage().trim(); //Trims off any empty characters
                cmdMsg.updateNickname(nickname); //Updates to propper nickname
                out.writeObject(cmdMsg);
                out.flush();
                break;
            default:
                System.out.println(ANSI_RED + "Hmmmmm something unexpected happened\n" + ANSI_RESET);
                break;
        }
    }

    /**
     * This method handles when a client wants a list of rooms in the server
     * @param cmdMsg
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    private static void handleList(CmdMsg cmdMsg) throws IOException, ClassNotFoundException, InterruptedException{
        try {
            out.writeObject(cmdMsg);
            out.flush();
        } catch (Exception leaveException) {
            System.out.println(ANSI_RED + "Cannot list channels, not connected to server. Please connect use /connect <name> first\n" + ANSI_RESET);
        }
    }

    /**
     * This method handles when a client is joining a room
     * @param cmdMsg
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleJoin(CmdMsg cmdMsg) throws IOException, ClassNotFoundException{
        try {
            out.writeObject(cmdMsg);
            out.flush();

        } catch (Exception leaveException) {
            System.out.println(ANSI_RED + "Cannot join any channels, not connected to server. Please connect use /connect <name> first\n" + ANSI_RESET);
            currentChannel = null;
        }
    }

    /**
     * This method handles when a client attempts to leave a channel
     * @param cmdMsg
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void handleLeave(CmdMsg cmdMsg) throws IOException, ClassNotFoundException{
        try {
            currentChannel = null; //Resets the current channel
            out.writeObject(cmdMsg);
            out.flush();
            //System.out.println("Client sent /leave to server!");
        } catch (Exception leaveException) {
            System.out.println(ANSI_RED + "Cannot leave any channels, not connected to server. Please connect use /connect <name> first\n" + ANSI_RESET);
        }
    }

    /**
     * This method handles when a user uses the /quit command
     * @param cmdMsg
     * @throws IOException
     * @throws InterruptedException
     */
    private static void handleQuit(CmdMsg cmdMsg) throws IOException, InterruptedException{
        try {
            out.writeObject(cmdMsg);
            out.flush();

        } catch (Exception e) {
            if(in == null){
                System.out.println(ANSI_RED + "Cannot leave any channels, not connected to server. Please connect use /connect <name> first\n" + ANSI_RESET);
            } else {
                System.out.println(ANSI_GREEN + "You have disconnedted from the channel and server.");
                System.out.println("Thanks for using Amit chat™! Hope to see you again!\n" + ANSI_RESET);
            }
        }

    }

    /**
     * Handles the /help command. This is fully client side
     */
    private static void handleHelp() {
        System.out.println(ANSI_GREEN + "-----------Chat Server Commands-----------\n\n");
        System.out.println("/connect <server-name>: Connects you to a named server\n");
        System.out.println("/nick <name>: Sets your nickname (can only be set when connected to server)\n");
        System.out.println("/list: Lists the channels and number of users in channels (can only be used when connected to server)\n");
        System.out.println("/join <channel>: Joins a chosen channel for you to chat in (can only be used when connected to server)\n");
        System.out.println("/leave <channel>: Leaves the channel (can only be used when connected to server)\n");
        System.out.println("/quit: Leaves the chat and disconnects from server (can only be used when connected to server)\n");
        System.out.println("/help: Prints out the help message (THATS ME :) )\n");
        System.out.println("/stats: Prints out some server stats for curious minds\n\n" + ANSI_RESET);
        if(listenerEnabled == true){
            System.out.print(lineChar);
        }
    }

    /**
     * This method handles the /Stats command
     * @param cmdMsg
     * @throws IOException
     */
    private static void handleStats(CmdMsg cmdMsg) throws IOException{
        try {
            out.writeObject(cmdMsg);
            out.flush();

        } catch (Exception e) {
            System.out.println(ANSI_RED + "Cannot print server stats, not connected to server. Please connect use /connect <name> first\n" + ANSI_RESET);
        }
    }

    /**
     * This method handles when the user wants to chat
     * @param cmdMsg
     * @throws IOException
     */
    private static void handleChat(CmdMsg cmdMsg) throws IOException{
        if(out == null || in == null || server == null){ //Checks if connection is established and prints message if it is
            System.out.println(ANSI_RED + "Talking to yourself are we?");
            System.out.println("No server connected please connect using /connect <name>\n" + ANSI_RESET);
            return;
        }
        out.writeObject(cmdMsg);
        out.flush();
        System.out.print(lineChar);
    }

    //UTILITY METHODS

    /**
     * This method extracts the nickname for the user and assigns it to a nickname type to decide
     * how the nickname should be handled when it is passed to the handleNick() method
     * @param cmdMsg
     * @param currentNickname
     * @return
     */
    public static NicknameValidation validateNickname(CmdMsg cmdMsg, String currentNickname) {
        if(out == null || in == null){ //Is there a connected server
            return NicknameValidation.NO_CONNECTION;
        }else if (cmdMsg.getMessage() == null || cmdMsg.getMessage().isEmpty()) { //If there is no nickname after /nick
            return NicknameValidation.NULL_OR_EMPTY;
        } else if (cmdMsg.checkBadNickChars(cmdMsg.getMessage())) { //If there is bad characters in a nickname
            return NicknameValidation.BAD_CHARACTERS;
        } else if (cmdMsg.getMessage().equals(currentNickname)) { //If the users nickname is the same as what they have now
            return NicknameValidation.SAME_NICKNAME;
        } else {
            return NicknameValidation.VALID; //The user is valid
        }
    }

    /**
     * This is the Listener class that waits to hear if the server has sent a message and then displays it
     * Contains some internal backend commands for differentiation
     */
    public static class Listener extends Thread{
        private ObjectInputStream inListener;

        /**
         * Listener constructor
         * @param inListener
         */
        public Listener(ObjectInputStream inListener){
            this.inListener = in;
        }

        /**
         * The run method for the listener
         */
        @Override
        public void run() {
            listenerEnabled = true;
            try {
                while (true) {
                    synchronized (inListener) { //Only one thread can use this at a time
                        Object obj = inListener.readObject();
                        if (obj instanceof CmdMsg) {
                            CmdMsg message = (CmdMsg) obj; //Creates new local CmdMsg
                            if(message.getCommand().equals("/quit")){ //Handles server quit when /quit is used
                                System.out.println("\n");
                                System.out.print(ANSI_GREEN + "You have quit.\n");
                                System.out.println(message.getMessage() + "\n" + ANSI_RESET);
                                //serverListener.join();
                                listenerEnabled = false;
                                server.close();
                                System.out.print(lineChar);
                                break;
                            } else if(message.getCommand().equals("/shut")){ //Handles when the server shutsdown
                                in = null;
                                System.out.println("\n");
                                System.out.println(ANSI_WHITE + lineChar + message.getMessage() + ANSI_RESET);
                                listenerEnabled = false;
                                server.close();
                                System.out.print(lineChar);
                                break;
                            }else if (message.getCommand().equals("/print")){ //regular print event from server 
                                System.out.println(ANSI_GREEN + message.getMessage() + ANSI_RESET);
                                System.out.print(lineChar);

                            }
                        }
                    }
                }
            } catch(StreamCorruptedException s){
                System.out.print(ANSI_RED + "Listener has crashed...");
                serverListener.start();
                System.out.println(ANSI_RED + "Listener restarting");
            }catch(EOFException eof){
                System.out.println(ANSI_RED + "Server has been disconnected" + ANSI_RESET);
                System.out.print(lineChar);
            }catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

} //END CLASS


