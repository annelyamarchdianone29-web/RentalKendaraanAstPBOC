
package rental.controller;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

public class Koneksi {

    public static Connection con;
    public static Statement stm;

    //Method untuk koneksi 
    public void config() {
        try {
            //variabel koneksi
            String url = "jdbc:mysql://localhost/db_rental";
            String user = "root";
            String pass = "";
            Class.forName("com.mysql.cj.jdbc.Driver");

            //Membuat String Connection
            con = DriverManager.getConnection(url, user, pass);
            
            //Membuat SQL Statement
            stm = (Statement)con.createStatement();
            System.out.println("Koneksi Berhasil...");
        } catch (Exception e) {
            System.out.println("Koneksi Gagal...");
        }
    }
}
