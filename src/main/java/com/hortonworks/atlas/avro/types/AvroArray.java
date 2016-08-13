package com.hortonworks.atlas.avro.types;

import java.util.List;

public class AvroArray extends AvroType{
	private String type = "array";
	private List<AvroType> items;

	public List<AvroType> getItems() {
		return items;
	}

	public void setItems(List<AvroType> items) {
		this.items = items;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
