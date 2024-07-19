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
	//"net"
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
	var cmdMsg cmdmsg.CmdMsg
	var scanner *bufio.Reader = bufio.NewReader(os.Stdin) //Creates a new pointer to a bufio reader type and then assigns it to a reader that reads from the OS's stdin
	for {                                                 //Go's version of a while (true)
		//BEGIN INPUT PARSING
		if !listenerEnabled {
			fmt.Print(linechar)
		}
		var inp string
		var scanErr error
		inp, scanErr = scanner.ReadString('\n') //Gets user input
		if scanErr != nil {
			log.Fatalln("Input error")
		}
		inp = strings.TrimSpace(inp) //Trims whitespace
		var command string = "chat"
		var message string
		if inp != "" { //If the user gave input
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
	if index < len(splitCM) {              //Checks for index out of bounds
		//fmt.Println(splitCM[index])
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
	panic("unimplemented")
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
