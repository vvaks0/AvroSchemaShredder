package com.hortonworks.atlas.avro.types;

import java.util.List;

public class AvroEnum extends AvroType{
	private String name;
	private String type = "enum";
	private List<String> symbols;

	public List<String> getSymbols() {
		return symbols;
	}

	public void setSymbols(List<String> symbols) {
		this.symbols = symbols;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
