

import data.MenuRepository;
import model.Allergen;
import model.Category;
import model.MenuItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the repository: id assignment, delete and CSV round-trip. */
class MenuRepositoryTest {

    @Test
    void addAssignsSequentialIdsWhenIdIsZero() {
        MenuRepository repo = new MenuRepository();
        MenuItem a = repo.add(new MenuItem(0, "A", Category.MAIN, 1.0,
                EnumSet.noneOf(Allergen.class)));
        MenuItem b = repo.add(new MenuItem(0, "B", Category.MAIN, 2.0,
                EnumSet.noneOf(Allergen.class)));
        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
    }

    @Test
    void addRejectsDuplicateExplicitId() {
        MenuRepository repo = new MenuRepository();
        repo.add(new MenuItem(5, "A", Category.MAIN, 1.0,
                EnumSet.noneOf(Allergen.class)));
        assertThrows(IllegalArgumentException.class, () ->
                repo.add(new MenuItem(5, "B", Category.MAIN, 2.0,
                        EnumSet.noneOf(Allergen.class))));
    }

    @Test
    void deleteByIdRemovesOnlyMatchingItem() {
        MenuRepository repo = new MenuRepository();
        repo.add(new MenuItem(0, "Keep", Category.MAIN, 1.0,
                EnumSet.noneOf(Allergen.class)));
        MenuItem drop = repo.add(new MenuItem(0, "Drop", Category.MAIN, 2.0,
                EnumSet.noneOf(Allergen.class)));
        assertTrue(repo.deleteById(drop.getId()));
        assertFalse(repo.deleteById(999));
        assertEquals(1, repo.size());
    }

    @Test
    void updateReplacesValuesButKeepsId() {
        MenuRepository repo = new MenuRepository();
        MenuItem orig = repo.add(new MenuItem(0, "Soup", Category.STARTER,
                7.0, EnumSet.noneOf(Allergen.class)));
        boolean ok = repo.update(new MenuItem(orig.getId(), "Tomato Soup",
                Category.STARTER, 8.5, EnumSet.of(Allergen.CELERY)));
        assertTrue(ok);
        MenuItem after = repo.findById(orig.getId()).orElseThrow();
        assertEquals("Tomato Soup", after.getName());
        assertEquals(8.5, after.getPrice());
        assertTrue(after.getAllergens().contains(Allergen.CELERY));
        assertEquals(1, repo.size());
    }

    @Test
    void updateReturnsFalseForUnknownId() {
        MenuRepository repo = new MenuRepository();
        assertFalse(repo.update(new MenuItem(42, "Ghost", Category.MAIN,
                1.0, EnumSet.noneOf(Allergen.class))));
    }

    @Test
    void saveThenLoadPreservesDataAndSkipsBadLines(@TempDir Path dir)
            throws IOException {
        Path file = dir.resolve("menu.csv");
        MenuRepository repo = new MenuRepository();
        repo.add(new MenuItem(0, "Pad Thai", Category.MAIN, 18.9,
                EnumSet.of(Allergen.PEANUTS, Allergen.EGGS)));
        repo.save(file);

        // Append a deliberately malformed line.
        Files.writeString(file, "not,enough\n",
                java.nio.file.StandardOpenOption.APPEND);

        MenuRepository reloaded = new MenuRepository();
        int skipped = reloaded.load(file);
        assertEquals(1, reloaded.size());
        assertEquals(1, skipped);
        MenuItem m = reloaded.findById(1).orElseThrow();
        assertEquals("Pad Thai", m.getName());
        assertTrue(m.getAllergens().contains(Allergen.PEANUTS));
    }
}
