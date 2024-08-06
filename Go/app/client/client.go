package main

import (
	"bufio"
	"bytes"
	"chatserver/cmdmsg"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"os"
	"strings"

	//"encoding"
	"net"
)

// Temp function to prevent unused import deletion on save
func importFormatPreventer() {
	fmt.Println("Imported fmt")
	err := errors.New("Import Error")
	errors.As(err, false)
	os.Exit(0)
	bytes.ErrTooLarge.Error()
}

const RESET string = "\u001B[0m"
const BLACK string = "\u001B[30m"
const RED string = "\u001B[31m"
const GREEN string = "\u001B[32m"
const YELLOW string = "\u001B[33m"
const BLUE string = "\u001B[34m"
const PURPLE string = "\u001B[35m"
const CYAN string = "\u001B[36m"
const WHITE string = "\u001B[37m"

var linechar string = GREEN + ">" + RESET
var nickname string
var server net.Conn

// Server info stored as constants
const (
	SERVERHOST = "localhost"
	SERVERTYPE = "tcp"
)

var listenerEnabled bool = false
var inConnect bool
var currentChannel string
var in *json.Decoder
var out *json.Encoder

var serverListener *Listener

type Listener struct{
	inListener = 
}

// Begin custom implementation of enum in Go as Go doesn't support enums
// Custom type to hold value for each of the nickname states
type NicknameState int

// Declaring related constants for nickname states
const (
	NO_CONNECTION NicknameState = iota + 1 //Enum index 1
	NULL_OR_EMPTY
	BAD_CHARACTERS
	SAME_NICKNAME
	VALID
) //END NICKNAME ENUM

/*
Runs the main method of the program
*/
func main() {
	fmt.Println(PURPLE + "------------------------------------------Chat Client (Go Implementation)------------------------------------------\n\n" + RESET)
	fmt.Println(PURPLE + "-------------------------------------------------Welcome to Chat---------------------------------------------------\n\n" + RESET)

	runClient()
}

/*
Runs the client and handles the user command parsing and switch statment for command handling
*/
func runClient() {
	var cmdMsg cmdmsg.CmdMsg                              //Declares new command message as a CmdMsg struct from the cmdmsg package
	var scanner *bufio.Reader = bufio.NewReader(os.Stdin) //Creates a new pointer to a bufio reader type and then assigns it to a reader that reads from the OS's stdin
	//Go's version of a while (true) below
	for {
		//BEGIN INPUT PARSING
		if !listenerEnabled { //Checks if the listener thread is not enabled and prints new line char if it is
			fmt.Print(linechar)
		}
		var inp string                          //Implicitly declares string that will store the user input
		var scanErr error                       //Implicitly declares error that could be returned by the scanner.ReadString() function
		inp, scanErr = scanner.ReadString('\n') //Gets user input by reading till the specified newline delimiter and assigning that to var inp string also assigns possible var scanErr error
		if scanErr != nil {                     //If scanErr is nil terminate program and give reason Input error
			log.Fatalln("Input error")
		}
		inp = strings.TrimSpace(inp) //Trims whitespace on imp string
		var command string = "chat"  //Sets default command string to chat
		var message string           //Implicitly declares var message string
		if inp != "" {               //If the user gave input and the input is not default value for strings in Go ""
			if runeAt(inp, 0) == '/' { //If input contains a command
				if strings.Contains(inp, " ") { // If input contains a space there may be an argument
					var space int = strings.Index(inp, " ")
					if len(inp) <= space { //If there are no arguments
						command = extractCommandMessage(inp, 0)
					} else { //If there is an argument
						command = extractCommandMessage(inp, 0)
						message = extractCommandMessage(inp, 1)
					}
				} else { //If input contains no space and is just a command
					command = inp
				}
			} else { //If the input is not a command and is just a message
				message = inp
			}
		}
		fmt.Println("\ncommand: " + command)
		fmt.Println("message: " + message + "\n")

		cmdMsg = cmdmsg.CmdMsg{Cmd: command, Msg: message}
		//fmt.Println(cmdmsg.ToString(&cmdMsg))

		switch command {
		case "/connect":
			handleConnect(cmdMsg)
		case "/nick":
			handleNick(cmdMsg)
		case "/list":
			handleList(cmdMsg)
		case "/join":
			handleJoin(cmdMsg)
		case "/leave":
			handleLeave(cmdMsg)
		case "/quit":
			handleQuit(cmdMsg)
		case "/help":
			handleHelp(cmdMsg)
		case "/stats":
			handleStats(cmdMsg)
		case "chat":
			if in == nil {
				fmt.Println(RED + "Please connect to a server first to chat" + RESET)
				break
			} else {
				handleChat(cmdMsg)
			}
		default:
			fmt.Println(RED + "No such command. Use /help to list all available commands" + RESET)
			if listenerEnabled {
				fmt.Println(linechar)
			}
		}
		//END INPUT PARSING
	}
} //END RUN CLIENT METHOD

