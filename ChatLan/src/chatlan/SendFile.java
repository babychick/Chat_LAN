package chatlan;

import chatlan.GUI.ClientForm;
import chatlan.GUI.ServerForm;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author philx
 */
public class SendFile implements Runnable {
    private File _f;
    private Socket _socket;
    private String _address;
    private FileInputStream _FIS;
    private OutputStream _OS;
    private int _port;
    private ClientForm _form;
    private ServerForm _sv;
    
    public SendFile (int port, ClientForm form, File f)
    {
        super();
        try {
            _f = f;
            _form = form;
            _socket = new Socket ("localhost",port);
            _OS = _socket.getOutputStream();
            _FIS = new FileInputStream(f);
        } catch (IOException ex) {
            Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public SendFile (int port, ServerForm form, File f)
    {
        super();
        try {
            _f = f;
            _sv = form;
            _socket = new Socket ("localhost",port);
            _OS = _socket.getOutputStream();
            _FIS = new FileInputStream(f);
        } catch (IOException ex) {
            Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run ()
    {
        try {
            byte[] _array = new byte[10*1024];
            int count;
            
            while ((count = _FIS.read(_array)) >= 0)
                _OS.write(_array, 0, count);
            
            _OS.flush();
            
            if (_OS != null ) _OS.close();
            if (_FIS != null) _FIS.close();
            if (_socket != null) _socket.close();
        } catch (IOException ex) {
            Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
