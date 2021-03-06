/**
 *
 */
package ru.ucoz.megadiablo.android.apm.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ru.ucoz.megadiablo.android.apm.Core;
import ru.ucoz.megadiablo.android.apm.iface.DevicesListener;
import ru.ucoz.megadiablo.android.apm.impl.DevicesListenerDefault;

import com.adbhelper.adb.AdbDevice;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.JToggleButton;

/**
 * @author MegaDiablo
 * 
 */
public class SearchPackages extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1752582856679525418L;

	private JTextField mFieldFilter;
	private JComboBox mComboBoxDevices;

	private ListPackages mListPackages;
	private Core mCore;

	private DevicesListener mDevicesListener;
	private JButton mButtonRefresh;

	public SearchPackages(final Core pCore, final ListPackages pListPackages) {
		setBorder(new EmptyBorder(0, 3, 0, 3));

		mListPackages = pListPackages;
		mCore = pCore;

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights =
				new double[] { 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel mLabelFilter =
				new JLabel(
						"\u0424\u0438\u043B\u044C\u0442\u0440\u043E\u0432\u0430\u0442\u044C \u043F\u043E : ");
		GridBagConstraints gbc_mLabelFilter = new GridBagConstraints();
		gbc_mLabelFilter.anchor = GridBagConstraints.EAST;
		gbc_mLabelFilter.insets = new Insets(0, 0, 0, 5);
		gbc_mLabelFilter.gridx = 0;
		gbc_mLabelFilter.gridy = 0;
		add(mLabelFilter, gbc_mLabelFilter);

		mFieldFilter = new JTextField();
		mFieldFilter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateFilter();
			}
		});
		GridBagConstraints gbc_mFieldFilter = new GridBagConstraints();
		gbc_mFieldFilter.insets = new Insets(0, 0, 0, 5);
		gbc_mFieldFilter.fill = GridBagConstraints.HORIZONTAL;
		gbc_mFieldFilter.gridx = 1;
		gbc_mFieldFilter.gridy = 0;
		add(mFieldFilter, gbc_mFieldFilter);
		mFieldFilter.setColumns(10);

		JLabel mLabelDevice = new JLabel("Устройство : ");
		GridBagConstraints gbc_mLabelDevice = new GridBagConstraints();
		gbc_mLabelDevice.insets = new Insets(0, 0, 0, 5);
		gbc_mLabelDevice.anchor = GridBagConstraints.EAST;
		gbc_mLabelDevice.gridx = 3;
		gbc_mLabelDevice.gridy = 0;
		add(mLabelDevice, gbc_mLabelDevice);

		mComboBoxDevices = new JComboBox();
		mComboBoxDevices.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {

				Object[] objects = e.getItemSelectable().getSelectedObjects();

				if (objects != null && objects.length > 0) {
					mCore.setSelectDevice((AdbDevice) objects[0]);
				}
			}
		});
		// mComboBoxDevices.setModel(new DefaultComboBoxModel(
		// mCore.devices().toArray()
		// new String[] {
		// "\u0412\u043E\u0437\u0440\u0430\u0441\u0442\u0430\u043D\u0438\u044E",
		// "\u0423\u0431\u044B\u0432\u0430\u043D\u0438\u044E"}
		// ));
		GridBagConstraints gbc_mComboBoxDevices = new GridBagConstraints();
		gbc_mComboBoxDevices.insets = new Insets(0, 0, 0, 5);
		gbc_mComboBoxDevices.gridx = 4;
		gbc_mComboBoxDevices.gridy = 0;
		add(mComboBoxDevices, gbc_mComboBoxDevices);

		mButtonRefresh = new JButton("");
		mButtonRefresh.setBorderPainted(false);
		mButtonRefresh.setContentAreaFilled(false);
		mButtonRefresh.setOpaque(false);

		mButtonRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				mCore.refreshDevices();
			}
		});
		mButtonRefresh.setFocusable(false);
		mButtonRefresh.setBorder(new EmptyBorder(1, 1, 1, 1));
		mButtonRefresh.setIcon(new ImageIcon(StatusBar.class
				.getResource("/res/refresh.png")));
		mButtonRefresh.setHorizontalTextPosition(SwingConstants.CENTER);

		GridBagConstraints gbc_ButtonRefresh = new GridBagConstraints();
		gbc_ButtonRefresh.gridx = 5;
		gbc_ButtonRefresh.gridy = 0;
		add(mButtonRefresh, gbc_ButtonRefresh);

		initListeners();
	}

	private void initListeners() {
		if (mCore == null) {
			return;
		}

		mCore.removeDevicesListener(mDevicesListener);

		mDevicesListener = new DevicesListenerDefault() {

			public void changeSelectDevice(final AdbDevice pAdbDevice) {
				updateSelectDevice(pAdbDevice);
			}

			public void updateListDevices(final List<AdbDevice> pAdbDevices) {
				updateDevices(pAdbDevices);
			}
		};

		mCore.addDevicesListener(mDevicesListener);
	}

	private void updateSelectDevice(final AdbDevice pDevice) {
		if (mComboBoxDevices.getModel().getSelectedItem() != pDevice) {
			mComboBoxDevices.getModel().setSelectedItem(pDevice);
		}
	}

	private void updateDevices(final List<AdbDevice> pDevices) {
		DefaultComboBoxModel model;
		if (pDevices != null) {
			model = new DefaultComboBoxModel(pDevices.toArray());
		} else {
			model =
					new DefaultComboBoxModel(
							new String[] { "- Нет устройства-" });
		}

		Object object = mComboBoxDevices.getSelectedItem();
		mComboBoxDevices.setModel(model);
		mComboBoxDevices.setSelectedItem(object);
	}

	private void updateFilter() {
		String text = mFieldFilter.getText();
		if (text.length() == 0) {
			mListPackages.setFilter(null);
		} else {
			mListPackages.setFilter(text);
		}
	}

	public void setFilter(final String pFilter) {
		mFieldFilter.setText(pFilter);
		updateFilter();
	}

	public String getFilter() {
		return mFieldFilter.getText();
	}

}
