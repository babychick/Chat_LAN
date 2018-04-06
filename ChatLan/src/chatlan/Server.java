package chatlan;

import chatlan.GUI.LoginForm;
import chatlan.GUI.ServerForm;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author philx
 */
public class Server implements Runnable{
    public ServerForm _form;
    public ServerSocket _server = null;
    public clientThread[] _clients;
    public Thread _thread = null;
    public int _nlogin = 0;
    public int _count = 0;
    public int _port = 1996;
    public int ID = -1;
    String realName = null;
    
    public Server (ServerForm form)
    {
        _clients = new clientThread[50];
        _form = form;        
        try {
            _server = new ServerSocket(_port);
            _port = _server.getLocalPort();
            _form.taDialog.append("IP: " + InetAddress.getLocalHost() + "\n");
            _form.taDialog.append("Port: " + _server.getLocalPort() + "\n");
            _form.taDialog.append("Waiting for a connection...\n");
            start();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run()
    {
        while (_thread != null)
        {            
            try {              
                addUser(_server.accept());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void start ()
    {
        if (_thread == null)
        {
            _thread = new Thread(this);
            _thread.start();
        }
    }
    
    public void stop(){  
        if (_thread != null){  
            _thread.stop(); 
	    _thread = null;
	}
    }
    
    public int findClient (int ID)
    {
        for (int i = 0; i < _count; i++)
        {
            if (_clients[i].getID() == ID)
                return i;
        }
        return -1;
    }
    
    public clientThread findUser (String userName)
    {
        for (int i = 0; i < _count; i++)
            if (_clients[i]._userName.equals(userName))
                return _clients[i];
        
        return null;
    }
    
    public void sendUserList (String name)
    {
        for (int i = 0; i < _count; i++)
            findUser(name).send(new Message("new", "Server",_clients[i]._userName, name));
    }
    
    public void addUser (Socket socket)
    {
        if (_count < _clients.length)
        {
            _clients[_count] = new clientThread (this, socket);
            _clients[_count].openThread();
            _clients[_count].start();
            _form.taDialog.append("Someone is logging in\nChecking...\n");
            _count++;
        }
    }
    public void sendAll (String type, String sender, String content)
    {
        for (int i = 0; i < _count; i++)
            _clients[i].send(new Message(type, sender, content, "All"));
    }
    
    public synchronized void removeClient (int ID)
    {
        int cli = findClient(ID);
        
        if (cli >= 0)
        {
            clientThread end = _clients[cli];
            if (!end._userName.equals(""))
                _form.taDialog.append("Removed ID " + ID + " as " + end._userName + "\n");
            else _form.taDialog.append("failed!");
            if (cli < _count - 1)
            {
                
                for (int i = cli+1; i < _count; i++)
                    _clients[i-1] = _clients[i];
            }
            _count--;
            end.closeThread();
        }
    }
    
    public synchronized void Handling (int ID, Message msg)
    {        
        String t = msg._type;
        if (t.equals("message"))
        {
            if (msg._receiver.equals("All"))
                sendAll(t,msg._sender,msg._content);
            else
            {
                // send msg to receiver
                findUser(msg._receiver).send(new Message(msg._type, msg._sender, msg._content, msg._receiver));
                // send msg to itsself
                _clients[findClient(ID)].send(msg);
            }
        }
        
        if (t.equals("off"))
        {
            Logoff(msg._sender);
            sendAll("off","Server",msg._sender);
        }
        if (t.equals("login"))
        {
            int i = findClient(ID);
            int isCorrect = userCheck(msg._sender, msg._content);
            
            if (isCorrect == 1)
            {
                realName = selectName(msg._sender);
                _clients[i]._userName = realName;
                try {
                    _clients[i].send(new Message("login","Server",InetAddress.getLocalHost().toString(),realName));
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                sendAll("new","Server",realName);
                sendUserList(realName);
                _form.taDialog.append("Login successfully\n");
                _form.taDialog.append("Username: " + realName + "\n");
                _form.taDialog.append("ID: " + ID + "\n");
                _form.taDialog.append("Waiting for a connection...\n");
            }
            if (isCorrect == 0)
            {
                _clients[i].send(new Message("login","Server","0",realName));
                _form.taDialog.append("Login failed!\n");                    
            }
            if (isCorrect == -1)
            {
                _clients[i].send(new Message("login","Server","-1",realName));
                _form.taDialog.append("Login failed!\n");
            }
        }
        
        if (t.equals("sendFile"))
        {
            if (msg._receiver.equals("All"))
                sendAll("getFile",msg._sender,msg._content);
            else
                findUser(msg._receiver).send(new Message("getFile",msg._sender,msg._content,msg._receiver));
        }
        
        if (t.equals("yes"))
        {
            findUser(msg._receiver).send(new Message("yes",msg._sender,msg._content,msg._receiver));
        }
        
        if (t.equals("pass"))
        {
            changePass(msg._sender,msg._content);
        }
        
        if (t.equals("name"))
        {
            changeName(findUsername(msg._sender),msg._content);
            sendAll("name", msg._sender, msg._content);
        }
        
        if (t.equals("folder"))
        {
            int i = findClient(ID);
            showFolder(i);
        }
        
        if (t.equals("upload"))
        {
            JFileChooser jfc = new JFileChooser();
            jfc.setSelectedFile(new File(msg._content));
            String path = "E:\\Chat Client Server\\CommonFolder\\" + msg._content;
            if (path != null)
            {
                GetFile gf = new GetFile(path, _form);
                Thread get = new Thread(gf);
                get.start();
                findUser(msg._sender).send(new Message("yes","Server","" + gf._port,msg._sender));
            }
            saveFile(msg._sender,msg._content);
            _form.taDialog.append(msg._sender + " has uploaded a file to common folder.");
        }
                
        if (t.equals("download"))
        {
            findUser(msg._sender).send(new Message("download","Server",msg._content,msg._sender)); // _content is a file
        }
        
        if (t.equals("downloadnow"))
        {
            JFileChooser jfc = new JFileChooser();
            jfc.setSelectedFile(new File("E:\\Chat Client Server\\CommonFolder\\" + msg._receiver));
            int port = Integer.valueOf(msg._content);
            SendFile sf = new SendFile(port, _form, jfc.getSelectedFile());
            Thread send = new Thread(sf);
            send.start();
        }
    }
    
    public void saveFile (String un, String filename)
    {
        try {
            Connection con = Library.connectTO();
            PreparedStatement pre = con.prepareStatement("insert into Folder(Uploader, Filename) values (?,?)");
            pre.setString(1,un);
            pre.setString(2,filename);
            pre.execute();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void showFolder (int i)
    {
        try {
            Connection con = Library.connectTO();
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("select * from Folder");
            while (rs.next())
            {
                String uploader = rs.getString(1);
                String filename = rs.getString(2);
                _clients[i].send(new Message("folder",uploader,filename,""));
            }
            _clients[i].send((new Message("open","Server","openTable","")));
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void Logoff (String un)
    {
        try {
            Connection con = Library.connectTO();
            PreparedStatement pre = con.prepareStatement("update UserAccount set isLogin=? where Name='"+ un +"'");
            pre.setBoolean(1, false);
            pre.executeUpdate();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String findUsername (String n)
    {
        String un = "";
        try {
            Connection con = Library.connectTO();
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("select Username from UserAccount where Name = '"+ n +"'");
            if (rs.next())
                un = rs.getString(1);
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return un;
    }
    
    public void changeName (String un, String newname)
    {
        try {
            Connection con = Library.connectTO();
            PreparedStatement pre = con.prepareStatement("update UserAccount set Name=? where Username='"+ un +"'");
            pre.setString(1, newname);
            pre.executeUpdate();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void changePass (String un, String pass)
    {
        try {
            Connection con = Library.connectTO();
            PreparedStatement pre = con.prepareStatement("update UserAccount set Password=? where Name='"+ un +"'");
            pre.setString(1, pass);
            pre.executeUpdate();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int userCheck (String un, String pwd)
    {
        try {
            Connection con = Library.connectTO();
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("select Username, Password, isLogin from UserAccount");
            
            while (rs.next())
            {
                if (rs.getString(1).trim().equals(un) && rs.getString(2).trim().equals(pwd))
                {
                    if (rs.getBoolean(3) == true)
                        return 0; // Logging in
                    else
                        return 1; // can log in
                }
            }
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    public String selectName (String un)
    {
        String name = "";
        try {
            Connection con = Library.connectTO();
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("select Name from UserAccount where Username = '"+ un +"'");
            
            if (rs.next())
                name = rs.getString(1).trim();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return name;
    }
}

class clientThread extends Thread
{
    public Server _server = null;
    public Socket _socket = null;
    public int _ID = -1;
    public ObjectInputStream _OIS = null;
    public ObjectOutputStream _OOS = null;
    public ServerForm _form;
    public String _userName = "";
    
    public int getID() {
        return _ID;
    }
    
    public clientThread (Server server, Socket socket)
    {
        super();
        _server = server;
        _socket = socket;
        _ID = _socket.getPort();
        _form = server._form;
    }
    
    public void send (Message msg)
    {
        try {
            _OOS.writeObject(msg);
            _OOS.flush();
        } catch (IOException ex) {
            Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run ()
    {
        while (true)
        {
            try {
                Message msg = (Message) _OIS.readObject();
                _server.Handling(_ID, msg);
            } catch (IOException | ClassNotFoundException ex) {
                _server.removeClient(_ID);
                stop();
            }
        }
    }
    
    public void openThread ()
    {
        try {
            
            _OOS = new ObjectOutputStream(_socket.getOutputStream());
            _OIS = new ObjectInputStream(_socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void closeThread ()
    {
        try {
            if (_socket != null) _socket.close();
            if (_OIS != null) _OIS.close();
            if (_OOS != null) _OOS.close();
        } catch (IOException ex) {
            Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
