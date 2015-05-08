package io.rong.imkit;

import java.io.File;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;

 class CrashHandler implements UncaughtExceptionHandler {

	public final String fTag = "CrashHandler";
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	/** 错误报告文件的扩展名 */
	private final String fCrashReporterExtension = ".log";

	public CrashHandler(Context context) {
		// mContext = context;
	}

	/**
	 * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
	 */
	public void init() {
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// if (!handleException(ex) && mDefaultHandler != null) {
		// // 如果用户没有处理则让系统默认的异常处理器来处理
		// mDefaultHandler.uncaughtException(thread, ex);
		// }
		handleException(ex);
		mDefaultHandler.uncaughtException(thread, ex);
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return true;
		}
		ex.printStackTrace();
		new Thread() {

			public void run() {
				Looper.prepare();
				Looper.loop();
			}

		}.start();

		// 保存错误报告文件
		saveCrashInfoToFile(ex);

		return true;
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return
	 */
	private void saveCrashInfoToFile(Throwable ex) {
		if (isSdCardReady()) {
			try {
				String dirName = getRoot() + "/crash";
				File dir = new File(dirName);
				if (!dir.exists())
					dir.mkdirs();
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				String fileName = dirName + "/crash-" + DateFormat.format("MMM_dd_yyyy_h_mmaa", calendar) + "-" + fCrashReporterExtension;
				PrintWriter printWriter = new PrintWriter(fileName);
				ex.printStackTrace(printWriter);
				Throwable cause = ex.getCause();
				while (cause != null) {
					cause.printStackTrace(printWriter);
					cause = cause.getCause();
				}
				printWriter.flush();
				printWriter.close();
			} catch (Exception e) {
				Log.d(fTag, "an error occured while writing report file...", e);
			}
		}
	}

	// public boolean isCanWrite()
	// {
	// mFile = new File(SDPATH);
	// if( mFile.exists())
	// {
	// return true;
	// }
	// else {
	// try {
	// return mFile.createNewFile();
	// } catch (IOException e) {
	// e.printStackTrace();
	// return false;
	// }
	// }
	// }
	private boolean isSdCardReady() {
		String sd = Environment.getExternalStorageState();
		return sd.equals(Environment.MEDIA_MOUNTED);
	}

	public String getRoot() {
		File sd = Environment.getExternalStorageDirectory();
		return sd.getPath() + "/RongCloud";
	}

}
