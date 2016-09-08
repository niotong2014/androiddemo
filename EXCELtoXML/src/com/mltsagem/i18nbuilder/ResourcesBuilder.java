package com.mltsagem.i18nbuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.mltsagem.i18nbuilder.model.ArrayEntity;
import com.mltsagem.i18nbuilder.model.StringEntity;

/**
 * ��ȡEXCEL����ģ���ļ������ɸ����������Ե���Դ�ļ�
 * @author yangxin
 */
public class ResourcesBuilder {
	
	public static final String DEFAULT_LANGUAGE_FLAG = "values";
	
	/**
	 * �������ҵ�����
	 */
	public static final String[] LANGUAGE = {
		DEFAULT_LANGUAGE_FLAG,
		"en-rGB","de-rDE","fr-rFR","es-rES","it-rIT","pt-rPT","nl-rNL","sv-rSE","no-rNO",
		"fi-rFI","da-rDK","hu-rHU","pl-rPL","cs-rCZ","tr-rTR","ru-rRU","el-rGR","ro-rRO"
	}; 
	
	public static final String[] STRINGS_SHEETS = {
		"Settings",
		"MusicPlayer"
		
	};
	
	public static final String[] ARRAYS_SHEETS = {
		"MusicPlayer" //thtfitpos_arrays
	};
	
	/**
	 * ��Դ�ļ����ɵ���ʱĿ¼
	 */
	public static final String I18N_TEMP_DIR = "/home/thtfit/workspace/thtfitXML/xmltmp/";
	
	/**
	 * �����ļ���ǰ׺
	 */
	public static final String RESOURCES_DIR_PREFIX = "values-";
	
	/**
	 * ��Դ�ļ���
	 */
	public static final String STRING_RESOURCES_FILE_NAME = "strings.xml";
	public static final String ARRAY_RESOURCES_FILE_NAME = "arrays.xml";

