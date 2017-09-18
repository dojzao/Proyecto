package com.example.degoj.proyecto;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.example.degoj.proyecto.R.id.precio;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase firebase = FirebaseDatabase.getInstance();
    DatabaseReference bdref;

    private SearchView searchView;
    private AutoCompleteTextView SearchText;

    private StorageReference storage;
    FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private String nombreUsuario;
    private double Total;
    private double precioactual;
    TextView Mi_total;
    TextView Mi_actual;

    private List<String> Llaves = new ArrayList<>();
    private List<String> searchList = new ArrayList<>();

    ArrayAdapter<Producto> adapter;
    ProductoBD db;
    private Spinner sprCoun;
    private ArrayList<String> NombresSupers = new ArrayList<>();
    VariablesGlobales vg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebase.setPersistenceEnabled(true);

        MensajePrincipal("Menu Principal");

        Intent callingIntent = getIntent();
        nombreUsuario = callingIntent.getStringExtra("usuario");

        CrearYAbrirBaseDeDatos();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.BorrarRegistroConIDSQL("Favorito01");
                for(Producto p : vg.getMyProducts()){
                    db.insertDatoSQL(p.getImagenBD(), p.getNombre(), p.getMarca(), p.getPrecio(), p.getCant(), p.getSupermercado(), "Favorito01");
                }
                Mensaje("Se ha guardado la lista de compra en favoritos");
            }
        });

        Total = 0.0;
        precioactual = 0.0;
        vg = VariablesGlobales.getInstance();

        storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://dbsnapshop.appspot.com");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        TextView Mi_textUsuario = (TextView) header.findViewById(R.id.textUsuario);
        Mi_textUsuario.setText(nombreUsuario);

        ObtenerProductos();

        bdref = firebase.getReference(FirebaseReferences.SUPER_MERCADOS_REFERENCES);
        bdref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                NombresSupers.add("Todos");
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    SuperMercados mercados = postSnapshot.getValue(SuperMercados.class);
                    NombresSupers.add(mercados.getNombreM());
                }
                CargarSpinner();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                MensajeOK("Se cancelo");
            }
        });

        Mi_total = (TextView) findViewById(R.id.textViewPagar);
        Mi_total.setText("0.0");
        Mi_actual = (TextView) findViewById(R.id.textViewActual);
        Mi_actual.setText("0.0");
        LlenarListView();
        RegistrarClicks();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            }
        };
        SearchText = (AutoCompleteTextView) findViewById(R.id.editTextBuscar);
        SearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ObtenerProducto(SearchText.getText().toString());
                SearchText.setText("");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void MensajePrincipal(String msg){getSupportActionBar().setTitle(msg);};

    public void CrearYAbrirBaseDeDatos() {
        db = new ProductoBD(this);
        db.open();
    }

    private ArrayAdapter<String> getProductosAdapter() {
        return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Llaves);
    }

    private void ObtenerProductos() {
        bdref = firebase.getReference(FirebaseReferences.PRODUCTOS_REFERENCES);
        bdref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Llaves.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Long cantidad = (Long) postSnapshot.child("cant").getValue();
                    if(cantidad > 1){
                        Llaves.add(cantidad + "-" + postSnapshot.getKey() + "_" + postSnapshot.child("precio").getValue());
                    }else{
                        Llaves.add(postSnapshot.getKey() + "_" + postSnapshot.child("precio").getValue());
                    }
                }
                SearchText.setAdapter(getProductosAdapter());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ObtenerProducto(String Llave) {
        bdref = firebase.getReference(FirebaseReferences.PRODUCTOS_REFERENCES);
        String LlaveArray[] = Llave.split("_");
        Llave = LlaveArray[0] + "_" + LlaveArray[1] + "_" + LlaveArray[2];
        LlaveArray = Llave.split("-");
        if(LlaveArray.length > 1){
            Llave = LlaveArray[1];
        }

        bdref.child(Llave).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Producto producto = dataSnapshot.getValue(Producto.class);
                producto.setComprado(false);
                vg.getMyProducts().add(producto);
                Ordenar();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        bdref.child(Llave).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Total = 0;
                precioactual = 0;
                for(Producto p : vg.getMyProducts()) {
                    Total += p.getPrecio();
                    if(p.isComprado()){
                        precioactual += p.getPrecio();
                    }
                }
                Mi_total.setText(String.valueOf(Total));
                update();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void Ordenar(){
        Collections.sort(vg.getMyProducts(), new Comparator<Producto>() {
            @Override
            public int compare(Producto fruit2, Producto fruit1) {
                return  fruit2.getImagenBD().compareTo(fruit1.getImagenBD());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        final CursorAdapter suggestionAdapter = new SimpleCursorAdapter(this,
                R.layout.desplegar_search_view,
                null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1},
                new int[]{R.id.itemDespliegue},
                0);

        searchView.setSuggestionsAdapter(suggestionAdapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                ObtenerProducto(searchList.get(position));
                searchView.clearFocus();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String[] columns = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA,};
                filter(newText, sprCoun.getSelectedItem().toString());
                MatrixCursor cursor = new MatrixCursor(columns);
                for (int i = 0; i < searchList.size(); i++) {
                    String[] tmp = {Integer.toString(i), searchList.get(i), searchList.get(i)};
                    cursor.addRow(tmp);
                }
                suggestionAdapter.swapCursor(cursor);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void filter(String charText, String mercado) {
        charText = charText.toLowerCase(Locale.getDefault());
        if(mercado.equals("Todos")){
            mercado = charText;
        }else{
            mercado = mercado.toLowerCase(Locale.getDefault());
        }
        searchList.clear();
        if (charText.length() == 0) {
            searchList.addAll(Llaves);
        } else {
            for (String s : Llaves) {
                if (s.toLowerCase(Locale.getDefault()).contains(charText) && s.toLowerCase(Locale.getDefault()).contains(mercado)) {
                    searchList.add(s);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.favoritos) {
            vg.getMyProducts().clear();
            vg.getMyProducts().addAll(db.Seleccione("Favorito01"));
            Total = 0;
            precioactual = 0;
            for(Producto p : vg.getMyProducts()){
                Total += p.getPrecio();
            }
            Mi_total.setText(String.valueOf(Total));
            Mi_actual.setText("0");
            LlenarListView();
        } else if (id == R.id.nuevoP) {
            Intent intento = new Intent(getApplicationContext(), AgregarProducto.class);
            vg.setMiproducto(-1);
            startActivity(intento);
        }else if (id == R.id.acercade) {
            Intent intento = new Intent(getApplicationContext(), AcercaDe.class);
            startActivity(intento);
        } else if (id == R.id.salir) {
            mAuth.signOut();
            FirebaseAuth.getInstance().signOut();
            Intent intento = new Intent(getApplicationContext(), InicioSesion.class);
            startActivity(intento);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void Mensaje(String msg){Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();};

    private void LlenarListView() {
        adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.cars_listView);
        list.setAdapter(adapter);
    }

    private void update(){
        Hilo01 MiHilo01 = new Hilo01();
        MiHilo01.execute();
    }

    private class Hilo01 extends AsyncTask<Void,Integer,Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean resultado) {

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    private void RegistrarClicks() {
        ListView list = (ListView) findViewById(R.id.cars_listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {
                vg.getMyProducts().get(position).setComprado(!vg.getMyProducts().get(position).isComprado());
                if(vg.getMyProducts().get(position).isComprado()){
                    precioactual += vg.getMyProducts().get(position).getPrecio();
                    Mi_actual.setText(String.valueOf(precioactual));
                }else{
                    precioactual -= vg.getMyProducts().get(position).getPrecio();
                    Mi_actual.setText(String.valueOf(precioactual));
                }
                update();
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showOptions(position);
                return false;
            }
        });
    }

    private void showOptions(final int position) {
        final CharSequence[] option = {"Detalle", "Borrar", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Eleige una opción");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == "Detalle"){
                    Intent intento = new Intent(getApplicationContext(), AgregarProducto.class);
                    vg.setMiproducto(position);
                    startActivity(intento);
                }else if(option[which] == "Borrar"){
                    Total -= vg.getMyProducts().get(position).getPrecio();
                    if(vg.getMyProducts().get(position).isComprado()){
                        precioactual -= vg.getMyProducts().get(position).getPrecio();
                        Mi_actual.setText(String.valueOf(precioactual));
                    }
                    Mi_total.setText(String.valueOf(Total));
                    vg.getMyProducts().remove(position);
                    LlenarListView();
                }else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void CargarSpinner() {
        sprCoun = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, NombresSupers.toArray(new String[NombresSupers.size()]));

        sprCoun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                for(int i = 0; i < vg.getMyProducts().size(); i++){
                    final int pos = i;
                    if(!vg.getMyProducts().get(i).getSupermercado().equals(sprCoun.getSelectedItem().toString())){
                        Total -=  vg.getMyProducts().get(i).getPrecio();
                        bdref = firebase.getReference(FirebaseReferences.PRODUCTOS_REFERENCES);
                        bdref.child(vg.getMyProducts().get(i).getNombre() + "_" + vg.getMyProducts().get(i).getMarca() + "_"
                                + sprCoun.getSelectedItem().toString()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    Producto producto = dataSnapshot.getValue(Producto.class);
                                    producto.setComprado(false);
                                    vg.getMyProducts().set(pos, producto);
                                    Total += producto.getPrecio();
                                    Mi_total.setText(String.valueOf(Total));
                                    LlenarListView();
                                }catch (NullPointerException ex){
                                    Total +=  vg.getMyProducts().get(pos).getPrecio();
                                    Mi_total.setText(String.valueOf(Total));
                                    LlenarListView();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sprCoun.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<Producto> {
        public MyListAdapter() {
            super(MainActivity.this, R.layout.item_view, vg.getMyProducts());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
            }

            final Producto currentCar = vg.getMyProducts().get(position);

            if (!sprCoun.getSelectedItem().toString().equals("Todos") && !sprCoun.getSelectedItem().toString().equals(currentCar.getSupermercado())) {
                itemView.setBackgroundColor(Color.RED);
            }else{
                itemView.setBackgroundColor(Color.parseColor("#FFFF66"));
            }

            final ImageView MIimageView = (ImageView) itemView.findViewById(R.id.img);
            if(currentCar.getImagen() == null) {
                storage.child("photos/" + currentCar.getImagenBD()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        MIimageView.setImageURI(uri);
                        Picasso.with(MIimageView.getContext()).load(uri.toString()).into(MIimageView);
                        currentCar.setImagen(MIimageView.getDrawable());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        MensajeOK("Error al cargar la imagen");
                    }
                });
            }else{
                MIimageView.setImageDrawable(currentCar.getImagen());
            }

            TextView makeText = (TextView) itemView.findViewById(R.id.nombre);
            makeText.setText("  " + currentCar.getNombre() + ", " + currentCar.getMarca());

            TextView makeText2 = (TextView) itemView.findViewById(R.id.textsuper);
            makeText2.setText("   " + currentCar.getSupermercado());

            TextView condionText = (TextView) itemView.findViewById(precio);
            if(currentCar.getCant() > 1){
                condionText.setText("  Precio: " + String.valueOf(currentCar.getPrecio()) + "  Cantidad: " + currentCar.getCant());
            }else{
                condionText.setText("  Precio: " + String.valueOf(currentCar.getPrecio()));
            }

            ImageView MimageBox = (ImageView) itemView.findViewById(R.id.imageBox);
            if(currentCar.isComprado()){
                MimageBox.setImageResource(android.R.drawable.checkbox_on_background);
            }else{
                MimageBox.setImageResource(android.R.drawable.checkbox_off_background);
            }

            return itemView;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);
    }
}