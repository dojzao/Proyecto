package com.example.degoj.proyecto;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Producto implements Serializable{
    public Producto(String imagenBD, String nombre, String marca, double precio, String desc, String supermercado) {
        this.ImagenBD = imagenBD;
        this.Nombre = nombre;
        this. Marca = marca;
        this.Precio = precio;
        this.desc = desc;
        this.Supermercado = supermercado;
        this.Imagen = null;
    }

    public Producto() {
        this.Nombre = "";
    }

    public Drawable getImagen() {
        return Imagen;
    }

    public void setImagen(Drawable imagen) {
        Imagen = imagen;
    }

    public String getImagenBD() {
        return ImagenBD;
    }

    public void setImagenBD(String imagenBD) {
        ImagenBD = imagenBD;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getMarca() {
        return Marca;
    }

    public void setMarca(String marca) {
        Marca = marca;
    }

    public double getPrecio() {
        return Precio;
    }

    public void setPrecio(double precio) {
        Precio = precio;
    }

    public String getSupermercado() { return Supermercado; }

    public void setSupermercado(String supermercado) { Supermercado = supermercado; }

    public String getDesc() { return desc; }

    public void setString(String desc) { this.desc = desc; }

    public boolean isComprado() {
        return comprado;
    }

    public void setComprado(boolean comprado) {
        this.comprado = comprado;
    }
    
    @Override
    public String toString(){
        String s;
        s = Nombre + " " + Marca + " " + Precio;
        return s;
    }

    private Drawable Imagen;
    private String Nombre;
    private String Marca;
    private double Precio;
    private String desc;
    private String Supermercado;
    private String ImagenBD;
    private boolean comprado;
}
