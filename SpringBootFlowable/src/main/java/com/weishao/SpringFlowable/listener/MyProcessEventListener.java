package com.weishao.SpringFlowable.listener;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.impl.event.FlowableEngineEventImpl;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 实例的事件监听处理器
 * 
 * @author Tang
 *
 */
public class MyProcessEventListener implements FlowableEventListener  {

	private static final Logger logger = LoggerFactory.getLogger(MyProcessEventListener.class);

	@Override
	public void onEvent(FlowableEvent event) {
		FlowableEngineEventImpl  engineEvent=(FlowableEngineEventImpl)event;
		ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		String processInstanceId = engineEvent.getProcessInstanceId();
		
		if(event.getType() == FlowableEngineEventType.JOB_EXECUTION_SUCCESS) {
			if (null != processInstanceId) {
				logger.info("### A job well done,processInstanceId="+ processInstanceId);
			}
		} else if (event.getType() == FlowableEngineEventType.JOB_EXECUTION_FAILURE) {
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
			
		}else {
			//logger.info("Event received: " + event.getType());
		}
	}

	@Override
	public boolean isFailOnException() {
		logger.info("### MyTaskEventListener->isFailOnException()");
		return false;
	}

	@Override
	public boolean isFireOnTransactionLifecycleEvent() {
		return false;
	}

	@Override
	public String getOnTransaction() {
		return null;
	}

}
