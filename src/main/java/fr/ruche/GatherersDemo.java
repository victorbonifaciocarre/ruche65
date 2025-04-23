package fr.ruche;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;

public class GatherersDemo {

    /**
     * Crée un Gatherer permettant d'éliminer les doublons selon une clé d'extraction personnalisée.
     * On utilise un Set pour stocker les clés déjà rencontrées, et une liste pour collecter les éléments uniques.
     *
     * @param keyExtractor fonction qui extrait une clé d'un élément
     * @return un Gatherer qui retourne une Map.Entry contenant l'ensemble des clés et la liste filtrée
     */
    public static <T> Gatherer<T, ?, Map.Entry<Set<String>, List<T>>> distinctBy(Function<T, String> keyExtractor) {
        return Gatherers.<T, Map.Entry<Set<String>, List<T>>>fold(
            () -> new AbstractMap.SimpleEntry<>(new HashSet<>(), new ArrayList<>()),
            (entry, item) -> {
                String key = keyExtractor.apply((T) item);
                if (entry.getKey().add(key)) {
                    entry.getValue().add((T) item);
                }
                return entry;
            }
        );
    }

    public static void main(String[] args) {

        List<Plane> planes = List.of(
                new Plane("White", "Boeing", "Dreamliner", "787", 300),
                new Plane("White", "Boeing", "Dreamliner", "787", 300),
                new Plane("Blue", "Airbus", "A320", "Neo", 180),
                new Plane("Red", "Airbus", "A350", "XWB", 350),
                new Plane("Red", "Airbus", "A350", "XWB", 350)
        );

        /**
         * Use case 1 : Fenêtrage fixe
         * Groupe les avions par paquets fixes de 3.
         * Pratique pour découper une séquence en blocs d’une taille définie.
         */
        System.out.println("=== Fenêtrage fixe (3 avions par groupe) ===");
        Stream<List<Plane>> windowed = planes
            .stream()
            .gather(Gatherers.windowFixed(3));
        windowed.forEach(group -> {
            System.out.println("Groupe : ");
            group.forEach(p -> System.out.println("- " + p.getName() + " " + p.getPassengers() + " pax"));
        });

        /**
         * Use case 1.1 : Fenêtrage glissant
         * Crée des fenêtres glissantes de 3 éléments (avec chevauchement).
         * Permet d'analyser les éléments en continu dans une perspective locale.
         */
        System.out.println("\n=== Fenêtrage glissante (3 avions par groupe) avec un pas de 2 ===");
        Stream<List<Plane>> sliding = planes
            .stream()
            .gather(Gatherers.windowSliding(3));
        sliding.forEach(group -> {
            System.out.println("Groupe : ");
            group.forEach(p -> System.out.println("- " + p.getName() + " " + p.getPassengers() + " pax"));
        });

        /**
         * Use case 2 : Suppression des doublons
         * Évite d’avoir des doublons dans une collection en définissant une clé unique par avion.
         * Ici, l’unicité est basée sur le nom et la marque.
         */
        System.out.println("\n=== Suppression des doublons ===");
        Map.Entry<Set<String>, List<Plane>> result = planes
            .stream()
            .gather(distinctBy(p -> p.getName() + "::" + p.getMarque()))
            .findFirst()
            .orElseThrow();
        result.getValue().forEach(p ->
            System.out.println("Unique plane: " + p.getName() + " (" + p.getMarque() + ")")
        );

        /**
         * Use case 3 : Regroupement dynamique (fold)
         * Regroupe les avions dans des lots tant que leur nombre total de passagers n’excède pas 500.
         * Utile pour du batching intelligent (groupement conditionnel).
         */
        System.out.println("\n=== Regroupement jusqu'à 500 passagers cumulés ===");
        Stream<List<Plane>> batchedByPassengers = planes.stream().gather(
                Gatherers.fold(
                        () -> new ArrayList<Plane>(), // État initial : liste vide
                        (currentBatch, plane) -> {
                            int currentTotal = currentBatch.stream().mapToInt(Plane::getPassengers).sum();
                            if (currentTotal + plane.getPassengers() <= 500) {
                                currentBatch.add(plane);
                            }
                            return currentBatch;
                        }
                )
        );
        batchedByPassengers.forEach(batch -> {
            int total = batch.stream().mapToInt(Plane::getPassengers).sum();
            System.out.println("Batch total : " + total + " pax");
        });

        /**
         * Use case 4 : mapConcurrent
         * Transformation concurrente (parallèle) des noms de véhicules en majuscules.
         * On simule ici un traitement parallèle avec affichage du thread en cours.
         */
        System.out.println("\n=== mapConcurrent : noms de véhicules en majuscules (simulé en parallèle) ===");
        List<Vehicule> vehicules = List.of(
            new Plane("White", "Boeing", "Dreamliner", "787", 300),
            new Plane("Blue", "Airbus", "A320", "Neo", 180),
            new Car("Red", "Toyota", "Corolla", "Hybrid", 5)
        );
        Stream<String> upperNames = vehicules.stream().gather(
            Gatherers.mapConcurrent(
                3, // Niveau de parallélisme
                veh -> {
                    System.out.println("Processing " + veh.getName() + " in " + Thread.currentThread().getName());
                    return veh.getName().toUpperCase();
                }
            )
        );
        upperNames.forEach(name -> System.out.println("Nom traité : " + name));

        /**
         * Use case 5 : scan
         * Produit une accumulation progressive des passagers des avions.
         * À chaque étape, on connaît la somme cumulée jusqu’à présent.
         */
        System.out.println("\n=== scan : cumul progressif du nombre de passagers ===");
        Stream<Integer> cumulativePassengers = planes.stream().gather(
            Gatherers.scan(
                () -> 0,
                (partialSum, plane) -> partialSum + plane.getPassengers()
            )
        );
        cumulativePassengers.forEach(sum -> System.out.println("Cumul passagers : " + sum));
    }
}
