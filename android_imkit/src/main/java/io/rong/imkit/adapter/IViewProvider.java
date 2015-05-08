package io.rong.imkit.adapter;

import java.util.List;

import io.rong.imkit.model.RCloudType;
import android.view.LayoutInflater;
import android.view.View;

public interface IViewProvider {

	public abstract View getItemView(View convertView, LayoutInflater inflater, RCloudType data, int position, List datas);

	public abstract View getConvertView();

}
