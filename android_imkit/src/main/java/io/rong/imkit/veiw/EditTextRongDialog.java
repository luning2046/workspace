package io.rong.imkit.veiw;

import android.content.Context;
import android.widget.EditText;
import android.widget.FrameLayout;

/**
 * Created by zhjchen on 14-7-31.
 */
public class EditTextRongDialog extends RongDialog {

    private EditText mEditText;


    public EditTextRongDialog(Context context, float width) {
        super(context, width);
        initView(context);
    }

    public EditTextRongDialog(Context context) {
        super(context);
        initView(context);
    }


    public void initView(Context context) {
        getContentViewGroup().removeAllViews();
        mEditText = new EditText(context);
        getContentViewGroup().addView(mEditText);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mEditText.getLayoutParams();
        layoutParams.setMargins(20, 0, 20, 0);
    }


    public String getText() {
        if (mEditText != null) {
            return mEditText.getText().toString();
        }
        return null;
    }

    public void setEditText(String name) {
        if (mEditText != null) {
            mEditText.setText(name);
        }
    }
}
