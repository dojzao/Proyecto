package com.example.degoj.proyecto;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.degoj.proyecto.R.id.editTextAddAdmin;

/**
 * Created by degoj on 10/3/2017.
 */

public class Vista_Administrador extends Fragment {

    FirebaseDatabase firebase = FirebaseDatabase.getInstance();
    DatabaseReference bdref;

    private ArrayList<String> administradores = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_admin, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Administrador");

        bdref = firebase.getReference(FirebaseReferences.ADMINISTRADORES_REFERENCES);
        bdref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                administradores.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String nombreA = postSnapshot.getValue(String.class);
                    administradores.add(nombreA);
                }
                LlenarListView();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        DandoClickALosItems();

        TabHost host = (TabHost) getActivity().findViewById(R.id.tabAdmin);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Tab One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Tab One");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Tab Two");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Tab Two");
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("Tab Three");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Tab Three");
        host.addTab(spec);

        final TextView campotextNombre = (TextView) getActivity().findViewById(editTextAddAdmin);
        Button MiBoton = (Button) getActivity().findViewById(R.id.btnAddAdmin);
        MiBoton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                bdref = firebase.getReference(FirebaseReferences.ADMINISTRADORES_REFERENCES);
                bdref.child(campotextNombre.getText().toString().split("@")[0].replace('.', 'p')).setValue(campotextNombre.getText().toString());
                Toast.makeText(getContext(), "El usuario " + campotextNombre.getText().toString() + " es ahora Administrador", Toast.LENGTH_LONG).show();
                campotextNombre.setText("");
            }
        });
    }

    private void LlenarListView() {
        ArrayAdapter<String> adaptador = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, administradores);
        ListView milistview = (ListView) getActivity().findViewById(R.id.admin_listView);
        milistview.setAdapter(adaptador);
    }

    public void DandoClickALosItems() {
        ListView list = (ListView) getActivity().findViewById(R.id.admin_listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> paret, View viewClicked, int position, long id) {
                Toast.makeText(getContext(), "El usuario " + administradores.get(position) + " ya no es Administrador", Toast.LENGTH_LONG).show();
                bdref = firebase.getReference(FirebaseReferences.ADMINISTRADORES_REFERENCES);
                bdref.child(administradores.get(position).split("@")[0].replace('.', 'p')).removeValue();
            }
        });
    }
}
