package io.rong.imkit.model;

import java.util.ArrayList;
import java.util.Collection;

public class RCloudTypeList<E extends RCloudType> extends ArrayList<E> implements RCloudType {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RCloudTypeList(Collection<? extends E> collection) {
		super(collection);
	}

	RCloudTypeList() {
		super();
	}

}
