package cn.margele.ModInjector;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.swing.JOptionPane;

public class ModInjector {
	public final static String INJECTOR_NAME = "MModInjector";
	public final static String INJECTOR_VERSION = "v1";

	public final String MCL_DOWNLOAD_PATH;
	public final String DOT_MINECRAFT_PATH;

	public final String SOURCE_PATH;
	public final File SOURCE_PATH_FILE;

	public static void main(String[] args) {
		System.out.println(INJECTOR_NAME + " " + INJECTOR_VERSION);
		try {
			new ModInjector();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ʵ���� & Ѱ��Ŀ¼
	public ModInjector() throws IOException, InterruptedException {
		System.out.println("Ѱ��MCLDownloadĿ¼��...");
		HashMap<String, String> registery = getRegistery("HKEY_CURRENT_USER\\Software\\Netease\\MCLauncher");

		MCL_DOWNLOAD_PATH = registery.get("DownloadPath");

		System.out.println("MCLDownloadĿ¼: " + MCL_DOWNLOAD_PATH);
		JOptionPane.showMessageDialog(null, "��ʼע�룬MCLDownloadĿ¼: " + MCL_DOWNLOAD_PATH);

		DOT_MINECRAFT_PATH = MCL_DOWNLOAD_PATH + "\\Game\\.minecraft";
		SOURCE_PATH = MCL_DOWNLOAD_PATH + "\\" + INJECTOR_NAME;

		SOURCE_PATH_FILE = new File(SOURCE_PATH);

		this.prepare();
		while (true) this.doInject();
	}

	// ׼�� 'INJECTOR_NAME' ·��
	public void prepare() throws IOException {
		if (!SOURCE_PATH_FILE.exists() && SOURCE_PATH_FILE.mkdirs()) {
			new File(SOURCE_PATH + "\\mods").mkdir();
			new File(SOURCE_PATH + "\\resourcepacks").mkdir();
			new File(SOURCE_PATH + "\\shaderpacks").mkdir();

			System.out.println(INJECTOR_NAME + "·�������ڣ����Զ�����: " + SOURCE_PATH_FILE.getAbsolutePath());
			Desktop.getDesktop().open(SOURCE_PATH_FILE);
		}
		System.out.println(INJECTOR_NAME + "����Ŀ¼: " + SOURCE_PATH_FILE.getAbsolutePath());
	}

	// ע�뷽��
	public void doInject() throws IOException, InterruptedException {
		File waitingMod = new File(DOT_MINECRAFT_PATH + "\\mods\\" + INJECTOR_NAME + ".jar");
		if (!waitingMod.exists()) {
			if (waitingMod.createNewFile()) {
				System.out.println("�ѳɹ���������ļ���");
			} else {
				System.out.println("����ļ�����ʧ�ܡ�");
				System.exit(0);
			}
		}

		long startTime = System.currentTimeMillis();
		System.out.println("�ȴ���Ϸ����...");

		while (waitingMod.exists()) {
			Thread.sleep(100);
		}

		System.out.println("��Ϸ����������ʱ: " + (System.currentTimeMillis() - startTime) + "ms��");

		System.out.println("׼��ע��...");
		Thread.sleep(1500);

		startTime = System.currentTimeMillis();
		this.removeDir(DOT_MINECRAFT_PATH + "\\mods");
		this.copyDir(SOURCE_PATH, DOT_MINECRAFT_PATH);
		String result = "ע���ѳɹ�����ʱ: " + (System.currentTimeMillis() - startTime) + "ms��";
		JOptionPane.showMessageDialog(null, result);
		System.out.println(result);
	}

	// Windows��ʹ�� 'reg.exe' ����ע���
	public HashMap<String, String> getRegistery(String path) throws IOException {
		HashMap<String, String> registery = new HashMap<String, String>();
		InputStream is = Runtime.getRuntime().exec("reg query " + path).getInputStream();
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);

		String str;
		while ((str = br.readLine()) != null) {
			str = str.replace(" ", "");
			if (str.contains("REG_SZ")) {
				String[] spilt = str.split("REG_SZ");
				registery.put(spilt[0], spilt[1]);
			}
		}
		return registery;
	}

	// https://blog.csdn.net/weixin_44547599/article/details/89717032
	public void copyDir(String fromDir, String toDir) throws IOException {
		File dirSouce = new File(fromDir);
		if (!dirSouce.isDirectory()) {
			return;
		}
		File destDir = new File(toDir);
		if (!destDir.exists()) {
			destDir.mkdir();
		}

		File[] files = dirSouce.listFiles();
		for (File file : files) {
			String strFrom = fromDir + File.separator + file.getName();
			String strTo = toDir + File.separator + file.getName();
			if (file.isDirectory()) {
				copyDir(strFrom, strTo);
			}
			if (file.isFile()) {
				System.out.println("���ڸ����ļ�: " + file.getName());
				copyFile(strFrom, strTo);
			}
		}
	}

	public void copyFile(String fromFile, String toFile) throws IOException {
		FileInputStream in = new FileInputStream(fromFile);
		FileOutputStream out = new FileOutputStream(toFile);
		byte[] bs = new byte[1 * 1024 * 1024];
		int count = 0;
		while ((count = in.read(bs)) != -1) {
			out.write(bs, 0, count);
		}
		in.close();
		out.flush();
		out.close();
	}

	// https://blog.csdn.net/weixin_42735880/article/details/82926925
	private void removeDir(String dirString) {
		File dir = new File(dirString);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				removeDir(file.getAbsolutePath());
			} else {
				System.out.println("����ɾ���ļ�: " + file.getName());
				if (!file.delete()) {
					System.out.println(file.getName() + "ɾ��ʧ�ܣ���������");
				}
			}
		}
	}
}
