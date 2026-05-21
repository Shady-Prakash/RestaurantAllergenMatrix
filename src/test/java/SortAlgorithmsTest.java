import algorithms.SortAlgorithms;
import model.Allergen;
import model.Category;
import model.MenuItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the hand-written Quick Sort and Merge Sort. */
class SortAlgorithmsTest {

    private List<MenuItem> sample() {
        List<MenuItem> list = new ArrayList<>();
        list.add(new MenuItem(1, "Pizza", Category.MAIN, 16.5,
                EnumSet.of(Allergen.GLUTEN)));
        list.add(new MenuItem(2, "Apple Pie", Category.DESSERT, 9.0,
                EnumSet.of(Allergen.GLUTEN, Allergen.EGGS)));
        list.add(new MenuItem(3, "Salad", Category.STARTER, 9.0,
                EnumSet.noneOf(Allergen.class)));
        list.add(new MenuItem(4, "Soup", Category.STARTER, 7.25,
                EnumSet.noneOf(Allergen.class)));
        return list;
    }

    @Test
    void quickSortOrdersByPriceAscending() {
        List<MenuItem> data = sample();
        SortAlgorithms.quickSort(data, SortAlgorithms.byPrice());
        for (int i = 1; i < data.size(); i++) {
            assertTrue(data.get(i - 1).getPrice() <= data.get(i).getPrice(),
                    "prices must be non-decreasing");
        }
        assertEquals("Soup", data.get(0).getName());
    }

    @Test
    void mergeSortOrdersByNameCaseInsensitively() {
        List<MenuItem> data = sample();
        SortAlgorithms.mergeSort(data, SortAlgorithms.byName());
        assertEquals(List.of("Apple Pie", "Pizza", "Salad", "Soup"),
                data.stream().map(MenuItem::getName).toList());
    }

    @Test
    void mergeSortIsStableForEqualKeys() {
        // Apple Pie (id 2) and Salad (id 3) both cost 9.00; a stable sort
        // must keep their original relative order.
        List<MenuItem> data = sample();
        SortAlgorithms.mergeSort(data, SortAlgorithms.byPrice());
        List<Integer> idsAtNine = data.stream()
                .filter(m -> m.getPrice() == 9.0)
                .map(MenuItem::getId)
                .toList();
        assertEquals(List.of(2, 3), idsAtNine);
    }

    @Test
    void emptyAndSingleElementListsAreUnchanged() {
        List<MenuItem> empty = new ArrayList<>();
        SortAlgorithms.quickSort(empty, SortAlgorithms.byName());
        assertTrue(empty.isEmpty());

        List<MenuItem> one = new ArrayList<>(sample().subList(0, 1));
        SortAlgorithms.mergeSort(one, SortAlgorithms.byPrice());
        assertEquals(1, one.size());
    }
}
