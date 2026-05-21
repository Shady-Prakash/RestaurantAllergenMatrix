package data;

import model.Allergen;
import model.Category;
import model.MenuItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * In-memory store of {@link MenuItem}s backed by a CSV file. Handles all
 * create / delete / query operations and file persistence for both the GUI
 * and the text interface so the two share identical behaviour.
 */
public class MenuRepository {

    private final List<MenuItem> items = new ArrayList<>();
    private Path file;

    /** Creates an empty repository not yet bound to a file. */
    public MenuRepository() {
    }

    /** @return a live list of all menu items. */
    public List<MenuItem> all() {
        return new ArrayList<>(items);
    }

    public int size() {
        return items.size();
    }

    /**
     * Adds an item, assigning the next free id when {@code item.getId() <= 0}.
     *
     * @param item the item to add
     * @return the stored item (with its assigned id)
     */
    public MenuItem add(MenuItem item) {
        if (item.getId() <= 0) {
            item.setId(nextId());
        } else if (findById(item.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    "An item with id " + item.getId() + " already exists.");
        }
        items.add(item);
        return item;
    }

    /**
     * Deletes the item with the given id.
     *
     * @param id the id to remove
     * @return true if an item was removed
     */
    public boolean deleteById(int id) {
        return items.removeIf(i -> i.getId() == id);
    }

    /**
     * Replaces the stored item that shares {@code updated}'s id with the new
     * values. The id itself is never changed.
     *
     * @param updated the item carrying the id to match and the new values
     * @return true if a matching item was found and updated
     */
    public boolean update(MenuItem updated) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == updated.getId()) {
                items.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public Optional<MenuItem> findById(int id) {
        return items.stream().filter(i -> i.getId() == id).findFirst();
    }

    private int nextId() {
        int max = 0;
        for (MenuItem i : items) {
            max = Math.max(max, i.getId());
        }
        return max + 1;
    }

    // ----------------------------------------------------------------- I/O

    /**
     * Loads items from a CSV file. Missing files are tolerated (the
     * repository simply stays empty); malformed lines are skipped and the
     * count of skipped lines is returned so callers can warn the user.
     *
     * @param path the CSV file to read
     * @return number of malformed lines skipped
     * @throws IOException if the file cannot be read
     */
    public int load(Path path) throws IOException {
        this.file = path;
        items.clear();
        if (!Files.exists(path)) {
            return 0;
        }
        int skipped = 0;
        try (BufferedReader r = Files.newBufferedReader(path)) {
            String line;
            boolean first = true;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (first && line.toLowerCase().startsWith("id,")) {
                    first = false; // header row
                    continue;
                }
                first = false;
                try {
                    items.add(MenuItem.fromCsv(line));
                } catch (RuntimeException ex) {
                    skipped++;
                }
            }
        }
        return skipped;
    }

    /**
     * Saves all items to the bound file (or {@code path} if given).
     *
     * @param path target file, or null to reuse the file from {@link #load}
     * @throws IOException if writing fails
     */
    public void save(Path path) throws IOException {
        Path target = path != null ? path : file;
        if (target == null) {
            throw new IllegalStateException("No file bound to repository.");
        }
        this.file = target;
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        try (BufferedWriter w = Files.newBufferedWriter(target)) {
            w.write("id,name,category,price,allergens");
            w.newLine();
            for (MenuItem i : items) {
                w.write(i.toCsv());
                w.newLine();
            }
        }
    }

    public Path getFile() {
        return file;
    }

    /** Populates a fresh repository with a representative sample menu. */
    public void seedSampleData() {
        items.clear();
        add(new MenuItem(0, "Garlic Bread", Category.STARTER, 7.50,
                EnumSet.of(Allergen.GLUTEN, Allergen.MILK)));
        add(new MenuItem(0, "Prawn Cocktail", Category.STARTER, 12.90,
                EnumSet.of(Allergen.CRUSTACEANS, Allergen.EGGS)));
        add(new MenuItem(0, "Garden Salad", Category.STARTER, 9.00,
                EnumSet.noneOf(Allergen.class)));
        add(new MenuItem(0, "Margherita Pizza", Category.MAIN, 16.50,
                EnumSet.of(Allergen.GLUTEN, Allergen.MILK)));
        add(new MenuItem(0, "Grilled Salmon", Category.MAIN, 24.00,
                EnumSet.of(Allergen.FISH)));
        add(new MenuItem(0, "Pad Thai", Category.MAIN, 18.90,
                EnumSet.of(Allergen.PEANUTS, Allergen.EGGS, Allergen.SOYBEANS)));
        add(new MenuItem(0, "Beef Stir Fry", Category.MAIN, 21.00,
                EnumSet.of(Allergen.SOYBEANS, Allergen.SESAME)));
        add(new MenuItem(0, "Steamed Rice", Category.SIDE, 4.00,
                EnumSet.noneOf(Allergen.class)));
        add(new MenuItem(0, "Sweet Potato Fries", Category.SIDE, 6.50,
                EnumSet.noneOf(Allergen.class)));
        add(new MenuItem(0, "Cheesecake", Category.DESSERT, 10.00,
                EnumSet.of(Allergen.GLUTEN, Allergen.MILK, Allergen.EGGS)));
        add(new MenuItem(0, "Fruit Sorbet", Category.DESSERT, 8.00,
                EnumSet.noneOf(Allergen.class)));
        add(new MenuItem(0, "Almond Biscotti", Category.DESSERT, 5.50,
                EnumSet.of(Allergen.GLUTEN, Allergen.TREE_NUTS, Allergen.EGGS)));
        add(new MenuItem(0, "Soy Latte", Category.BEVERAGE, 4.50,
                EnumSet.of(Allergen.SOYBEANS)));
        add(new MenuItem(0, "Fresh Orange Juice", Category.BEVERAGE, 5.00,
                EnumSet.noneOf(Allergen.class)));
        add(new MenuItem(0, "House Red Wine", Category.BEVERAGE, 11.00,
                EnumSet.of(Allergen.SULPHITES)));
    }
}
