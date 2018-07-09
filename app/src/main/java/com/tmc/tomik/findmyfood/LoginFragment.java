package com.tmc.tomik.findmyfood;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class LoginFragment extends Fragment {

    private View mView;
    private RequestQueue mQueue;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.login_layout, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTextOnInformLabel("");
    }

    private void setTextOnInformLabel(String textToDisplay) {
        TextView informationLabel = (TextView) getView().findViewById(R.id.informationDuringLoginTextView);
        if (informationLabel != null) {
            informationLabel.setText(textToDisplay);
        }
    }

    public void signIn(MainNavigationActivity parentActivity) {
        EditText providedEmail = mView.findViewById(R.id.emailEditText);
        EditText providedPassword = mView.findViewById(R.id.passwordEditText);

        if (providedEmail != null && providedPassword != null
                && providedDataValidator(String.valueOf(providedEmail.getText()), String.valueOf(providedPassword.getText()))) {

            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(String.valueOf(providedPassword.getText()).getBytes(StandardCharsets.UTF_8));
                String hashedPassword = String.format("%064x", new BigInteger(1, hash));
                //setTextOnInformLabel(hashedPassword);

                trySingInUsingAPI(String.valueOf(providedEmail.getText()), hashedPassword.toLowerCase(), parentActivity);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean providedDataValidator(String email, String password) {
        if (email.trim() == "" || password.trim() == "") {
            setTextOnInformLabel("Oba pola są obowiązkowe i nie mogą pozostać puste");
            return false;
        }

        if (!email.contains("@") || email.contains("&") || password.length() < 5) {
            setTextOnInformLabel("Dostarczone dane uwierzytelniania są niepoprawne");
            return false;
        }

        return true;
    }

    private void trySingInUsingAPI(final String email, final String password, final MainNavigationActivity parentActivity) {
        String url = null;

        try {
            url = "http://findmyfood.azurewebsites.net/api/SignIn/"
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
                            String signInResponse = response.getString("response");
                            String usersLogin = response.getString("message");

                            if (signInResponse.toLowerCase().equals("true")) {
                                String configFileContent = usersLogin + "&" + email + "&" + password;
                                writeToFile(getString(R.string.config_file_name), configFileContent);

                                removeFile(getString(R.string.filter_file_name));
                                Toast.makeText(getActivity(), "Zalogowano", Toast.LENGTH_SHORT).show();
                                parentActivity.moveMeToHomeFragmentAfterSignIn();
                            } else {
                                setTextOnInformLabel("Dostarczone dane uwierzytelniania są niepoprawne");
                            }

                        } catch (JSONException e) {
                            setTextOnInformLabel("Dostarczone dane uwierzytelniania są niepoprawne");
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

    private void removeFile(String fileName) {
        try {
            String filename = fileName;
            File directory = getActivity().getFilesDir();
            File configFile = new File(directory, filename);
            configFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(String fileName, String content) {
        FileOutputStream outputStream;

        try {
            String filename = fileName;
            outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}