package com.example.degoj.proyecto;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase firebase = FirebaseDatabase.getInstance();
    FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private String nombreUsuario;
    ProductoBD db;
    VariablesGlobales vg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        firebase.setPersistenceEnabled(true);

        MensajePrincipal("Menu Principal");

        Intent callingIntent = getIntent();
        nombreUsuario = callingIntent.getStringExtra("usuario");

        TextView Mi_textUsuario = (TextView) header.findViewById(R.id.textUsuario);
        Mi_textUsuario.setText(nombreUsuario);

        CrearYAbrirBaseDeDatos();

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.BorrarRegistroConIDSQL("Favorito01");
                for(Producto p : vg.getMyProducts()){
                    db.insertDatoSQL(p.getImagenBD(), p.getNombre(), p.getMarca(), p.getPrecio(), p.getCant(), p.getSupermercado(), "Favorito01");
                }
                Mensaje("Se ha guardado la lista de compra en favoritos");
            }
        });*/

        vg = VariablesGlobales.getInstance();
        displaySelectedScreen(R.id.principal);
    }

    public void Mensaje(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();};

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displaySelectedScreen(int itemId) {
        Fragment fragment = null;
        switch (itemId) {
            case R.id.principal:
                fragment = new ListaProductos();
                break;
            case R.id.acercade:
                fragment = new AcercaDe();
                break;
            case R.id.favoritos:
                fragment = new ListaProductos();
                ((ListaProductos) fragment).LlenarListFavoritos();
                break;
            case R.id.nuevoP:
                vg.setMiproducto(-1);
                fragment = new AgregarProducto();
                break;
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void MensajePrincipal(String msg){getSupportActionBar().setTitle(msg);};

    public void CrearYAbrirBaseDeDatos() {
        db = new ProductoBD(this);
        db.open();
    }
}