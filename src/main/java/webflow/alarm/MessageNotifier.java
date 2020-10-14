package webflow.alarm;

import webflow.identity.UserDetailsEntity;
import org.activiti.engine.task.Task;

public interface MessageNotifier
{
	void notify(UserDetailsEntity[] users, Task task) throws Exception;
}
