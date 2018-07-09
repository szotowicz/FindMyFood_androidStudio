package com.tmc.tomik.findmyfood;

public class Promotion {

    private double _lngPosition;
    private double _latPosition;
    private String _restaurant;
    private String _restaurantAddress;
    private String _description;
    private String _rating;
    private String _tags;

    public Promotion(double lng, double lat, String restaurant, String restaurantAddress, String description, String rating, String tags) {
        _lngPosition = lng;
        _latPosition = lat;
        _restaurant = restaurant;
        _restaurantAddress = restaurantAddress;
        _description = description;
        _rating = rating;
        _tags = tags;
    }

    public double get_latPosition() {
        return _latPosition;
    }

    public void set_latPosition(double _latPosition) {
        this._latPosition = _latPosition;
    }

    public double get_lngPosition() {
        return _lngPosition;
    }

    public void set_lngPosition(double _lngPosition) {
        this._lngPosition = _lngPosition;
    }

    public String get_restaurant() {
        return _restaurant;
    }

    public void set_restaurant(String _restaurant) {
        this._restaurant = _restaurant;
    }

    public String get_restaurantAddress() {
        return _restaurantAddress;
    }

    public void set_restaurantAddress(String _restaurantAddress) {
        this._restaurantAddress = _restaurantAddress;
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public String get_rating() {
        return _rating;
    }

    public void set_rating(String _rating) {
        this._rating = _rating;
    }

    public String get_tags() {
        return _tags;
    }

    public void set_tags(String _tags) {
        this._tags = _tags;
    }
}