package Server;



import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

/*
* Предназначен для контроля соединения с БД
*/

@RestController
public class Controller {
    @RequestMapping("/person")
    public Person person(@RequestParam(value = "id", required = false, defaultValue = "0") String id) {
        int index = Integer.parseInt(id);
        Person people;
        try {
            people = createPeople(Integer.parseInt(id));
        } catch (SQLException e) {
            e.printStackTrace();
            return new Person(-1, "ERROR", "ERROR" , null );
        }
        return people;
    }

    /*
    * Реализован для возможности получения информации в JSON - формате о клиенте с id в браузере.
    */
    public static Person createPeople(int id) throws SQLException {

        Connection conn = getPostgresConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM clients");
        Person result;
        String id1 = "-1";
        String name = "ERROR";
        String comment = "ERROR";
        byte[] photo = null;
        ArrayList<Person> clients = new ArrayList<>();
        boolean status = false;
        while (rs.next()) {
            id1 = rs.getString("id");
            name = rs.getString("name");
            comment = rs.getString("comment");
            photo = rs.getBytes("photo");
            clients.add(new Person(Integer.parseInt(id1), name, comment , photo));
            if (Integer.parseInt(id1) == id) {
                status = true;
                break;
            }
            status = false;

        }

        if (!status) {

            result = new Person(-2, "ERROR", "ERROR" , "Error".getBytes());
            rs.close();
            stmt.close();
            conn.close();
            return result;
        } else {
            result = new Person(Integer.parseInt(id1), name, comment , photo);
            rs.close();
            stmt.close();
            conn.close();
            return result;
        }

    }

    /*
    * Cоединение , посредством JDBC , к БД.
    */

    public static Connection getPostgresConnection() {
        try {
            DriverManager.registerDriver((Driver)

                    Class.forName("org.postgresql.Driver").newInstance());

            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/clients", "postgres", "12345");
            return connection;
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}






