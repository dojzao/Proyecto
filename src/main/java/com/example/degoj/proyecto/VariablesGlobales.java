package com.example.degoj.proyecto;

import java.util.ArrayList;
import java.util.List;

public class VariablesGlobales {
    private int pos = -1;

    private List<Producto> myProducts = new ArrayList<>();

    private static VariablesGlobales instance = null;

    protected VariablesGlobales() {}
    public static VariablesGlobales getInstance() {
        if(instance == null) {instance = new VariablesGlobales(); }
        return instance;
    }

    public int getPos() {
        return pos;
    }

    public void setMiproducto(int pos) {
        this.pos = pos;
    }

    public List<Producto> getMyProducts() { return myProducts;}

    public void setMyProducts(List<Producto> myProducts) {this.myProducts = myProducts;}
}
