package com.example.femtaxi;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class RegistroDriverImageActivity extends AppCompatActivity {

        ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_driver_image);

         imageView = findViewById(R.id.imageView);
    }

    public void loadImage(View view) {
        String url = "https://keraph.000webhostapp.com/public/img/prueba_31.jpg";

        Glide.with(this)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(imageView);
    }
}