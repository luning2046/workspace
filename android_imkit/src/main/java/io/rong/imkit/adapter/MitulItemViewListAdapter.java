package io.rong.imkit.adapter;

import io.rong.imkit.adapter.BaseViewProvider.OnMessageItemClickListener;
import io.rong.imkit.common.MessageContext;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utils.ResourceUtils;

import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MitulItemViewListAdapter extends BaseAdapter<UIMessage> {

    private LayoutInflater mInflater;
    private HashMap<String, BaseViewProvider> mViewProviderMap = new HashMap<String, BaseViewProvider>();

    public OnMessageItemClickListener mOnMessageItemClickListener;
    private BaseViewProvider.OnGetDataListener mOnGetDataListener;

    public MitulItemViewListAdapter(MessageContext context) {
        super(context);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        UIMessage message = dataSet.get(position);
        BaseViewProvider iViewProvider = null;

        Log.d("MitulView--getView--messageType", "" + message.getMessageDirection());

        if (!mViewProviderMap.containsKey(message.getMessageCategory())) {

            iViewProvider = message.getItemViewProvider((MessageContext) mContext);

            if (iViewProvider == null) {//客户空消息
                return null;
            }

            mViewProviderMap.put(message.getMessageCategory(), iViewProvider);

        } else {
            iViewProvider = mViewProviderMap.get(message.getMessageCategory());
        }

        if(iViewProvider!=null) {
            iViewProvider.setOnMessageItemClickListener(mOnMessageItemClickListener);
            iViewProvider.setOnGetDataListener(mOnGetDataListener);
        }

        if (convertView == null) {

            convertView = iViewProvider.getItemView(convertView, mInflater, message, position, dataSet);

        } else {

            String type = (String) convertView.getTag(ResourceUtils.getStringResourceId(mContext, "tag_id"));

            if (type != null && message.getMessageCategory().equals(type)) {
                convertView = iViewProvider.getItemView(convertView, mInflater, message, position, dataSet);
            } else {
                convertView = iViewProvider.getItemView(null, mInflater, message, position, dataSet);
                convertView.setTag(ResourceUtils.getStringResourceId(mContext, "tag_id"), message.getMessageCategory());
            }

        }

        return convertView;
    }


    public void setOnMessageItemClickListener(OnMessageItemClickListener listener) {
        mOnMessageItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {

        if (position < 0 || position >= dataSet.size()) {
            return 0;
        }

        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }


    public void setOnGetDataListener(BaseViewProvider.OnGetDataListener mOnGetDataListener) {
        this.mOnGetDataListener = mOnGetDataListener;
    }
}
