package com.tmc.tomik.findmyfood;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class FilterWindow extends Activity {

    private EditText filterEditText;
    private String filename;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.filter_window);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .8), (int) (height * .3));
        filterEditText = findViewById(R.id.filterEditText);
        String currentFilter = "";

        filename = getString(R.string.filter_file_name);
        try {
            File directory = this.getFilesDir();
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

        filterEditText.setText(currentFilter);
    }

    private AlphaAnimation buttonClickEffect = new AlphaAnimation(1F, 0.5F);

    public void setFilterOnClick(View view) {
        view.startAnimation(buttonClickEffect);

        if (filterEditText != null) {
            String fileContents = String.valueOf(filterEditText.getText());
            //Toast.makeText(this, "Write: " + fileContents, Toast.LENGTH_SHORT).show();
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        finish();
    }

    public void hideFilterWindowOnClick(View view) {
        view.startAnimation(buttonClickEffect);
        finish();
    }
}
