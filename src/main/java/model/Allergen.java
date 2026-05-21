package model;

/**
 * The fourteen major food allergens commonly declared on menus.
 * Each constant carries a human-readable label used by both interfaces.
 */
public enum Allergen {
    GLUTEN("Gluten"),
    CRUSTACEANS("Crustaceans"),
    EGGS("Eggs"),
    FISH("Fish"),
    PEANUTS("Peanuts"),
    SOYBEANS("Soybeans"),
    MILK("Milk"),
    TREE_NUTS("Tree Nuts"),
    CELERY("Celery"),
    MUSTARD("Mustard"),
    SESAME("Sesame"),
    SULPHITES("Sulphites"),
    LUPIN("Lupin"),
    MOLLUSCS("Molluscs");

    private final String label;

    Allergen(String label) {
        this.label = label;
    }

    /** @return the display label, e.g. "Tree Nuts". */
    public String label() {
        return label;
    }

    /**
     * Resolves an Allergen from a case-insensitive name or label.
     *
     * @param text the enum name ("TREE_NUTS") or label ("Tree Nuts")
     * @return the matching Allergen
     * @throws IllegalArgumentException if no allergen matches
     */
    public static Allergen from(String text) {
        String t = text.trim();
        for (Allergen a : values()) {
            if (a.name().equalsIgnoreCase(t) || a.label.equalsIgnoreCase(t)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown allergen: " + text);
    }
}
