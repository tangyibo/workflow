package com.weishao.SpringActivit.service;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;

/**
 *  动态工作流：生成BPMN2.0格式的
 * @author Tang Yibo
 */
public class DynamicWorkflowFactory {

	/**
	 * 创建UserTask任务节点-用户
	 * @param id
	 * @param name
	 * @param assignee
	 * @return
	 */
	public UserTask createUserTask(String id, String name, String assignee) {
		UserTask userTask = new UserTask();
		userTask.setName(name);
		userTask.setId(id);
		userTask.setAssignee(assignee);
		return userTask;
	}
	
	/**
	 * 创建UserTask任务节点-组
	 * @param id
	 * @param name
	 * @param candidateGroup
	 * @return
	 */
	public UserTask createGroupTask(String id, String name,String candidateGroup) {
		List<String> candidateGroups = new ArrayList<String>();
		candidateGroups.add(candidateGroup);
		UserTask userTask = new UserTask();
		userTask.setName(name);
		userTask.setId(id);
		userTask.setCandidateGroups(candidateGroups);
		return userTask;
	}
	
	/**
	 * 创建UserTask任务节点-锁定者
	 * @param id
	 * @param name
	 * @param assignee
	 * @return
	 */
	public UserTask createAssigneeTask(String id, String name, String assignee) {
	         UserTask userTask = new UserTask();
	         userTask.setName(name);
	         userTask.setId(id);
	         userTask.setAssignee(assignee);
	       return userTask;
	     }
	
	/**
	 *  创建ServiceTask任务节点
	 * @param id
	 * @param name
	 * @return
	 */
	public ServiceTask createServiceTask(String id, String name) {
		ServiceTask serviceTask = new ServiceTask();
		serviceTask.setName(name);
		serviceTask.setId(id);
		
		return serviceTask;
	}

	/**
	 * 创建连线
	 * @param from
	 * @param to
	 * @param name
	 * @param conditionExpression
	 * @return
	 */
	public SequenceFlow createSequenceFlow(String from, String to,String name, String conditionExpression) {
		SequenceFlow flow = new SequenceFlow();
		
		flow.setSourceRef(from);
		flow.setTargetRef(to);
		
		if (null!=name && !name.isEmpty()) {
			flow.setName(name);
		}
		
		if (null!=conditionExpression && !conditionExpression.isEmpty()) {
			flow.setConditionExpression(conditionExpression);
		}
		
		return flow;
	}
	
	/**
	 * 创建连线
	 * @param from  连线的源端
	 * @param to    连线的目的端
	 * @return
	 */
	public SequenceFlow createSequenceFlow(String from, String to){
		return createSequenceFlow(from,to,"","");
	}

	/**
	 * 创建排他网关
	 * @param id
	 * @param name
	 * @return
	 */
	public ExclusiveGateway createExclusiveGateway(String id, String name) {
		ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
		exclusiveGateway.setId(id);
		exclusiveGateway.setName(name);
		return exclusiveGateway;
	}

	/**
	 * 创建并行网关
	 * @param id
	 * @param name
	 * @return
	 */
	public ParallelGateway createParallelGateway(String id, String name) {
		ParallelGateway gateway = new ParallelGateway();
		gateway.setId(id);
		gateway.setName(name);
		return gateway;
	}
	
	/**
	 * 创建开始节点
	 * @param id
	 * @return
	 */
	public StartEvent createStartEvent(String id) {
		StartEvent startEvent = new StartEvent();
		startEvent.setId(id);
		return startEvent;
	}

	/**
	 * 创建结束节点
	 * @param id
	 * @return
	 */
	public EndEvent createEndEvent(String id) {
		EndEvent endEvent = new EndEvent();
		endEvent.setId(id);
		return endEvent;
	}

}
