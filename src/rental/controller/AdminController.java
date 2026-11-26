package rental.controller;

import java.sql.Statement;
import java.sql.ResultSet;

import rental.model.Admin;

public class AdminController {

    public Statement stm;
    public ResultSet res;
    public String sql;
    
    public Admin adminLogin;
    
    //Konstruktor
    public AdminController() {
        //auto-Connect
        Koneksi db = new Koneksi();
        db.config();
        stm = db.stm;
    }

    //Method cek login  
    public boolean cekLogin(String un, String pw) {
        Admin adm = new Admin();
        adm.setUsername(un);
        adm.setPassword(pw);
        boolean status = false;
        try {
            //Query
            sql = "SELECT * FROM m_user where username='" + adm.getUsername() + "' AND password ='" + adm.getPassword() + "' " + "AND role = 'admin' AND is_active = 0";

            //jalankan querynya
            res = stm.executeQuery(sql);
            //khusus SELECT gunakan perintah executeQuery

            //Pengecekan
            if (res.next()) { //jika dapat data
                adm.setId(res.getInt("id"));
                adm.setNama(res.getString("nama"));
                adm.setNoHp(res.getString("no_hp"));
                adm.setRole(res.getString("role"));
                adm.setPassword(null); 
                adminLogin = adm;
                status = true;
            } else { //jika tidak dapat data
                status = false;
            }
        } catch (Exception e) {
            System.out.println("Query Gagal...");
        }
        return status;
    }
}
