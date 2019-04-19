package com.weishao.SpringActivit.handlers;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceHandlerFirst implements JavaDelegate {

	private static final Logger logger = LoggerFactory.getLogger(ServiceHandlerFirst.class);
	
	// 流程变量
	private Expression param1;
	private Expression param2;

	public void execute(DelegateExecution execution) {
		logger.info("========================================");
		logger.info("ServiceHandlerFirst 已经执行！");
			
		String val=(String)execution.getVariable("name");
		logger.info("global param name={}",val);
		
		String value1 = (String) param1.getValue(execution);
		String value2 = (String) param2.getValue(execution);
		logger.info(String.format("p1=%s,p2=%s", value1,value2));
		
		try {
			Thread.sleep(20*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		execution.setVariable("name", "ok");
		logger.info("---------------------------------");
	}
	
}
