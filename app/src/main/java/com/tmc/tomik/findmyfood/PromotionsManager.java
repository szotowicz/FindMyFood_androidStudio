package com.tmc.tomik.findmyfood;

import android.app.Activity;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PromotionsManager {

    private LatLng currentPosition;
    private double radiusToSearch;
    private List<Promotion> promotionsList = new ArrayList<>();

    public PromotionsManager(LatLng position, double radius, Activity activity) {
        this.currentPosition = position;
        this.radiusToSearch = radius;
    }

    private void GetProms() {
        try {
            URL url = new URL("https://api.myjson.com/bins/nz2am");
            /*URL url = new URL("http://tomik.azurewebsites.net/api/Promotion/" + currentPosition.longitude
                    + "&" + currentPosition.latitude + "&" + radiusToSearch);*/
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String data = "";
            String line = "";
            while(line != null) {
                line = bufferedReader.readLine();
                data += line;
            }

            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                promotionsList.add(new Promotion(
                        18.9,
                        54.2,
                        jsonObject.getString("restaurantName"),
                        jsonObject.getString("address"),
                        jsonObject.getString("description"),
                        jsonObject.getString("rating"),
                        jsonObject.getString("tags")));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void AddTempRestaurants() {
        /* Older schema

        // Wrzeszcz
        promotionsList.add(new Promotion("01", 54.376480, 18.615109, "Nazar", "Sałatka gratis"));
        promotionsList.add(new Promotion("02", 54.368620, 18.611081, "Autsajder", "Paluszki do każdego 4paka"));
        promotionsList.add(new Promotion("03", 54.376841, 18.612162, "Da Grasso", "-10% na pizze dnia"));

        // Old town
        promotionsList.add(new Promotion("04", 54.354476, 18.654364, "Restauracja Bunkier", "Darmowe frytki"));
        promotionsList.add(new Promotion("05", 54.352564, 18.659374, "Restauracja Filharmonia", "-20% na dania wegańskie"));
        promotionsList.add(new Promotion("06", 54.351746, 18.657816, "Billy's American", "-10% na wszystkie burgery"));
        */
    }

    public List<Promotion> findAllPromotions() {
        return promotionsList;
    }

    public List<Promotion> findPromotions(double lat, double lng, double radiusToSearch) {
        List<Promotion> promotionsListForMe = new ArrayList<>();
        for (Promotion promotion : promotionsList) {
            double distanceBetweenPromotionAndClient = calculateDistanceBetweenTwoPoints(lat, lng,
                    promotion.get_latPosition(), promotion.get_lngPosition());
            if (distanceBetweenPromotionAndClient <= radiusToSearch) {
                promotionsListForMe.add(promotion);
            }
        }

        return promotionsListForMe;
    }

    // Return distance in KM between two locations
    private double calculateDistanceBetweenTwoPoints(double lat1, double lng1, double lat2, double lng2) {
        double p = Math.PI / 180;
        double a = 0.5 - Math.cos((lat2 - lat1) * p) / 2 + Math.cos(lat1 * p) *
                Math.cos(lat2 * p) * (1 - Math.cos((lng2 - lng1) * p)) / 2;

        return 2 * 6371 * Math.asin(Math.sqrt(a)); //R = 6371 km
    }
}