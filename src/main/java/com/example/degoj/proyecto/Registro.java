package com.example.degoj.proyecto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Registro extends AppCompatActivity {

    private EditText edit_nombre;
    private EditText edit_password;
    private EditText edit_confirmacion;

    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        edit_nombre = (EditText) findViewById(R.id.editTextNombre);
        edit_password = (EditText) findViewById(R.id.editTextPassword);
        edit_confirmacion = (EditText) findViewById(R.id.editTextConfirmacion);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null ){
                   //MensajeOK("Entro");
                }else{
                    //MensajeOK("No entro");
                }
            }
        };

        Button MiBoton = (Button) findViewById(R.id.btnRegistro);
        MiBoton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                String nombre = edit_nombre.getText().toString();
                String password = edit_password.getText().toString();
                String confirmacion = edit_confirmacion.getText().toString();

                if(nombre.length() < 1){
                    MensajeOK("Debe escribir un nombre");
                }else if(!nombre.matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")){
                    MensajeOK("Debe escribir un correo para registrarse");
                }else if(password.length() < 6){
                    MensajeOK("La contrseña debe ser mayor de 6 carateres");
                } else if(!password.equals(confirmacion)){
                    MensajeOK("Las contrseñas deben ser iguales");
                }else{
                    Registrar(nombre, password);
                }
            }
        });
    }

    public void Mensaje(String msg){Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();};

    public void MensajeOK(String msg){
        View v1 = getWindow().getDecorView().getRootView();
        AlertDialog.Builder builder1 = new AlertDialog.Builder( v1.getContext());
        builder1.setMessage(msg);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {} });
        AlertDialog alert11 = builder1.create();
        alert11.show();
        ;};


    public void Registrar(String nombre, String password){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(nombre, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Mensaje("Usted ha sido Registrado");
                    Intent intento = new Intent(getApplicationContext(), InicioSesion.class);
                    startActivity(intento);
                }else{
                    MensajeOK("Usuario ya registrado");
                }
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop(){
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
    }
}