//BEGIN UTILITY FUNCTIONS

// Returns the rune(char) at position index
func runeAt(str string, index int) rune {
	return []rune(str)[index] //Casts the string to a rune array and returns the rune at the index
} //END RUNEAT FUNCTION

// Extracts the specified substring after the input is split into two substrings by the first " " delimiter
func extractCommandMessage(str string, index int) string {
	splitCM := strings.SplitN(str, " ", 2) //Creates a new variable and implicitly assigns it to an array of strings made my by using SplitN to split the string into two substrings at the first " " character
	if index < len(splitCM) {              //Checks for index out of bounds in this case if the index is less than 2
		//fmt.Println(splitCM[index]) //Test prints the string at the index requested
		return strings.ToLower(splitCM[index]) //Sets to lowercase and returns the substring at the index position
	}
	return ""
} //END EXTRACTCOMMANDMESSAGE FUNCTION

//END UTILITY FUNCTIONS

//BEGIN COMMAND HANDLING METHODS

/*
This method handles when the user tries to connect to the server
*/
func handleConnect(cmdMsg cmdmsg.CmdMsg) {
	if cmdMsg.Msg == "" { //If message is empty then no connection will be created
		fmt.Println(RED + "No server name specified. Please connect using /connect <name>" + RESET)
		return
	} else if in != nil { //If the server in connection is not nil then print message about being connected to the server
		fmt.Println(RED + "Already connected to a server.\n" + RESET)
		fmt.Print(linechar)
		return
	}
	var connectErr error                                  //Declaring the error explicitly
	server, connectErr = net.Dial(SERVERHOST, cmdMsg.Msg) //Setting the server connection and value for possible net.Dial error return
	if connectErr != nil {                                //If net.Dial returns an error tell user no server is found
		fmt.Println(RED + "No server connection found." + RESET)
	}
	out = json.NewEncoder(server) //Declares out as a new json encoder and writes it to the server connection
	in = json.NewDecoder(server)  //Declares in as a new json decoder and reads from server connection

	go listener(server)
}

/*
This method handles when a user tries to update or change thier nickname
*/
func handleNick(cmdMsg cmdmsg.CmdMsg) {
	panic("unimplemented")
}

/*
This method handles when a user tries to update or change thier nickname
*/
func handleList(cmdMsg cmdmsg.CmdMsg) {
	panic("unimplemented")
}

/*
This method handles when a client is joining a room
*/
func handleJoin(cmdMsg cmdmsg.CmdMsg) {
	panic("unimplemented")
}

/*
This method handles when a client attempts to leave a channel
*/
func handleLeave(cmdMsg cmdmsg.CmdMsg) {
	panic("unimplemented")
}

/*
This method handles when a user uses the /quit command
*/
func handleQuit(cmdMsg cmdmsg.CmdMsg) {
	panic("unimplemented")
}

/*
Handles the /help command. This is fully client side
*/
func handleHelp(cmdMsg cmdmsg.CmdMsg) {
	panic("unimplemented")
}

/*
This method handles the /stats command
*/
func handleStats(cmdMsg cmdmsg.CmdMsg) {
	panic("unimplemented")
}

/*
This method handles when the user wants to chat
*/
func handleChat(cmdMsg cmdmsg.CmdMsg) {
	panic("unimplemented")
}
