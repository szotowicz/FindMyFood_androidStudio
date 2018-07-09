package com.tmc.tomik.findmyfood;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class RateRestaurantsFragment extends Fragment {

    private View mView;
    private RequestQueue mQueueSearching;
    private RequestQueue mQueueRate;
    String[] parsedFile = null;
    private String currentRate = "-1";
    private String selectedRestaurantId = "-1";
    private List<Restaurant> restaurantsList = new ArrayList<>();
    private ListView restaurantsListView;
    private EditText nameOfRestaurantEditText;
    private Button star1Btn = null;
    private Button star2Btn = null;
    private Button star3Btn = null;
    private Button star4Btn = null;
    private Button star5Btn = null;

    public RateRestaurantsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueueSearching = Volley.newRequestQueue(getActivity());
        mQueueRate = Volley.newRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_rate_restaurants, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restaurantsListView = (ListView) mView.findViewById(R.id.restaurantsListView);
        nameOfRestaurantEditText = (EditText) mView.findViewById(R.id.nameOfRestaurantToSearchEditText);
        star1Btn = (Button) mView.findViewById(R.id.star1Btn);
        star2Btn = (Button) mView.findViewById(R.id.star2Btn);
        star3Btn = (Button) mView.findViewById(R.id.star3Btn);
        star4Btn = (Button) mView.findViewById(R.id.star4Btn);
        star5Btn = (Button) mView.findViewById(R.id.star5Btn);
        parsedFile = parseConfigFile();
    }

    public void setRateRestaurant(String rate) {
        currentRate = rate;
        /*
        Button star1Btn = (Button) mView.findViewById(R.id.star1Btn);
        Button star2Btn = (Button) mView.findViewById(R.id.star2Btn);
        Button star3Btn = (Button) mView.findViewById(R.id.star3Btn);
        Button star4Btn = (Button) mView.findViewById(R.id.star4Btn);
        Button star5Btn = (Button) mView.findViewById(R.id.star5Btn);
        */

        star1Btn.setBackgroundResource(R.mipmap.star_empty);
        star2Btn.setBackgroundResource(R.mipmap.star_empty);
        star3Btn.setBackgroundResource(R.mipmap.star_empty);
        star4Btn.setBackgroundResource(R.mipmap.star_empty);
        star5Btn.setBackgroundResource(R.mipmap.star_empty);

        switch (rate) {
            case "1":
                star1Btn.setBackgroundResource(R.mipmap.star_full);
                break;
            case "2":
                star1Btn.setBackgroundResource(R.mipmap.star_full);
                star2Btn.setBackgroundResource(R.mipmap.star_full);
                break;
            case "3":
                star1Btn.setBackgroundResource(R.mipmap.star_full);
                star2Btn.setBackgroundResource(R.mipmap.star_full);
                star3Btn.setBackgroundResource(R.mipmap.star_full);
                break;
            case "4":
                star1Btn.setBackgroundResource(R.mipmap.star_full);
                star2Btn.setBackgroundResource(R.mipmap.star_full);
                star3Btn.setBackgroundResource(R.mipmap.star_full);
                star4Btn.setBackgroundResource(R.mipmap.star_full);
                break;
            case "5":
                star1Btn.setBackgroundResource(R.mipmap.star_full);
                star2Btn.setBackgroundResource(R.mipmap.star_full);
                star3Btn.setBackgroundResource(R.mipmap.star_full);
                star4Btn.setBackgroundResource(R.mipmap.star_full);
                star5Btn.setBackgroundResource(R.mipmap.star_full);
                break;
            default:
                break;
        }
    }

    public void searchRestaurant() {
        String nameOfRestaurantToSearch = String.valueOf(nameOfRestaurantEditText.getText());
        if (!nameOfRestaurantToSearch.trim().equals("")) {
            currentRate = "-1";
            selectedRestaurantId = "-1";
            setRateRestaurant("0");

            //Toast.makeText(getActivity(), "Search: " + nameOfRestaurantToSearch, Toast.LENGTH_SHORT).show();
            searchRestaurantUsingAPI(parsedFile[1], parsedFile[2].toLowerCase(), nameOfRestaurantToSearch);
        }
    }

    private void searchRestaurantUsingAPI(String myEmail, String myPassword, String nameOfRestaurantToSearch) {
        /*
        restaurantsList.clear();
        // TMP
        restaurantsList.add(new Restaurant("1", "restauracja1", "adres 1 a1110", "-1"));
        restaurantsList.add(new Restaurant("2", "restauracja2", "adres 2 as   ads 10", "1"));
        restaurantsList.add(new Restaurant("3", "restauracja3", "adres 3 1423110", "4"));
        restaurantsList.add(new Restaurant("4", "restauracja4", "adres 4 3242sad ddd1110", "4"));

        addPromotionsToListView();
        */

        String url = null;

        try {
            url = "http://findmyfood.azurewebsites.net/api/Restaurant/"
                    + URLEncoder.encode(myEmail, "UTF-8") + "&" + myPassword + "&" + URLEncoder.encode(nameOfRestaurantToSearch, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        //Toast.makeText(getActivity(), "Get: " + url, Toast.LENGTH_SHORT).show();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            restaurantsList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject restaurant = response.getJSONObject(i);

                                int id = restaurant.getInt("id");
                                String restaurantID = String.valueOf(id);
                                String restaurantName = restaurant.getString("name");
                                String restaurantAddress = restaurant.getString("address");
                                int rate = restaurant.getInt("rate");
                                String myRate = String.valueOf(rate);

                                restaurantsList.add(new Restaurant(
                                        restaurantID,
                                        restaurantName,
                                        restaurantAddress,
                                        myRate));
                            }

                            // Show results
                            addPromotionsToListView();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                int mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };

        mQueueSearching.add(request);
    }

    private String[] parseConfigFile() {
        String[] parsedFile = new String[3];
        String filename = getString(R.string.config_file_name);
        try {
            File directory = getActivity().getFilesDir();
            File configFile = new File(directory, filename);

            if (configFile.exists() && configFile.isFile()) {
                StringBuilder text = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                }
                br.close();
                if (!text.toString().trim().equals("") && text.toString().contains("&")) {
                    parsedFile = text.toString().split("&");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parsedFile;
    }

    public void rateRestaurant() {
        if (currentRate.equals("-1") || selectedRestaurantId.equals("-1")) {
            Toast.makeText(getActivity(), "Należy wybrać restaurację oraz ocenę", Toast.LENGTH_SHORT).show();
            return;
        }

        rateRestaurantUsingAPI(parsedFile[1], parsedFile[2].toLowerCase());
    }

    private void rateRestaurantUsingAPI(String myEmail, String myPassword) {
        //Toast.makeText(getActivity(), "Res: " + selectedRestaurantId + " " + currentRate, Toast.LENGTH_SHORT).show();

        String url = null;

        try {
            url = "http://findmyfood.azurewebsites.net/api/Rate/"
                    + URLEncoder.encode(myEmail, "UTF-8") + "&" + myPassword + "&" + selectedRestaurantId + "&" + currentRate;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        //Toast.makeText(getActivity(), "Get: " + url, Toast.LENGTH_SHORT).show();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String rateResponse = response.getString("response");
                            String rateMessage = response.getString("message");

                            if (rateResponse.toLowerCase().equals("true")) {
                                Toast.makeText(getActivity(), "Ocena restauracji została zaktualizowana", Toast.LENGTH_SHORT).show();
                                searchRestaurant();
                            }
                            else {
                                Toast.makeText(getActivity(), "Warning: " + rateMessage, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                int mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };

        mQueueRate.add(request);
    }

    private void addPromotionsToListView() {
        if (restaurantsListView != null && restaurantsList != null && restaurantsList.size() > 0) {
            RestaurantAdapter restaurantAdapter = new RestaurantAdapter();
            restaurantsListView.setAdapter(restaurantAdapter);
            restaurantsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectPromotionFromListView(i);
                }
            });
        }
    }

    private void selectPromotionFromListView(int listViewElementIndex) {
        String currentRate = restaurantsList.get(listViewElementIndex).get_currentRate();
        selectedRestaurantId = restaurantsList.get(listViewElementIndex).get_id();
        setRateRestaurant(currentRate);
    }

    class RestaurantAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return restaurantsList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.restaurant_list_view_element_layout, null);

            TextView nameTextView = (TextView) view.findViewById(R.id.restaurantNameTextView);
            TextView addressTextView = (TextView) view.findViewById(R.id.restaurantAddressTextView);
            TextView currentRateTextView = (TextView) view.findViewById(R.id.currentRateTextView);

            nameTextView.setText(restaurantsList.get(i).get_restaurantName());
            addressTextView.setText(restaurantsList.get(i).get_restaurantAddress());
            if (restaurantsList.get(i).get_currentRate().equals("-1")) {
                currentRateTextView.setText("-");
            } else {
                currentRateTextView.setText(restaurantsList.get(i).get_currentRate());
            }

            return view;
        }
    }
}