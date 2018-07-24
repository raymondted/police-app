package com.project.raymond.reporttopolice;

public class Crime {

    private String Id;
    private String Street;
    private String City;
    private String County;
    private String Details;

    Crime(String id, String street, String city, String county, String details) {
        Id = id;
        Street = street;
        City = city;
        County = county;
        Details = details;
    }

    public String getId() {
        return Id;
    }

    public String getStreet() {
        return Street;
    }

    public String getCity() {
        return City;
    }

    public String getCounty() {
        return County;
    }

    public String getDetails() {
        return Details;
    }
}

