// Code was originally written by developer - https://github.com/zlatinb
// Taken from repository https://github.com/zlatinb/zcash-swing-wallet-ui under an MIT licemse
package com.vaklinov.zcashui;

import static net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig.log;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import net.ddns.lsmobile.zencashvaadinwalletui4cpp.business.IConfig;

public class AddressBookPanel extends JPanel  implements IConfig{
    
    private static class AddressBookEntry {
        final String name,address;
        AddressBookEntry(final String name, final String address) {
            this.name = name;
            this.address = address;
        }
    }
    
    private final List<AddressBookEntry> entries =
            new ArrayList<>();

    private final Set<String> names = new HashSet<>();
    
    private JTable table;
    
    private JButton sendCashButton, deleteContactButton,copyToClipboardButton;
    
    private final SendCashPanel sendCashPanel;
    private final JTabbedPane tabs;
    
    private JPanel buildButtonsPanel() {
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
        
        final JButton newContactButton = new JButton("New contact...");
        newContactButton.addActionListener(new NewContactActionListener());
        panel.add(newContactButton);
                
        this.sendCashButton = new JButton("Send ZEN");
        this.sendCashButton.addActionListener(new SendCashActionListener());
        this.sendCashButton.setEnabled(false);
        panel.add(this.sendCashButton);
        
        this.copyToClipboardButton = new JButton("Copy address to clipboard");
        this.copyToClipboardButton.setEnabled(false);
        this.copyToClipboardButton.addActionListener(new CopyToClipboardActionListener());
        panel.add(this.copyToClipboardButton);
        
        this.deleteContactButton = new JButton("Delete contact");
        this.deleteContactButton.setEnabled(false);
        this.deleteContactButton.addActionListener(new DeleteAddressActionListener());
        panel.add(this.deleteContactButton);
        
        return panel;
    }

    private JScrollPane buildTablePanel() {
        this.table = new JTable(new AddressBookTableModel(),new DefaultTableColumnModel());
        final TableColumn nameColumn = new TableColumn(0);
        final TableColumn addressColumn = new TableColumn(1);
        this.table.addColumn(nameColumn);
        this.table.addColumn(addressColumn);
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // one at a time
        this.table.getSelectionModel().addListSelectionListener(new AddressListSelectionListener());
        this.table.addMouseListener(new AddressMouseListener());
        
        // TODO: isolate in utility
		final TableCellRenderer renderer = this.table.getCellRenderer(0, 0);
		final Component comp = renderer.getTableCellRendererComponent(this.table, "123", false, false, 0, 0);
		this.table.setRowHeight(new Double(comp.getPreferredSize().getHeight()).intValue() + 2);
        
        final JScrollPane scrollPane = new JScrollPane(this.table);
        return scrollPane;
    }

    public AddressBookPanel(final SendCashPanel sendCashPanel, final JTabbedPane tabs) throws IOException {
        this.sendCashPanel = sendCashPanel;
        this.tabs = tabs;
        final BoxLayout boxLayout = new BoxLayout(this,BoxLayout.Y_AXIS);
        setLayout(boxLayout);
        add(buildTablePanel());
        add(buildButtonsPanel());
       
        loadEntriesFromDisk();
    }
    
