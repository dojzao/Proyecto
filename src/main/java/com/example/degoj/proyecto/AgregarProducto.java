package com.example.degoj.proyecto;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AgregarProducto extends AppCompatActivity {

    private static String APP_DIRECTORY = "MyPictureApp/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";

    private final int MY_PERMISSIONS = 100;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;

    RelativeLayout l;
    ImageView MiImageView;

    StorageReference storage;
    ProgressDialog progress;

    Uri path;
    private String mPath;

    FirebaseDatabase firebase = FirebaseDatabase.getInstance();
    DatabaseReference bdref;

    Spinner sprCoun;
    private ArrayList<String> NombresSupers = new ArrayList<>();
    private ShapeDrawable shape;
    private boolean yaimagen;

    private boolean MODO_EDITAR;
    VariablesGlobales vg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(Color.RED);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(3);

        yaimagen = false;
        vg = VariablesGlobales.getInstance();

        bdref = firebase.getReference(FirebaseReferences.SUPER_MERCADOS_REFERENCES);
        bdref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(vg.getPos() == -1) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        SuperMercados mercados = postSnapshot.getValue(SuperMercados.class);
                        NombresSupers.add(mercados.getNombreM());
                    }
                }else{
                    NombresSupers.add(vg.getMyProducts().get(vg.getPos()).getSupermercado());
                }
                CargarSpinner();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        storage = FirebaseStorage.getInstance().getReference();
        progress = new ProgressDialog(this);

        TextView textNombre = (TextView) findViewById(R.id.textNombre);
        TextView textMarca = (TextView) findViewById(R.id.textMarca);
        TextView textPrecio = (TextView) findViewById(R.id.textPrecio);
        TextView textCantidad = (TextView) findViewById(R.id.textCant);
        ImageView imagenProd = (ImageView) findViewById(R.id.imagenProducto);

        textMarca.setBackgroundResource(R.drawable.lost_border);
        textNombre.setBackgroundResource(R.drawable.lost_border);
        textPrecio.setBackgroundResource(R.drawable.lost_border);
        textCantidad.setBackgroundResource(R.drawable.lost_border);

        if(vg.getPos() > -1){
            MODO_EDITAR = true;
            imagenProd.setImageDrawable(vg.getMyProducts().get(vg.getPos()).getImagen());
            textNombre.setText(vg.getMyProducts().get(vg.getPos()).getNombre());
            textMarca.setText(vg.getMyProducts().get(vg.getPos()).getMarca());
            textPrecio.setText(String.valueOf(vg.getMyProducts().get(vg.getPos()).getPrecio()));
            textCantidad.setText(String.valueOf(vg.getMyProducts().get(vg.getPos()).getCant()));

            textNombre.setEnabled(false);
            textMarca.setEnabled(false);
            textCantidad.setEnabled(false);
        }else{
            MODO_EDITAR = false;
            textNombre.setEnabled(true);
            textMarca.setEnabled(true);
            textCantidad.setEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TextView textNombre = (TextView) findViewById(R.id.textNombre);
                final TextView textMarca = (TextView) findViewById(R.id.textMarca);
                final TextView textPrecio = (TextView) findViewById(R.id.textPrecio);
                final TextView textCantidad = (TextView) findViewById(R.id.textCant);

                textMarca.setBackgroundResource(R.drawable.lost_border);
                textNombre.setBackgroundResource(R.drawable.lost_border);
                textPrecio.setBackgroundResource(R.drawable.lost_border);
                textCantidad.setBackgroundResource(R.drawable.lost_border);

                try {
                    String nombre = textNombre.getText().toString();
                    String marca = textMarca.getText().toString();
                    double precio = Double.parseDouble(textPrecio.getText().toString());
                    int cant = Integer.parseInt(textCantidad.getText().toString());
                    String SuperM = sprCoun.getSelectedItem().toString();
                    String ImagenBD = nombre + "_" + marca;

                    if(!yaimagen && !MODO_EDITAR){
                        Toast.makeText(getApplicationContext(),"No ha selecionado una imagen",Toast.LENGTH_LONG).show();
                    }else if(marca.equals("")){
                        textMarca.setBackgroundResource(R.drawable.focus_border_style);
                        Toast.makeText(getApplicationContext(),"La marca no puede ser vacia",Toast.LENGTH_LONG).show();
                    }else if(nombre.equals("")){
                        textNombre.setBackgroundResource(R.drawable.focus_border_style);
                        Toast.makeText(getApplicationContext(),"El nombre no puede ser vacio",Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(getApplicationContext(), "Agregado Correctamente", Toast.LENGTH_LONG).show();
                                    textMarca.setText("");
                                    textNombre.setText("");
                                    textPrecio.setText("");
                                    textCantidad.setText("");
                                    ImageView Mi_imageview = (ImageView) findViewById(R.id.imagenProducto);
                                    Mi_imageview.setImageResource(R.drawable.imgvacia);
                                    yaimagen = false;
                                }
                            });
                        }else{
                            progress.dismiss();
                            Toast.makeText(getApplicationContext(),"Producto Actualizado",Toast.LENGTH_LONG).show();
                        }
                    }
                }catch (NumberFormatException ex){
                    if(MODO_EDITAR){
                        textPrecio.setBackgroundResource(R.drawable.focus_border_style);
                    }else{
                        textPrecio.setBackgroundResource(R.drawable.focus_border_style);
                        textCantidad.setBackgroundResource(R.drawable.focus_border_style);
                    }
                    Toast.makeText(getApplicationContext(),"No se permiten letras ni espacios vacios en estos campos",Toast.LENGTH_LONG).show();
                }
            }
        });

        mayRequestStoragePermission();

        l = (RelativeLayout) findViewById(R.id.content_agregar_producto);

        MiImageView = (ImageView) findViewById(R.id.imagenProducto);
        if(!MODO_EDITAR) {
            MiImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    showOptions();
                }
            });
        }
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
            Devolverse();
        }else {
            bdref.child(ImagenBD + "_" + mercado).setValue(producto);
        }
    }

    private void Devolverse(){
        this.finish();
    }

    private void showOptions() {
        final CharSequence[] option = {"Tomar foto", "Elegir de galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(AgregarProducto.this);
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

    private void CargarSpinner() {
        sprCoun = (Spinner) findViewById(R.id.spinnerLugares);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, NombresSupers.toArray(new String[NombresSupers.size()]));

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void openCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        boolean isDirectoryCreated = file.exists();

        if(!isDirectoryCreated) {
            file.mkdirs();
        }

        //if(isDirectoryCreated){
           // MensajeOK("Llego2");
            Long timestamp = System.currentTimeMillis() / 1000;
            String imageName = timestamp.toString() + ".jpg";

            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY + File.separator + imageName;

            File newFile = new File(mPath);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
            startActivityForResult(intent, PHOTO_CODE);
        //}
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPath = savedInstanceState.getString("file_path");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{mPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> Uri = " + uri);
                                }
                            });

                    Bitmap bitmap = BitmapFactory.decodeFile(mPath);
                    MiImageView.setImageBitmap(bitmap);
                    path = getImageUri(AgregarProducto.this, bitmap);
                    yaimagen = true;
                    break;
                case SELECT_PICTURE:
                    path = data.getData();
                    MiImageView.setImageURI(path);
                    yaimagen = true;
            }
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_PERMISSIONS){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(AgregarProducto.this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
            }
        }else{
            showExplanation();
        }
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AgregarProducto.this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }

    private boolean mayRequestStoragePermission() {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED))
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
