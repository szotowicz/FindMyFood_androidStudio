package com.tmc.tomik.findmyfood;

import android.app.Activity;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChangePasswordFragment extends Fragment {

    private View mView;
    private RequestQueue mQueue;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_change_password, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTextOnInformLabel("");
    }

    public void changeCurrentPassword(MainNavigationActivity parentActivity) {
        EditText currentPasswordEditText = (EditText) mView.findViewById(R.id.changePasswordCurrentEditText);
        String currentPassword = String.valueOf(currentPasswordEditText.getText());
        EditText newPasswordEditText = (EditText) mView.findViewById(R.id.changePasswordNewEditText);
        String newPassword = String.valueOf(newPasswordEditText.getText());
        EditText newPassword2EditText = (EditText) mView.findViewById(R.id.changePasswordNew2EditText);
        String newPassword2 = String.valueOf(newPassword2EditText.getText());

        if (currentPassword.trim().equals("") || newPassword.trim().equals("") || newPassword2.trim().equals("")) {
            setTextOnInformLabel("Uzupełnij wszystkie pola");
            return;
        }

        if (!newPassword.equals(newPassword2)) {
            setTextOnInformLabel("Powtórz poprawnie nowe hasło");
            return;
        }

        if (newPassword.length() < 5) {
            setTextOnInformLabel("Nowe hasło musi mieć minimum 5 znaków");
            return;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(currentPassword.getBytes(StandardCharsets.UTF_8));
            String hashedPassword = String.format("%064x", new BigInteger(1, hash));

            String[] parsedFile = parseConfigFile();
            if (hashedPassword.toLowerCase().equals(parsedFile[2].toLowerCase())) {
                MessageDigest digest2 = MessageDigest.getInstance("SHA-256");
                byte[] hash2 = digest2.digest(newPassword.getBytes(StandardCharsets.UTF_8));
                String hashedNewPassword = String.format("%064x", new BigInteger(1, hash2));

                changePasswordUsingAPI(parsedFile[1], parsedFile[2].toLowerCase(), hashedNewPassword.toLowerCase(), parentActivity);
            }
            else {
                setTextOnInformLabel("Podano niepoprawne aktualne hasło");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            setTextOnInformLabel("Wystąpił problem techniczny, spróbuj ponownie");
        }
    }

    private void setTextOnInformLabel(String textToDisplay) {
        TextView informationLabel = (TextView) getView().findViewById(R.id.informationDuringChangePasswordTextView);
        if (informationLabel != null) {
            informationLabel.setText(textToDisplay);
        }
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

    private void changePasswordUsingAPI(String email, String currentPassword, String newPassword, final MainNavigationActivity parentActivity) {
        String url = null;

        try {
            url = "http://findmyfood.azurewebsites.net/api/ChangeUserPassword/"
                    + URLEncoder.encode(email, "UTF-8") + "&" + currentPassword + "&" + newPassword;
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
                            String signInResponse = response.getString("response");
                            String message = response.getString("message");

                            if (signInResponse.toLowerCase().equals("true") && parentActivity != null) {
                                parentActivity.logoutMeAfterChangingPassword();
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
    }
}
