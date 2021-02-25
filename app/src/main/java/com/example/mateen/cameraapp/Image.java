package com.example.mateen.cameraapp;

public class Image {
    private byte[] image;
    private String address;
    private String latitude;
    private String longitude;

    //    parameterized constructor
    public Image(byte[] image, String address, String latitude, String longitude) {
        this.image = image;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    //    setters and getters
    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
