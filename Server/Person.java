package Server;

/*
* Модель клиента  , информация о котором находится на нашей БД.
* Данный класс необходим для парсинга , получаемой после запроса на сервер  информации , представленной в   JSON формате.
* POJO -  класс.
*/

public class Person {
    private int id;
    private String name, comment ;
    byte[] photo;

    public Person(int id, String name, String comment , byte[] photo) {
        this.name = name;
        this.id = id;
        this.comment = comment;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public int getId() {
        return id;
    }

    public byte[] getPhoto() { return photo; }

}
