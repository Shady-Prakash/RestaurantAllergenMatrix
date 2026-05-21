package model;

import java.util.EnumSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * A single dish on the restaurant menu together with the set of major
 * allergens it contains. This is the core entity stored, sorted and
 * searched by the application.
 */
public class MenuItem {

    private int id;
    private String name;
    private Category category;
    private double price;
    private final EnumSet<Allergen> allergens;

    public MenuItem(int id, String name, Category category, double price,
                    Set<Allergen> allergens) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.allergens = allergens == null || allergens.isEmpty()
                ? EnumSet.noneOf(Allergen.class)
                : EnumSet.copyOf(allergens);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    /** @return an unmodifiable view of the allergens this dish contains. */
    public Set<Allergen> getAllergens() {
        return EnumSet.copyOf(allergens.isEmpty()
                ? EnumSet.noneOf(Allergen.class) : allergens);
    }

    public void addAllergen(Allergen a) {
        allergens.add(a);
    }

    /**
     * Tests whether this dish is safe for a guest with the given allergies.
     *
     * @param guestAllergies allergens the guest must avoid
     * @return true if the dish contains none of the guest's allergens
     */
    public boolean isSafeFor(Set<Allergen> guestAllergies) {
        for (Allergen a : guestAllergies) {
            if (allergens.contains(a)) {
                return false;
            }
        }
        return true;
    }

    /** Serialises this item to one CSV record: id,name,category,price,a|b|c */
    public String toCsv() {
        StringJoiner allergenJoiner = new StringJoiner("|");
        for (Allergen a : allergens) {
            allergenJoiner.add(a.name());
        }
        // Escape any comma in the name so the record stays well-formed.
        String safeName = name.replace(",", ";");
        return id + "," + safeName + "," + category.name() + ","
                + String.format("%.2f", price) + "," + allergenJoiner;
    }

    /**
     * Parses a CSV record produced by {@link #toCsv()}.
     *
     * @param line the raw CSV line
     * @return the reconstructed MenuItem
     * @throws IllegalArgumentException if the record is malformed
     */
    public static MenuItem fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Malformed record: " + line);
        }
        int id = Integer.parseInt(parts[0].trim());
        String name = parts[1].trim();
        Category category = Category.from(parts[2].trim());
        double price = Double.parseDouble(parts[3].trim());
        EnumSet<Allergen> allergens = EnumSet.noneOf(Allergen.class);
        if (parts.length >= 5 && !parts[4].isBlank()) {
            for (String token : parts[4].split("\\|")) {
                if (!token.isBlank()) {
                    allergens.add(Allergen.from(token.trim()));
                }
            }
        }
        return new MenuItem(id, name, category, price, allergens);
    }

    @Override
    public String toString() {
        return String.format("#%d  %-26s  %-9s  $%6.2f  [%s]",
                id, name, category.label(), price, allergenSummary());
    }

    /** @return comma-separated allergen labels, or "none". */
    public String allergenSummary() {
        if (allergens.isEmpty()) {
            return "none";
        }
        StringJoiner j = new StringJoiner(", ");
        for (Allergen a : allergens) {
            j.add(a.label());
        }
        return j.toString();
    }
}
