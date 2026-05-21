package cli;

import algorithms.SearchAlgorithms;
import algorithms.SortAlgorithms;
import data.MenuRepository;
import model.Allergen;
import model.Category;
import model.MenuItem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;

/**
 * Console interface offering the same operations as the GUI: list, add,
 * delete, sort, search and the allergen-safety filter. All user input is
 * defensively parsed so a typo never crashes the program.
 */
public class TextInterface {

    private final MenuRepository repo;
    private final Path dataFile;
    private final Scanner in;

    public TextInterface(MenuRepository repo, Path dataFile, Scanner sharedIn) {
        this.repo = repo;
        this.dataFile = dataFile;
        this.in = sharedIn;
    }

    public void run() {
        System.out.println("\n===== Restaurant Allergy Matrix — Text Mode =====");
        boolean running = true;
        while (running) {
            printMenu();
            if (!in.hasNextLine()) {
                break; // end of input stream (e.g. piped script finished)
            }
            String choice = prompt("Choose an option: ").trim();
            // Defensive guard: a stream that keeps yielding blank lines (a
            // closed/non-interactive stdin) must never spin forever.
            if (choice.isEmpty()) {
                if (++blankStreak >= MAX_BLANKS) {
                    System.out.println("  (no input — exiting)");
                    break;
                }
                continue;
            }
            blankStreak = 0;
            switch (choice) {
                case "1":
                    listAll();
                    break;
                case "2":
                    addItem();
                    break;
                case "3":
                    deleteItem();
                    break;
                case "4":
                    sortItems();
                    break;
                case "5":
                    searchByName();
                    break;
                case "6":
                    filterSafe();
                    break;
                case "7":
                    saveData();
                    break;
                case "8":
                    reloadData();
                    break;
                case "9":
                    editItem();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("  ! Unknown option, try again.");
            }
        }
        System.out.println("Goodbye.");
    }

    private int blankStreak = 0;
    private static final int MAX_BLANKS = 5;

    private void printMenu() {
        System.out.println("                ---------------------------------------------");
        System.out.println("                 1) List all dishes (allergy matrix)");
        System.out.println("                 2) Add a dish");
        System.out.println("                 3) Delete a dish by id");
        System.out.println("                 4) Sort dishes");
        System.out.println("                 5) Search dish by name");
        System.out.println("                 6) Show dishes safe for a guest");
        System.out.println("                 7) Save to file");
        System.out.println("                 8) Reload from file");
        System.out.println("                 9) Edit a dish by id");
        System.out.println("                 0) Exit");
        System.out.println("                ---------------------------------------------");
    }

    private void listAll() {
        print(repo.all());
    }

    private void print(List<MenuItem> items) {
        if (items.isEmpty()) {
            System.out.println("  (no dishes)");
            return;
        }
        System.out.println();
        for (MenuItem m : items) {
            System.out.println("  " + m);
        }
        System.out.println("  -- " + items.size() + " dish(es) --");
    }

