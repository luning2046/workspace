package io.rong.imkit.parse;

import io.rong.imkit.model.RCloudType;

public interface IParser<T extends RCloudType> {
	
	public T jsonParse(String reader);

}
