package com.hortonworks.atlas.avro.types;

import java.util.List;

public class AvroSchema extends AvroType {
	private String name;
	private String type;
	private String namespace;
	private String doc;
	private List<AvroField> fields;
	
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
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getDoc() {
		return doc;
	}
	public void setDoc(String doc) {
		this.doc = doc;
	}
	public List<AvroField> getFields() {
		return fields;
	}
	public void setFields(List<AvroField> avroFieldList) {
		this.fields = avroFieldList;
	}
}
