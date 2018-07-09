package com.tmc.tomik.findmyfood;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private View mView;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private List<Promotion> promotionsForMeList = new ArrayList<>();
    private List<Marker> restaurantsMarkersList = new ArrayList<>();
    private LatLng currentPosition;
    private LocationManager locationManager;
    private String provider;
    private double radiusPromotions = 2.0;
    private Circle currentPositionCircle;
    private Circle promotionsAreaCircle;
    private TextView informationTextView;
    private ListView promotionsListView;

    private RequestQueue mQueue;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 500; // 500 milliseconds

    //Toast.makeText(getActivity(), "Selected: ", Toast.LENGTH_SHORT).show();
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.home_layout, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = (MapView) mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);

            // Location Manager
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setAltitudeRequired(false);
            criteria.setSpeedRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(false);

            provider = locationManager.getBestProvider(criteria, false);

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                return;
            }

            informationTextView = (TextView) mView.findViewById(R.id.informationTextView);
            promotionsListView = (ListView) getActivity().findViewById(R.id.promotionsListView);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Default marker in ETI
        LatLng defaultLocation = new LatLng(54.371675, 18.612487); // ETI
        currentPosition = defaultLocation;
        //Toast.makeText(getActivity(), "Position: " + defaultLocation.latitude + " " + defaultLocation.longitude, Toast.LENGTH_SHORT).show();
        SimulateGPSOnStart();

        /*
        // For enable of 'my location' option
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        mMap.setMyLocationEnabled(true);
        */
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        //informationTextView.setText("Searching...");
        locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        currentPosition = new LatLng(lat, lng);

        // TODO: optimalization of searching - difference between current and new position itp
        findPromotions();
        addPromotionsToListView();

        drawPositionOnMap();

        // Camera is always focus on my position
        focusCameraOnCurrentPosition();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    ///////////////////////////////////////////////////////////////////////////////
    private void getPromotionsFromAPI() {
        String url = "http://findmyfood.azurewebsites.net/api/Promotion/" + currentPosition.longitude
                + "&" + currentPosition.latitude + "&" + radiusPromotions;
        //Toast.makeText(getActivity(), "Get: " + url, Toast.LENGTH_SHORT).show();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            promotionsForMeList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject promotion = response.getJSONObject(i);

                                String longitude = promotion.getString("longitude");
                                String latitude = promotion.getString("latitude");
                                String restaurantName = promotion.getString("restaurantName");
                                String address = promotion.getString("address");
                                String description = promotion.getString("description");
                                String rating = promotion.getString("rating");
                                // round a double value #.#
                                if (rating.length() >= 3) {
                                    rating = rating.substring(0, 3);
                                }
                                String tags = promotion.getString("tags");
                                //Toast.makeText(getActivity(), "RATE: " + rating, Toast.LENGTH_SHORT).show();
                                String currentFilter = GetCurrentFilter();
                                if (!currentFilter.equals("")
                                        && !tags.toLowerCase().contains(currentFilter.toLowerCase())
                                        && !description.toLowerCase().contains(currentFilter.toLowerCase())
                                        && !address.toLowerCase().contains(currentFilter.toLowerCase())
                                        && !restaurantName.toLowerCase().contains(currentFilter.toLowerCase())) {

                                    continue;
                                }

                                promotionsForMeList.add(new Promotion(
                                        Double.parseDouble(longitude),
                                        Double.parseDouble(latitude),
                                        restaurantName,
                                        address,
                                        description,
                                        rating,
                                        tags));
                            }

                            // Show results
                            addPromotionsToListView();
                            informationTextView.setText("W promieniu " + radiusPromotions + " km znaleziono ofert: " + promotionsForMeList.size());
                            for (Promotion promotion : promotionsForMeList) {
                                addMarker(promotion.get_latPosition(), promotion.get_lngPosition(), promotion.get_restaurant());
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
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                int mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };

        mQueue.add(request);
    }

    ///////////////////////////////////////////////////////////////////////////////
    private void SimulateGPSOnStart() {
        findPromotions();

        // Refresh circles on map
        drawPositionOnMap();
        focusCameraOnCurrentPosition();
    }

    private String GetCurrentFilter() {
        String currentFilter = "";
        try {
            String filename = getString(R.string.filter_file_name);
            File directory = getActivity().getFilesDir();
            File filterFile = new File(directory, filename);

            if (filterFile.exists() && filterFile.isFile()) {
                StringBuilder text = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(filterFile));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                }
                br.close();
                currentFilter = text.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentFilter;
    }

    private void findPromotions() {
        try {
            clearMapFromMarkers();
            getPromotionsFromAPI();
        } catch (Exception e) {
        }
    }

    private void addMarker(double x, double y, String restaurant) {
        LatLng markerLocation = new LatLng(x, y);
        Marker newMarker = mGoogleMap.addMarker(new MarkerOptions().position(markerLocation).title(restaurant)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        restaurantsMarkersList.add(newMarker);
    }

    private void selectPromotionFromListView(int listViewElementIndex) {
        // For rest set default color
        for (Marker marker : restaurantsMarkersList) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        }

        restaurantsMarkersList.get(listViewElementIndex).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        restaurantsMarkersList.get(listViewElementIndex).showInfoWindow();
    }

    private void clearMapFromMarkers() {
        try {
            // mMap.clear(); - removes all elements, also point of location
            for (Marker markerForRemoving : restaurantsMarkersList) {
                markerForRemoving.remove();
            }
            restaurantsMarkersList.clear();
        } catch (Exception e) {
        }
    }

    private void drawPositionOnMap() {
        if (currentPositionCircle == null) {
            currentPositionCircle = mGoogleMap.addCircle(new CircleOptions()
                    .center(currentPosition)
                    .radius(30)
                    .strokeColor(Color.argb(255, 0, 100, 255))
                    .fillColor(Color.argb(255, 0, 100, 255)));
        } else {
            currentPositionCircle.setCenter(currentPosition);
        }

        if (promotionsAreaCircle == null) {
            promotionsAreaCircle = mGoogleMap.addCircle(new CircleOptions()
                    .center(currentPosition)
                    .radius(radiusPromotions * 1000.0)
                    .strokeColor(Color.argb(120, 0, 0, 255))
                    .strokeWidth(3.0f)
                    .fillColor(Color.argb(50, 0, 200, 255)));
        } else {
            promotionsAreaCircle.setCenter(currentPosition);
            promotionsAreaCircle.setRadius(radiusPromotions * 1000);
        }
    }

    private void focusCameraOnCurrentPosition() {
        if (currentPosition != null) {
            Map<Double, Float> zoomMap = new HashMap<>();
            zoomMap.put(0.5, 14.4f);
            zoomMap.put(1.0, 13.4f);
            zoomMap.put(1.5, 12.8f);
            zoomMap.put(2.0, 12.4f);
            zoomMap.put(2.5, 12.0f);
            zoomMap.put(3.0, 11.8f);
            zoomMap.put(3.5, 11.6f);
            zoomMap.put(4.0, 11.4f);
            zoomMap.put(4.5, 11.2f);
            zoomMap.put(5.0, 11.1f);

            Float zoom = 12.0f;
            zoom = zoomMap.get(radiusPromotions);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPosition, zoom);
            mGoogleMap.animateCamera(cameraUpdate);

            //informationTextView.setText("zoom: " + mGoogleMap.getCameraPosition().zoom);
        }
    }

    private void addPromotionsToListView() {
        if (promotionsListView != null && promotionsForMeList != null && promotionsForMeList.size() > 0) {
            PromotionAdapter promotionAdapter = new PromotionAdapter();
            promotionsListView.setAdapter(promotionAdapter);
            promotionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectPromotionFromListView(i);
                }
            });
        }
    }

    public void changeSearchRadius(double val) {
        if ((val > 0 && radiusPromotions < 5) || (val < 0 && radiusPromotions > 0.5)) {
            radiusPromotions += val;

            findPromotions();

            // Refresh circles on map
            drawPositionOnMap();
            focusCameraOnCurrentPosition();
        }
    }

    public void findLocation(LatLng location) {
        currentPosition = location;
        findPromotions();

        // Refresh circles on map
        drawPositionOnMap();
        focusCameraOnCurrentPosition();
    }

    class PromotionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return promotionsForMeList.size();
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
            view = getLayoutInflater().inflate(R.layout.promotion_list_view_element_layout, null);

            TextView restaurantRateTextView = (TextView) view.findViewById(R.id.restaurantRateTextView);
            TextView nameTextView = (TextView) view.findViewById(R.id.promotionNameTextView);
            TextView descriptionTextView = (TextView) view.findViewById(R.id.promotionDescriptionTextView);

            restaurantRateTextView.setText(promotionsForMeList.get(i).get_rating());
            nameTextView.setText(promotionsForMeList.get(i).get_restaurant());
            descriptionTextView.setText(promotionsForMeList.get(i).get_description());

            return view;
        }
    }
}