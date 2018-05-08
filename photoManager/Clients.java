package com.example.michail.photomanager;

/**
 * Модель клиента  , информация о котором находится на нашей БД
 * Данный класс необходим для парсинга , получаемого после запроса на сервер , информации в  JSON
 */

public class Clients {

    private String id;
    private String name;
    private String comment;
    private String photo;

    public Clients(String id, String name, String comment , String photo) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.photo = photo;

    }
    public Clients(){}

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public String getPhoto() { return photo; }


}
