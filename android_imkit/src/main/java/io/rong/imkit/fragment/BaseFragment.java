package io.rong.imkit.fragment;

import io.rong.imkit.RongActivity;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.ActionBar;

import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

    @SuppressWarnings("unchecked")
    protected <T extends View> T getViewById(View view, int id) {
        return (T) view.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T getViewById(View view, String id) {
        return (T) view.findViewById(ResourceUtils.getIDResourceId(this.getActivity(), id));
    }

    public View getInflateView(String idstr, ViewGroup view, boolean attachToRoot) {
        return LayoutInflater.from(this.getActivity()).inflate(ResourceUtils.getLayoutResourceId(this.getActivity(), idstr), view, attachToRoot);
    }

    protected void handleLeftMessage(Message msg) {

    }

    public ActionBar getActionBar() {
        if (getActivity() instanceof RongActivity) {
            return ((RongActivity) getActivity()).getBar();
        }
        return null;
    }

    public abstract boolean onBackPressed();

}
