package com.example.degoj.proyecto;

import java.util.ArrayList;
import java.util.List;

public class VariablesGlobales {
    private int pos = -1;

    private List<Producto> myProducts = new ArrayList<>();
    private List<String> mySupermercados = new ArrayList<>();
    private double Total = 0;
    private double precioactual = 0;
    private boolean mensajeMostrado = true;
    private boolean todosExisten;

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

    public List<String> getmySupermercados() { return mySupermercados;}

    public void setmySupermercados(List<String> mySupermercados) {this.mySupermercados = mySupermercados;}

    public boolean ismensajeMostrado() {
        return mensajeMostrado;
    }

    public void setmensajeMostrado(boolean mensajeMostrado) {
        this.mensajeMostrado = mensajeMostrado;
    }

    public boolean isTodosExisten() {
        return todosExisten;
    }

    public void setTodosExisten(boolean todosExisten) {
        this.todosExisten = todosExisten;
    }

    public double getTotal() {
        return Total;
    }

    public void setTotal(double total) {
        Total = total;
    }

    public double getPrecioactual() {
        return precioactual;
    }

    public void setPrecioactual(double precioactual) {
        this.precioactual = precioactual;
    }
}
