package com.hortonworks.atlas.avro.types;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;



public class AvroField extends AvroType {
	private String name;
	private List<Object> type;
	private String doc;
	private String order;
	private List<String> aliases;
	@JsonProperty(value = "default")
	private Object defaults;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Object> getType() {
		return type;
	}
	public void setType(List<Object> type) {
		this.type = type;
	}
	public String getDoc() {
		return doc;
	}
	public void setDoc(String doc) {
		this.doc = doc;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	
	public List<String> getAliases() {
		return aliases;
	}
	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}
	public Object getDefaults() {
		return defaults;
	}
	public void setDefaults(Object defaults) {
		this.defaults = defaults;
	}
}