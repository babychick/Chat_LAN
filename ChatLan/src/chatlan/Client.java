package chatlan;

import chatlan.GUI.ClientForm;
import chatlan.GUI.Folder;
import chatlan.GUI.LoginForm;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author philx
 */
public class Client implements Runnable {
    public int _port;
    public Socket _socket;
    public ObjectInputStream _OIS;
    public ObjectOutputStream _OOS;
    public ClientForm _form;
    public LoginForm _lf;
    public String _address;
    public int row = 0;
    public String[] up = new String[20];
    public String[] filename = new String[20];
    
    public Client (ClientForm form) throws IOException
    {
        _form = form;
        _address = _form._address;
        _port = _form._port;
//        _socket = new Socket(InetAddress.getByName(_address), _port);        
//        _OOS = new ObjectOutputStream(_socket.getOutputStream());
//        _OIS = new ObjectInputStream(_socket.getInputStream());
    }
    
    public Client (LoginForm lf) throws IOException
    {
        _lf = lf;
        _address = _lf._address;
        _port = _lf._port;
        _socket = new Socket("localhost", _port);        
        _OOS = new ObjectOutputStream(_socket.getOutputStream());
        _OIS = new ObjectInputStream(_socket.getInputStream());
    }
    
    @Override
    @SuppressWarnings("empty-statement")
    public void run ()
    {
        boolean isRunning = true;
        while (isRunning)
        {
            try {
                Message msg = (Message) _OIS.readObject();
                String t = msg._type;
                if (t.equals("message"))
                {
                    if (msg._receiver.equals(_form._userName))
                        _form.taDialog.append("<<" + msg._sender + ">> to me: " + msg._content + "\n");
                    else
                    {
                        if (msg._sender.equals(_form._userName))
                            _form.taDialog.append("me to <<" + msg._receiver + ">>: " + msg._content + "\n");
                        else
                            _form.taDialog.append("<<" + msg._sender + ">> to <<" + msg._receiver + ">>: " + msg._content + "\n");
                    }
                }
                    
                if (t.equals("new"))
                {
                    if (!msg._content.equals(_form._userName))
                    {
                        _form.taDialog.append(msg._content + " has online\n");
                        _form.dlm.addElement(msg._content);
                    }
                }
                
                if (t.equals("off"))
                {
                    _form.taDialog.append(msg._content + " has offline\n");
                    for (int i = 0; i < _form.dlm.getSize(); i++)
                        if (_form.dlm.getElementAt(i).equals(msg._content))
                        {
                            _form.dlm.removeElementAt(i);
                            _form.lstUser.setSelectedIndex(0);
                        }
                }
                
                if (t.equals("login"))
                {
                    String x = msg._content;
                    
                    if (x.equals("0"))
                        _lf.lblError.setText("This account is logging in!");
                    else if (x.equals("-1"))
                        _lf.lblError.setText("Username or password is incorrect!");
                    else
                    {
                        _form = new ClientForm();
                        _form.setSocket(_socket);
                        _form.setAddress(msg._content);
                        _form.setPort(_lf.getPort());
                        _form.setUserName(msg._receiver);
                        _form.setClient(_lf.getLogin());
                        _form.setClientThread(_lf.getT());
                        _form.setOIS(_OIS);
                        _form.setOOS(_OOS);
                        _form._client.send(new Message ("new",_form._userName, _form._userName, "Server"));
                        _form.setVisible(true);
                        _lf.setVisible(false);
                    }
                }
                
                if (t.equals("getFile"))
                {
                    if (!msg._sender.equals(_form._userName))
                    {
                        String confirm = msg._sender + " send you a file: " + msg._content;
                        int isGet = JOptionPane.showConfirmDialog(_form, confirm);
                        if (isGet == 0)
                        {
                            JFileChooser jfc = new JFileChooser();
                            jfc.setSelectedFile(new File(msg._content));
                            int i = jfc.showSaveDialog(_form);
                            String path = jfc.getSelectedFile().getPath();
                            if (path != null && i == JFileChooser.APPROVE_OPTION)
                            {
                                GetFile gf = new GetFile(path, _form);
                                Thread get = new Thread(gf);
                                get.start();
                                send(new Message("yes",_form._userName,"" + gf._port,msg._sender));
                            }
                            _form.taDialog.append("Download complete.\n");
                        }
                    }
                }
                // upload
                if (t.equals("yes"))
                {
                    int port = Integer.valueOf(msg._content);
                    SendFile sf = new SendFile(port, _form, _form.f);
                    Thread send = new Thread(sf);
                    send.start();
                    _form.taDialog.append("Upload complete.\n");
                }
                
                if (t.equals("name"))
                {
                    if (!msg._sender.equals(_form._userName))
                    _form.taDialog.append(msg._sender + " has changed his name to " + msg._content);
                }
                // upload
                if (t.equals("folder"))
                {
                    up[row] = msg._sender;
                    filename[row] = msg._content;
                    row++;
                }
                // upload
                if (t.equals("open"))
                {
                    _form.fd = new Folder();
                    _form.fd.uploader = up;
                    _form.fd.filename = filename;
                    _form.fd.setUsername(_form._userName);
                    _form.fd.setVisible(true);
                    row = 0;
                }
                
                if (t.equals("download"))
                {
                    JFileChooser jfc = new JFileChooser();
                    jfc.setSelectedFile(new File(msg._content));
                    int i = jfc.showSaveDialog(_form);
                    String path = jfc.getSelectedFile().toString();
                    if (path != null && i == JFileChooser.APPROVE_OPTION)
                    {
                        GetFile gf = new GetFile(path, _form);
                        Thread get = new Thread(gf);
                        get.start();
                        send(new Message("downloadnow",_form._userName,"" + gf._port,msg._content)); // _content is a file
                    }
                    _form.taDialog.append("Download complete.");
                }
                
            } catch (IOException | ClassNotFoundException ex) {
                isRunning = false; 
            }
        }
    }
    
    public void send (Message msg)
    {
        try {
            _OOS.writeObject(msg);
            _OOS.flush();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void closeThread (Thread t){
        t = null;
    }
}
