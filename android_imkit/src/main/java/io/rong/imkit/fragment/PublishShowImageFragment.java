package io.rong.imkit.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import io.rong.imkit.utils.BitmapUtils;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.LoadingDialog;
import uk.co.senab.photoview.PhotoView;

public class PublishShowImageFragment extends BaseFragment implements OnClickListener {


    private PhotoView mImageView;
    private Button mBtnDel;
    private Button mBtnConfirm;
    private LoadingDialog mDialog;
    private Uri mUri;
    private File mFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(ResourceUtils.getLayoutResourceId(getActivity(), "rc_fragment_show_image"), null);
        mImageView = getViewById(view, android.R.id.icon);
        mBtnDel = getViewById(view, android.R.id.button1);
        mBtnConfirm = getViewById(view, android.R.id.button2);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mBtnDel.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);
        getActionBar().setOnBackClick(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mDialog = new LoadingDialog(getActivity());

        mDialog.setText(ResourceUtils.getStringResource(getActivity(), "show_download_image_loading"));
        mDialog.show();

        if (getActivity().getIntent().getData() == null) {
            Toast.makeText(getActivity(), ResourceUtils.getStringResource(getActivity(), "rc_image_load_fail"), Toast.LENGTH_SHORT).show();
            return;
        }

        mUri = getActivity().getIntent().getData();
        mFile = new File(getActivity().getCacheDir(), String.valueOf(new Random().nextInt()));
        new ImageProcess().execute(mUri);

        super.onViewCreated(view, savedInstanceState);
    }


    class ImageProcess extends AsyncTask<Uri, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Bitmap doInBackground(Uri... params) {
            try {
                Bitmap bitmap =  BitmapUtils.getResizedBitmap(getActivity(),params[0],960,960);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(mFile));
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mImageView.setImageBitmap(result);
            mDialog.dismiss();
        }

    }


    @Override
    public void onClick(View v) {
        if (mBtnConfirm == v) {
            Intent intent = new Intent();
            if (mImageView.getDrawable() != null) {
                intent.setData(Uri.fromFile(mFile));//mFile是压缩后的缩略图。
                getActivity().setResult(Activity.RESULT_OK, intent);
            }
            getActivity().finish();
        } else if (v == mBtnDel) {
            getActivity().finish();
        }
    }

    @Override
    public boolean onBackPressed() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        return false;
    }
}
