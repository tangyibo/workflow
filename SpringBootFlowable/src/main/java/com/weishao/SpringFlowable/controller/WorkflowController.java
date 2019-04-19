package com.weishao.SpringFlowable.controller;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngine;
//import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.*;

import com.weishao.SpringFlowable.service.ActivitiToolUtil;
import com.weishao.SpringFlowable.service.DynamicWorkflowFactory;
import com.weishao.SpringFlowable.service.MD5ToolUtil;

/**
 * 工作流相关操作的控制器
 * 接口文档地址：http://127.0.0.1:8082/swagger-ui.html
 * @author Tang Yibo
 *
 */
@Api(description = "流程模型Model操作相关", tags = {"workflow"})
@RestController
@RequestMapping(value = "/workflow")
public class WorkflowController extends BaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);
	
	@Autowired
	private ProcessEngine processEngine;
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	private FlowElement findStartNode(BpmnModel model){
		Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
		for(FlowElement e : flowElements) {
			if (e instanceof org.flowable.bpmn.model.StartEvent) {
				return e;
			}
		}
		
		return null;
	}
	
	private List<FlowElement> findNextNode(BpmnModel model,FlowElement now) {
		List<FlowElement> results=new ArrayList<FlowElement>();
		Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
		for (FlowElement e : flowElements) {
			if (e.getId().equals(now.getId())) {
				org.flowable.bpmn.model.FlowNode flowNode = (org.flowable.bpmn.model.FlowNode) model.getFlowElement(now.getId());
				List<SequenceFlow> outFlows = flowNode.getOutgoingFlows();
		        for (SequenceFlow sequenceFlow : outFlows){
		        	FlowElement targetFlow = sequenceFlow.getTargetFlowElement();
		        	results.add(targetFlow);
		        }
			}
		}

		return results;
	}
	
	/**
	 * 根据工作流的部署ID查找出所有的节点信息列表
	 * @param id
	 * @return
	 */
	public List<Map<String, String>> findAllNodes(String processDefinitionId) {
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();

		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		try {
			BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
			if (null != model) {
				FlowElement e = findStartNode(model);
				if (null != e) {
					Map<String, String> one = new HashMap<String, String>();
					one.put("id", e.getId());
					one.put("name", e.getName());

					logger.info(">>>>>>>>>> name[id] is {}[{}]", e.getName(),e.getId());
					results.add(one);

					List<FlowElement> listNodes = findNextNode(model, e);
					do {
						if (null != listNodes && listNodes.size() > 0) {
							for (FlowElement elem : listNodes) {
								Map<String, String> onenew = new HashMap<String, String>();
								onenew.put("id", elem.getId());
								onenew.put("name", elem.getName());

								logger.info(">>>>>>>>>> name[id] is {}[{}]",elem.getName(), elem.getId());
								results.add(onenew);
							}

							listNodes = findNextNode(model, listNodes.get(0));
						}

					} while (null != listNodes && listNodes.size()>0);

				}
			}
		} catch (Exception e) {
			logger.error("Error in findNodes:", e);

		}
		return results;
	}
				
	
	/**
	 * 生成一个DEMO工作流模型
	 * @return
	 */
	@RequestMapping(value ="/demo",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="生成工作流模型", notes="生成一个DEMO工作流模型")
    public Map<String, Object> demoModule() {
		logger.info("demoModule 已经执行！");
		
		try{
			DynamicWorkflowFactory dw=new DynamicWorkflowFactory();
			
			// 实例化BpmnModel对象
			BpmnModel model = new BpmnModel();
			
			// 创建工作流中的四个节点
			StartEvent startNode=dw.createStartEvent("start");
			
			ServiceTask task1Node=dw.createServiceTask("task1", "First task");
			
			List<FieldExtension> fieldList=new ArrayList<FieldExtension>();
			FieldExtension fe1=new FieldExtension();
			fe1.setFieldName("param1");
			fe1.setStringValue("task1_param1");
			fieldList.add(fe1);
			FieldExtension fe2=new FieldExtension();
			fe2.setFieldName("param2");
			fe2.setStringValue("task1_param2");
			fieldList.add(fe2);
			
			//设置任务节点的两个扩展参数
			task1Node.setFieldExtensions(fieldList);
			//task1Node.setImplementationType("expression");
			//task1Node.setImplementation("${serviceTaskNController.implementPlug(execution)}");
			//设置任务节点执行的类
			task1Node.setImplementationType("class");
			task1Node.setImplementation("cn.com.weishao.handler.ServiceHandlerFirst");
			
			ServiceTask task2Node=dw.createServiceTask("task2", "Second task");
			
			task2Node.setFieldExtensions(fieldList);
			//task2Node.setImplementationType("expression");
			//task2Node.setImplementation("${serviceTaskNController.implementPlug(execution)}");
			task2Node.setImplementationType("class");
			task2Node.setImplementation("cn.com.weishao.handler.ServiceHandlerSecond");
			
			EndEvent endNode=dw.createEndEvent("end");
			
			// 创建连线信息
			SequenceFlow f1=dw.createSequenceFlow("start", "task1");
			SequenceFlow f2=dw.createSequenceFlow("task1", "task2");
			SequenceFlow f3=dw.createSequenceFlow("task2", "end");

			//创建Process对象
			Process process = new Process();
			process.setId("myProcess");
			process.setName("My process");
			
			//设置任务节点的两个扩展参数
			task1Node.setFieldExtensions(fieldList);
			
			//将节点添加到Process中
			process.addFlowElement(startNode);
			process.addFlowElement(task1Node);
			process.addFlowElement(task2Node);
			process.addFlowElement(endNode);

			//将连线添加到Process中
			process.addFlowElement(f1);
			process.addFlowElement(f2);
			process.addFlowElement(f3);
			
			model.addProcess(process);
			
			//自动布局
			new BpmnAutoLayout(model).execute();

			if(ActivitiToolUtil.checkValidate(model)){
				String xml=ActivitiToolUtil.converterBpmnToXML(model);
				Map<String,Object> data = new HashMap<String,Object>();
				data.put("md5", MD5ToolUtil.GetMD5Code(xml));
				data.put("xml", xml);
				return success(data);
			}else{
				throw new Exception("invlid workflow module");
			}
		}catch(Exception e){
			logger.error("Error in demoModule:",e);
			return failed(500,e.getMessage());
		}
    }
	
	/**
	 * 发布工作流接口
	 * @param body
	 * @return
	 */
	@RequestMapping(value ="/create",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="发布工作流", notes="使用XML文本来发布一个工作流模型")
    public Map<String, Object> createDeployment(@RequestBody String body) {
		logger.info("createDeployment 已经执行！");
		
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		try{
			RepositoryService repositoryService = processEngine.getRepositoryService();
			
			// 将xml文本转换成BPMN2.0的模型对象
			BpmnModel model = ActivitiToolUtil.converterXMLToBpmn(body.getBytes("utf-8"));
			if(!ActivitiToolUtil.checkValidate(model)){
				throw new Exception("invlid workflow module");
			}

			String md5Name = MD5ToolUtil.GetMD5Code(body);
			DeploymentBuilder builder = repositoryService.createDeployment();
			builder.addBpmnModel(md5Name+".bpmn", model);
			builder.name(md5Name);
			builder.enableDuplicateFiltering();// 过滤重复部署

			//实际部署工作流
			Deployment deployment = builder.deploy();
			logger.info("create deployment information ：" + deployment.toString());

			Map<String,Object> data = new HashMap<String,Object>();
			data.put("deploymentId", deployment.getId());
			data.put("deploymentName", deployment.getName());
			return success(data);
			
		}catch(Exception e){
			logger.error("Error in createDeployment:",e);
			return failed(500,e.getMessage());
		}

    }
	
	/**
	 * 立即执行工作流
	 * @param id
	 * @return
	 */
	@RequestMapping(value ="/start",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="立即执行工作流", notes="根据发布ID来立即执行一个工作流模型")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name = "id", value = "工作流模型ID", required = true, dataType = "String")
    })
    public Map<String, Object> startProcess(@RequestParam(value="id") String id) {
		logger.info("startProcess 已经执行！id="+id);
		
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		RuntimeService runtimeService=processEngine.getRuntimeService();
		
		try{
			//根据工作流的部署ID转换为工作流定义的ID
			ProcessDefinition processDef=repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult();
			runtimeService.startProcessInstanceById(processDef.getId());
		}catch(Exception e){
			logger.error("Error in startProcess:",e);
			return failed(-1,e.getMessage());
		}
		
		return success(null);
    }
	
	
	
	/**
	 * 删除工作流接口
	 * @param id
	 * @return
	 */
	@RequestMapping(value ="/delete",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="删除工作流", notes="根据发布ID来删除一个工作流模型")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name = "id", value = "工作流模型ID", required = true, dataType = "String")
    })
    public Map<String, Object> deleteDeployment(@RequestParam(value="id") String id) {
		logger.info("deleteDeployment 已经执行！id="+id);
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		
		try{
			//这里为级联删除工作流的部署（定义）
			repositoryService.deleteDeployment(id,true);
		}catch(Exception e){
			logger.error("Error in deleteDeployment:",e);
			return failed(-1,e.getMessage());
		}
		
		return success(null);
    }
	
		
	/**
	 * 更新工作流接口
	 * @param id 工作流的部署ID
	 * @return
	 *
	@RequestMapping(value ="/update",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="更新工作流", notes="根据发布ID来更新一个工作流模型")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name = "id", value = "工作流模型ID", required = true, dataType = "String")
    })*/
    public Map<String, Object> updateDeployment(@RequestParam(value="id") final String id, @RequestBody String str) {
		logger.info("updateDeployment 已经执行！id="+id);
		
		final byte[] btyesXml = str.getBytes();
		try {
			//直接用jdbcTemplate直接操作数据库来更新
			String sql = "update ACT_GE_BYTEARRAY set BYTES_=? where NAME_ LIKE '%.bpmn' and DEPLOYMENT_ID_= ? ";
			int count = jdbcTemplate.update(sql, new PreparedStatementSetter() {
				public void setValues(PreparedStatement pstmt) throws SQLException {
					pstmt.setBytes(1, btyesXml);
					pstmt.setString(2, id);
				}
			});
			logger.info("updateDeployment affect count =" + count);
		} catch (Exception e) {
			logger.error("Error in updateDeployment:", e);
			return failed(-1, e.getMessage());
		}
		
		return success(null);
    }
	
	@RequestMapping(value ="/activate",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="激活工作流", notes="根据发布ID来激活一个工作流模型,当一个工作流被挂起或有节点异常时，需要调用该接口激活该工作流")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name = "id", value = "工作流模型ID", required = true, dataType = "String")
    })
    public Map<String, Object> activateProcess(@RequestParam(value="id") String id) {
		logger.info("activateProcess 已经执行！id="+id);
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		RuntimeService runtimeService=processEngine.getRuntimeService();
		
		try{
			//根据工作流的部署ID转换为工作流定义的ID
			ProcessDefinition processDef=repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult();
			
			// 先删除所有的该工作流定义的实例（因为存在的实例为异常的示例）
			List<ProcessInstance> pis=runtimeService.createProcessInstanceQuery().processDefinitionId(processDef.getId()).list();
			for(ProcessInstance pi : pis){
				runtimeService.deleteProcessInstance(pi.getId(), "exception process delete operation");
			}
			
			//这里激活工作流定义，将会创建新的工作流实例了
			if (processDef.isSuspended()) {
				repositoryService.activateProcessDefinitionById(processDef.getId());
			}
		}catch(Exception e){
			logger.error("Error in activateProcess:",e);
			return failed(-1,e.getMessage());
		}
		
		return success(null);
    }
	
	@RequestMapping(value ="/suspend",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="暂停工作流", notes="根据发布ID来挂起一个工作流模型")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name = "id", value = "工作流模型ID", required = true, dataType = "String")
    })
    public Map<String, Object> suspendProcess(@RequestParam(value="id") String id) {
		logger.info("suspendProcess 已经执行！id="+id);
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		
		try{
			//如果工作流已经被挂起了，则忽略
			ProcessDefinition processDef=repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult();
			if (!processDef.isSuspended()) {
				repositoryService.suspendProcessDefinitionById(processDef.getId());
			}
		}catch(Exception e){
			logger.error("Error in suspendProcess:",e);
			return failed(-1,e.getMessage());
		}
		
		return success(null);
    }
	
	
	@RequestMapping(value ="/find",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="查找工作流定义", notes="根据发布ID来查找工作流定义")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name = "id", value = "工作流模型ID", required = true, dataType = "String")
    })
    public Map<String, Object> findProcess(@RequestParam(value="id") String id) {
		logger.info("findProcess 已经执行！id="+id);
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		
		try{
			//根据工作流的部署ID转换为工作流定义的ID
			ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult();
			
			Map<String,Object> item = new HashMap<String,Object>();
			item.put("deploymentId", id);
			item.put("definitionId",def.getId());
			item.put("definitionKey",def.getKey());
			item.put("definitionName", def.getName());

			return success(item);
		}catch(Exception e){
			logger.error("Error in findProcess:",e);
			return failed(-1,e.getMessage());
		}
			
    }
	
	/**
	 * 验证工作流模型是否有效
	 * @param body
	 * @return
	 */
	@RequestMapping(value ="/check",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="验证模型有效性", notes="验证工作流模型是否有效")
    public Map<String, Object> checkDeployment(@RequestBody String body) {
		logger.info("checkDeployment 已经执行！");
		
		try{		
			// 将提交的xml文本转换成BPMN2.0的模型对象
			BpmnModel model = ActivitiToolUtil.converterXMLToBpmn(body.getBytes("utf-8"));
			if(!ActivitiToolUtil.checkValidate(model)){
				throw new Exception("invlid workflow module");
			}
			
			return success(null);
		}catch(Exception e){
			logger.error("Error in checkDeployment:",e);
			return failed(-1,e.getMessage());
		}
		
    }
	
	/**
	 * 获取工作流部署明细列表
	 * @return
	 */
	@RequestMapping(value ="/listAll",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="工作流部署列表", notes="获取工作流部署明细列表")
    public Map<String, Object> listDeployment() {
		logger.info("listDeployment 已经执行！");
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		
		//实际上的获取工作流部署列表
		List<Deployment> repList=repositoryService.createDeploymentQuery().list();
		
		List<Map<String,Object>> listData=new ArrayList<Map<String,Object>>();
		for(int i=0;i<repList.size();++i){
			Deployment dep=repList.get(i);
			
			//根据工作流的部署ID转换为工作流定义的ID
			ProcessDefinition processDef=repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
			
			Map<String,Object> item = new HashMap<String,Object>();
			item.put("deploymentId", dep.getId());
			item.put("deploymentName", dep.getName());
			item.put("definitionId",processDef.getId());
			
			listData.add(item);
		}
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("list", listData);
		return success(data);
    }
	
	/**
	 * 查询部署的工作流模型的状态
	 * @return
	 */
	@RequestMapping(value ="/status",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="查询工作流状态", notes="根据发布ID来查询部署的工作流模型的状态")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name = "id", value = "发布工作流模型ID", required = true, dataType = "String")
    })
    public Map<String, Object> statusProcess(@RequestParam(value="id") String id) {
		//logger.info("statusProcess 已经执行！id="+id);
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		RuntimeService runtimeService=processEngine.getRuntimeService();
		HistoryService historyService=processEngine.getHistoryService();
		ManagementService managementService=processEngine.getManagementService();
		
		try{
			ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(id).singleResult();
			List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
			
			//首先判断流程是否正在进行
			List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().deploymentId(id).list();
			if (processInstances.size() > 0) {
				logger.info("statusProcess =================id=" + id);
				for (ProcessInstance pi : processInstances) {
					logger.info("activity node=" + pi.getActivityId());
				}
				logger.info("============= =================id=" + id);
			}
			
			List<Execution> executionList = runtimeService.createExecutionQuery()  //创建正在执行的流程查询对象
                    .processDefinitionId(def.getId())                              //根据流程定义的ID查询
                    .orderByProcessInstanceId()                                    //根据流程实例id排序
                    .desc()                                                        //倒序
                    .list();                                                       //查询出集合
			if (executionList.size() > 0) {
				for (Execution exe : executionList) {
					if (null != exe.getActivityId()) {
						Map<String, Object> item = new HashMap<String, Object>();
						item.put("processId", exe.getProcessInstanceId());
						item.put("taskNodeId", exe.getActivityId());
						
						//这里获取工作流节点的实际执行状态
						List<org.flowable.job.api.Job> jobs=managementService.createDeadLetterJobQuery()
								.processDefinitionId(def.getId())
								.processInstanceId(exe.getProcessInstanceId())
								.list();
						if(jobs.size()>0){
							org.flowable.job.api.Job job=jobs.get(0);
							if(job.isExclusive()){
								item.put("status", -1);
								item.put("errmsg", job.getExceptionMessage());
							}else{
								item.put("status", -1);
								item.put("errmsg", "unkown");
							}
						}else{
							item.put("status", 0);
							item.put("errmsg", "running");
						}

						listData.add(item);
					}
				}
				
				if (listData.size() > 0) {
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("list", listData);
					return success(data);
				}
			}

			//到这里，说明流程已经执行完，需要查询最近的历史流程
			List<HistoricProcessInstance> hisPiList=historyService.createHistoricProcessInstanceQuery() 
					.processDefinitionId(def.getId()).finished().orderByProcessInstanceStartTime().desc().list();
			if(hisPiList.size()>0){
				HistoricProcessInstance hisProcessInstance=hisPiList.get(0);
				
				Map<String, Object> item = new HashMap<String, Object>();
				item.put("processId", hisProcessInstance.getId());
				item.put("taskNodeId", hisProcessInstance.getEndActivityId());

				listData.add(item);
				
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("list", listData);
				return success(data);
			}
			
			return success(null);
		}catch(Exception e){
			logger.error("Error in statusProcess:",e);
			return failed(-1,e.getMessage());
		}
				
    }
	
	/**
	 * 根据key查询部署的工作流模型的状态
	 * @return
	 */
	@RequestMapping(value ="/nodes",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="查询工作流图的节点列表", notes="根据部署的ID来查询查询工作流图的节点列表")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name = "id", value = "部署工作流模型ID", required = true, dataType = "String")
    })
    public Map<String, Object> nodesProcess(@RequestParam(value="id") String id) {
		logger.info("traceProcess 已经执行！key="+id);
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		
		try{
			List<Map<String,Object>> activityInfos=new ArrayList<Map<String,Object>>();
			ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(id).latestVersion().singleResult();
			logger.info("traceProcess 已经执行！ProcessDefinitionId="+def.getId());
			
			BpmnModel model = repositoryService.getBpmnModel(def.getId());
			if(model != null) {
			    Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
			    for(FlowElement e : flowElements) {
					Map<String, Object> one = new HashMap<String, Object>();
					one.put("taskId", e.getId());
					one.put("taskName", e.getName());
					
					activityInfos.add(one);
			    }
			}
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("list", activityInfos);
			return success(data);
		}catch(Exception e){
			logger.error("Error in statusProcess:",e);
			return failed(-1,e.getMessage());
		}
	}
	
	
	/**
	 * 查询部署的工作流模型的状态
	 * @return
	 */
	@RequestMapping(value ="/trace",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="查询工作流各个节点状态", notes="根据发布ID来查询部署的工作流模型各个节点的状态")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name = "id", value = "发布工作流模型ID", required = true, dataType = "String")
    })
    public Map<String, Object> traceProcess(@RequestParam(value="id") String id) {
		logger.info("traceProcess 已经执行！id="+id);
		//ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		RuntimeService runtimeService=processEngine.getRuntimeService();
		HistoryService historyService=processEngine.getHistoryService();
		ManagementService managementService=processEngine.getManagementService();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		
		//工作流是否异常标志
		boolean isExceptStatus=false;
		String startTime=null;//工作流的开始时间
		String endTime=null;//工作流的结束时间
		
		try{
			ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(id).latestVersion().singleResult();
			List<Map<String,String>> nodes=findAllNodes(def.getId());
			
			if(null==nodes || nodes.isEmpty()){//当获取不到节点列表时，直接返回
				logger.warn("find process nodes count is 0");
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("list", null);
				return failed(-1,"find process nodes count is 0");
			}
			
			List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
			List<Execution> executionList = runtimeService.createExecutionQuery()  //创建正在执行的流程查询对象
                    .processDefinitionId(def.getId())                              //根据流程定义的ID查询
                    .orderByProcessInstanceId()                                    //根据流程实例id排序
                    .desc()                                                        //倒序
                    .list();                                                       //查询出集合
			if (executionList.size() > 0) {
				//该工作流当前有实例正在执行
				ProcessInstance pi=runtimeService.createProcessInstanceQuery().processDefinitionId(def.getId()).singleResult();
				startTime=df.format(pi.getStartTime());
				
				//找到正在运行的节点或异常的节点列表存放到Map中
				Map<String,Map<String,Object>> runningNodeList=new HashMap<String,Map<String,Object>>();
				logger.info("traceProcess execution count="+executionList.size());
				for (Execution exe : executionList) {
					String processInstanceId=exe.getProcessInstanceId();
					String activityNodeId=exe.getActivityId();
					if (null != activityNodeId) {
						Map<String, Object> theOne = new HashMap<String, Object>();
						theOne.put("processId", processInstanceId);
						theOne.put("executionId", exe.getId());
						theOne.put("activityNodeId", activityNodeId);
						String exceptMsg=null;
						List<org.flowable.job.api.Job> jobs=managementService.createDeadLetterJobQuery()
								.processDefinitionId(def.getId())
								.processInstanceId(processInstanceId)
								.list();
						if(jobs.size()>0){//检查是否有节点任务异常
							org.flowable.job.api.Job job=jobs.get(0);
							if(null!=job.getExceptionMessage()){
								java.util.Date now=new java.util.Date();
								exceptMsg=job.getExceptionMessage();
								isExceptStatus=true;
								endTime=df.format(now);
							}
						}
						
						theOne.put("errorMsg", exceptMsg);
						runningNodeList.put(activityNodeId, theOne);
					}
				}
				
				//为正在执行的工作流上的每个节点添加状态标记
				boolean findCurrentNode = false;
				for (Map<String, String> one : nodes) {
					Map<String, Object> item = new HashMap<String, Object>();
							
					String nodeTaskId = one.get("id");
					String nodeTaskName=one.get("name");
							
					item.put("nodeId", nodeTaskId);
					item.put("nodeName", nodeTaskName);
							
					//节点状态：-1为异常；0为执行成功；1为正在执行；2为尚未执行（未知）
					if (runningNodeList.containsKey(nodeTaskId)) {
						item.put("executionId",  runningNodeList.get(nodeTaskId).get("executionId"));
						item.put("processInstanceId", runningNodeList.get(nodeTaskId).get("processId"));
						
						if(null!=runningNodeList.get(nodeTaskId).get("errorMsg")){
							item.put("status", -1);
							item.put("errmsg", runningNodeList.get(nodeTaskId).get("errorMsg"));
						}else{
							item.put("status", 1);
							item.put("errmsg", "running");
						}
						findCurrentNode = true;
					} else if (!findCurrentNode) {
						item.put("status", 0);
						item.put("errmsg", "success");
					} else {
						item.put("status", 2);
						item.put("errmsg", "unknown");
					}
					listData.add(item);
				}
				
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("list", listData);
				data.put("exception", isExceptStatus?"1":"0");
				data.put("exceptMsg", isExceptStatus?"error":"success");
				data.put("startTime", startTime);
				data.put("endTime", endTime);
				return success(data);
			}else{
				List<HistoricProcessInstance> hisPiList = historyService
						.createHistoricProcessInstanceQuery()
						.processDefinitionId(def.getId())
						.finished()
						.orderByProcessInstanceStartTime().desc().list();

				if (hisPiList.size() > 0) {
					//工作流已经执行过了,全部节点标记为success
					for (Map<String, String> one : nodes) {
						Map<String, Object> item = new HashMap<String, Object>();
						String nodeTaskId = one.get("id");
						String nodeTaskName=one.get("name");
						
						item.put("nodeId", nodeTaskId);
						item.put("nodeName", nodeTaskName);
						item.put("status", 0);
						item.put("errmsg", "success");
						listData.add(item);
						
						startTime=df.format(hisPiList.get(0).getStartTime());
						endTime=df.format(hisPiList.get(0).getEndTime());
					}
					
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("list", listData);
					data.put("exception", isExceptStatus?"1":"0");
					data.put("exceptMsg", isExceptStatus?"error":"success");
					data.put("startTime", startTime);
					data.put("endTime", endTime);
					return success(data);
				} else {
					//工作流尚未执行过,全部节点标记为unkown
					for (Map<String, String> one : nodes) {
						Map<String, Object> item = new HashMap<String, Object>();
						String nodeTaskId = one.get("id");
						String nodeTaskName=one.get("name");
						
						item.put("nodeId", nodeTaskId);
						item.put("nodeName", nodeTaskName);
						item.put("status", 2);
						item.put("errmsg", "unkown");
						listData.add(item);
					}
					
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("list", listData);
					data.put("exception", "0");
					data.put("exceptMsg", "unkown");
					data.put("startTime", null);
					data.put("endTime", null);
					return success(data);
				}
			}
			
		}catch(Exception e){
			logger.error("Error in traceProcess:",e);
			return failed(-1,e.getMessage());
		}
				
    }
}
