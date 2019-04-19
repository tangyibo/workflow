package com.weishao.SpringActivit.listener;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 实例的事件处理器,这里主要监听错误的执行任务，然后将该工作流挂起
 * 
 * @author Tang yibo
 *
 */
@Component
public class MyProcessEventListener implements ActivitiEventListener {

	private static final Logger logger = LoggerFactory
			.getLogger(MyProcessEventListener.class);

	/**
	 * 事件发生处理
	 * 参考事件列表：https://blog.csdn.net/zhangdaiscott/article/details/80944389
	 */
	public void onEvent(ActivitiEvent event) {
		ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		String processInstanceId = event.getProcessInstanceId();
		
		switch (event.getType()) {

		case JOB_EXECUTION_SUCCESS:{
			if (null != processInstanceId) {
				logger.info("### A job well done,processInstanceId="+ event.getProcessInstanceId());
			}
			break;
		}
		case JOB_EXECUTION_FAILURE: {
			logger.error("### A job has failed,processInstanceId="+ processInstanceId);

			try {
				RepositoryService repositoryService = processEngine.getRepositoryService();
				RuntimeService runtimeService = processEngine.getRuntimeService();
				ProcessInstance pi = runtimeService.createProcessInstanceQuery()
						.processInstanceId(processInstanceId).singleResult();
				ProcessDefinition pdef=repositoryService.createProcessDefinitionQuery()
						.processDefinitionId(pi.getProcessDefinitionId()).singleResult();
				if (!pdef.isSuspended()) {
					repositoryService.suspendProcessDefinitionById(pi.getProcessDefinitionId());
				}
			} catch (Exception e) {
				logger.error("The failed job's processInstanceId="+ processInstanceId);
				logger.error("Error when suspend process definition:", e);
			}

			break;
		}
		default:
			//logger.info("Event received: " + event.getType());
		}
	}

	public boolean isFailOnException() {
		logger.info("MyTaskEventListener->isFailOnException()");
		return false;
	}

}
