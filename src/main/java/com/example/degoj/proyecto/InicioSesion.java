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
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class InicioSesion extends AppCompatActivity {

    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        final EditText nombre = (EditText) findViewById(R.id.editText);
        final EditText password = (EditText) findViewById(R.id.editText2);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null ){
                    MensajeOK("Entro");
                }else{
                    MensajeOK("No entro");
                }
            }
        };

        TextView MiTextView = (TextView) findViewById(R.id.TextRegistro);
        MiTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                Intent intento = new Intent(getApplicationContext(), Registro.class);
                startActivity(intento);
            }
        });

        Button MiButton = (Button) findViewById(R.id.btnSesion);
        MiButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                if(nombre.getText().toString().equals("") || password.getText().toString().equals("")){
                    MensajeOK("No se permiten espacios vacios");
                }else {
                    IniciarSesion(nombre.getText().toString(), password.getText().toString());
                }
            }

        });
    }

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

    public void IniciarSesion(String nombre, String password){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(nombre, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    final EditText nombre = (EditText) findViewById(R.id.editText);
                    Intent intento = new Intent(getApplicationContext(), MainActivity.class);
                    intento.putExtra("usuario", nombre.getText().toString());
                    startActivity(intento);
                }else{
                    MensajeOK("Usuario o contraseña incorrecta");
                }
            }
        });
    }
}
