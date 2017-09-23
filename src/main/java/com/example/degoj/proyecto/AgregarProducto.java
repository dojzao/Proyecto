package com.example.degoj.proyecto;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static com.example.degoj.proyecto.R.id.textMarca;
import static com.example.degoj.proyecto.R.id.textNombre;
import static com.example.degoj.proyecto.R.id.textPrecio;

public class AgregarProducto extends Fragment {

    private final int MY_PERMISSIONS = 100;
    private final int PHOTO_CODE = 111;
    private final int SELECT_PICTURE = 300;

    RelativeLayout l;
    ImageView MiImageView;

    StorageReference storage;
    ProgressDialog progress;

    Uri path;

    FirebaseDatabase firebase = FirebaseDatabase.getInstance();
    DatabaseReference bdref;

    Spinner sprCoun;
    private ShapeDrawable shape;
    private boolean yaimagen;

    private List<String> superauxiliar;
    private boolean MODO_EDITAR;
    VariablesGlobales vg;

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_agregar_producto, container, false);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Agregar Producto");

        shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(Color.RED);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(3);

        yaimagen = false;
        vg = VariablesGlobales.getInstance();

        storage = FirebaseStorage.getInstance().getReference();
        progress = new ProgressDialog(view.getContext());

        TextView campotextNombre = (TextView) getActivity().findViewById(textNombre);
        TextView campotextMarca = (TextView) getActivity().findViewById(textMarca);
        TextView campotextPrecio = (TextView) getActivity().findViewById(textPrecio);
        TextView textCantidad = (TextView) getActivity().findViewById(R.id.textCant);
        ImageView imagenProd = (ImageView) getActivity().findViewById(R.id.imagenProducto);

        campotextMarca.setBackgroundResource(R.drawable.lost_border);
        campotextNombre.setBackgroundResource(R.drawable.lost_border);
        campotextPrecio.setBackgroundResource(R.drawable.lost_border);
        textCantidad.setBackgroundResource(R.drawable.lost_border);

        superauxiliar = new ArrayList<>();
        superauxiliar.addAll(vg.getmySupermercados());

        if(vg.getPos() > -1){
            MODO_EDITAR = true;
            imagenProd.setImageDrawable(vg.getMyProducts().get(vg.getPos()).getImagen());
            campotextNombre.setText(vg.getMyProducts().get(vg.getPos()).getNombre());
            campotextMarca.setText(vg.getMyProducts().get(vg.getPos()).getMarca());
            campotextPrecio.setText(String.valueOf(vg.getMyProducts().get(vg.getPos()).getPrecio()));
            textCantidad.setText(String.valueOf(vg.getMyProducts().get(vg.getPos()).getCant()));
            superauxiliar.clear();
            superauxiliar.add(vg.getMyProducts().get(vg.getPos()).getSupermercado());

            campotextNombre.setEnabled(false);
            campotextMarca.setEnabled(false);
            textCantidad.setEnabled(false);
        }else{
            MODO_EDITAR = false;
            superauxiliar.remove(0);
            campotextNombre.setEnabled(true);
            campotextMarca.setEnabled(true);
            textCantidad.setEnabled(true);
        }

        CargarSpinner();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TextView campotextNombre = (TextView) getActivity().findViewById(textNombre);
                final TextView campotextMarca = (TextView) getActivity().findViewById(textMarca);
                final TextView campotextPrecio = (TextView) getActivity().findViewById(textPrecio);
                final TextView textCantidad = (TextView) getActivity().findViewById(R.id.textCant);

                campotextMarca.setBackgroundResource(R.drawable.lost_border);
                campotextNombre.setBackgroundResource(R.drawable.lost_border);
                campotextPrecio.setBackgroundResource(R.drawable.lost_border);
                textCantidad.setBackgroundResource(R.drawable.lost_border);

                try {
                    String nombre = campotextNombre.getText().toString();
                    String marca = campotextMarca.getText().toString();
                    double precio = Double.parseDouble(campotextPrecio.getText().toString());
                    int cant = Integer.parseInt(textCantidad.getText().toString());
                    String SuperM = sprCoun.getSelectedItem().toString();
                    String ImagenBD = nombre + "_" + marca;

                    if(!yaimagen && !MODO_EDITAR){
                        Toast.makeText(getActivity().getApplicationContext(),"No ha selecionado una imagen",Toast.LENGTH_LONG).show();
                    }else if(marca.equals("")){
                        campotextMarca.setBackgroundResource(R.drawable.focus_border_style);
                        Toast.makeText(getActivity().getApplicationContext(),"La marca no puede ser vacia",Toast.LENGTH_LONG).show();
                    }else if(nombre.equals("")){
                        campotextNombre.setBackgroundResource(R.drawable.focus_border_style);
                        Toast.makeText(getActivity().getApplicationContext(),"El nombre no puede ser vacio",Toast.LENGTH_LONG).show();
                    }else {
                        progress.setMessage("uploading");
                        progress.show();
                        AgregarProductoBD(ImagenBD, nombre, marca, precio, cant, SuperM);
                        if(!MODO_EDITAR) {
                            StorageReference pathRE = storage.child("photos").child("Imagen_" + ImagenBD);
                            pathRE.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progress.dismiss();
                                    Toast.makeText(getActivity().getApplicationContext(), "Agregado Correctamente", Toast.LENGTH_LONG).show();
                                    campotextMarca.setText("");
                                    campotextNombre.setText("");
                                    campotextPrecio.setText("");
                                    textCantidad.setText("");
                                    ImageView Mi_imageview = (ImageView) getActivity().findViewById(R.id.imagenProducto);
                                    Mi_imageview.setImageResource(R.drawable.imgvacia);
                                    yaimagen = false;
                                }
                            });
                        }else{
                            progress.dismiss();
                            Toast.makeText(getActivity().getApplicationContext(),"Producto Actualizado",Toast.LENGTH_LONG).show();
                        }
                    }
                }catch (NumberFormatException ex){
                    if(MODO_EDITAR){
                        campotextPrecio.setBackgroundResource(R.drawable.focus_border_style);
                    }else{
                        campotextPrecio.setBackgroundResource(R.drawable.focus_border_style);
                        textCantidad.setBackgroundResource(R.drawable.focus_border_style);
                    }
                    Toast.makeText(getActivity().getApplicationContext(),"No se permiten letras ni espacios vacios en estos campos",Toast.LENGTH_LONG).show();
                }
            }
        });

        mayRequestStoragePermission();

        l = (RelativeLayout) view.findViewById(R.id.content_agregar_producto);

        MiImageView = (ImageView) view.findViewById(R.id.imagenProducto);
        if(!MODO_EDITAR) {
            MiImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    showOptions();
                }
            });
        }
    }

    private void CargarSpinner() {
        sprCoun = (Spinner) view.findViewById(R.id.spinnerLugares);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                view.getContext(), android.R.layout.simple_spinner_item, superauxiliar.toArray(new String[superauxiliar.size()]));

        sprCoun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sprCoun.setAdapter(adapter);
    }

    private void AgregarProductoBD(String ImagenBD, String nombre, String Marca, double precio, int cant, String mercado) {
        bdref = firebase.getReference(FirebaseReferences.PRODUCTOS_REFERENCES);
        final Producto producto = new Producto("Imagen_" + ImagenBD, nombre, Marca, precio, cant, mercado);
        if(MODO_EDITAR){
            for(int i = 0; i < vg.getMyProducts().size(); i++){
                if(vg.getMyProducts().get(i).getImagenBD().equals(producto.getImagenBD())
                        && vg.getMyProducts().get(i).getSupermercado().equals(mercado)){
                    vg.getMyProducts().set(i, new Producto("Imagen_" + ImagenBD, nombre, Marca, precio, cant, mercado));
                }
            }
            bdref.child(ImagenBD + "_" + mercado).child("precio").setValue(producto.getPrecio());
        }else {
            bdref.child(ImagenBD + "_" + mercado).setValue(producto);
        }
    }

    private void showOptions() {
        final CharSequence[] option = {"Tomar foto", "Elegir de galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Eleige una opción");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == "Tomar foto"){
                    openCamera();
                }else if(option[which] == "Elegir de galeria"){
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                }else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    public void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(view.getContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 111);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case PHOTO_CODE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    MiImageView.setImageBitmap(imageBitmap);
                    encodeBitmapAndSaveToFirebase(imageBitmap);
                    yaimagen = true;
                    break;

                case SELECT_PICTURE:
                    path = data.getData();
                    MiImageView.setImageURI(path);
                    yaimagen = true;
            }
        }
    }

    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String routepath = MediaStore.Images.Media.insertImage(view.getContext().getContentResolver(), bitmap, "Title", null);
        path = Uri.parse(routepath);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_PERMISSIONS){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(view.getContext(), "Permisos aceptados", Toast.LENGTH_SHORT).show();
            }
        }else{
            showExplanation();
        }
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //finish();
            }
        });

        builder.show();
    }

    private boolean mayRequestStoragePermission() {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if((ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
            return true;

        if((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))){
            Snackbar.make(l, "Los permisos son necesarios para poder usar la aplicación",
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                }
            });
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
        }

        return false;
    }
}
