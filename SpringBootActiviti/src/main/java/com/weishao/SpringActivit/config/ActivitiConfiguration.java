package com.weishao.SpringActivit.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

//@Configuration
public class ActivitiConfiguration {
	
	@Bean
	public ProcessEngine processEngine(DataSourceTransactionManager transactionManager, DataSource dataSource)
			throws IOException {
		SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
		configuration.setTransactionManager(transactionManager);
		configuration.setDataSource(dataSource);
		configuration.setDatabaseSchemaUpdate("true");
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
