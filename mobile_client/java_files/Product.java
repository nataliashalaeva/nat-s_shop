package com.example.myshop;

public class Product {

    private String imageUrl;
    private String name;
    private String price;
    private int  id;

    public Product(int  id, String image, String name, String price) {
        this.id = id;
        this.imageUrl = image;
        this.name = name;
        this.price = price;
    }
    public Product() {
    }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getPrice() { return price; }
}
