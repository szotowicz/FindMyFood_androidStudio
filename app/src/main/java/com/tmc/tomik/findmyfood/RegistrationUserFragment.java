package com.tmc.tomik.findmyfood;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegistrationUserFragment extends Fragment {

    private View mView;
    private RequestQueue mQueue;

    public RegistrationUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_registration_user, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTextOnInformLabel("");
    }

    private void setTextOnInformLabel(String textToDisplay) {
        TextView informationLabel = (TextView) getView().findViewById(R.id.informationDuringRegistrationTextView);
        if (informationLabel != null) {
            informationLabel.setText(textToDisplay);
        }
    }

    public void registryNewUser(MainNavigationActivity parentActivity) {
        EditText newLoginEditText = (EditText) mView.findViewById(R.id.newLoginEditText);
        String newLogin = String.valueOf(newLoginEditText.getText());
        EditText newEmailEditText = (EditText) mView.findViewById(R.id.newEmailEditText);
        String newEmail = String.valueOf(newEmailEditText.getText());
        EditText newPasswordEditText = (EditText) mView.findViewById(R.id.newPasswordEditText);
        String newPassword = String.valueOf(newPasswordEditText.getText());
        EditText newRepeatedPasswordEditText = (EditText) mView.findViewById(R.id.newRepeatedPasswordEditText);
        String newRepeatedPassword = String.valueOf(newRepeatedPasswordEditText.getText());

        if (newLogin.trim().equals("") || newEmail.trim().equals("") || newPassword.trim().equals("") || newRepeatedPassword.trim().equals("")) {
            setTextOnInformLabel("Uzupełnij wszystkie pola");
            return;
        }

        if (!newPassword.equals(newRepeatedPassword)) {
            setTextOnInformLabel("Powtórz poprawnie nowe hasło");
            return;
        }

        if (newPassword.length() < 5) {
            setTextOnInformLabel("Nowe hasło musi mieć minimum 5 znaków");
            return;
        }

        if (!newEmail.contains("@") || !newEmail.contains(".")) {
            setTextOnInformLabel("Podano niepoprawny adres email");
            return;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(newPassword.getBytes(StandardCharsets.UTF_8));
            String hashedPassword = String.format("%064x", new BigInteger(1, hash));

            registryUserUsingAPI(newLogin, newEmail, hashedPassword.toLowerCase(), parentActivity);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            setTextOnInformLabel("Wystąpił problem techniczny, spróbuj ponownie");
        }
    }

    private void registryUserUsingAPI(String login, String email, String password, final MainNavigationActivity parentActivity) {
        String url = null;
        try {
            url = "http://findmyfood.azurewebsites.net/api/Registration/"
                    + URLEncoder.encode(login, "UTF-8") + "&"
                    + URLEncoder.encode(email, "UTF-8") + "&" + password;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            setTextOnInformLabel("Wystąpił problem techniczny, spróbuj jeszcze raz");
            return;
        }
        //Toast.makeText(getActivity(), "Get: " + url, Toast.LENGTH_SHORT).show();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String registrationResponse = response.getString("response");
                            String message = response.getString("message");

                            if (registrationResponse.toLowerCase().equals("true") && parentActivity != null) {
                                //Toast.makeText(getActivity(), "YEAH!", Toast.LENGTH_SHORT).show();
                                parentActivity.moveMeToLoginFragmentAfterRegistration();
                            } else {
                                setTextOnInformLabel(message);
                            }

                        } catch (JSONException e) {
                            setTextOnInformLabel("Wystąpił problem techniczny, spróbuj ponownie");
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

        mQueue.add(request);
        //setTextOnInformLabel("Wystąpił problem techniczny, spróbuj jeszcze raz");
    }
}