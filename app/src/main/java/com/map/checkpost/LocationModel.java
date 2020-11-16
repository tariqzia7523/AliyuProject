package com.map.checkpost;

public class LocationModel {
    String id;
    String address;
    double lat;
    double lng;
    long passedTime;

    public long getPassedTime() {
        return passedTime;
    }

    public void setPassedTime(long passedTime) {
        this.passedTime = passedTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