    private void addItem() {
        try {
            String name = prompt("  Dish name: ").trim();
            if (name.isEmpty()) {
                System.out.println("  ! Name cannot be empty.");
                return;
            }
            System.out.println("  Categories: STARTER, MAIN, SIDE, "
                    + "DESSERT, BEVERAGE");
            Category cat = Category.from(prompt("  Category: "));
            double price = Double.parseDouble(prompt("  Price: ").trim());
            if (price < 0) {
                System.out.println("  ! Price must be non-negative.");
                return;
            }
            EnumSet<Allergen> allergens = EnumSet.noneOf(Allergen.class);
            System.out.println("  Allergens (comma separated, blank for none).");
            System.out.println("  Options: " + allergenOptions());
            String raw = prompt("  Allergens: ").trim();
            if (!raw.isEmpty()) {
                for (String tok : raw.split(",")) {
                    if (!tok.isBlank()) {
                        allergens.add(Allergen.from(tok.trim()));
                    }
                }
            }
            MenuItem created = repo.add( new MenuItem(0, name, cat, price, allergens));
            try {
                repo.save(dataFile);                       // auto-persist
                System.out.println("  + Added id " + created.getId()
                        + " and saved to " + dataFile);
            } catch (IOException ex) {
                System.out.println("  + Added id " + created.getId()
                        + " but save failed: " + ex.getMessage());
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("  ! Invalid input: " + ex.getMessage());
        }
    }

    private void deleteItem() {
        try {
            int id = Integer.parseInt(prompt("  Id to delete: ").trim());
            
            if (repo.deleteById(id)) {
                try {
                    repo.save(dataFile);
                    System.out.println("  - Deleted id " + id + " and saved changes.");
                } catch (IOException ex) {
                    System.out.println("  - Deleted id " + id + " but failed to save: "
                            + ex.getMessage());
                }
            } else {
                System.out.println("  ! No dish with id " + id);
            }
        } catch (NumberFormatException ex) {
            System.out.println("  ! Id must be a whole number.");
        }
    }

    private void editItem() {
        try {
            int id = Integer.parseInt(prompt("  Id to edit: ").trim());
            MenuItem current = repo.findById(id).orElse(null);
            if (current == null) {
                System.out.println("  ! No dish with id " + id);
                return;
            }
            System.out.println("  Editing: " + current);
            System.out.println("  Press Enter to keep the current value.");

            String name = prompt("  Name [" + current.getName() + "]: ").trim();
            if (name.isEmpty()) {
                name = current.getName();
            }

            String catRaw = prompt("  Category [" + current.getCategory().name()
                    + "]: ").trim();
            Category cat = catRaw.isEmpty()
                    ? current.getCategory() : Category.from(catRaw);

            String priceRaw = prompt(String.format(
                    "  Price [%.2f]: ", current.getPrice())).trim();
            double price = priceRaw.isEmpty()
                    ? current.getPrice() : Double.parseDouble(priceRaw);
            if (price < 0) {
                System.out.println("  ! Price must be non-negative.");
                return;
            }

            System.out.println("  Current allergens: "
                    + current.allergenSummary());
            System.out.println("  Options: " + allergenOptions());
            String raw = prompt("  New allergen list (blank = keep, "
                    + "'none' = clear): ").trim();
            EnumSet<Allergen> allergens;
            if (raw.isEmpty()) {
                allergens = EnumSet.noneOf(Allergen.class);
                allergens.addAll(current.getAllergens());
            } else if (raw.equalsIgnoreCase("none")) {
                allergens = EnumSet.noneOf(Allergen.class);
            } else {
                allergens = EnumSet.noneOf(Allergen.class);
                for (String tok : raw.split(",")) {
                    if (!tok.isBlank()) {
                        allergens.add(Allergen.from(tok.trim()));
                    }
                }
            }
            boolean ok = repo.update(
                    new MenuItem(id, name, cat, price, allergens));
            if (ok) {
                try {
                    repo.save(dataFile);
                    System.out.println("  ~ Updated id " + id
                            + " and saved to " + dataFile);
                } catch (IOException ex) {
                    System.out.println("  ~ Updated id " + id
                            + " but save failed: " + ex.getMessage());
                }
            } else {
                System.out.println("  ! Update failed for id " + id);
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("  ! Invalid input: " + ex.getMessage());
        }
    }

    private void sortItems() {
        System.out.println("  Sort key: 1=Name 2=Price 3=Category "
                + "4=Allergen count");
        Comparator<MenuItem> cmp;
        switch (prompt("  Key: ").trim()) {
            case "2":
                cmp = SortAlgorithms.byPrice();
                break;
            case "3":
                cmp = SortAlgorithms.byCategory();
                break;
            case "4":
                cmp = SortAlgorithms.byAllergenCount();
                break;
            default:
                cmp = SortAlgorithms.byName();
                break;
        }
        List<MenuItem> data = new ArrayList<>(repo.all());
        String algo = prompt("  Algorithm 1=QuickSort 2=MergeSort: ").trim();
        long t0 = System.nanoTime();
        if ("2".equals(algo)) {
            SortAlgorithms.mergeSort(data, cmp);
        } else {
            SortAlgorithms.quickSort(data, cmp);
        }
        long us = (System.nanoTime() - t0) / 1000;
        print(data);
        System.out.println("  (sorted in " + us + " µs)");
    }

    private void searchByName() {
        String q = prompt("  Name contains: ").trim();
        List<MenuItem> hits =
                SearchAlgorithms.linearSearchByName(repo.all(), q);
        print(hits);

        // Also demonstrate binary search for an exact match.
        List<MenuItem> sorted = new ArrayList<>(repo.all());
        SortAlgorithms.mergeSort(sorted, SortAlgorithms.byName());
        int idx = SearchAlgorithms.binarySearchByName(sorted, q);
        if (idx >= 0) {
            System.out.println("  Binary search exact match: "
                    + sorted.get(idx).getName());
        }
    }

    private void filterSafe() {
        System.out.println("  Options: " + allergenOptions());
        String raw = prompt("  Guest is allergic to: ").trim();
        EnumSet<Allergen> avoid = EnumSet.noneOf(Allergen.class);
        try {
            if (!raw.isEmpty()) {
                for (String tok : raw.split(",")) {
                    if (!tok.isBlank()) {
                        avoid.add(Allergen.from(tok.trim()));
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("  ! " + ex.getMessage());
            return;
        }
        List<MenuItem> safe =
                SearchAlgorithms.filterSafeFor(repo.all(), avoid);
        print(safe);
        System.out.println("  " + safe.size() + " of " + repo.size()
                + " dishes are safe.");
    }

    private void saveData() {
        try {
            repo.save(dataFile);
            System.out.println("  Saved to " + dataFile);
        } catch (IOException ex) {
            System.out.println("  ! Save failed: " + ex.getMessage());
        }
    }

    private void reloadData() {
        try {
            int skipped = repo.load(dataFile);
            System.out.println("  Reloaded " + repo.size() + " dishes"
                    + (skipped > 0 ? " (" + skipped + " bad lines skipped)"
                                   : ""));
        } catch (IOException ex) {
            System.out.println("  ! Reload failed: " + ex.getMessage());
        }
    }

    private String allergenOptions() {
        StringBuilder sb = new StringBuilder();
        for (Allergen a : Allergen.values()) {
            sb.append(a.name()).append(' ');
        }
        return sb.toString().trim();
    }

    private String prompt(String label) {
        System.out.print(label);
        return in.hasNextLine() ? in.nextLine() : "";
    }
}
