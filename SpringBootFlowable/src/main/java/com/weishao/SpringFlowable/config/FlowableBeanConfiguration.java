package com.weishao.SpringFlowable.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.weishao.SpringFlowable.listener.MyProcessEventListener;

@Configuration
public class FlowableBeanConfiguration {
	
	@Bean
	public ProcessEngine processEngine(SpringProcessEngineConfiguration configuration)
			throws IOException {
		//为引擎注册全局监听器
		List<FlowableEventListener> eventListeners=new ArrayList<FlowableEventListener>();
		eventListeners.add(new MyProcessEventListener() );
		configuration.setEventListeners(eventListeners);//设置监听器
		configuration.setClock(new MyDefaultClock());//设置时钟
		return configuration.buildProcessEngine();
	}

	@Bean
	public RepositoryService repositoryService(ProcessEngine processEngine) {
		return processEngine.getRepositoryService();
	}

	@Bean
	public RuntimeService runtimeService(ProcessEngine processEngine) {
		return processEngine.getRuntimeService();
	}

	@Bean
	public TaskService taskService(ProcessEngine processEngine) {
		return processEngine.getTaskService();
	}

	@Bean
	public HistoryService historyService(ProcessEngine processEngine) {
		return processEngine.getHistoryService();
	}

	@Bean
	public ManagementService managementService(ProcessEngine processEngine) {
		return processEngine.getManagementService();
	}
	
}
