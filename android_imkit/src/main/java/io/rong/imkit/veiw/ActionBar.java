package io.rong.imkit.veiw;

import io.rong.imkit.utils.ResourceUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhjchen on 14-4-4.
 */
public class ActionBar extends RelativeLayout {

    private List<View> mAddView;

    public ActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
        mAddView = new ArrayList<View>();
    }

    public ActionBar(Context context) {
        super(context);
        initView(context);
        mAddView = new ArrayList<View>();
    }

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        mAddView = new ArrayList<View>();
    }

    private ImageView mBackView;
    private ImageView mLogoView;
    private TextView mTitleView;
    private TextView mNewMessageView;
    private ViewGroup mTitleLayout;
    int mLastAddId;
    List<View> mAddChilds = new ArrayList<View>();

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(ResourceUtils.getLayoutResourceId(context, "rc_action_bar"), this);
        mBackView = (ImageView) findViewById(ResourceUtils.getIDResourceId(getContext(), "rc_back"));
        mLogoView = (ImageView) findViewById(ResourceUtils.getIDResourceId(getContext(), "rc_logo"));
        mTitleView = (TextView) findViewById(ResourceUtils.getIDResourceId(getContext(), "rc_title"));
        mNewMessageView = (TextView) findViewById(ResourceUtils.getIDResourceId(getContext(), "rc_new"));
        mTitleLayout = (ViewGroup) findViewById(ResourceUtils.getIDResourceId(getContext(), "rc_title_layout"));
    }

    public void recycle() {
        if (mAddView != null) {
            for (View view : mAddView) {
                this.removeView(view);
            }
        }
        mLastAddId = 0;
        mAddView.clear();
    }

    @Override
    public void addView(View child) {
        LayoutParams params = (LayoutParams) child.getLayoutParams();
        params.setMargins(10, 10, 10, 10);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        if (mLastAddId == 0)
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        else
            params.addRule(RelativeLayout.LEFT_OF, mLastAddId);


        child.setId(ResourceUtils.getIDResourceId(getContext(), "rc_item" + mAddChilds.size()));
        mLastAddId = child.getId();
        mAddView.add(child);

        super.addView(child);
    }

    public TextView getTitleTextView() {
        return mTitleView;
    }

    public TextView getNewMessageView() {
        return mNewMessageView;
    }

    public void setOnBackClick(OnClickListener listener) {
        mBackView.setOnClickListener(listener);
        mTitleLayout.setOnClickListener(listener);
    }
}
