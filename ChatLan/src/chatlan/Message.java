package chatlan;

import java.io.Serializable;

/**
 *
 * @author philx
 */
public class Message implements Serializable{
    
    /*
    4 types: userList, message, login, logoff
    */
    public String _type, _sender, _content, _receiver;
    
    public Message (String type, String sender, String content, String receiver)
    {
        _type = type;
        _sender = sender;
        _content = content;
        _receiver = receiver;
    }
}
