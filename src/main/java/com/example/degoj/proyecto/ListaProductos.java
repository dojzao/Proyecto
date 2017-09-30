package com.example.degoj.proyecto;

import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import static com.example.degoj.proyecto.R.id.precio;

public class ListaProductos extends Fragment {

    FirebaseDatabase firebase = FirebaseDatabase.getInstance();
    DatabaseReference bdref;

    private AutoCompleteTextView SearchText;
    private StorageReference storage;

    TextView Mi_total;
    TextView Mi_actual;

    private List<String> Llaves = new ArrayList<>();

    ArrayAdapter<Producto> adapter;
    ProductoBD db;
    private Spinner sprCoun;
    VariablesGlobales vg;

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_listaproductos, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Lista de Productos");

        vg = VariablesGlobales.getInstance();
        storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://dbsnapshop.appspot.com");

        bdref = firebase.getReference(FirebaseReferences.SUPER_MERCADOS_REFERENCES);
        bdref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                vg.getmySupermercados().clear();
                vg.getmySupermercados().add("Todos");
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    SuperMercados mercados = postSnapshot.getValue(SuperMercados.class);
                    vg.getmySupermercados().add(mercados.getNombreM());
                }
                CargarSpinner();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                MensajeOK("Se cancelo");
            }
        });

        CargarSpinner();

        SearchText = (AutoCompleteTextView) view.findViewById(R.id.editTextBuscar);
        SearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ObtenerProducto(SearchText.getText().toString());
                SearchText.setText("");
            }
        });

        ObtenerProductos();

        Mi_total = (TextView) view.findViewById(R.id.textViewPagar);
        Mi_total.setText(String.valueOf(vg.getTotal()));
        Mi_actual = (TextView) view.findViewById(R.id.textViewActual);
        Mi_actual.setText(String.valueOf(vg.getPrecioactual()));
        LlenarListView();
        RegistrarClicks();
    }

    private ArrayAdapter<String> getProductosAdapter() {
        return new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, Llaves);
    }

    private void ObtenerProductos() {
        bdref = firebase.getReference(FirebaseReferences.PRODUCTOS_REFERENCES);
        bdref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Llaves.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Llaves.add(postSnapshot.getKey() + "-" + postSnapshot.child("precio").getValue());
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
        String LlaveArray[] = Llave.split("-");
        Llave = LlaveArray[0];

        bdref.child(Llave).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Producto producto = dataSnapshot.getValue(Producto.class);
                vg.getMyProducts().add(producto);
                vg.setTotal(vg.getTotal() + vg.getMyProducts().get(vg.getMyProducts().size() - 1).getPrecio());
                Mi_total.setText(String.valueOf(vg.getTotal()));
                update();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        bdref.child(Llave).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Ordenar();
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

    private void update(){
        adapter.notifyDataSetChanged();
    }

    private void CargarSpinner() {
        sprCoun = (Spinner) getView().findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_item, vg.getmySupermercados().toArray(new String[vg.getmySupermercados().size()]));

        sprCoun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                vg.setTodosExisten(true);
                vg.setTotal(0);
                vg.setPrecioactual(0);
                for(int i = 0; i < vg.getMyProducts().size(); i++){
                    final int pos = i;
                    String nombreSuper;
                    if(!vg.getMyProducts().get(i).getSupermercado().equals(sprCoun.getSelectedItem().toString())){
                        if(sprCoun.getSelectedItem().toString() == "Todos"){
                            nombreSuper = vg.getMyProducts().get(i).getSupermercado();
                        }else{
                            nombreSuper = sprCoun.getSelectedItem().toString();
                        }
                        bdref = firebase.getReference(FirebaseReferences.PRODUCTOS_REFERENCES);
                        bdref.child(vg.getMyProducts().get(i).getNombre() + " " + vg.getMyProducts().get(i).getMarca() + " "
                                + nombreSuper).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    Producto producto = dataSnapshot.getValue(Producto.class);
                                    producto.setComprado(vg.getMyProducts().get(pos).isComprado());
                                    vg.getMyProducts().set(pos, producto);
                                    update();
                                }catch (NullPointerException ex){
                                    update();
                                    if (!vg.ismensajeMostrado() && vg.isTodosExisten()) {
                                        MensajeOK("Algunos productos no se encuentran en el supermercado, dichos productos estan marcados en Rojo");
                                        vg.setTodosExisten(false);
                                        vg.setmensajeMostrado(true);
                                    }
                                }
                                vg.setTotal(vg.getTotal() +  vg.getMyProducts().get(pos).getPrecio());
                                if(vg.getMyProducts().get(pos).isComprado()){
                                    vg.setPrecioactual(vg.getPrecioactual() + vg.getMyProducts().get(pos).getPrecio());
                                }
                                Mi_total.setText(String.valueOf(vg.getTotal()));
                                Mi_actual.setText(String.valueOf(vg.getPrecioactual()));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }else{
                        vg.setTotal(vg.getTotal() +  vg.getMyProducts().get(pos).getPrecio());
                        if(vg.getMyProducts().get(pos).isComprado()){
                            vg.setPrecioactual(vg.getPrecioactual() + vg.getMyProducts().get(pos).getPrecio());
                        }
                        Mi_total.setText(String.valueOf(vg.getTotal()));
                        Mi_actual.setText(String.valueOf(vg.getPrecioactual()));
                    }
                    update();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sprCoun.setAdapter(adapter);
    }

    private void RegistrarClicks() {
        ListView list = (ListView) view.findViewById(R.id.cars_listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {
                vg.getMyProducts().get(position).setComprado(!vg.getMyProducts().get(position).isComprado());
                if(vg.getMyProducts().get(position).isComprado()){
                    vg.setPrecioactual(vg.getPrecioactual() + vg.getMyProducts().get(position).getPrecio());
                    Mi_actual.setText(String.valueOf(vg.getPrecioactual()));
                }else{
                    vg.setPrecioactual(vg.getPrecioactual() - vg.getMyProducts().get(position).getPrecio());
                    Mi_actual.setText(String.valueOf(vg.getPrecioactual()));
                }
                update();
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showOptions(position);
                return true;
            }
        });
    }

    private void showOptions(final int position) {
        final CharSequence[] option = {"Detalle", "Borrar", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Eleige una opción");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == "Detalle"){
                    vg.setMiproducto(position);
                    Fragment fragment = new AgregarProducto();
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, fragment);
                    ft.commit();
                    DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);

                }else if(option[which] == "Borrar"){
                    vg.setTotal(vg.getTotal() - vg.getMyProducts().get(position).getPrecio());
                    if(vg.getMyProducts().get(position).isComprado()){
                        vg.setPrecioactual(vg.getPrecioactual() - vg.getMyProducts().get(position).getPrecio());
                        Mi_actual.setText(String.valueOf(vg.getPrecioactual()));
                    }
                    Mi_total.setText(String.valueOf(vg.getTotal()));
                    vg.getMyProducts().remove(position);
                    update();
                }else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void LlenarListFavoritos(){
        /*vg.getMyProducts().clear();
        vg.getMyProducts().addAll(db.Seleccione("Favorito01"));
        Total = 0;
        precioactual = 0;
        for(Producto p : vg.getMyProducts()){
            Total += p.getPrecio();
        }
        Mi_total.setText(String.valueOf(Total));
        Mi_actual.setText("0");
        LlenarListView();*/
    }

    private void LlenarListView() {
        adapter = new MyListAdapter();
        ListView list = (ListView) getView().findViewById(R.id.cars_listView);
        list.setAdapter(adapter);
    }

    public void MensajeOK(String msg){
        AlertDialog.Builder builder1 = new AlertDialog.Builder( view.getContext());
        builder1.setMessage(msg);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {} });
        AlertDialog alert11 = builder1.create();
        alert11.show();
        ;};

    private class MyListAdapter extends ArrayAdapter<Producto> {
        public MyListAdapter() {
            super(view.getContext(), R.layout.item_view, vg.getMyProducts());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = LayoutInflater.from(getActivity()).inflate(R.layout.item_view, parent, false);
            }

            final Producto current = vg.getMyProducts().get(position);

            if (!sprCoun.getSelectedItem().toString().equals("Todos") && !sprCoun.getSelectedItem().toString().equals(current.getSupermercado())) {
                itemView.setBackgroundColor(Color.RED);
            }else{
                itemView.setBackgroundColor(Color.parseColor("#FFFF66"));
            }

            final ImageView MIimageView = (ImageView) itemView.findViewById(R.id.img);
            if(current.getImagen() == null) {
                storage.child("photos/" + current.getImagenBD()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        MIimageView.setImageURI(uri);
                        Picasso.with(MIimageView.getContext()).load(uri.toString()).into(MIimageView);
                        current.setImagen(MIimageView.getDrawable());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        MensajeOK("Error al cargar la imagen");
                    }
                });
            }else{
                MIimageView.setImageDrawable(current.getImagen());
            }

            TextView makeText = (TextView) itemView.findViewById(R.id.nombre);
            makeText.setText("  " + current.getNombre() + ", " + current.getMarca());

            TextView makeText2 = (TextView) itemView.findViewById(R.id.textsuper);
            makeText2.setText("   " + current.getSupermercado());

            TextView condionText = (TextView) itemView.findViewById(precio);
            condionText.setText("  Precio: " + String.valueOf(current.getPrecio()));

            ImageView MimageBox = (ImageView) itemView.findViewById(R.id.imageBox);

            if(current.isComprado()){
                MimageBox.setImageResource(android.R.drawable.checkbox_on_background);
            }else{
                MimageBox.setImageResource(android.R.drawable.checkbox_off_background);
            }

            return itemView;
        }
    }
}
