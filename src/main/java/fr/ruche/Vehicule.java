package fr.ruche;

public abstract class Vehicule {

    protected String color;
    protected String brand;
    protected String name;
    protected String marque;
    protected Integer passengers;

    public Vehicule(String color, String brand, String name, String marque, Integer passengers) {
        this.color = color;
        this.brand = brand;
        this.name = name;
        this.marque = marque;
        this.passengers = passengers;
    }

    public String getName() {
        return name;
    }

    public String getMarque() {
        return marque;
    }

    public Integer getPassengers() {
        return passengers;
    }
}


