package webflow.ctrl.impl;

import webflow.ctrl.RuntimeActivityDefinitionManager;
import webflow.ctrl.TaskFlowControlService;
import webflow.ctrl.command.CreateAndTakeTransitionCmd;
import webflow.ctrl.command.DeleteRunningTaskCmd;
import webflow.ctrl.command.StartActivityCmd;
import webflow.ctrl.creator.ChainedActivitiesCreator;
import webflow.ctrl.creator.MultiInstanceActivityCreator;
import webflow.ctrl.creator.RuntimeActivityDefinitionEntityIntepreter;
import webflow.mgr.common.SimpleRuntimeActivityDefinitionEntity;
import webflow.util.ProcessDefinitionUtils;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DefaultTaskFlowControlService implements TaskFlowControlService
{
	RuntimeActivityDefinitionManager _activitiesCreationStore;

	ProcessDefinitionEntity _processDefinition;

	ProcessEngine _processEngine;

	private String _processInstanceId;

	public DefaultTaskFlowControlService(RuntimeActivityDefinitionManager activitiesCreationStore,
										 ProcessEngine processEngine, String processId)
	{
		_activitiesCreationStore = activitiesCreationStore;
		_processEngine = processEngine;
		_processInstanceId = processId;

		String processDefId = _processEngine.getRuntimeService().createProcessInstanceQuery()
				.processInstanceId(_processInstanceId).singleResult().getProcessDefinitionId();

		_processDefinition = ProcessDefinitionUtils.getProcessDefinition(_processEngine, processDefId);
	}

	private ActivityImpl[] cloneAndMakeChain(String prototypeActivityId, String nextActivityId, String... assignees)
			throws Exception
	{
		// 封装了部分活动节点定义信息
		SimpleRuntimeActivityDefinitionEntity info = new SimpleRuntimeActivityDefinitionEntity();
		info.setProcessDefinitionId(_processDefinition.getId());
		info.setProcessInstanceId(_processInstanceId);

		// 对 SimpleRuntimeActivityDefinitionEntity 进一步封装
		RuntimeActivityDefinitionEntityIntepreter radei = new RuntimeActivityDefinitionEntityIntepreter(info);
		radei.setPrototypeActivityId(prototypeActivityId);
		radei.setAssignees(CollectionUtils.arrayToList(assignees));
		radei.setNextActivityId(nextActivityId);

		// 创建活动节点（多个）
		ActivityImpl[] activities = new ChainedActivitiesCreator().createActivities(_processEngine, _processDefinition,
			info);

		// 跳转（包括回退和向前）至指定活动节点
		moveTo(activities[0].getId());
		// 记录创建
		recordActivitiesCreation(info);

		return activities;
	}

	private void executeCommand(Command<Void> command)
	{
		((RuntimeServiceImpl) _processEngine.getRuntimeService()).getCommandExecutor().execute(command);
	}

	private TaskEntity getCurrentTask()
	{
		return (TaskEntity) _processEngine.getTaskService().createTaskQuery().processInstanceId(_processInstanceId)
				.active().singleResult();
	}

	private TaskEntity getTaskById(String taskId)
	{
		return (TaskEntity) _processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
	}

	/**
	 * 后加签
	 */
	@Override
	public ActivityImpl[] insertTasksAfter(String targetTaskDefinitionKey, String... assignees) throws Exception
	{
		// 将当前用户和代理人们缓存assigneeList
		List<String> assigneeList = new ArrayList<String>();
		assigneeList.add(Authentication.getAuthenticatedUserId());
		assigneeList.addAll(CollectionUtils.arrayToList(assignees));
		String[] newAssignees = assigneeList.toArray(new String[0]);

		// 获取原型活动节点
		ActivityImpl prototypeActivity = ProcessDefinitionUtils.getActivity(_processEngine, _processDefinition.getId(),
			targetTaskDefinitionKey);

		return cloneAndMakeChain(targetTaskDefinitionKey, prototypeActivity.getOutgoingTransitions().get(0)
				.getDestination().getId(), newAssignees);
	}

	/**
	 * 前加签
	 */
	@Override
	public ActivityImpl[] insertTasksBefore(String targetTaskDefinitionKey, String... assignees) throws Exception
	{
		return cloneAndMakeChain(targetTaskDefinitionKey, targetTaskDefinitionKey, assignees);
	}

	@Override
	public void moveBack() throws Exception
	{
		moveBack(getCurrentTask());
	}

	@Override
	public void moveBack(TaskEntity currentTaskEntity) throws Exception
	{
		ActivityImpl activity = (ActivityImpl) ProcessDefinitionUtils
				.getActivity(_processEngine, currentTaskEntity.getProcessDefinitionId(),
					currentTaskEntity.getTaskDefinitionKey()).getIncomingTransitions().get(0).getSource();

		moveTo(currentTaskEntity, activity);
	}

	@Override
	public void moveForward() throws Exception
	{
		moveForward(getCurrentTask());
	}

	@Override
	public void moveForward(TaskEntity currentTaskEntity) throws Exception
	{
		ActivityImpl activity = (ActivityImpl) ProcessDefinitionUtils
				.getActivity(_processEngine, currentTaskEntity.getProcessDefinitionId(),
					currentTaskEntity.getTaskDefinitionKey()).getOutgoingTransitions().get(0).getDestination();

		moveTo(currentTaskEntity, activity);
	}

	/**
	 * 跳转（包括回退和向前）至指定活动节点
	 *
	 * @param targetTaskDefinitionKey
	 * @throws Exception
	 */
	@Override
	public void moveTo(String targetTaskDefinitionKey) throws Exception
	{
		moveTo(getCurrentTask(), targetTaskDefinitionKey);
	}

	@Override
	public void moveTo(String currentTaskId, String targetTaskDefinitionKey) throws Exception
	{
		moveTo(getTaskById(currentTaskId), targetTaskDefinitionKey);
	}

	private void moveTo(TaskEntity currentTaskEntity, ActivityImpl activity)
	{
		// 设置开始活动节点，并执行
		executeCommand(new StartActivityCmd(currentTaskEntity.getExecutionId(), activity));
		// 删除当前运行中的任务，并执行
		executeCommand(new DeleteRunningTaskCmd(currentTaskEntity));
	}

	/**
	 *
	 * @param currentTaskEntity
	 *            当前任务节点
	 * @param targetTaskDefinitionKey
	 *            目标任务节点（在模型定义里面的节点名称）
	 * @throws Exception
	 */
	@Override
	public void moveTo(TaskEntity currentTaskEntity, String targetTaskDefinitionKey) throws Exception
	{
		// 目标任务的活动节点
		ActivityImpl activity = ProcessDefinitionUtils.getActivity(_processEngine,
			currentTaskEntity.getProcessDefinitionId(), targetTaskDefinitionKey);

		moveTo(currentTaskEntity, activity);
	}

	private void recordActivitiesCreation(SimpleRuntimeActivityDefinitionEntity info) throws Exception
	{
		info.serializeProperties();
		_activitiesCreationStore.save(info);
	}

	/**
	 * 分裂某节点为多实例节点
	 *
	 * @param targetTaskDefinitionKey
	 * @param assignee
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Override
	public ActivityImpl split(String targetTaskDefinitionKey, boolean isSequential, String... assignees)
			throws Exception
	{
		SimpleRuntimeActivityDefinitionEntity info = new SimpleRuntimeActivityDefinitionEntity();
		info.setProcessDefinitionId(_processDefinition.getId());
		info.setProcessInstanceId(_processInstanceId);

		RuntimeActivityDefinitionEntityIntepreter radei = new RuntimeActivityDefinitionEntityIntepreter(info);

		radei.setPrototypeActivityId(targetTaskDefinitionKey);
		radei.setAssignees(CollectionUtils.arrayToList(assignees));
		radei.setSequential(isSequential);

		ActivityImpl clone = new MultiInstanceActivityCreator().createActivities(_processEngine, _processDefinition,
			info)[0];

		TaskEntity currentTaskEntity = getCurrentTask();
		executeCommand(new CreateAndTakeTransitionCmd(currentTaskEntity.getExecutionId(), clone));
		executeCommand(new DeleteRunningTaskCmd(currentTaskEntity));

		recordActivitiesCreation(info);
		return clone;
	}

	@Override
	public ActivityImpl split(String targetTaskDefinitionKey, String... assignee) throws Exception
	{
		return split(targetTaskDefinitionKey, true, assignee);
	}
}
