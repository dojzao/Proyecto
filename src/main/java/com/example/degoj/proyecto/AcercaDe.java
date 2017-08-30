package com.example.degoj.proyecto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AcercaDe extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca_de);
        Mensaje("Acerca De");
    }

    public void Mensaje(String msg){getSupportActionBar().setTitle(msg);};
}
