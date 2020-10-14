package webflow.mgr.mybatis.mapper;

import webflow.mgr.mybatis.entity.SqlNotificationEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface SqlNotificationEntityMapper
{
	@Delete("DELETE from OWF_NOTIFICATION")
	public void deleteAll();

	@Select("SELECT * FROM OWF_NOTIFICATION where TASK_ID=#{taskId}")
	@Results(value = { @Result(property = "taskId", column = "TASK_ID"),
			@Result(property = "opTime", column = "OP_TIME") })
	List<SqlNotificationEntity> findByTaskId(@Param("taskId")
                                                     String taskId);

	@Insert("INSERT INTO OWF_NOTIFICATION (TASK_ID,OP_TIME) values (#{taskId},#{opTime})")
	void saveNotificationDetails(SqlNotificationEntity sde);
}
