/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Reuse;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author Kieu Nhut Truong
 */
public class Library {

    //Database
    static private String passwordDB;
    static private String userDB;
    static private String computerName;
    static private String databaseName = computerName = userDB = passwordDB = "";

    public static Connection connector() {

        Connection con = null;//bien tra ve
        //lay du lieu tu file database info
        {
            try {
                File f = new File("database/Database.txt");
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                computerName = br.readLine();
                databaseName = br.readLine();
                userDB = br.readLine();
                passwordDB = br.readLine();
            } catch (Exception e) {

            }
        }
        // tao connection
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            if (computerName.equals("") || userDB.equals("") || computerName.equals("") || databaseName.equals("")) {
                GetBatabaseString f = new GetBatabaseString(null, true);
                f.setVisible(true);
            } else {
                String stringKetNoi = "jdbc:sqlserver://" + computerName + "\\SQLEXPRESS:1433;databaseName=" + databaseName + ";user=" + userDB + ";password=" + passwordDB;
                con = DriverManager.getConnection(stringKetNoi);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connect fail!!!");
            //update lai file database info
            int i = JOptionPane.showConfirmDialog(null, "Do you want update database information ?");
            if (i == 0) {
                GetBatabaseString f = new GetBatabaseString(null, true);
                f.setVisible(true);
            }
        }
        return con;
    }

    public static ImageIcon FitImageSize(String imagePath, int width, int height) {
        ImageIcon imgicon = new ImageIcon(imagePath);
        Image img = imgicon.getImage();
        Image img2 = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon imgicon2 = new ImageIcon(img2);
        return imgicon2;
    }
//    private void btnTimActionPerformed(java.awt.event.ActionEvent evt) {                                       
//        JFileChooser file = new JFileChooser();
//        file.setCurrentDirectory(new File(System.getProperty("user.home")));
//       
//        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.images", "jpg","png");
//        file.addChoosableFileFilter(filter);
//        int result = file.showSaveDialog(null);
//        if(result == JFileChooser.APPROVE_OPTION)
//        {
//            File selectedFile = file.getSelectedFile();
//            String path = selectedFile.getAbsolutePath();
//            
//            lbAnh.setIcon(ResizeImage(path, null));
//            
//            ImgPath = path;
//            txtDD.setText(path);
//
//        }
//        else{
//            System.out.println("Không tìm được ảnh");
//        }
//        
//    }
    public static boolean Existed(String table, String where, String value)
    {
        boolean yes=false;
        try {
            Statement st = connector().createStatement();
            ResultSet r = st.executeQuery("select * from "+table+" where "+where+"='"+value+"'");
            if(r.next())
            {
                yes=true;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
        }
        return yes;
    }


    

}
