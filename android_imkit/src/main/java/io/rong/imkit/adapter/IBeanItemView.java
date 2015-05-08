package io.rong.imkit.adapter;

import android.content.Context;

import io.rong.imkit.common.MessageContext;

public interface IBeanItemView {
	public IViewProvider getItemViewProvider(MessageContext context);
}
