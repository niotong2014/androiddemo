package com.mltsagem.i18nbuilder;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * ����strings.xml��array.xml�ַ�����Դ�ļ�����ȡ��Դ�ļ��еļ���ֵ
 * @author yangxin
 */
public class ParseStringResources {
	
	public static void main(String[] args) {
		try {
			getStringsIds();
			
			//getArraysIds();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void getStringsIds() throws Exception {
		/*System.out.println("-------------------��Ƶ----------------------------");
		XMLParseUtils.parseStringsResourcesKey(new FileInputStream("movie_strings.xml"));*/
		
		/*System.out.println("-------------------ͼƬ�����----------------------------");
		XMLParseUtils.parseStringsResourcesKey(new FileInputStream("picture_strings.xml"));
		XMLParseUtils.parseStringsResourcesValue(new FileInputStream("picture_strings.xml"));*/
		
		/*System.out.println("-------------------�ļ������----------------------------");
		XMLParseUtils.parseStringsResourcesKey(new FileInputStream("foledview_strings.xml"));
		XMLParseUtils.parseStringsResourcesValue(new FileInputStream("foledview_strings.xml"));*/
		
		/*System.out.println("-------------------����----------------------------");
		XMLParseUtils.parseStringsResourcesKey(new FileInputStream("settings_strings.xml"));*/
		
		/*System.out.println("-------------------�豸ɨ��----------------------------");
		XMLParseUtils.parseStringsResourcesKey(new FileInputStream("P4DeviceScan.xml"));
		XMLParseUtils.parseStringsResourcesValue(new FileInputStream("P4DeviceScan.xml"));*/
		
		System.out.println("-------------------DVB_T----------------------------");
		ParseStringResources.parseStringsResourcesKey(new FileInputStream("dvbt_strings.xml"));
		ParseStringResources.parseStringsResourcesValue(new FileInputStream("dvbt_strings.xml"));
		
		/*System.out.println("-------------------USBListener----------------------------");
		XMLParseUtils.parseStringsResourcesKey(new FileInputStream("usblistener_strings.xml"));
		XMLParseUtils.parseStringsResourcesValue(new FileInputStream("usblistener_strings.xml"));*/
		
		/*System.out.println("-------------------RKUpdateService----------------------------");
		XMLParseUtils.parseStringsResourcesKey(new FileInputStream("update_service_strings.xml"));
		XMLParseUtils.parseStringsResourcesValue(new FileInputStream("update_service_strings.xml"));*/
		
	}
	
	public static void getArraysIds() throws Exception {
		InputStream is = null;
		//is = new FileInputStream("settings_arrays.xml");
		
		/*is = new FileInputStream("P4DeviceScan_arrays.xml");
		XMLParseUtils.parseArraysResources(is);*/
		
		is = new FileInputStream("dvbt_arrays.xml");
		ParseStringResources.parseArraysResources(is);
	}
	
	/**
	 * ����strings.xml�е�key
	 */
	public static void parseStringsResourcesKey(InputStream is) throws Exception {
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(is);
		Element rootElement = document.getRootElement();
		List<Element> elements = rootElement.elements();
		for (Element element : elements) {
			String resid = element.attribute("name").getValue();
			System.out.println(resid);
		}
		System.out.println("-----------------------------------key�������---------------------------------");
	}
	
	/**
	 * ����strings.xml�е�value
	 */
	public static void parseStringsResourcesValue(InputStream is) throws Exception {
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(is);
		Element rootElement = document.getRootElement();
		List<Element> elements = rootElement.elements();
		for (Element element : elements) {
			String text = element.getTextTrim();
			System.out.println(text);
		}
		System.out.println("-----------------------------------value�������---------------------------------");
	}
	
	/**
	 * ����arrays.xml�ļ�
	 */
	public static void parseArraysResources(InputStream is) throws Exception {
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(is);
		Element rootElement = document.getRootElement();
		List<Element> elements = rootElement.elements();
		for (Element element : elements) {
			Attribute attribute = element.attribute("name");
			if (attribute == null) continue;
			String resid = attribute.getValue();
			System.out.println(resid);
			List<Element> items = element.elements();
			for (Element item : items) {
				String text = item.getTextTrim();
				System.out.println("      " + text);
			}
		}
	}
}
