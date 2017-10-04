package com.example.degoj.proyecto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class ProductoBD {

    static final String PRIMARY_KEY = "id";
    static final String KEY_ROWID = "image";
    static final String KEY_Nombre = "nombre";
    static final String KEY_Marca = "marca";
    static final String KEY_Precio = "precio";
    static final String KEY_Cantidad = "cantidad";
    static final String KEY_Mercado = "mercado";
    static final String KEY_Comprado = "false";

    public  String getDatabaseName() {
        return DATABASE_NAME;
    }

    static final  String DATABASE_NAME = "SnapShop.db";
    static final String DATABASE_TABLE = "mitabla";
    static String mensaje = "Mensaje inicial";

    static final int DATABASE_VERSION = 1;

    static final String DATABASE_CREATE_vieja =
            "create table mitabla ("
                    + PRIMARY_KEY + " integer primary key autoincrement, "
                    + KEY_ROWID + " text, "
                    + KEY_Nombre + " text, "
                    + KEY_Marca + " text, "
                    + KEY_Precio + " text, "
                    + KEY_Cantidad + " text, "
                    + KEY_Mercado + " text, "
                    + KEY_Comprado + " text);";

    static final String DATABASE_CREATE_nueva =
            "create table mitabla ("
                    + PRIMARY_KEY + " integer primary key autoincrement, "
                    + KEY_ROWID + " text, "
                    + KEY_Nombre + " text, "
                    + KEY_Marca + " text, "
                    + KEY_Precio + " text, "
                    + KEY_Cantidad + " text, "
                    + KEY_Mercado + " text, "
                    + KEY_Comprado + " text);";

    final Context context;
    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public ProductoBD(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREATE_vieja);
                mensaje = "Se ha creado la BD";
            } catch (SQLException e) {
                e.printStackTrace();
                mensaje = "Error creando la BD";
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            try {
                db.execSQL(DATABASE_CREATE_nueva);
                mensaje ="Se ha creado una nueva version de la BD";
            } catch (SQLException e) {
                e.printStackTrace();
                mensaje ="Error creando la BD nueva";
            }
        }
    }

    public ProductoBD open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        mensaje = DATABASE_NAME + " abierta para escritura";
        return this;
    }

    public void close() {
        DBHelper.close();
        mensaje ="La BD se ha cerrado";
    }

    public long insertDato(String imageBD, String nombre, String marca, double precio, String desc, String mercado, boolean comprado) {
        long valor =-1;
        if(db != null) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_ROWID,  imageBD);
            initialValues.put(KEY_Nombre, nombre);
            initialValues.put(KEY_Marca,  marca);
            initialValues.put(KEY_Precio, String.valueOf(precio));
            initialValues.put(KEY_Cantidad, desc);
            initialValues.put(KEY_Mercado, mercado);
            initialValues.put(KEY_Comprado, comprado);
            valor = db.insert(DATABASE_TABLE, null, initialValues);
        }
        mensaje = "Insertando. valor ="+valor;
        return valor;
    }

    void insertDatoSQL(String imageBD, String nombre, String marca, double precio, String desc, String mercado, boolean comprado) {
        String orden = "INSERT INTO " + DATABASE_TABLE + " (" + KEY_ROWID + "," + KEY_Nombre + ","
                + KEY_Marca + "," + KEY_Precio + "," + KEY_Cantidad + "," + KEY_Mercado + "," + KEY_Comprado
                +") VALUES ('" +  imageBD + "','" + nombre + "','" + marca + "','"
                + String.valueOf(precio) + "'," + desc + ",'" + mercado + "','" + comprado + "')";
        try {
            db.execSQL(orden);
            mensaje ="Inserción OK";
        } catch (SQLException e) {
            e.printStackTrace();
            mensaje ="Error insertando";

        }
    }

    public boolean BorrarRegistroConID(long rowId) {
        mensaje = "Registro con codigo "+rowId+" de la BD";
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    void BorrarRegistroConIDSQL(String favoritoID) {
        try {
            db.execSQL("DELETE FROM "+ DATABASE_TABLE + " WHERE " + KEY_Comprado + " = '" +  favoritoID + "'");
            mensaje ="Borrado OK";
        } catch (SQLException e) {
            e.printStackTrace();
            mensaje ="Error borrando";
        }
    }

    public int NumeroRegistrosTabla() {
        String countQuery = "SELECT  * FROM " + DATABASE_TABLE;
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    void VaciarTabla(){
        try {
            db.execSQL("delete from " + DATABASE_TABLE + " where 1 ");
            mensaje ="Borrado OK";
        } catch (SQLException e) {
            e.printStackTrace();
            mensaje ="Error borrando";
        }

    }

    public ArrayList<Producto> Seleccione(){
        ArrayList<Producto> respuesta = new ArrayList<>();
        Cursor c = db.query(true, DATABASE_TABLE, new String[] {PRIMARY_KEY, KEY_ROWID, KEY_Nombre, KEY_Marca,
        KEY_Precio, KEY_Cantidad, KEY_Mercado, KEY_Comprado}, null, null, null, null, null, null);
        System.err.println("HIZO CARGA");

        if (c.moveToFirst()) {
            do {
                Integer codigo = c.getInt(0);
                System.err.println("HIZO CARGA: "+ codigo);
                String image = c.getString(1);
                String nombre = c.getString(2);
                String marca = c.getString(3);
                double precio = Double.parseDouble(c.getString(4));
                String desc = c.getString(5);
                String mercado = c.getString(6);
                boolean comprado = Boolean.parseBoolean(c.getString(7));
                respuesta.add(new Producto(image, nombre, marca, precio, desc, mercado));
                respuesta.get(respuesta.size() - 1).setComprado(comprado);
            } while(c.moveToNext());
        }
        return respuesta;
    }

    public void BorrarPrimeraFila() {
        Cursor cursor = db.query(DATABASE_TABLE, null, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            String rowId = cursor.getString(cursor.getColumnIndex(KEY_ROWID));
            db.delete(DATABASE_TABLE, KEY_ROWID + "=?",  new String[]{rowId});
        }
    }
}
