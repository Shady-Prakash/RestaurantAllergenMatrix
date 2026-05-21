package algorithms;

import model.MenuItem;

import java.util.Comparator;
import java.util.List;

/**
 * Hand-written sorting algorithms used to order the menu. Quick Sort and
 * Merge Sort are implemented from scratch (rather than calling
 * {@link java.util.Collections#sort}) so their behaviour and complexity can
 * be discussed and unit-tested for the assessment.
 *
 * <p>Both algorithms are generic over a {@link Comparator}, which lets the
 * UI sort by name, price or category through the same code path.</p>
 */
public final class SortAlgorithms {

    private SortAlgorithms() {
    }

    // -------------------------------------------------- reusable comparators

    public static Comparator<MenuItem> byName() {
        return Comparator.comparing(MenuItem::getName, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<MenuItem> byPrice() {
        return Comparator.comparingDouble(MenuItem::getPrice);
    }

    public static Comparator<MenuItem> byCategory() {
        return Comparator.comparing((MenuItem m) -> m.getCategory().label())
                .thenComparing(byName());
    }

    public static Comparator<MenuItem> byAllergenCount() {
        return Comparator.comparingInt((MenuItem m) -> m.getAllergens().size())
                .thenComparing(byName());
    }

    // -------------------------------------------------------------- Quick Sort

    /**
     * Sorts {@code list} in place using Quick Sort (Lomuto partition with a
     * median-of-three pivot to avoid worst-case behaviour on sorted input).
     *
     * <p>Time complexity: O(n log n) average, O(n^2) worst case.
     * Space complexity: O(log n) recursion stack. Not stable.</p>
     *
     * @param list the list to sort
     * @param cmp  ordering to apply
     */
    public static void quickSort(List<MenuItem> list, Comparator<MenuItem> cmp) {
        if (list.size() < 2) {
            return;
        }
        quickSort(list, 0, list.size() - 1, cmp);
    }

    private static void quickSort(List<MenuItem> list, int lo, int hi,
                                  Comparator<MenuItem> cmp) {
        if (lo >= hi) {
            return;
        }
        int p = partition(list, lo, hi, cmp);
        quickSort(list, lo, p - 1, cmp);
        quickSort(list, p + 1, hi, cmp);
    }

    private static int partition(List<MenuItem> list, int lo, int hi,
                                 Comparator<MenuItem> cmp) {
        int mid = lo + (hi - lo) / 2;
        // Median-of-three: park the median at hi to use as the pivot.
        if (cmp.compare(list.get(mid), list.get(lo)) < 0) {
            swap(list, lo, mid);
        }
        if (cmp.compare(list.get(hi), list.get(lo)) < 0) {
            swap(list, lo, hi);
        }
        if (cmp.compare(list.get(hi), list.get(mid)) < 0) {
            swap(list, mid, hi);
        }
        MenuItem pivot = list.get(hi);
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (cmp.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, hi);
        return i + 1;
    }

    private static void swap(List<MenuItem> list, int a, int b) {
        MenuItem tmp = list.get(a);
        list.set(a, list.get(b));
        list.set(b, tmp);
    }

    // -------------------------------------------------------------- Merge Sort

    /**
     * Sorts {@code list} in place using a stable top-down Merge Sort.
     *
     * <p>Time complexity: O(n log n) in every case (best, average, worst).
     * Space complexity: O(n) for the auxiliary buffers. Stable, which makes
     * it the right default when a previous ordering must be preserved.</p>
     *
     * @param list the list to sort
     * @param cmp  ordering to apply
     */
    public static void mergeSort(List<MenuItem> list, Comparator<MenuItem> cmp) {
        if (list.size() < 2) {
            return;
        }
        MenuItem[] arr = list.toArray(new MenuItem[0]);
        MenuItem[] aux = new MenuItem[arr.length];
        mergeSort(arr, aux, 0, arr.length - 1, cmp);
        for (int i = 0; i < arr.length; i++) {
            list.set(i, arr[i]);
        }
    }

    private static void mergeSort(MenuItem[] arr, MenuItem[] aux,
                                  int lo, int hi, Comparator<MenuItem> cmp) {
        if (lo >= hi) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        mergeSort(arr, aux, lo, mid, cmp);
        mergeSort(arr, aux, mid + 1, hi, cmp);
        merge(arr, aux, lo, mid, hi, cmp);
    }

    private static void merge(MenuItem[] arr, MenuItem[] aux,
                              int lo, int mid, int hi,
                              Comparator<MenuItem> cmp) {
        System.arraycopy(arr, lo, aux, lo, hi - lo + 1);
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                arr[k] = aux[j++];
            } else if (j > hi) {
                arr[k] = aux[i++];
            } else if (cmp.compare(aux[j], aux[i]) < 0) {
                arr[k] = aux[j++];
            } else {
                arr[k] = aux[i++]; // <= keeps equal elements stable
            }
        }
    }
}
