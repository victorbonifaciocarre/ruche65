package fr.ruche;

public class Car extends Vehicule{

    protected int wheelCount;

    Car(String color, String brand, String name, String marque, Integer passengers) {
        super(color, brand, name, marque, passengers);
        this.wheelCount = 4;
    }

    private String carToString() {
        StringConverter<Car> carConverter = myVehicule -> String.format("%s %s %d", myVehicule.color, myVehicule.brand, myVehicule.wheelCount);
        return carConverter.test(this);
    }
}
