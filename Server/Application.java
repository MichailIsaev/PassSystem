package Server;

import Desktop.Frame1;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/*
* Предназначен для запуска SpringFramework приложения.
*/

@ComponentScan
@EnableAutoConfiguration
public class Application {
    public static void main(String[] args) {
        Frame1 frame = new Frame1();
        SpringApplication.run(Application.class, args);
        frame.setStatus();
        while (true) {
            if (frame.isStatus()) {
                try {
                    insert(frame);
                    frame.setStatus(false);
                    frame.setResultLabel("client has been inserted");
                } catch (SQLException e) {
                    e.printStackTrace();
                    frame.setResultLabel("client is not inserted");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    * Реализован для вставки на БД информации о новом клиенте , логирование попытки создать новых клиентов.
    */

    public static void insert(Frame1 frame) throws SQLException, IOException {
        Connection conn = Controller.getPostgresConnection();
        String name1 = frame.getName();
        String comment1 = frame.getComment();
        byte[] photo1 = frame.getPhoto();
        String stm = "INSERT INTO clients(id , name , comment , photo) VALUES(?,?,?,?)";
        PreparedStatement preparedStatement = conn.prepareStatement(stm);
        preparedStatement.setString(1 , frame.getId());
        preparedStatement.setString(2 , name1);
        preparedStatement.setString(3 , comment1);
        preparedStatement.setBinaryStream(4 , new ByteArrayInputStream(photo1));
        preparedStatement.executeUpdate();

    }


}
