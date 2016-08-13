package com.hortonworks.atlas.avro.types;

public class AvroMap extends AvroType {
	private String type = "map";
	private String values;

	public String getValues() {
		return values;
	}

	public void setValues(String values) {
		this.values = values;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
