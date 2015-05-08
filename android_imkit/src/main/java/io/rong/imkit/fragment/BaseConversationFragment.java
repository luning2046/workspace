package io.rong.imkit.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import io.rong.imkit.RongActivity;
import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.fragment.ActionBaseFrament;
import io.rong.imkit.fragment.PublishShowImageFragment;

public abstract class BaseConversationFragment extends ActionBaseFrament {

	public final static int SYS_CAMERA = 0;
	public final static int SYS_PIC = 1;

	private static final String ACTION_CAMERA = "android.media.action.IMAGE_CAPTURE";
	public static final int REQUEST_CODE_GET_PIC = 200;

	private File mCameraFile;
	protected static final int HANDLE_ADAPTER_NOTIFY = 101;

	/**
	 * 进入系统拍照页面
	 */
	@SuppressLint("WorldWriteableFiles")
	@SuppressWarnings("deprecation")
	protected void gotoSysCamera() {

		long maxM = Runtime.getRuntime().maxMemory() / 1024 / 1024;
		long nowM = android.os.Debug.getNativeHeapAllocatedSize() / 1024 / 1024;

		if (maxM - nowM < 2) {
			Log.d("BasePhotoActivity-gotoSysCamera", "memory is low!!!");
			return;
		}

		Intent intent = new Intent(ACTION_CAMERA);
		mCameraFile = new File(getActivity().getFilesDir(), "camera.jpg");
		FileOutputStream fileOutputStream;

		try {
			fileOutputStream = getActivity().openFileOutput("camera.jpg", Context.MODE_WORLD_WRITEABLE);
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Uri outputUri = Uri.fromFile(mCameraFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

		startActivityForResult(intent, SYS_CAMERA);

	}

	/**
	 * 进入=======================系统相册页面=============================
	 */
	protected void gotoSysPic() {

		long maxM = Runtime.getRuntime().maxMemory() / 1024 / 1024;
		long nowM = android.os.Debug.getNativeHeapAllocatedSize() / 1024 / 1024;

		if (maxM - nowM < 2) {
			Log.d("BasePhotoActivity-gotoSysPic", "memory is low!!!");
			return;
		}

		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		startActivityForResult(intent, SYS_PIC);//跳转到系统图库

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case SYS_PIC://================从系统相册返回==============================================
			if (data != null && resultCode == Activity.RESULT_OK) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						gotoShowImage(data.getData());
					}
				}, 10);

			}

			break;
		case SYS_CAMERA:// 拍照
			if (mCameraFile != null && mCameraFile.length() > 0 && resultCode == Activity.RESULT_OK) {

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						gotoShowImage(Uri.fromFile(mCameraFile));
					}
				}, 10);
			}
			break;
//============================选中图片后，放入列表中=================================================================
		case REQUEST_CODE_GET_PIC:// 选完图片返回
			resultShowPic(resultCode, data);
			break;
		default:
			break;
		}
	}

	private void resultShowPic(int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_CANCELED) {

		} else if (resultCode == Activity.RESULT_OK) {
			if (data.getData() != null) {
				setPhoto(data.getData());
			}
		}

	}
//跳到显示图片的fragment
	private final void gotoShowImage(Uri uri) {
		if (uri != null) {
			Intent intent = new Intent(getActivity(), RongActivity.class);
            intent.putExtra(RCloudConst.EXTRA.CONTENT, PublishShowImageFragment.class.getCanonicalName());
			intent.setData(uri);
			startActivityForResult(intent, REQUEST_CODE_GET_PIC);
		}
	}
//photoUri是缩略图的uri
	public void setPhoto(final Uri photoUri) {

		if (photoUri != null) {
			publishBitmap(photoUri);
		}
	}

	public void publishBitmap(Uri uri) {}
}