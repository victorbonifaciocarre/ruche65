package fr.ruche;

public class Plane extends Vehicule{

    private int wheelCount;

    Plane(String color, String brand, String name, String marque, Integer passengers) {
        super(color, brand, name, marque, passengers);
        wheelCount = 5;
    }
}
