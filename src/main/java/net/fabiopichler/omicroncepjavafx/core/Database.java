/*-------------------------------------------------------------------------------

Copyright (c) 2023 Fábio Pichler

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

-------------------------------------------------------------------------------*/

package net.fabiopichler.omicroncepjavafx.core;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class Database {
    private static Database db;

    private Connection conn = null;

    public static Database get() {
        if (db == null)
            db = new Database();

        return db;
    }

    public boolean connect(String dbName) {
        if (conn != null)
            return false;

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);

            defaultConfig();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public void close() {
        try {
            if (conn != null)
                conn.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private void defaultConfig() throws SQLException {
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS cep (cep TEXT PRIMARY KEY NOT NULL, "
                + "logradouro TEXT, complemento TEXT, bairro TEXT, localidade TEXT, uf TEXT, "
                + "ibge TEXT, gia TEXT, ddd TEXT, siafi TEXT, updated_at TEXT NOT NULL)");
    }

    public boolean setCep(Cep cep) {
        if (cepExists(cep))
            return updateCep(cep);
        else
            return insertCep(cep);
    }

    public boolean insertCep(Cep cep) {
        String query = "INSERT INTO cep(cep, logradouro, complemento, bairro, localidade, "
                + "uf, ibge, gia, ddd, siafi, updated_at) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, cep.cep);
            pstmt.setString(2, cep.logradouro);
            pstmt.setString(3, cep.complemento);
            pstmt.setString(4, cep.bairro);
            pstmt.setString(5, cep.localidade);
            pstmt.setString(6, cep.uf);
            pstmt.setString(7, cep.ibge);
            pstmt.setString(8, cep.gia);
            pstmt.setString(9, cep.ddd);
            pstmt.setString(10, cep.siafi);
            pstmt.setObject(11, LocalDateTime.now());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean updateCep(Cep cep) {
        String query = "UPDATE cep "
                + "SET logradouro = ?, complemento = ?, bairro = ?, localidade = ?, uf = ?, ibge = ?, "
                + "gia = ?, ddd = ?, siafi = ?, updated_at = ? "
                + "WHERE cep = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, cep.logradouro);
            pstmt.setString(2, cep.complemento);
            pstmt.setString(3, cep.bairro);
            pstmt.setString(4, cep.localidade);
            pstmt.setString(5, cep.uf);
            pstmt.setString(6, cep.ibge);
            pstmt.setString(7, cep.gia);
            pstmt.setString(8, cep.ddd);
            pstmt.setString(9, cep.siafi);
            pstmt.setObject(10, LocalDateTime.now());
            pstmt.setString(11, cep.cep);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public void readCeps(List<Cep> data) {
        try (Statement stmt = conn.createStatement()) {
            String query = "SELECT * FROM cep ORDER BY updated_at DESC";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                var cep = new Cep();

                cep.cep = rs.getString("cep");
                cep.logradouro = rs.getString("logradouro");
                cep.complemento = rs.getString("complemento");
                cep.bairro = rs.getString("bairro");
                cep.localidade = rs.getString("localidade");
                cep.uf = rs.getString("uf");
                cep.ibge = rs.getString("ibge");
                cep.gia = rs.getString("gia");
                cep.ddd = rs.getString("ddd");
                cep.siafi = rs.getString("siafi");

                data.add(cep);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteCep(String cepId) {
        String query = "DELETE FROM cep WHERE cep = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, cepId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean cepExists(Cep cep) {
        try {
            String query = "SELECT (COUNT(*) > 0) as found FROM cep WHERE cep = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, cep.cep);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    if (rs.getBoolean("found"))
                        return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
