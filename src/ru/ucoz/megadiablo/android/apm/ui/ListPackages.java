package ru.ucoz.megadiablo.android.apm.ui;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.adbhelper.adb.AdbPackage;
import javax.swing.JSeparator;

import ru.ucoz.megadiablo.android.apm.Core;
import ru.ucoz.megadiablo.android.apm.FileDrop;
import ru.ucoz.megadiablo.android.apm.iface.PackagesListener;
import ru.ucoz.megadiablo.android.apm.impl.PackagesListenerDefault;

/**
 * @author MegaDiablo
 * */
public class ListPackages extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 6197850016233338650L;
	private JPopupMenu popupMenu;
	private JTable table;
	private DefaultTableModel model;
	private FilterByText mFilter = new FilterByText();
	private TableRowSorter<TableModel> sorter;

	private JFolderChoose mFolderChoose;

	private PackagesListener mPackagesListner;

	private Core mCore = null;
	private JMenuItem mMenuItemDownload;
	private JMenuItem mMenuItemDelete;
	private JMenuItem mMenuItemRun;
	private JMenuItem mMenuItemRefresh;
	private JSeparator separator;

	public ListPackages(Core pCore) {
		mCore = pCore;

		setLayout(new BorderLayout(0, 0));

		sorter = new TableRowSorter<TableModel>();

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		table = new JTable();
		table.setVerifyInputWhenFocusTarget(false);
		table.setRowSorter(sorter);

		updateModel(null);

		popupMenu = new JPopupMenu();

		{
			mMenuItemRun = new JMenuItem("Запустить");
			mMenuItemRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					startPackages();
				}
			});
			popupMenu.add(mMenuItemRun);
		}

		{
			mMenuItemDelete = new JMenuItem("Удалить");
			mMenuItemDelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int result = JOptionPane.showConfirmDialog(
							ListPackages.this,
							"Точно хотите удалить приложение(я)?",
							"Предупреждение", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						removePackages();
					}
				}
			});
			popupMenu.add(mMenuItemDelete);
		}

		{
			mMenuItemDownload = new JMenuItem("Скачать");
			mMenuItemDownload.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// int result = mChooseFolder
					// .showSaveDialog(ListPackages.this);

					if (mFolderChoose == null) {
						mFolderChoose = new JFolderChoose(null);
					}

					int result = mFolderChoose.open();

					if (result == JFileChooser.APPROVE_OPTION) {
						downloadPackages(mFolderChoose.getSelectedFolder());
					}
				}
			});
			popupMenu.add(mMenuItemDownload);
		}

		{
			separator = new JSeparator();
			popupMenu.add(separator);
		}

		{
			mMenuItemRefresh = new JMenuItem("Обновить");
			mMenuItemRefresh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mCore.refreshPackages();
				}
			});

			popupMenu.add(mMenuItemRefresh);
		}

		MouseListener popupListener = new PopupListener();
		scrollPane.addMouseListener(popupListener);
		table.addMouseListener(popupListener);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					Point p = e.getPoint();
					int rowNumber = table.rowAtPoint(p);
					ListSelectionModel model = table.getSelectionModel();
					if (!model.isSelectedIndex(rowNumber)) {
						model.setSelectionInterval(rowNumber, rowNumber);
						// model.addSelectionInterval(rowNumber, rowNumber);
					}
				}
			}
		});

		scrollPane.setViewportView(table);

		new FileDrop(scrollPane, new FileDrop.Listener() {
			@Override
			public void filesDropped(File[] files) {
				installPackages(files);
			}
		});

		initListeners();
	}

	private void initListeners() {
		if (mCore == null) {
			return;
		}

		mCore.removePackagesListener(mPackagesListner);

		mPackagesListner = new PackagesListenerDefault() {
			public void updatePackages(List<AdbPackage> pAdbPackages) {
				updateListPackages(pAdbPackages);
			};
		};

		mCore.addPackagesListener(mPackagesListner);
	}

	private void updateListPackages(final List<AdbPackage> packages) {
		updateModel(packages);
	}

	public void setFilter(String text) {
		if (text == null) {
			text = "";
		}
		mFilter.text = text.toLowerCase();
		sorter.setRowFilter(mFilter);
	}

	private void updateModel(final List<AdbPackage> pList) {
		Object data[][] = {};
		if (pList != null) {
			data = new Object[pList.size()][1];

			for (int i = 0; i < pList.size(); i++) {
				AdbPackage item = pList.get(i);
				if (item != null) {
					data[i][0] = item;
				} else {
					data[i][0] = " - пакет не определен - ";
				}
			}
		}

		String columsNames[] = { "Название пакета" };

		model = new DefaultTableModel(data, columsNames) {
			/**
			 *
			 */
			private static final long serialVersionUID = 5795337809978978846L;

			public boolean isCellEditable(int row, int column) {
				return false;
			};
		};

		table.setModel(model);
		sorter.setModel(model);
	}

	class FilterByText extends RowFilter<TableModel, Integer> {

		String text;

		@Override
		public boolean include(
				javax.swing.RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {

			if (text == null) {
				return true;
			}

			int count = entry.getValueCount();
			for (int i = 0; i < count; i++) {
				String value = entry.getStringValue(i);
				if (value != null && value.toLowerCase().contains(text)) {
					return true;
				}
			}

			return false;
		}
	};

	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			showPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}

		private void showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
				setButtonEnabled();
			}
		}

		private void setButtonEnabled() {
			int sel = table.getSelectedRowCount();
			boolean enabled = (sel > 0 ? true : false);

			mMenuItemRun.setEnabled(enabled);
			mMenuItemDelete.setEnabled(enabled);
			mMenuItemDownload.setEnabled(enabled);
		}

	}

	public void removePackages() {
		int[] selected = table.getSelectedRows();
		for (int index : selected) {
			removePackage(index);
		}

		mCore.refreshPackages();
	}

	public void startPackages() {
		int[] selected = table.getSelectedRows();
		for (int index : selected) {
			startPackage(index);
		}
	}

	public void downloadPackages(String to) {
		if (to != null && to.length() > 1 && !to.endsWith("\\")) {
			to += "\\";
		}
		int[] selected = table.getSelectedRows();
		for (int index : selected) {
			downloadPackage(index, to);
		}
	}

	private boolean removePackage(int index) {
		Object obj = table.getValueAt(index, 0);
		if (obj != null && obj instanceof AdbPackage) {
			AdbPackage item = (AdbPackage) obj;
			mCore.uninstall(item);
		}
		return true;
	}

	private boolean startPackage(int index) {
		Object obj = table.getValueAt(index, 0);
		if (obj != null && obj instanceof AdbPackage) {
			AdbPackage item = (AdbPackage) obj;
			mCore.startApp(item);
		}
		return true;
	}

	private boolean downloadPackage(int index, String to) {
		Object obj = table.getValueAt(index, 0);
		if (obj != null && obj instanceof AdbPackage) {
			AdbPackage item = (AdbPackage) obj;
			mCore.download(item, String.format("%s%s.apk", to, item));
		}
		return true;
	}

	private void installPackages(File[] pFiles) {
		for (File file : pFiles) {
			mCore.install(file);
		}

		mCore.refreshPackages();
	}

}