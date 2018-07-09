package com.tmc.tomik.findmyfood;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class AccountSettingsFragment extends Fragment {

    private View mView;

    public AccountSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_account_settings, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView loginTextView = (TextView) mView.findViewById(R.id.sayHelloTextView);
        TextView emailTextView = (TextView) mView.findViewById(R.id.infoEmailTextView);

        String[] parsedFile = parseConfigFile();
        loginTextView.setText("Witaj " + parsedFile[0]);
        emailTextView.setText("Email: " + parsedFile[1]);

        String favouriteFilter = readFavoriteFilterFromFile();
        EditText favoriteFilterEditText = (EditText) mView.findViewById(R.id.favoriteFilterEditText);
        favoriteFilterEditText.setText(favouriteFilter);
    }

    public void confirmFavoriteFilter() {
        EditText favoriteFilterEditText = (EditText) mView.findViewById(R.id.favoriteFilterEditText);
        String newFavoriteFilter = String.valueOf(favoriteFilterEditText.getText());
        writeToFile(getString(R.string.favorite_filter_file_name), newFavoriteFilter);
        writeToFile(getString(R.string.filter_file_name), newFavoriteFilter);

        Toast.makeText(getActivity(), "Ustawiono: " + newFavoriteFilter, Toast.LENGTH_SHORT).show();
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

    private String readFavoriteFilterFromFile() {
        String filename = getString(R.string.favorite_filter_file_name);
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
                if (!text.toString().trim().equals("")) {
                    return  text.toString().trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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