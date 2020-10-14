package webflow.ctrl.creator;

import webflow.ctrl.RuntimeActivityDefinitionEntity;
import webflow.util.ProcessDefinitionUtils;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ChainedActivitiesCreator extends RuntimeActivityCreatorSupport implements RuntimeActivityCreator
{
	public ActivityImpl[] createActivities(ProcessEngine processEngine, ProcessDefinitionEntity processDefinition,
			RuntimeActivityDefinitionEntity info)
	{
		info.setFactoryName(ChainedActivitiesCreator.class.getName());
		RuntimeActivityDefinitionEntityIntepreter radei = new RuntimeActivityDefinitionEntityIntepreter(info);

		if (radei.getCloneActivityIds() == null)
		{
			radei.setCloneActivityIds(CollectionUtils.arrayToList(new String[radei.getAssignees().size()]));
		}

		return createActivities(processEngine, processDefinition, info.getProcessInstanceId(),
			radei.getPrototypeActivityId(), radei.getNextActivityId(), radei.getAssignees(),
			radei.getCloneActivityIds());
	}

	private ActivityImpl[] createActivities(ProcessEngine processEngine, ProcessDefinitionEntity processDefinition,
			String processInstanceId, String prototypeActivityId, String nextActivityId, List<String> assignees,
			List<String> activityIds)
	{
		// 获取原型活动节点
		ActivityImpl prototypeActivity = ProcessDefinitionUtils.getActivity(processEngine, processDefinition.getId(),
			prototypeActivityId);

		List<ActivityImpl> activities = new ArrayList<ActivityImpl>();
		for (int i = 0; i < assignees.size(); i++) {
			if (activityIds.get(i) == null)
			{
				String activityId = createUniqueActivityId(processInstanceId, prototypeActivityId);
				activityIds.set(i, activityId);
			}

			//  利用原型活动节点克隆出新活动节点
			ActivityImpl clone = createActivity(processEngine, processDefinition, prototypeActivity,
				activityIds.get(i), assignees.get(i));
			activities.add(clone);
		}

		// 获取下一个活动节点
		ActivityImpl nextActivity = ProcessDefinitionUtils.getActivity(processEngine, processDefinition.getId(),
			nextActivityId);
		// 创建活动节点链
		createActivityChain(activities, nextActivity);

		return activities.toArray(new ActivityImpl[0]);
	}
}
