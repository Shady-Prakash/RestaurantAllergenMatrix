package algorithms;

import model.Allergen;
import model.MenuItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Hand-written searching algorithms. Binary Search demonstrates the payoff
 * of keeping data sorted; the linear allergen filter is the core business
 * query of the system ("what can a guest with these allergies eat?").
 */
public final class SearchAlgorithms {

    private SearchAlgorithms() {
    }

    /**
     * Binary Search for a dish by exact name. The supplied list MUST already
     * be sorted by {@link SortAlgorithms#byName()}; the caller is responsible
     * for that precondition (the GUI sorts before searching).
     *
     * <p>Time complexity: O(log n). Space complexity: O(1).</p>
     *
     * @param sortedByName list pre-sorted ascending by name
     * @param targetName   exact dish name (case-insensitive)
     * @return the index of a match, or -1 if not found
     */
    public static int binarySearchByName(List<MenuItem> sortedByName,
                                         String targetName) {
        int lo = 0;
        int hi = sortedByName.size() - 1;
        Comparator<String> c = String.CASE_INSENSITIVE_ORDER;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int cmp = c.compare(sortedByName.get(mid).getName(), targetName);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return -1;
    }

    /**
     * Linear Search returning every dish whose name contains {@code query}
     * (case-insensitive substring match). Used by the live search box.
     *
     * <p>Time complexity: O(n). Space complexity: O(k) for k matches.</p>
     *
     * @param items the items to scan (any order)
     * @param query substring to look for
     * @return all matching items, in input order
     */
    public static List<MenuItem> linearSearchByName(List<MenuItem> items,
                                                    String query) {
        String q = query.trim().toLowerCase();
        List<MenuItem> hits = new ArrayList<>();
        for (MenuItem item : items) {
            if (item.getName().toLowerCase().contains(q)) {
                hits.add(item);
            }
        }
        return hits;
    }

    /**
     * The headline query: returns every dish that is safe for a guest who
     * must avoid all of {@code guestAllergies}.
     *
     * <p>Time complexity: O(n * a) where a is the number of allergens to
     * avoid (a is bounded by 14, so effectively O(n)).</p>
     *
     * @param items          the menu
     * @param guestAllergies allergens the guest must avoid
     * @return the safe subset of the menu
     */
    public static List<MenuItem> filterSafeFor(List<MenuItem> items,
                                               Set<Allergen> guestAllergies) {
        List<MenuItem> safe = new ArrayList<>();
        for (MenuItem item : items) {
            if (item.isSafeFor(guestAllergies)) {
                safe.add(item);
            }
        }
        return safe;
    }
}
