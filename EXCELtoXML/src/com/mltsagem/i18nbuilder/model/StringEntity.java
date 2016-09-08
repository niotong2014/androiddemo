package com.mltsagem.i18nbuilder.model;

public class StringEntity {
	
	private String resId;
	private String value;
	public StringEntity() {
		super();
	}
	public StringEntity(String resId, String value) {
		super();
		this.resId = resId;
		this.value = value;
	}
	public String getResId() {
		return resId;
	}
	public void setResId(String resId) {
		this.resId = resId;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
