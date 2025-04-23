package fr.ruche;

@FunctionalInterface
public interface StringConverter<T extends Vehicule> {
    String test(T myVehicule);
}




