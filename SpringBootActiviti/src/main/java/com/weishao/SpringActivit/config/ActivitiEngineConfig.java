package com.weishao.SpringActivit.config;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.weishao.SpringActivit.listener.MyProcessEventListener;

/**
 * springboot配置activiti全局监听器ActivitiEventListener
 *     
 * @author tang
 * 
 */
@Component
public class ActivitiEngineConfig implements ProcessEngineConfigurator {

	@Autowired
	protected MyProcessEventListener myActivitiEventListener;

	@Override
	public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {

	}

	@Override
	public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		List<ActivitiEventListener> activitiEventListeners = new ArrayList<ActivitiEventListener>();
		activitiEventListeners.add(myActivitiEventListener);             // 配置全局监听器
		processEngineConfiguration.setEventListeners(activitiEventListeners);
	}

	@Override
	public int getPriority() {
		return 0;
	}

}