    private void loadEntriesFromDisk() throws IOException {
        final File addressBookFile = new File(OSUtil.getSettingsDirectory(),"addressBook.csv");
        if (!addressBookFile.exists()) {
			return;
		}
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(addressBookFile))) {
            String line;
            while((line = bufferedReader.readLine()) != null) {
                // format is address,name - this way name can contain commas ;-)
                final int addressEnd = line.indexOf(',');
                if (addressEnd < 0) {
					throw new IOException("Address Book is corrupted!");
				}
                final String address = line.substring(0, addressEnd);
                final String name = line.substring(addressEnd + 1);
                if (!this.names.add(name))
				 {
					continue; // duplicate
				}
                this.entries.add(new AddressBookEntry(name,address));
            }
        }
        
        log.info("loaded "+this.entries.size()+" address book entries");
    }
    
    private void saveEntriesToDisk() {
    	log.info("Saving "+this.entries.size()+" addresses");
        try {
            final File addressBookFile = new File(OSUtil.getSettingsDirectory(),"addressBook.csv");
            try (PrintWriter printWriter = new PrintWriter(new FileWriter(addressBookFile))) {
                for (final AddressBookEntry entry : this.entries) {
					printWriter.println(entry.address+","+entry.name);
				}
            }
        } catch (final IOException bad) {
        	// TODO: report error to the user!
        	log.error("Saving Address Book Failed!!!!", bad);
        }
    }
    
    private class DeleteAddressActionListener implements ActionListener {
        @Override
		public void actionPerformed(final ActionEvent e) {
            final int row = AddressBookPanel.this.table.getSelectedRow();
            if (row < 0) {
				return;
			}
            final AddressBookEntry entry = AddressBookPanel.this.entries.get(row);
            AddressBookPanel.this.entries.remove(row);
            AddressBookPanel.this.names.remove(entry.name);
            AddressBookPanel.this.deleteContactButton.setEnabled(false);
            AddressBookPanel.this.sendCashButton.setEnabled(false);
            AddressBookPanel.this.copyToClipboardButton.setEnabled(false);
            AddressBookPanel.this.table.repaint();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
				public void run() {
                    saveEntriesToDisk();
                }
            });
        }
    }
    
    private class CopyToClipboardActionListener implements ActionListener {
        @Override
		public void actionPerformed(final ActionEvent e) {
            final int row = AddressBookPanel.this.table.getSelectedRow();
            if (row < 0) {
				return;
			}
            final AddressBookEntry entry = AddressBookPanel.this.entries.get(row);
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(entry.address), null);
        }
    }
    
    private class NewContactActionListener implements ActionListener {
        @Override
		public void actionPerformed(final ActionEvent e) {
            final String name = (String) JOptionPane.showInputDialog(AddressBookPanel.this,
                    "Please enter the name of the contact:",
                    "Add new contact step 1",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
            if (name == null || "".equals(name))
			 {
				return; // cancelled
			}

            // TODO: check for dupes
            AddressBookPanel.this.names.add(name);
            
            final String address = (String) JOptionPane.showInputDialog(AddressBookPanel.this,
                    "Please enter the t-address or z-address of "+name,
                    "Add new contact step 2",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
            if (address == null || "".equals(address))
			 {
				return; // cancelled
			}
            AddressBookPanel.this.entries.add(new AddressBookEntry(name,address));
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
				public void run() {
                    AddressBookPanel.this.table.invalidate();
                    AddressBookPanel.this.table.revalidate();
                    AddressBookPanel.this.table.repaint();
                	
                    saveEntriesToDisk();
                }
            });
        }
    }
    
    private class SendCashActionListener implements ActionListener {
        @Override
		public void actionPerformed(final ActionEvent e) {
            final int row = AddressBookPanel.this.table.getSelectedRow();
            if (row < 0) {
				return;
			}
            final AddressBookEntry entry = AddressBookPanel.this.entries.get(row);
            AddressBookPanel.this.sendCashPanel.prepareForSending(entry.address);
            AddressBookPanel.this.tabs.setSelectedIndex(2);
        }
    }

    private class AddressMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(final MouseEvent e) {
            if (e.isConsumed() || (!e.isPopupTrigger())) {
				return;
			}

            final int row = AddressBookPanel.this.table.rowAtPoint(e.getPoint());
            final int column = AddressBookPanel.this.table.columnAtPoint(e.getPoint());
            AddressBookPanel.this.table.changeSelection(row, column, false, false);
            final AddressBookEntry entry = AddressBookPanel.this.entries.get(row);
            
            final JPopupMenu menu = new JPopupMenu();
            
            final JMenuItem sendCash = new JMenuItem("Send ZEN to "+entry.name);
            sendCash.addActionListener(new SendCashActionListener());
            menu.add(sendCash);
            
            final JMenuItem copyAddress = new JMenuItem("Copy address to clipboard");
            copyAddress.addActionListener(new CopyToClipboardActionListener());
            menu.add(copyAddress);
            
            final JMenuItem deleteEntry = new JMenuItem("Delete "+entry.name+" from contacts");
            deleteEntry.addActionListener(new DeleteAddressActionListener());
            menu.add(deleteEntry);
            
            menu.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
            e.consume();
        }
        
        @Override
		public void mouseReleased(final MouseEvent e)
        {
        	if ((!e.isConsumed()) && e.isPopupTrigger())
        	{
        		mousePressed(e);
            }
        }
    }
    
    private class AddressListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            final int row = AddressBookPanel.this.table.getSelectedRow();
            if (row < 0) {
                AddressBookPanel.this.sendCashButton.setEnabled(false);
                AddressBookPanel.this.deleteContactButton.setEnabled(false);
                AddressBookPanel.this.copyToClipboardButton.setEnabled(false);
                return;
            }
            final String name = AddressBookPanel.this.entries.get(row).name;
            AddressBookPanel.this.sendCashButton.setText("Send ZEN to "+name);
            AddressBookPanel.this.sendCashButton.setEnabled(true);
            AddressBookPanel.this.deleteContactButton.setText("Delete contact "+name);
            AddressBookPanel.this.deleteContactButton.setEnabled(true);
            AddressBookPanel.this.copyToClipboardButton.setEnabled(true);
        }
        
    }

    private class AddressBookTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return AddressBookPanel.this.entries.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(final int columnIndex) {
            switch(columnIndex) {
            case 0 : return "name";
            case 1 : return "address";
            default:
                throw new IllegalArgumentException("invalid column "+columnIndex);
            }
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            final AddressBookEntry entry = AddressBookPanel.this.entries.get(rowIndex);
            switch(columnIndex) {
            case 0 : return entry.name;
            case 1 : return entry.address;
            default:
                throw new IllegalArgumentException("bad column "+columnIndex);
            }
        }
    }
}