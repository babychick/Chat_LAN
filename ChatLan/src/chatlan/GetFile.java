package chatlan;

import chatlan.GUI.ClientForm;
import chatlan.GUI.ServerForm;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author philx
 */
public class GetFile implements Runnable {
    public String _path = "";
    public ServerSocket _server;
    public Socket _socket;
    public InputStream _IS;
    public FileOutputStream _FOS;
    public int _port;
    public ClientForm _form;
    public ServerForm _sv;
    
    public GetFile (String path, ClientForm form)
    {
        try {
            _server = new ServerSocket(0);
            _port = _server.getLocalPort();
            _path = path;
            _form = form;
            
        } catch (IOException ex) {
            Logger.getLogger(GetFile.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    public GetFile (String path, ServerForm form)
    {
        try {
            _server = new ServerSocket(0);
            _port = _server.getLocalPort();
            _path = path;
            _sv = form;
            
        } catch (IOException ex) {
            Logger.getLogger(GetFile.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    @Override
    public void run ()
    {
        try {
            _socket = _server.accept();
            _IS = _socket.getInputStream();
            _FOS = new FileOutputStream(_path);
            
            byte[] _array = new byte[10*1024];
            int count;
            
            while ((count = _IS.read(_array)) >= 0)
                _FOS.write(_array, 0, count);
            
            _FOS.flush();
            
            if (_IS != null) _IS.close();
            if (_FOS != null) _FOS.close();
            if (_socket != null) _socket.close();
            
        } catch (IOException ex) {
            Logger.getLogger(GetFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
