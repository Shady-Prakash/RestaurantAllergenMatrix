package model;

/** Menu sections used to group dishes. */
public enum Category {
    STARTER("Starter"),
    MAIN("Main"),
    SIDE("Side"),
    DESSERT("Dessert"),
    BEVERAGE("Beverage");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Category from(String text) {
        String t = text.trim();
        for (Category c : values()) {
            if (c.name().equalsIgnoreCase(t) || c.label.equalsIgnoreCase(t)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + text);
    }
}
