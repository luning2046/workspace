package io.rong.imkit.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.os.Environment;
import android.util.Log;

public class FileUtil {

	private static final String TAG = "FileUtil";
	private static final String NOMEDIA = ".nomedia";

	public static InputStream getFileInputStream(String path) {

		FileInputStream fileInputStream = null;

		try {
			fileInputStream = new FileInputStream(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return fileInputStream;
	}

	public static final String createDirectory(File storageDirectory) {

		if (!storageDirectory.exists()) {
			Log.d(TAG, "Trying to create storageDirectory: " + String.valueOf(storageDirectory.mkdirs()));

			Log.d(TAG, "Exists: " + storageDirectory + " " + String.valueOf(storageDirectory.exists()));
			Log.d(TAG, "State: " + Environment.getExternalStorageState());
			Log.d(TAG, "Isdir: " + storageDirectory + " " + String.valueOf(storageDirectory.isDirectory()));
			Log.d(TAG, "Readable: " + storageDirectory + " " + String.valueOf(storageDirectory.canRead()));
			Log.d(TAG, "Writable: " + storageDirectory + " " + String.valueOf(storageDirectory.canWrite()));
			File tmp = storageDirectory.getParentFile();
			Log.d(TAG, "Exists: " + tmp + " " + String.valueOf(tmp.exists()));
			Log.d(TAG, "Isdir: " + tmp + " " + String.valueOf(tmp.isDirectory()));
			Log.d(TAG, "Readable: " + tmp + " " + String.valueOf(tmp.canRead()));
			Log.d(TAG, "Writable: " + tmp + " " + String.valueOf(tmp.canWrite()));
			tmp = tmp.getParentFile();
			Log.d(TAG, "Exists: " + tmp + " " + String.valueOf(tmp.exists()));
			Log.d(TAG, "Isdir: " + tmp + " " + String.valueOf(tmp.isDirectory()));
			Log.d(TAG, "Readable: " + tmp + " " + String.valueOf(tmp.canRead()));
			Log.d(TAG, "Writable: " + tmp + " " + String.valueOf(tmp.canWrite()));
		}

		File nomediaFile = new File(storageDirectory, NOMEDIA);

		if (!nomediaFile.exists()) {
			try {
				Log.d(TAG, "Created file: " + nomediaFile + " " + String.valueOf(nomediaFile.createNewFile()));
			} catch (IOException e) {
				Log.d(TAG, "Unable to create .nomedia file for some reason.", e);
				throw new IllegalStateException("Unable to create nomedia file.");
			}
		}

		if (!(storageDirectory.isDirectory() && nomediaFile.exists())) {
			throw new RuntimeException("Unable to create storage directory and nomedia file.");
		}

		return storageDirectory.getPath();
	}

}
