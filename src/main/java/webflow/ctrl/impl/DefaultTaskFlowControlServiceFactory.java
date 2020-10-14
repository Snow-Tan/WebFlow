package webflow.ctrl.impl;

import webflow.ctrl.RuntimeActivityDefinitionManager;
import webflow.ctrl.TaskFlowControlService;
import webflow.ctrl.TaskFlowControlServiceFactory;
import org.activiti.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultTaskFlowControlServiceFactory implements TaskFlowControlServiceFactory
{
	@Autowired
	RuntimeActivityDefinitionManager _activitiesCreationStore;

	@Autowired
	ProcessEngine _processEngine;

	@Override
	public TaskFlowControlService create(String processId)
	{
		return new DefaultTaskFlowControlService(_activitiesCreationStore, _processEngine, processId);
	}
}
