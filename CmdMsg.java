import java.io.Serializable;

/**
* Object to contain command and messages
* @author Alex Lewtschuk, Kai Sorensen
*/
public class CmdMsg extends Object implements Serializable {
    String cmd;
    String msg;
    String event;

    /**
     * Constructor 
     * @param command
     * @param messsage
     */
    public CmdMsg(String command, String messsage){
        this.cmd = command;
        this.msg = messsage;
    }

    // /**
    //  * Constructor for an event if we needed it 
    //  * @param event
    //  */
    // public CmdMsg(String event){
    //     this.msg = event;
    // }


    /**
     * This updates the objects contents
     * @param updateCmd
     * @param updateMsg
     */
    public void updateContents(String updateCmd, String updateMsg){
        this.cmd = updateCmd;
        this.msg = updateMsg;
    }

    /**
     * Alows modification of nickname
     * @param trimmedNick
     */
    public void updateNickname(String trimmedNick){
        this.msg = trimmedNick;
    }

    /**
     * Gets the command part of the object
     * @return
     */
    public String getCommand(){
        return cmd;
    }

    /**
     * Gets the message part of the object
     * @return
     */
    public String getMessage(){
        return msg;
    }

    /**
     * Gets the possible event in the object
     * @return
     */
    public String getEvent(){
        return msg;
    }

    @Override
    public String toString(){
        return "Object contains command: " + cmd + " argument: " + msg;
    }

    /**
     * Checks if the nickncame in the object has invalid characters 
     * @param msg
     * @return
     */
    public boolean checkBadNickChars(String msg) {
        final Character[] specialChars = {'\t', '\n', '/', '\\'};
        if(msg == null || msg.isEmpty()){
            return true;
        }
        for(int i = 0; i < specialChars.length; i++){
            for(int j = 0; j < msg.length(); j++){
                char c = msg.charAt(j);
                if(c == specialChars[i]){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the first character is blank. Used as an edge case check
     * @param msg
     * @return
     */
    public boolean firstNickCharIsSpace(String msg){
        if(msg.startsWith(" ")){
            System.out.println("The first character is a space. Please reenter your name\n");
            return true;
        }
        return false;
    }
}
