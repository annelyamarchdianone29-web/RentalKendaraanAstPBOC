package rental.controller;

import java.sql.Statement;
import java.sql.ResultSet;

import rental.model.Member;

public class MemberController {

    public Statement stm;
    public ResultSet res;
    public String sql;

    public Member memberLogin;

    //Konstruktor
    public MemberController() {
        //auto-Connect
        Koneksi db = new Koneksi();
        db.config();
        stm = db.stm;
    }

    //Method cek login  
    public boolean cekLogin(String un, String pw) {
        Member mc = new Member();
        mc.setUsername(un);
        mc.setPassword(pw);
        boolean status = false;
        try {
            //Query
            sql = "SELECT * FROM m_user where username='" + mc.getUsername() + "' AND password ='" + mc.getPassword() + "' " + "AND role = 'member' AND is_active = 0";

            //jalankan querynya
            res = stm.executeQuery(sql);
            //khusus SELECT gunakan perintah executeQuery

            //Pengecekan
            if (res.next()) { //jika dapat data
                mc.setId(res.getInt("id"));
                mc.setNama(res.getString("nama"));
                mc.setNoHp(res.getString("no_hp"));
                mc.setRole(res.getString("role"));
                mc.setPassword(null);
                memberLogin = mc;
                status = true;
            } else { //jika tidak dapat data
                status = false;
            }
        } catch (Exception e) {
            System.out.println("Query Gagal...");
        }
        return status;
    }

    //untuk mengecek apakah usn duplikat
    public boolean cekUsername(String un) {
        boolean ada = false;
        try {
            sql = "SELECT * FROM m_user WHERE username='" + un + "'";
            res = stm.executeQuery(sql);

            if (res.next()) {
                ada = true;
            }
        } catch (Exception e) {
            System.out.println("Query Gagal...");
        }
        return ada;
    }

    public boolean daftar(Member m) {
        boolean status = false;
        try {
            sql = "INSERT INTO m_user (username, password, nama, no_hp, role, is_active) VALUES ('" + m.getUsername() + "','" + m.getPassword() + "','" + m.getNama() + "','" + m.getNoHp() + "','member',0)";
            stm.executeUpdate(sql);
            status = true;
        } catch (Exception e) {
            System.out.println("Query Gagal...");
        }
        return status;
    }
}
