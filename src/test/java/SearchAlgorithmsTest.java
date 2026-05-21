import algorithms.SearchAlgorithms;
import algorithms.SortAlgorithms;
import model.Allergen;
import model.Category;
import model.MenuItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for binary search, linear search and the allergen filter. */
class SearchAlgorithmsTest {

    private List<MenuItem> sample() {
        List<MenuItem> list = new ArrayList<>();
        list.add(new MenuItem(1, "Pad Thai", Category.MAIN, 18.9,
                EnumSet.of(Allergen.PEANUTS, Allergen.EGGS)));
        list.add(new MenuItem(2, "Garden Salad", Category.STARTER, 9.0,
                EnumSet.noneOf(Allergen.class)));
        list.add(new MenuItem(3, "Cheesecake", Category.DESSERT, 10.0,
                EnumSet.of(Allergen.MILK, Allergen.GLUTEN)));
        list.add(new MenuItem(4, "Grilled Salmon", Category.MAIN, 24.0,
                EnumSet.of(Allergen.FISH)));
        return list;
    }

    @Test
    void binarySearchFindsExactNameInSortedList() {
        List<MenuItem> data = sample();
        SortAlgorithms.mergeSort(data, SortAlgorithms.byName());
        int idx = SearchAlgorithms.binarySearchByName(data, "Cheesecake");
        assertTrue(idx >= 0);
        assertEquals("Cheesecake", data.get(idx).getName());
    }

    @Test
    void binarySearchReturnsMinusOneWhenAbsent() {
        List<MenuItem> data = sample();
        SortAlgorithms.mergeSort(data, SortAlgorithms.byName());
        assertEquals(-1,
                SearchAlgorithms.binarySearchByName(data, "Tiramisu"));
    }

    @Test
    void linearSearchMatchesSubstringCaseInsensitively() {
        List<MenuItem> hits =
                SearchAlgorithms.linearSearchByName(sample(), "sal");
        assertEquals(2, hits.size()); // Garden Salad, Grilled Salmon
    }

    @Test
    void filterSafeForExcludesDishesWithGuestAllergens() {
        List<MenuItem> safe = SearchAlgorithms.filterSafeFor(
                sample(), EnumSet.of(Allergen.PEANUTS, Allergen.MILK));
        List<String> names = safe.stream().map(MenuItem::getName).toList();
        assertTrue(names.contains("Garden Salad"));
        assertTrue(names.contains("Grilled Salmon"));
        assertFalse(names.contains("Pad Thai"));   // has peanuts
        assertFalse(names.contains("Cheesecake")); // has milk
    }
}
