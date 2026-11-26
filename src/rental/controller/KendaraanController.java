package rental.controller;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

import rental.model.Kendaraan;

public class KendaraanController {

    public Statement stm;
    public ResultSet res;
    public String sql;

    // tabel untuk admin (semua kendaraan aktif)
    DefaultTableModel dtm = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    // tabel untuk member (kendaraan ready saja)
    DefaultTableModel dtmReady = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    public KendaraanController() {
        //auto-Connect
        Koneksi db = new Koneksi();
        db.config();
        stm = db.stm;
    }

    // method design tabel admin
    public DefaultTableModel creatTable() {
        if (dtm.getColumnCount() == 0) {
            dtm.addColumn("ID Kendaraan");
            dtm.addColumn("Kode Kendaraan");
            dtm.addColumn("Merk Kendaraan");
            dtm.addColumn("Jenis Kendaraan");
            dtm.addColumn("Harga Perhari");
            dtm.addColumn("Ketersedian");
            dtm.addColumn("Status");
        }
        return dtm;
    }

    // method design tabel member
    public DefaultTableModel creatTableReady() {
        if (dtmReady.getColumnCount() == 0) {
            dtmReady.addColumn("ID Kendaraan");
            dtmReady.addColumn("Kode Kendaraan");
            dtmReady.addColumn("Merk Kendaraan");
            dtmReady.addColumn("Jenis Kendaraan");
            dtmReady.addColumn("Harga Perhari");
        }
        return dtmReady;
    }

    //query menampilakn data member (SELECT)
    public void tampilkanKendaraan() {
        try {
            //clear table sementara dtm
            dtm.getDataVector().removeAllElements();
            dtm.fireTableDataChanged();
            //query
            sql = "SELECT * FROM m_kendaraan ORDER BY id DESC";

            //eksekusi query
            res = stm.executeQuery(sql);
            //data hasil query masukan ke design dtm
            while (res.next()) {
                Object[] obj = new Object[7];
                //nama di getstring harus sama dengan di DB
                obj[0] = res.getString("id");
                obj[1] = res.getString("kode_kendaraan");
                obj[2] = res.getString("merk");
                obj[3] = res.getString("jenis");
                obj[4] = res.getString("harga_perhari");
                obj[5] = res.getInt("status") == 0 ? "Ready" : "Disewa";
                obj[6] = res.getInt("is_active") == 0 ? "active" : "not active";
                dtm.addRow(obj);
            }
        } catch (Exception e) {
            System.out.println("Query Gagal..."+e);
        }
    }

    // query menampilkan kendaraan yang ready saja (untuk tab Sewa member)
    public void tampilkanReady() {
        try {
            dtmReady.getDataVector().removeAllElements();
            dtmReady.fireTableDataChanged();
            
            sql = "SELECT * FROM m_kendaraan WHERE status = 0 AND is_active = 0";

            res = stm.executeQuery(sql);
            while (res.next()) {
                Object[] obj = new Object[6];
                obj[0] = res.getString("id");
                obj[1] = res.getString("kode_kendaraan");
                obj[2] = res.getString("merk");
                obj[3] = res.getString("jenis");
                obj[4] = res.getString("harga_perhari");
                obj[5] = res.getInt("status") == 0 ? "Ready" : "Disewa";
                dtmReady.addRow(obj);
            }
        } catch (Exception e) {
            System.out.println("Query Gagal...");
        }
    }

    //CREATE
    // cek duplikasi kode (tanpa is_active, karena kode UNIQUE di database)
    public boolean cekKode(String kode) {
        boolean ada = false;
        try {
            sql = "SELECT COUNT(*) FROM m_kendaraan WHERE kode_kendaraan = ?";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setString(1, kode);
            res = ps.executeQuery();
            if (res.next() && res.getInt(1) > 0) {
                ada = true;
            }
        } catch (Exception e) {
            System.out.println("Query Gagal...");
            ada = true;   // kalau error, anggap duplikat supaya tidak menyimpan
        }
        return ada;
    }

