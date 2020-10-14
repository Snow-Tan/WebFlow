package webflow.mgr.mybatis.service;

import webflow.alarm.TaskNotificationManager;
import webflow.mgr.ext.TaskNotificationManagerEx;
import webflow.mgr.mybatis.entity.SqlNotificationEntity;
import webflow.mgr.mybatis.mapper.SqlNotificationEntityMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;

@Transactional(readOnly = true)
public class SqlTaskNotificationManager extends SqlMapperBasedServiceBase<SqlNotificationEntityMapper> implements
		TaskNotificationManager, TaskNotificationManagerEx
{
	public boolean isNotified(String taskId)
	{
		return !_mapper.findByTaskId(taskId).isEmpty();
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void removeAll()
	{
		_mapper.deleteAll();
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void setNotified(String taskId)
	{
		SqlNotificationEntity sde = new SqlNotificationEntity();
		sde.setTaskId(taskId);
		sde.setOpTime(new Date(System.currentTimeMillis()));
		_mapper.saveNotificationDetails(sde);
	}
}
