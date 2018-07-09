package com.tmc.tomik.findmyfood;

public class Restaurant {

    private String _id;
    private String _restaurantName;
    private String _restaurantAddress;
    private String _currentRate;

    public Restaurant(String _id, String _restaurantName, String _restaurantAddress, String _currentRate) {
        this._id = _id;
        this._restaurantName = _restaurantName;
        this._restaurantAddress = _restaurantAddress;
        this._currentRate = _currentRate;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_restaurantName() {
        return _restaurantName;
    }

    public void set_restaurantName(String _restaurantName) {
        this._restaurantName = _restaurantName;
    }

    public String get_restaurantAddress() {
        return _restaurantAddress;
    }

    public void set_restaurantAddress(String _restaurantAddress) {
        this._restaurantAddress = _restaurantAddress;
    }

    public String get_currentRate() {
        return _currentRate;
    }

    public void set_currentRate(String _currentRate) {
        this._currentRate = _currentRate;
    }
}
