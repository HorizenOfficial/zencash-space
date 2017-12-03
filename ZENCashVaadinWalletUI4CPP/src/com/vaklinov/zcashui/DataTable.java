/************************************************************************************************
 *  _________          _     ____          _           __        __    _ _      _   _   _ ___
 * |__  / ___|__ _ ___| |__ / ___|_      _(_)_ __   __ \ \      / /_ _| | | ___| |_| | | |_ _|
 *   / / |   / _` / __| '_ \\___ \ \ /\ / / | '_ \ / _` \ \ /\ / / _` | | |/ _ \ __| | | || |
 *  / /| |__| (_| \__ \ | | |___) \ V  V /| | | | | (_| |\ V  V / (_| | | |  __/ |_| |_| || |
 * /____\____\__,_|___/_| |_|____/ \_/\_/ |_|_| |_|\__, | \_/\_/ \__,_|_|_|\___|\__|\___/|___|
 *                                                 |___/
 *
 * Copyright (c) 2016 Ivan Vaklinov <ivan@vaklinov.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **********************************************************************************/
package com.vaklinov.zcashui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;



/**
 * Table to be used for transactions, addresses etc.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class DataTable
	extends JTable
{
	protected int lastRow = -1;
	protected int lastColumn = -1;
	
	protected JPopupMenu popupMenu;
	
	public DataTable(final Object[][] rowData, final Object[] columnNames)
	{
		super(rowData, columnNames);
		
		// TODO: isolate in utility
		final TableCellRenderer renderer = this.getCellRenderer(0, 0);
		final Component comp = renderer.getTableCellRendererComponent(this, "123", false, false, 0, 0);
		this.setRowHeight(new Double(comp.getPreferredSize().getHeight()).intValue() + 2);
		
		this.popupMenu = new JPopupMenu();
		final int accelaratorKeyMask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask();
		
		final JMenuItem copy = new JMenuItem("Copy value");
        this.popupMenu.add(copy);
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelaratorKeyMask));
        copy.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if ((DataTable.this.lastRow >= 0) && (DataTable.this.lastColumn >= 0))
				{
					final String text = DataTable.this.getValueAt(DataTable.this.lastRow, DataTable.this.lastColumn).toString();
				
					final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(new StringSelection(text), null);
				} else
				{
					// Log perhaps
				}
			}
		});
        
        
		final JMenuItem exportToCSV = new JMenuItem("Export data to CSV...");
        this.popupMenu.add(exportToCSV);
        exportToCSV.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, accelaratorKeyMask));
        exportToCSV.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				try
				{
					DataTable.this.exportToCSV();
				} catch (final Exception ex)
				{
					Log.error("Unexpected error: ", ex);
					// TODO: better error handling
					JOptionPane.showMessageDialog(
							DataTable.this.getRootPane().getParent(),
							"An unexpected error occurred when exporting data to CSV file.\n" +
							"\n" +
							ex.getMessage(),
							"Error in CSV export", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
        
        
        this.addMouseListener(new MouseAdapter()
        {
        	@Override
			public void mousePressed(final MouseEvent e)
        	{
                if ((!e.isConsumed()) && e.isPopupTrigger())
                {
                    final JTable table = (JTable)e.getSource();
                    DataTable.this.lastColumn = table.columnAtPoint(e.getPoint());
                    DataTable.this.lastRow = table.rowAtPoint(e.getPoint());
                    
                    if (!table.isRowSelected(DataTable.this.lastRow))
                    {
                        table.changeSelection(DataTable.this.lastRow, DataTable.this.lastColumn, false, false);
                    }

                    DataTable.this.popupMenu.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
                    e.consume();
                } else
                {
                	DataTable.this.lastColumn = -1;
                	DataTable.this.lastRow    = -1;
                }
        	}
        	
            @Override
			public void mouseReleased(final MouseEvent e)
            {
            	if ((!e.isConsumed()) && e.isPopupTrigger())
            	{
            		mousePressed(e);
            	}
            }
        });
        
//        this.addKeyListener(new KeyAdapter()
//		{
//			@Override
//			public void keyTyped(KeyEvent e)
//			{
//				if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
//				{
//					System.out.println("Context menu invoked...");;
//					popupMenu.show(e.getComponent(), e.getComponent().getX(), e.getComponent().getY());
//				}
//			}
//		});
	}

	
	// Make sure data in the table cannot be edited - by default.
	// Descendants may change this
	@Override
    public boolean isCellEditable(final int row, final int column)
    {
        return false;
    }
	
	
	// Exports the table data to a CSV file
	private void exportToCSV()
		throws IOException
	{
        final String ENCODING = "UTF-8";
		
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Export data to CSV file...");
		fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
		 
		final int result = fileChooser.showSaveDialog(this.getRootPane().getParent());
		 
		if (result != JFileChooser.APPROVE_OPTION)
		{
		    return;
		}
		
		final File f = fileChooser.getSelectedFile();
		
		final FileOutputStream fos = new FileOutputStream(f);
		fos.write(new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF } );
		
		// Write header
		final StringBuilder header = new StringBuilder();
		for (int i = 0; i < this.getColumnCount(); i++)
		{
			final String columnName = this.getColumnName(i);
			header.append(columnName);
			
			if (i < (this.getColumnCount() - 1))
			{
				header.append(",");
			}
		}
		header.append("\n");
		fos.write(header.toString().getBytes(ENCODING));
		
		// Write rows
		for (int row = 0; row < this.getRowCount(); row++)
		{
			final StringBuilder rowBuf = new StringBuilder();
			for (int col = 0; col < this.getColumnCount(); col++)
			{
				rowBuf.append(this.getValueAt(row, col).toString());
				
				if (col < (this.getColumnCount() - 1))
				{
					rowBuf.append(",");
				}
			}
			rowBuf.append("\n");
			fos.write(rowBuf.toString().getBytes(ENCODING));
		}
		
		fos.close();
		
		JOptionPane.showMessageDialog(
			this.getRootPane().getParent(),
			"The data has been exported successfully as CSV to location:\n" +
			f.getCanonicalPath(),
			"Export successful...", JOptionPane.INFORMATION_MESSAGE);
	}
}
