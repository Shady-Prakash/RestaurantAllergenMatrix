package ui;

import algorithms.SearchAlgorithms;
import algorithms.SortAlgorithms;
import data.MenuRepository;
import model.Allergen;
import model.MenuItem;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * The Swing graphical interface. Presents the menu as an allergy matrix and
 * exposes every operation the text interface offers — add, delete, sort,
 * search and the "safe for a guest" allergen filter — with live feedback.
 */
public class MainFrame extends JFrame {

    private final MenuRepository repo;
    private final Path dataFile;

    private final MenuTableModel model = new MenuTableModel();
    private final JTable table = new JTable(model);
    private final JLabel status = new JLabel(" Ready");
    private final JTextField searchField = new JTextField(16);
    private final List<JCheckBox> allergenChecks = new ArrayList<>();

    public MainFrame(MenuRepository repo, Path dataFile) {
        super("Restaurant Allergy Matrix System");
        this.repo = repo;
        this.dataFile = dataFile;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1040, 620));
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        refreshTable(repo.all());
        setLocationRelativeTo(null);
    }

    // --------------------------------------------------------------- header

    private JComponent buildHeader() {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY,
                        getWidth(), 0, Theme.PRIMARY_DK));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        banner.setPreferredSize(new Dimension(0, 76));
        banner.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JLabel title = new JLabel("Restaurant Allergy Matrix");
        title.setFont(Theme.H1);
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel(
                "Track the 14 major allergens across every dish");
        subtitle.setForeground(new Color(0xDD, 0xF0, 0xE6));
        subtitle.setFont(Theme.BODY);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(title);
        titles.add(subtitle);
        banner.add(titles, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        JButton add = Theme.button("+ Add Dish", Theme.SURFACE);
        add.setForeground(Theme.PRIMARY_DK);
        JButton edit = Theme.button("Edit Selected", Theme.SURFACE);
        edit.setForeground(Theme.PRIMARY_DK);
        JButton del = Theme.button("Delete Selected", Theme.ACCENT);
        JButton save = Theme.button("Save", Theme.PRIMARY_DK);
        JButton reload = Theme.button("Reload", Theme.PRIMARY_DK);
        JButton about = Theme.button("About", Theme.PRIMARY_DK);
        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        del.addActionListener(e -> onDelete());
        save.addActionListener(e -> onSave());
        reload.addActionListener(e -> onReload());
        about.addActionListener(e -> onAbout());
        actions.add(add);
        actions.add(edit);
        actions.add(del);
        actions.add(save);
        actions.add(reload);
        actions.add(about);
        banner.add(actions, BorderLayout.EAST);
        return banner;
    }

    // --------------------------------------------------------------- center

    private JComponent buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        center.add(buildControls(), BorderLayout.NORTH);
        center.add(buildTable(), BorderLayout.CENTER);
        return center;
    }

    private JComponent buildControls() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(Theme.BG);

        // Row 1: sort + search ------------------------------------------------
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row1.setBackground(Theme.SURFACE);
        row1.setBorder(Theme.card());

        row1.add(label("Sort by:"));
        JComboBox<String> sortKey = new JComboBox<>(
                new String[]{"Name", "Price", "Category", "Allergen count"});
        JComboBox<String> algo = new JComboBox<>(
                new String[]{"Quick Sort", "Merge Sort"});
        JButton sortBtn = Theme.button("Sort", Theme.PRIMARY);
        sortBtn.addActionListener(e -> onSort(
                (String) sortKey.getSelectedItem(),
                (String) algo.getSelectedItem()));
        row1.add(sortKey);
        row1.add(label("using"));
        row1.add(algo);
        row1.add(sortBtn);

        row1.add(Box.createHorizontalStrut(24));
        row1.add(label("Search dish:"));
        searchField.setFont(Theme.BODY);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearch(); }
            public void removeUpdate(DocumentEvent e) { onSearch(); }
            public void changedUpdate(DocumentEvent e) { onSearch(); }
        });
        row1.add(searchField);
        JButton clearSearch = Theme.button("Clear", Theme.MUTED);
        clearSearch.addActionListener(e -> {
            searchField.setText("");
            refreshTable(repo.all());
        });
        row1.add(clearSearch);

        // Row 2: allergen safety filter --------------------------------------
        JPanel row2 = new JPanel(new BorderLayout(8, 4));
        row2.setBackground(Theme.SURFACE);
        row2.setBorder(Theme.card());
        JLabel cap = new JLabel("Show dishes SAFE for a guest allergic to:");
        cap.setFont(Theme.H2);
        cap.setForeground(Theme.TEXT);
        row2.add(cap, BorderLayout.NORTH);

        JPanel checks = new JPanel(new GridLayout(0, 7, 4, 2));
        checks.setBackground(Theme.SURFACE);
        for (Allergen a : Allergen.values()) {
            JCheckBox cb = new JCheckBox(a.label());
            cb.setBackground(Theme.SURFACE);
            cb.setFont(Theme.BODY);
            allergenChecks.add(cb);
            checks.add(cb);
        }
        row2.add(checks, BorderLayout.CENTER);

        JPanel filterButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        filterButtons.setBackground(Theme.SURFACE);
        JButton apply = Theme.button("Apply Filter", Theme.PRIMARY);
        JButton clearFilter = Theme.button("Clear Filter", Theme.MUTED);
        apply.addActionListener(e -> onFilterSafe());
        clearFilter.addActionListener(e -> {
            allergenChecks.forEach(c -> c.setSelected(false));
            refreshTable(repo.all());
            setStatus("Filter cleared — showing all " + repo.size() + " dishes");
        });
        filterButtons.add(clearFilter);
        filterButtons.add(apply);
        row2.add(filterButtons, BorderLayout.SOUTH);

        wrap.add(row1);
        wrap.add(Box.createVerticalStrut(10));
        wrap.add(row2);
        return wrap;
    }

    private JComponent buildTable() {
        table.setRowHeight(28);
        table.setFont(Theme.BODY);
        table.setGridColor(new Color(0xE3, 0xE8, 0xEC));
        table.setSelectionBackground(Theme.SELECTION);
        table.setSelectionForeground(Theme.TEXT);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(0, 64));

        DefaultTableCellRenderer body = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc,
                    int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0
                            ? Theme.SURFACE : Theme.ROW_ALT);
                }
                boolean allergenCol = model.isAllergenColumn(col);
                setHorizontalAlignment(allergenCol || col == 0 || col == 3
                        ? SwingConstants.CENTER : SwingConstants.LEFT);
                if (allergenCol && "●".equals(v)) {
                    c.setForeground(Theme.ACCENT);
                    setFont(getFont().deriveFont(Font.BOLD, 16f));
                } else {
                    c.setForeground(Theme.TEXT);
                    setFont(Theme.BODY);
                }
                return c;
            }
        };
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setCellRenderer(body);
            if (i == 0) col.setPreferredWidth(46);
            else if (i == 1) col.setPreferredWidth(190);
            else if (i == 2) col.setPreferredWidth(90);
            else if (i == 3) col.setPreferredWidth(70);
            else col.setPreferredWidth(58);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xE2, 0xE8)));
        sp.getViewport().setBackground(Theme.SURFACE);
        return sp;
    }

    private JComponent buildStatusBar() {
        status.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        status.setFont(Theme.BODY);
        status.setForeground(Theme.MUTED);
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0xEC, 0xEF, 0xF2));
        bar.add(status, BorderLayout.WEST);
        return bar;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.TEXT);
        return l;
    }

    // -------------------------------------------------------------- actions

    private void onAdd() {
        MenuItem created = new AddItemDialog(this).showDialog();
        if (created == null) {
            return;
        }
        repo.add(created);
        refreshTable(repo.all());
        try {
            repo.save(dataFile);
            setStatus("Added \"" + created.getName() + "\" (id "
                    + created.getId() + ") — saved to "
                    + dataFile.toAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Added in memory but could not save: " + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a dish in the table first.", "Nothing selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MenuItem original = model.rowAt(viewRow);
        MenuItem edited =
                new AddItemDialog(this, original).showDialog();
        if (edited == null) {
            return; // user cancelled
        }
        repo.update(edited);
        refreshTable(repo.all());
        try {
            repo.save(dataFile); 
            setStatus("Updated \"" + edited.getName() + "\" (id "
                + edited.getId() + ") — saved to "
                + dataFile.toAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Updated in memory but could not save: " + ex.getMessage(),
                "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Select a dish in the table first.", "Nothing selected",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MenuItem m = model.rowAt(viewRow);
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete \"" + m.getName() + "\" (id " + m.getId() + ")?",
                "Confirm delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            repo.deleteById(m.getId());
            refreshTable(repo.all());
            try {
                repo.save(dataFile);
                setStatus("Deleted \"" + m.getName() + "\"");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Deleted in memory but could not save: "
                            + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);            }
        }
    }

    private void onSort(String key, String algoName) {
        List<MenuItem> data = new ArrayList<>(currentRows());
        java.util.Comparator<MenuItem> cmp;
        switch (key) {
            case "Price":
                cmp = SortAlgorithms.byPrice();
                break;
            case "Category":
                cmp = SortAlgorithms.byCategory();
                break;
            case "Allergen count":
                cmp = SortAlgorithms.byAllergenCount();
                break;
            default:
                cmp = SortAlgorithms.byName();
                break;
        }
        long t0 = System.nanoTime();
        if ("Merge Sort".equals(algoName)) {
            SortAlgorithms.mergeSort(data, cmp);
        } else {
            SortAlgorithms.quickSort(data, cmp);
        }
        long us = (System.nanoTime() - t0) / 1000;
        model.setRows(data);
        setStatus(algoName + " by " + key + " — " + data.size()
                + " rows in " + us + " µs");
    }

    private void onSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) {
            refreshTable(repo.all());
            return;
        }
        List<MenuItem> hits =
                SearchAlgorithms.linearSearchByName(repo.all(), q);
        model.setRows(hits);
        setStatus("Linear search \"" + q + "\" — " + hits.size()
                + " match(es)");
    }

    private void onFilterSafe() {
        EnumSet<Allergen> avoid = EnumSet.noneOf(Allergen.class);
        for (int i = 0; i < allergenChecks.size(); i++) {
            if (allergenChecks.get(i).isSelected()) {
                avoid.add(Allergen.values()[i]);
            }
        }
        if (avoid.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tick at least one allergen to filter by.",
                    "No allergen selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<MenuItem> safe =
                SearchAlgorithms.filterSafeFor(repo.all(), avoid);
        model.setRows(safe);
        setStatus(safe.size() + " of " + repo.size()
                + " dishes are safe (avoiding " + avoid.size()
                + " allergen(s))");
    }

    private void onSave() {
        try {
            repo.save(dataFile);
            Path saved = dataFile.toAbsolutePath();
            long bytes = java.nio.file.Files.size(saved);
            setStatus("Saved " + repo.size() + " dishes (" + bytes
                    + " bytes) to " + saved);
            JOptionPane.showMessageDialog(this,
                    "Saved " + repo.size() + " dishes to:\n" + saved,
                    "Save successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save: " + ex.getMessage(), "Save failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReload() {
        try {
            int skipped = repo.load(dataFile);
            refreshTable(repo.all());
            setStatus("Reloaded " + repo.size() + " dishes"
                    + (skipped > 0 ? " (" + skipped + " bad lines skipped)"
                                   : ""));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not reload: " + ex.getMessage(), "Reload failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAbout() {
        JOptionPane.showMessageDialog(this,
                "Restaurant Allergy Matrix System\n"
                        + "ICT711 Assessment 4\n\n"
                        + "Add, delete, sort and search the menu while\n"
                        + "tracking the 14 major food allergens.\n"
                        + "Quick Sort / Merge Sort and binary / linear\n"
                        + "search power the data operations.",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // -------------------------------------------------------------- helpers

    private List<MenuItem> currentRows() {
        List<MenuItem> rows = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            rows.add(model.rowAt(i));
        }
        return rows.isEmpty() ? repo.all() : rows;
    }

    private void refreshTable(List<MenuItem> items) {
        model.setRows(items);
    }

    private void setStatus(String msg) {
        status.setText(" " + msg);
    }

    /** Header renderer that paints the dark band and rotates nothing. */
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc,
                int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setBackground(Theme.HEADER_BG);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 11));
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            setText("<html><center>"
                    + String.valueOf(v).replace(" ", "<br>") + "</center></html>");
            return this;
        }
    }
}
