package cmdmsg

import (
	"fmt"
	"strings"
)

/*
Defines the struct for the command message that will be sent accross the network
*/
type CmdMsg struct {
	Cmd string `json:"cmd"`
	Msg string `jason:"msg"`
}

// Updates the struct's contents
func UpdateContents(cmdMsg *CmdMsg, updateCmd string, updateMsg string) {
	cmdMsg.Cmd = updateCmd
	cmdMsg.Msg = updateMsg
}

// Allows modification of the nickname
func UpdateNickname(cmdMsg *CmdMsg, trimmedNick string) {
	cmdMsg.Msg = trimmedNick
}

// func GetCommand(cmdMsg *CmdMsg) string {
// 	cmd := cmdMsg.Cmd
// 	return cmd
// }

// func GetMessage(cmdMsg *CmdMsg) string {
// 	msg := cmdMsg.Msg
// 	return msg
// }

func ToString(cmdmsg *CmdMsg) string {
	return "CmdMsg contains command " + cmdmsg.Cmd + " and argument: " + cmdmsg.Msg
}

func CheckBadNickChars(msg string) bool {

	if strings.HasPrefix(msg, " ") {
		fmt.Println("The first character is a space. Please reenter your name")
		return true
	}

	forbiddenChars := []rune{'\t', '\n', '/', '\\'}
	if msg == "" {
		return true
	}

	for i := 0; i < len(msg); i++ {
		for i2 := 0; i2 < len(forbiddenChars); i++ {
			runeAt := strings.IndexRune(msg, forbiddenChars[i2])
			if runeAt != -1 {
				return true
			}
		}
	}
	return false
}
