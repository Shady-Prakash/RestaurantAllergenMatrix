package ui;

import model.Allergen;
import model.MenuItem;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model that renders the menu as an allergy matrix: the first four
 * columns describe the dish, followed by one column per major allergen that
 * shows a bullet when the dish contains it.
 */
public class MenuTableModel extends AbstractTableModel {

    private static final String[] FIXED = {"ID", "Dish", "Category", "Price"};
    private final Allergen[] allergens = Allergen.values();
    private List<MenuItem> rows = new ArrayList<>();

    public void setRows(List<MenuItem> items) {
        this.rows = new ArrayList<>(items);
        fireTableDataChanged();
    }

    public MenuItem rowAt(int viewRow) {
        return rows.get(viewRow);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return FIXED.length + allergens.length;
    }

    @Override
    public String getColumnName(int col) {
        if (col < FIXED.length) {
            return FIXED[col];
        }
        return allergens[col - FIXED.length].label();
    }

    @Override
    public Object getValueAt(int row, int col) {
        MenuItem m = rows.get(row);
        switch (col) {
            case 0: return m.getId();
            case 1: return m.getName();
            case 2: return m.getCategory().label();
            case 3: return String.format("$%.2f", m.getPrice());
            default:
                Allergen a = allergens[col - FIXED.length];
                return m.getAllergens().contains(a) ? "●" : "";
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public boolean isAllergenColumn(int col) {
        return col >= FIXED.length;
    }
}
