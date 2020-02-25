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

	// 实例化 & 寻找目录
	public ModInjector() throws IOException, InterruptedException {
		System.out.println("寻找MCLDownload目录中...");
		HashMap<String, String> registery = getRegistery("HKEY_CURRENT_USER\\Software\\Netease\\MCLauncher");

		MCL_DOWNLOAD_PATH = registery.get("DownloadPath");

		System.out.println("MCLDownload目录: " + MCL_DOWNLOAD_PATH);
		JOptionPane.showMessageDialog(null, "开始注入，MCLDownload目录: " + MCL_DOWNLOAD_PATH);

		DOT_MINECRAFT_PATH = MCL_DOWNLOAD_PATH + "\\Game\\.minecraft";
		SOURCE_PATH = MCL_DOWNLOAD_PATH + "\\" + INJECTOR_NAME;

		SOURCE_PATH_FILE = new File(SOURCE_PATH);

		this.prepare();
		while (true) this.doInject();
	}

	// 准备 'INJECTOR_NAME' 路径
	public void prepare() throws IOException {
		if (!SOURCE_PATH_FILE.exists() && SOURCE_PATH_FILE.mkdirs()) {
			new File(SOURCE_PATH + "\\mods").mkdir();
			new File(SOURCE_PATH + "\\resourcepacks").mkdir();
			new File(SOURCE_PATH + "\\shaderpacks").mkdir();

			System.out.println(INJECTOR_NAME + "路径不存在！已自动创建: " + SOURCE_PATH_FILE.getAbsolutePath());
			Desktop.getDesktop().open(SOURCE_PATH_FILE);
		}
		System.out.println(INJECTOR_NAME + "工作目录: " + SOURCE_PATH_FILE.getAbsolutePath());
	}

	// 注入方法
	public void doInject() throws IOException, InterruptedException {
		File waitingMod = new File(DOT_MINECRAFT_PATH + "\\mods\\" + INJECTOR_NAME + ".jar");
		if (!waitingMod.exists()) {
			if (waitingMod.createNewFile()) {
				System.out.println("已成功创建检测文件。");
			} else {
				System.out.println("检测文件创建失败。");
				System.exit(0);
			}
		}

		long startTime = System.currentTimeMillis();
		System.out.println("等待游戏启动...");

		while (waitingMod.exists()) {
			Thread.sleep(100);
		}

		System.out.println("游戏已启动，用时: " + (System.currentTimeMillis() - startTime) + "ms！");

		System.out.println("准备注入...");
		Thread.sleep(1500);

		startTime = System.currentTimeMillis();
		this.removeDir(DOT_MINECRAFT_PATH + "\\mods");
		this.copyDir(SOURCE_PATH, DOT_MINECRAFT_PATH);
		String result = "注入已成功，用时: " + (System.currentTimeMillis() - startTime) + "ms！";
		JOptionPane.showMessageDialog(null, result);
		System.out.println(result);
	}

	// Windows下使用 'reg.exe' 操作注册表。
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
				System.out.println("正在复制文件: " + file.getName());
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
				System.out.println("正在删除文件: " + file.getName());
				if (!file.delete()) {
					System.out.println(file.getName() + "删除失败！已跳过。");
				}
			}
		}
	}
}
