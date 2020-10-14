package webflow.mgr.mybatis.service;

import webflow.assign.permission.ActivityPermissionEntity;
import webflow.assign.permission.ActivityPermissionManager;
import webflow.mgr.ext.ActivityPermissionManagerEx;
import webflow.mgr.mybatis.entity.SqlActivityPermissionEntity;
import webflow.mgr.mybatis.mapper.SqlActivityPermissionEntityMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;

@Transactional(readOnly = true)
public class SqlActivityPermissionManager extends SqlMapperBasedServiceBase<SqlActivityPermissionEntityMapper>
		implements ActivityPermissionManager, ActivityPermissionManagerEx
{
	public ActivityPermissionEntity load(String processDefinitionId, String taskDefinitionKey, boolean addOrRemove)
	{
		return _mapper.load(processDefinitionId, taskDefinitionKey);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void removeAll()
	{
		_mapper.deleteAll();
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void save(String processDefId, String taskDefinitionKey, String assignee, String[] candidateGroupIds,
			String[] candidateUserIds) throws Exception
	{
		SqlActivityPermissionEntity ap = new SqlActivityPermissionEntity();
		ap.setProcessDefinitionId(processDefId);
		ap.setActivityKey(taskDefinitionKey);
		ap.setAssignee(assignee);
		ap.setGrantedGroupIds(candidateGroupIds);
		ap.setGrantedUserIds(candidateUserIds);
		ap.setOpTime(new Date(System.currentTimeMillis()));

		_mapper.save(ap);
	}
}
