package ui;

import model.Allergen;
import model.Category;
import model.MenuItem;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.EnumSet;

/**
 * Modal form used to capture a new dish. Validates input and only returns a
 * {@link MenuItem} when the user confirms with valid data.
 */
public class AddItemDialog extends JDialog {

    private MenuItem result;

    /** Opens the form in "add" mode. */
    public AddItemDialog(Frame owner) {
        this(owner, null);
    }

    /**
     * Opens the form. When {@code existing} is non-null the fields are
     * pre-filled and the dialog edits that dish in place (its id is kept).
     *
     * @param owner    parent frame
     * @param existing the dish to edit, or null to create a new one
     */
    public AddItemDialog(Frame owner, MenuItem existing) {
        super(owner, existing == null ? "Add Menu Item" : "Edit Menu Item",
                true);
        boolean editing = existing != null;
        JTextField nameField = new JTextField();
        JComboBox<Category> categoryBox = new JComboBox<>(Category.values());
        JTextField priceField = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Dish name:"));
        form.add(nameField);
        form.add(new JLabel("Category:"));
        form.add(categoryBox);
        form.add(new JLabel("Price ($):"));
        form.add(priceField);

        JPanel allergenPanel = new JPanel(new GridLayout(0, 3, 4, 4));
        allergenPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Contains allergens"),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        JCheckBox[] boxes = new JCheckBox[Allergen.values().length];
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = new JCheckBox(Allergen.values()[i].label());
            allergenPanel.add(boxes[i]);
        }

        if (editing) {
            nameField.setText(existing.getName());
            categoryBox.setSelectedItem(existing.getCategory());
            priceField.setText(String.format("%.2f", existing.getPrice()));
            for (int i = 0; i < boxes.length; i++) {
                boxes[i].setSelected(existing.getAllergens()
                        .contains(Allergen.values()[i]));
            }
        }

        JButton ok = Theme.button(editing ? "Save" : "Add", Theme.PRIMARY);
        JButton cancel = Theme.button("Cancel", Theme.MUTED);
        JPanel buttons = new JPanel();
        buttons.add(cancel);
        buttons.add(ok);

        ok.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Dish name cannot be empty.", "Invalid input",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            double price;
            try {
                price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Price must be a non-negative number.", "Invalid input",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            EnumSet<Allergen> chosen = EnumSet.noneOf(Allergen.class);
            for (int i = 0; i < boxes.length; i++) {
                if (boxes[i].isSelected()) {
                    chosen.add(Allergen.values()[i]);
                }
            }
            int id = editing ? existing.getId() : 0;
            result = new MenuItem(id, name,
                    (Category) categoryBox.getSelectedItem(), price, chosen);
            dispose();
        });
        cancel.addActionListener(e -> dispose());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(form);
        content.add(allergenPanel);
        content.add(Box.createVerticalStrut(8));

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    /** @return the created item, or null if the user cancelled. */
    public MenuItem showDialog() {
        setVisible(true);
        return result;
    }
}
