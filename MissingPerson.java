package com.project.raymond.reporttopolice;

public class MissingPerson {

    private String Id;
    private String Name;
    private String Age;
    private String Gender;
    private String LastSeen;
    private String Details;

    MissingPerson(String id, String name, String age, String gender, String lastSeen, String details) {
        Id = id;
        Name = name;
        Age = age;
        Gender = gender;
        LastSeen = lastSeen;
        Details = details;
    }

    public String getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public String getAge() {
        return Age;
    }

    public String getGender() {
        return Gender;
    }

    public String getLastSeen() {
        return LastSeen;
    }

    public String getDetails() {
        return Details;
    }
}
