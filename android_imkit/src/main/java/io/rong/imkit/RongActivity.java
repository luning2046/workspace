package io.rong.imkit;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import io.rong.imkit.common.RCloudConst;
import io.rong.imkit.fragment.BaseFragment;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.fragment.ConversationSettingFragment;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.ActionBar;

public class RongActivity extends FragmentActivity {

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle arg0) {

        super.onCreate(arg0);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);// 使得音量键控制媒体声音
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(ResourceUtils.getLayoutResourceId(this, "rc_activity"));
        mActionBar = (ActionBar) findViewById(ResourceUtils.getIDResourceId(this, "rc_actionbar"));
        Intent intent = getIntent();

        if (intent != null && arg0 == null) {
            Fragment fragment = null;

            if (intent.getExtras() != null && intent.getExtras().containsKey(RCloudConst.EXTRA.CONTENT)) {
                String fragmentName = intent.getExtras().getString(RCloudConst.EXTRA.CONTENT);
                fragment = Fragment.instantiate(this, fragmentName);
            } else if (intent.getData() != null) {
                if (intent.getData().getPathSegments().get(0).equals("conversation")) {
                    String fragmentName = ConversationFragment.class.getCanonicalName();
                    fragment = Fragment.instantiate(this, fragmentName);
                } else if (intent.getData().getLastPathSegment().equals("conversationlist")) {
                    String fragmentName = ConversationListFragment.class.getCanonicalName();
                    fragment = Fragment.instantiate(this, fragmentName);
                } else if (intent.getData().getPathSegments().get(0).equals("conversationsetting")) {
                    String fragmentName = ConversationSettingFragment.class.getCanonicalName();
                    fragment = Fragment.instantiate(this, fragmentName);
                }
            }

            if (fragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(ResourceUtils.getIDResourceId(this, "rc_content"), fragment);
                transaction.commit();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Fragment fragment = getSupportFragmentManager().findFragmentById(ResourceUtils.getIDResourceId(this, "rc_content"));
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(fragment);
            transaction.commit();
        }

        setIntent(intent);

        mActionBar.recycle();

        if (intent.getExtras() != null && intent.getExtras().containsKey(RCloudConst.EXTRA.CONTENT)) {
            String fragmentName = intent.getExtras().getString(RCloudConst.EXTRA.CONTENT);
            fragment = Fragment.instantiate(this, fragmentName);
        } else if (intent.getData() != null) {
            if (intent.getData().getPathSegments().get(0).equals("conversation")) {
                String fragmentName = ConversationFragment.class.getCanonicalName();
                fragment = Fragment.instantiate(this, fragmentName);
            } else if (intent.getData().getLastPathSegment().equals("conversationlist")) {
                String fragmentName = ConversationListFragment.class.getCanonicalName();
                fragment = Fragment.instantiate(this, fragmentName);
            } else if (intent.getData().getPathSegments().get(0).equals("conversationsetting")) {
                String fragmentName = ConversationSettingFragment.class.getCanonicalName();
                fragment = Fragment.instantiate(this, fragmentName);
            }
        }

        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(ResourceUtils.getIDResourceId(this, "rc_content"), fragment);
            transaction.commit();
        }
    }

    public ActionBar getBar() {
        return mActionBar;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(ResourceUtils.getIDResourceId(this, "rc_content"));
        if (fragment != null && fragment instanceof BaseFragment) {
            if (((BaseFragment) fragment).onBackPressed())
                return;
        }
        super.onBackPressed();
    }
}