	public static void main(String[] args) {
		try {
			String file = "/home/thtfit/workspace/thtfitXML/thtfitposxml.xls";
			// �����ǰ���ɵ��ļ���Ŀ¼
			clearDir(new File(I18N_TEMP_DIR));
			// ���������ļ���
			createI18nDir();
			// ���ɸ���ģ���и������ҵ�strings.xml������Դ�ļ�
			builderStringResources(new FileInputStream(file));
			// ���ɸ���ģ���и������ҵ�arrays.xml������Դ�ļ�
			builderArrayResources(new FileInputStream(file));
			System.out.println("ȫ�����ɳɹ���" + I18N_TEMP_DIR);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���������ļ���
	 */
	public static void createI18nDir() {
		for (int i = 0; i < STRINGS_SHEETS.length; i++) {
			// ����ģ������Ӧ��Ŀ¼
			File parent = new File(I18N_TEMP_DIR,STRINGS_SHEETS[i]);
			parent.mkdirs();
			// ���������������Ե���ԴĿ¼
			for (int j = 0; j < LANGUAGE.length; j++) {
				String language = null;
				if (j == 0) {
					language = LANGUAGE[j];
				} else {
					language = RESOURCES_DIR_PREFIX + LANGUAGE[j];
				}
				File file = new File(parent,language);
				if (!file.exists()) {
					file.mkdirs();
				}
			}
		}
		for (int i = 0; i < ARRAYS_SHEETS.length; i++) {
			File parent = new File(I18N_TEMP_DIR,ARRAYS_SHEETS[i]);
			parent.mkdirs();
			for (int j = 0; j < LANGUAGE.length; j++) {
				String language = null;
				if (j == 0) {
					language = LANGUAGE[j];
				} else {
					language = RESOURCES_DIR_PREFIX + LANGUAGE[j];
				}
				File file = new File(parent,language);
				if (!file.exists()) {
					file.mkdirs();
				}
			}
		}
	}
	
	/**
	 * ����strings.xml��Դ�ļ�
	 */
	public static void builderStringResources(InputStream is) throws Exception {
		HSSFWorkbook book = new HSSFWorkbook(is);
		for (int i = 0; i < STRINGS_SHEETS.length; i++) {
			HSSFSheet sheet = book.getSheetAt(book.getSheetIndex(STRINGS_SHEETS[i]));
			System.out.println("build strings for " + sheet.getSheetName());
			int rowNum = sheet.getLastRowNum();
			System.out.println("builderStringResource-rowNum: " + rowNum);
			for (int j = 0; j < LANGUAGE.length; j++) {
				String language = LANGUAGE[j];
				ArrayList<StringEntity> stringEntitys = new ArrayList<StringEntity>();
				File dir = null;
				if (DEFAULT_LANGUAGE_FLAG.equals(language)) {	// ����Ĭ������
					dir = new File(I18N_TEMP_DIR + STRINGS_SHEETS[i] + File.separator + language);
				} else {
					dir = new File(I18N_TEMP_DIR + STRINGS_SHEETS[i] + File.separator + RESOURCES_DIR_PREFIX + language);
				}
				File file = new File(dir,STRING_RESOURCES_FILE_NAME);
				for (int k = 1; k <= rowNum; k++) {
					HSSFRow row = sheet.getRow(k);
					String resId = row.getCell(0).getStringCellValue().trim();			// resId
					HSSFCell cell = row.getCell(j+1);
					String value = null;
					if (cell != null) {
						//value = cell.getStringCellValue(); 			// ĳһ�����ҵ�����
						value = getValue(cell);
						if (value == null || "".equals(value.trim())) {
							continue;
						}
						StringEntity entity = new StringEntity(resId, value.trim());
						stringEntitys.add(entity);
					}
				}
				// ������Դ�ļ�
				builderStringResources(stringEntitys,file);
			}
		}
		is.close();
		System.out.println("------------------strings.xml��Դ�ļ����ɳɹ���------------------");
	}
	
	private static void builderStringResources(List<StringEntity> stringEntitys,File file) throws Exception {
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		XMLWriter writer = new XMLWriter(new FileOutputStream(file),format);
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("resources");
		for (StringEntity stringEntity : stringEntitys) {
			Element stringElement = root.addElement("string");
			stringElement.addAttribute("name", stringEntity.getResId());
			stringElement.setText(stringEntity.getValue());
		}
		writer.write(document);
		writer.close();
	}
	
	/**
	 * ����arrays.xml��Դ�ļ�
	 */
	public static void builderArrayResources(InputStream is) throws Exception {
		HSSFWorkbook book = new HSSFWorkbook(is);
		for (int i = 0; i < ARRAYS_SHEETS.length; i++) {	// ����ģ��
			HSSFSheet sheet = book.getSheetAt(book.getSheetIndex(ARRAYS_SHEETS[i]+"_arrays"));
			System.out.println("build arrays for " + sheet.getSheetName());
			int rowNum = sheet.getNumMergedRegions();	// sheet.getLastRowNum();
			System.out.println("builderArrayResources-rowNum:"+rowNum);
			for (int j = 0; j < LANGUAGE.length; j++) {		// ����
				String language = LANGUAGE[j];
				ArrayList<ArrayEntity> arrayEntities = new ArrayList<ArrayEntity>();
				File dir = null;
				if (DEFAULT_LANGUAGE_FLAG.equals(language)) {	// ����Ĭ������
					dir = new File(I18N_TEMP_DIR + ARRAYS_SHEETS[i] + File.separator + language);
				} else {
					dir = new File(I18N_TEMP_DIR + ARRAYS_SHEETS[i] + File.separator + RESOURCES_DIR_PREFIX + language);
				}
				File file = new File(dir,ARRAY_RESOURCES_FILE_NAME);
				for (int k = 1; k <= rowNum; k++) {
					CellRangeAddress range = sheet.getMergedRegion(k-1);
					int mergedRows = range.getNumberOfCells();
					int lastRow = range.getLastRow();
					int rowIndex = (lastRow - mergedRows) + 1;
					String resId = sheet.getRow(rowIndex).getCell(0).getStringCellValue().trim();			// resId
					ArrayEntity entity = new ArrayEntity(resId);
					ArrayList<String> items = new ArrayList<String>();
					for (int z = rowIndex; z <= lastRow; z++) {
						HSSFCell cell = sheet.getRow(z).getCell(j+1);
						String value = getValue(cell);
						
						if (value == null || "".equals(value.trim())) {	// ���������û�ж�Ӧ�ķ���,Ĭ��ʹ��Ӣ��
							HSSFCell defaultCell = sheet.getRow(z).getCell(1);
							value = getValue(defaultCell);
						}
						
						if ("temp".equalsIgnoreCase(value.trim())) {
							continue;
						}
						
						items.add(value);
					}
					entity.setItems(items);
					arrayEntities.add(entity);
				}
				// ������Դ�ļ�
				builderArrayResources(arrayEntities,file);
			}
		}
		System.out.println("------------------arrays.xml��Դ�ļ����ɳɹ���------------------");
	}
	
	/**
	 * ��ȡ��Ԫ���ֵ
	 * @param cell ��Ԫ��
	 * @return ��Ԫ���Ӧ��ֵ
	 */
	private static String getValue(HSSFCell cell) {
		String value = "";
		if (cell != null) {
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_NUMERIC:
				//System.out.println("Cell_Type_Numeric");
				value = String.valueOf((int)cell.getNumericCellValue()).trim();
				break;
			case Cell.CELL_TYPE_STRING:
				//System.out.println("Cell_Type_String");
				value = cell.getStringCellValue().trim(); 
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				//System.out.println("Cell_Type_Boolean");
				value = String.valueOf(cell.getBooleanCellValue()).trim();
				break;
			default:
				//System.out.println("Cell_Type_Default");
				value = cell.getStringCellValue().trim(); 
				break;
			}
		}
		return value;
	}
	
	private static void builderArrayResources(ArrayList<ArrayEntity> arrayEntities, File file) throws Exception {
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		XMLWriter writer = new XMLWriter(new FileOutputStream(file),format);
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("resources");
		for (ArrayEntity arrayEntity : arrayEntities) {
			Element arrayElement = root.addElement("string-array");
			arrayElement.addAttribute("name", arrayEntity.getName());
			List<String> items = arrayEntity.getItems();
			for (String item : items) {
				Element itemElement = arrayElement.addElement("item");
				itemElement.setText(item);
			}
		}
		writer.write(document);
		writer.close();
	}

	/**
	 * �����ǰ���ɵ��ļ���Ŀ¼
	 */
	public static void clearDir(File dir) {
		if (!dir.exists()) return;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				clearDir(file);
			} else {
				file.delete();
			}
		}
		dir.delete();
	}
}