    // Membuat kode otomatis, contoh: MBL-001, MBL-002, MTR-001
    public String buatKode(String jenis) {
        String prefix = jenis.equalsIgnoreCase("mobil") ? "MBL" : "MTR";
        String kodeBaru = prefix + "-001";
        try {
            // ambil kode terbesar dengan prefix itu (termasuk yang nonaktif, karena kode UNIQUE)
            sql = "SELECT MAX(CAST(SUBSTRING(kode_kendaraan, 5) AS UNSIGNED)) "
                    + "FROM m_kendaraan WHERE kode_kendaraan LIKE ?";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setString(1, prefix + "-%");
            res = ps.executeQuery();
            if (res.next()) {
                int terakhir = res.getInt(1);   // 0 kalau belum ada data
                kodeBaru = String.format("%s-%03d", prefix, terakhir + 1);
            }
        } catch (Exception e) {
            System.out.println("Query Gagal... ");
        }
        return kodeBaru;
    }

    // simpan kendaraan baru (INSERT)
    public boolean tambah(Kendaraan k) {
        boolean status = false;
        try {
            sql = "INSERT INTO m_kendaraan (kode_kendaraan, merk, jenis, harga_perhari, status, is_active) "
                    + "VALUES (?,?,?,?,?,0)";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setString(1, k.getKodeKendaraan());
            ps.setString(2, k.getMerk());
            ps.setString(3, k.getJenis());
            ps.setDouble(4, k.getHargaPerHari());
            ps.setInt(5, k.getStatus());
            ps.executeUpdate();   // khusus INSERT/UPDATE/DELETE
            status = true;
        } catch (Exception e) {
            System.out.println("Query Gagal..."+e);
        }
        return status;
    }

    //UPDATE
    // cek duplikasi kode saat edit (abaikan data sendiri)
    public boolean cekKodeEdit(String kode, int id) {
        boolean ada = false;
        try {
            sql = "SELECT COUNT(*) FROM m_kendaraan WHERE kode_kendaraan = ? AND id <> ?";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setString(1, kode);
            ps.setInt(2, id);
            res = ps.executeQuery();
            if (res.next() && res.getInt(1) > 0) {
                ada = true;
            }
        } catch (Exception e) {
            System.out.println("Query Gagal... ");
            ada = true;
        }
        return ada;
    }

    public boolean ubah(Kendaraan k) {
        boolean status = false;
        try {
            sql = "UPDATE m_kendaraan SET kode_kendaraan = ?, merk = ?, jenis = ?, "
                    + "harga_perhari = ?, status = ? WHERE id = ?";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setString(1, k.getKodeKendaraan());
            ps.setString(2, k.getMerk());
            ps.setString(3, k.getJenis());
            ps.setDouble(4, k.getHargaPerHari());
            ps.setInt(5, k.getStatus());
            ps.setInt(6, k.getId());
            ps.executeUpdate();   // khusus INSERT/UPDATE/DELETE
            status = true;
        } catch (Exception e) {
            System.out.println("Query Gagal...");
        }
        return status;
    }

    //DELETE
    // cek kendaraan sedang disewa (ada sewa aktif)
    public boolean sedangDisewa(int id) {
        boolean ada = false;
        try {
            sql = "SELECT COUNT(*) FROM t_sewa WHERE id_kendaraan = ? "
                    + "AND status_sewa = 0 AND is_active = 0";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setInt(1, id);
            res = ps.executeQuery();
            if (res.next() && res.getInt(1) > 0) {
                ada = true;
            }
        } catch (Exception e) {
            System.out.println("Query Gagal... " + e);
            ada = true;   // kalau error, anggap disewa supaya tidak terhapus
        }
        return ada;
    }

    public boolean nonaktifkan(int id) {
        boolean status = false;
        try {
            sql = "UPDATE m_kendaraan SET is_active = 1 WHERE id = ?";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            status = true;
        } catch (Exception e) {
            System.out.println("Query Gagal... " + e);
        }
        return status;
    }
    
    public boolean aktifkan(int id) {
        boolean status = false;
        try {
            sql = "UPDATE m_kendaraan SET is_active = 0 WHERE id = ?";
            PreparedStatement ps = Koneksi.con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            status = true;
        } catch (Exception e) {
            System.out.println("Query Gagal... " + e);
        }
        return status;
    }
    

    //RINGKASAN KAPASITAS
    // status: -1 = semua, 0 = ready, 1 = disewa
    public int hitung(int status) {
        int jumlah = 0;
        try {
            sql = "SELECT COUNT(*) FROM m_kendaraan WHERE is_active = 0";
            if (status >= 0) {
                sql += " AND status = " + status;
            }
            res = stm.executeQuery(sql);
            if (res.next()) {
                jumlah = res.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("Query Gagal...");
        }
        return jumlah;
    }
}
