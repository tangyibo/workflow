package com.weishao.SpringActivit.service;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ProcessValidatorFactory;
import org.activiti.validation.ValidationError;

public class ActivitiToolUtil {

	/**
	 * 将XML转换成BPMN对象
	 * 参考页面：https://blog.csdn.net/u014460967/article/details/80005187
	 * @param xmlJson
	 * @return 工作流BPMN对象
	 * @throws XMLStreamException
	 * @throws UnsupportedEncodingException
	 */
	public static BpmnModel converterXMLToBpmn(byte[] xml)
			throws XMLStreamException, UnsupportedEncodingException {

		ByteArrayInputStream bis = new ByteArrayInputStream(xml);
		BpmnXMLConverter converter = new BpmnXMLConverter();
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(bis);
		BpmnModel bpmnModel = converter	.convertToBpmnModel((XMLStreamReader) reader);
		return bpmnModel;
	}
	
	/**
	 *  bpmnModel 转换为标准的bpmn xml文本
	 * @param bpmnModel 工作流BPMN对象
	 * @return  XML格式的文本字符串
	 */
	public static String converterBpmnToXML(BpmnModel bpmnModel) {
		BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
		byte[] convertToXML = bpmnXMLConverter.convertToXML(bpmnModel);
		String bytes = new String(convertToXML);
		return bytes;
	}
	
	/**
	 * 验证BPMN对象是否合法
	 * @param model  工作流BPMN对象
	 * @return 合法返回true，否则返回false
	 */
	public static boolean checkValidate(BpmnModel model) {
		// 验证model 是否是正确的bpmn xml文件
		ProcessValidatorFactory processValidatorFactory = new ProcessValidatorFactory();
		ProcessValidator defaultProcessValidator = processValidatorFactory.createDefaultProcessValidator();

		// 验证失败信息的封装ValidationError
		List<ValidationError> validate = defaultProcessValidator.validate(model);
		// ValidationError封装的是验证信息，如果size为0说明，bpmnmodel正确;
		// 大于0,说明自定义的bpmnmodel是错误的，不可以使用的。
		return 0 == validate.size();
	}
}
