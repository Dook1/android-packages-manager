package ru.ucoz.megadiablo.android.apm;import java.io.File;import java.util.ArrayList;import java.util.List;import javax.swing.JOptionPane;import javax.swing.SwingUtilities;import ru.ucoz.megadiablo.android.apm.connect.ListConnect;import ru.ucoz.megadiablo.android.apm.iface.DevicesListener;import ru.ucoz.megadiablo.android.apm.iface.PackagesListener;import ru.ucoz.megadiablo.android.apm.ui.EnumPLAF;import com.adbhelper.adb.AdbDevice;import com.adbhelper.adb.AdbModule;import com.adbhelper.adb.AdbPackage;import com.adbhelper.adb.exceptions.DeviceIsEmulatorRebootException;import com.adbhelper.adb.exceptions.NotAccessPackageManager;import com.adbhelper.adb.exceptions.NotFoundActivityException;import com.adbhelper.adb.exceptions.install.InstallException;import com.adbhelper.adb.exceptions.install.InstallExceptionAlreadyExists;import com.adbhelper.adb.exceptions.install.InstallExceptionNotFoundFile;/** * @author MegaDiablo * */public class Core {	private static final String ERROR_MESSAGE_INSTALL =			"Приложение не может быть установленно.\n%s";	private Events mEvents;	private Settings mSettings;	private ListConnect mListConnect;	private AdbModule mAdbModule;	private AdbDevice mSelectDevice = null;	private List<DevicesListener> mDevicesListeners =			new ArrayList<DevicesListener>();	private List<PackagesListener> mPackagesListeners =			new ArrayList<PackagesListener>();	public Core(final AdbModule pAdbModule, final Events pEvents) {		mAdbModule = pAdbModule;		mEvents = pEvents;		mSettings = Settings.getInstance();		mListConnect = new ListConnect(this);	}	public AdbDevice getSelectDevice() {		return mSelectDevice;	}	public void setSelectDevice(final AdbDevice pAdbDevice) {		String name =				"\u0412\u044b\u0431\u043e\u0440 \u0443\u0441\u0442\u0440\u043e\u0439\u0441\u0442\u0432\u0430";		String desc =				String.format(						"\u0421\u043c\u0435\u043d\u0430 \u0443\u0441\u0442\u0440\u043e\u0439\u0441\u0442\u0432\u0430 \u043d\u0430 %s.",						pAdbDevice);		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				if (mSelectDevice == null && mSelectDevice != pAdbDevice						|| mSelectDevice != null						&& !mSelectDevice.equals(pAdbDevice)) {					mSelectDevice = pAdbDevice;					fireListener(							mDevicesListeners,							new EventListener<DevicesListener>() {								@Override								public void perfom(final DevicesListener pItem) {									pItem.changeSelectDevice(pAdbDevice);								}							});					refreshPackages();				}			}		});	}	public void refreshDevices() {		String name =				"\u041e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043f\u0438\u0441\u043a\u0430 \u0443\u0441\u0442\u0440\u043e\u0439\u0441\u0442\u0432";		String desc =				"\u041e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u044f \u0441\u043f\u0438\u0441\u043a\u0430 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u043d\u044b\u0445 \u0443\u0441\u0442\u0440\u043e\u0439\u0441\u0442\u0432.";		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				final List<AdbDevice> devices = mAdbModule.devices();				fireListener(						mDevicesListeners,						new EventListener<DevicesListener>() {							@Override							public void perfom(final DevicesListener pItem) {								pItem.updateListDevices(devices);							}						});				if (!devices.contains(mSelectDevice)) {					final AdbDevice device = mSelectDevice;					if (mSelectDevice != null) {						fireListener(								mDevicesListeners,								new EventListener<DevicesListener>() {									@Override									public void perfom(final DevicesListener pItem) {										pItem.lostSelectDevice(device);									}								});					}					if (devices.size() > 0) {						setSelectDevice(devices.get(0));					} else {						setSelectDevice(null);					}				}			}		});	}	public void refreshPackages() {		String name =				"\u041e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043f\u0438\u0441\u043a\u0430 \u043f\u0430\u043a\u0435\u0442\u043e\u0432";		String desc =				"\u041e\u0431\u043d\u043e\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u0441\u043f\u0438\u0441\u043e\u043a \u043f\u0430\u043a\u0435\u0442\u043e\u0432 \u043d\u0430 \u0442\u0435\u043a\u0443\u0449\u0435\u043c \u0432\u044b\u0434\u0435\u043b\u0435\u043d\u043d\u043e\u0439\u043c \u0443\u0442\u0441\u0440\u043e\u0439\u0441\u0442\u0432\u0435.";		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				List<AdbPackage> packs = new ArrayList<AdbPackage>();				if (mSelectDevice == null) {					packs = new ArrayList<AdbPackage>();				} else {					try {						packs =								mSelectDevice.refreshListPackages(mSettings										.isVisibleSystemPackages());					} catch (NotAccessPackageManager e) {						e.printStackTrace();					}				}				final List<AdbPackage> packages = packs;				fireListener(						mPackagesListeners,						new EventListener<PackagesListener>() {							@Override							public void perfom(final PackagesListener pItem) {								pItem.updatePackages(packages);							}						});			}		});	}	public void rebootDevice() {		String name =				"\u041f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0430";		String desc =				String.format(						"\u041f\u0435\u0440\u0435\u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0443\u0441\u0442\u0440\u043e\u0439\u0441\u0442\u0432\u0430 %s.",						mSelectDevice);		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				if (mSelectDevice != null) {					try {						mSelectDevice.reboot();					} catch (DeviceIsEmulatorRebootException e) {						e.printStackTrace();					}					refreshDevices();				}			}		});	}	public void install(final String pFile) {		String name =				"\u0423\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0430 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044f";		String desc =				String.format(						"\u0423\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0430 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435 %s.",						pFile);		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				try {					if (mSettings.isUseReinstall()) {						mSelectDevice.reinstall(								pFile,								mSettings.isAutostartPackage());					} else {						mSelectDevice.install(								pFile,								mSettings.isAutostartPackage());					}				} catch (InstallExceptionAlreadyExists e) {					String text =							String.format(									ERROR_MESSAGE_INSTALL,									"Приложение уже установлено.");					showErrorDialog(text);				} catch (InstallExceptionNotFoundFile e) {					String text =							String.format(									ERROR_MESSAGE_INSTALL,									"Файл не найден.");					showErrorDialog(text);				} catch (InstallException e) {					String text =							String.format(ERROR_MESSAGE_INSTALL, e.getMessage());					showErrorDialog(text);					e.printStackTrace();				}			}		});	}	public void install(final File pFile) {		install(pFile.getAbsolutePath());	}	public void uninstall(final AdbPackage pAdbPackage) {		String name =				"\u0423\u0434\u0430\u043b\u0435\u043d\u0438\u0435 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044f";		String desc =				String.format(						"\u0423\u0434\u0430\u043b\u044f\u0435\u0442\u0441\u044f \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435 %s.",						pAdbPackage);		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				pAdbPackage.uninstall();			}		});	}	public void download(final AdbPackage pAdbPackage, final String pPath) {		String name =				"\u0421\u043a\u0430\u0447\u043a\u0430 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044f";		String desc =				String.format(						"\u0421\u043a\u0430\u0447\u0438\u0432\u0430\u0435\u0442 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435 %s.",						pAdbPackage);		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				if (pPath == null) {					pAdbPackage.download();				} else {					pAdbPackage.download(pPath);				}			}		});	}	public void startApp(final AdbPackage pAdbPackage) {		String name =				"\u0417\u0430\u043f\u0443\u0441\u043a \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044f";		String desc =				String.format(						"\u0417\u0430\u043f\u0443\u0441\u043a\u0430\u0435\u0442 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435 %s.",						pAdbPackage);		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				try {					pAdbPackage.start();				} catch (NotFoundActivityException e) {					e.printStackTrace();				}			}		});	}	public void sendKey(final int pKey) {		String name =				"\u041d\u0430\u0436\u0430\u0442\u044c \u043a\u043b\u0430\u0432\u0438\u0448\u0443";		String desc =				String.format(						"\u041d\u0430\u0436\u0430\u0442\u044c \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u0441 \u043a\u043e\u0434\u043e\u043c %s.",						pKey);		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				mSelectDevice.sendKeyCode(pKey);			}		});	}	public void termantedAllTasks() {		mEvents.clearList();		terminatedCurrentTask();	}	public void terminatedCurrentTask() {		mAdbModule.stopCurrentProcess();	}	public void stopAdb() {		String name =				"\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0430 ADB";		String desc =				"\u041e\u0441\u0442\u0430\u043d\u0430\u0432\u043b\u0438\u0432\u0430\u0435\u0442\u0441\u044f ADB";		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				mAdbModule.stop();				mSelectDevice = null;				refreshEmptyDevices();				refreshEmptyPackages();			}		});	}	public void startAdb() {		String name = "\u0417\u0430\u043f\u0443\u0441\u043a ADB";		String desc =				"\u0417\u0430\u043f\u0443\u0441\u043a\u0430\u0435\u0442\u0441\u044f ADB";		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				mAdbModule.start();				refreshDevices();				refreshPackages();			}		});	}	public void restartAdb() {		String name =				"\u041f\u0435\u0440\u0435\u0437\u0430\u043f\u0443\u0441\u043a ADB";		String desc =				"\u041f\u0435\u0440\u0435\u0437\u0430\u043f\u0443\u0441\u043a\u0430\u0435\u0442\u0441\u044f ADB";		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				mAdbModule.restart();				mSelectDevice = null;				refreshDevices();				// refreshPackages();			}		});	}	public void connectNetworkDevice(final String pConnect) {		String name =				"\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u044c \u0443\u0441\u0442\u0440\u043e\u0439\u0441\u0442\u0432\u043e";		String desc =				String.format(						"\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0430\u0435\u0442 \u0441\u0435\u0442\u0435\u0432\u043e\u0435 \u0443\u0441\u0442\u0440\u043e\u0439\u0441\u0442\u0432\u043e %s",						pConnect);		mEvents.add(name, desc, new Runnable() {			@Override			public void run() {				mAdbModule.connet(pConnect);			}		});	}	public ListConnect getListConnects() {		return mListConnect;	}	public void setLookAndFeel(final EnumPLAF plaf) {		mSettings.setLookAndFeel(plaf.getName());	}	// ====================================================	// Private methods/interfaces	// ====================================================	// ====================================================	// Devices listener	// ====================================================	public void addDevicesListener(final DevicesListener pDevicesListener) {		addListener(mDevicesListeners, pDevicesListener);	}	public void removeDevicesListener(final DevicesListener pDevicesListener) {		removeListener(mDevicesListeners, pDevicesListener);	}	public void removeAllDevicesListener() {		removeAllListener(mDevicesListeners);	}	// ====================================================	// Packages listener	// ====================================================	public void addPackagesListener(final PackagesListener pListener) {		addListener(mPackagesListeners, pListener);	}	public void removePackagesListener(final PackagesListener pListener) {		removeListener(mPackagesListeners, pListener);	}	public void removeAllPackagesListener() {		removeAllListener(mPackagesListeners);	}	// ====================================================	// private methods/interface listeners	// ====================================================	private <T> void addListener(final List<T> pList, final T pItem) {		if (pList != null && pItem != null) {			pList.add(pItem);		}	}	private <T> void removeListener(final List<T> pList, final T pItem) {		if (pList != null && pItem != null) {			pList.remove(pItem);		}	}	private <T> void removeAllListener(final List<T> pList) {		pList.clear();	}	private <T> void fireListener(final List<T> pList,			final EventListener<T> pEvent) {		if (pList == null) {			return;		}		for (T item : pList) {			if (item != null) {				pEvent.perfom(item);			}		}	}	private void refreshEmptyPackages() {		final List<AdbPackage> packages = new ArrayList<AdbPackage>();		fireListener(mPackagesListeners, new EventListener<PackagesListener>() {			@Override			public void perfom(final PackagesListener pItem) {				pItem.updatePackages(packages);			}		});	}	private void refreshEmptyDevices() {		final List<AdbDevice> devices = new ArrayList<AdbDevice>();		fireListener(mDevicesListeners, new EventListener<DevicesListener>() {			@Override			public void perfom(final DevicesListener pItem) {				pItem.updateListDevices(devices);			}		});	}	private interface EventListener<T> {		void perfom(final T pItem);	}	private void showErrorDialog(final String pText) {		SwingUtilities.invokeLater(new Runnable() {			@Override			public void run() {				JOptionPane.showMessageDialog(						null,						pText,						"Ошибка",						JOptionPane.ERROR_MESSAGE);			}		});	}}