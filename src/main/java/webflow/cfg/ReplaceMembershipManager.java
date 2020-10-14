package webflow.cfg;

import webflow.identity.IdentityMembershipManager;
import webflow.identity.SessionedEntityManagerFactory;
import webflow.identity.impl.DummyGroupIdentityManager;
import webflow.identity.impl.DummyMembershipIdentityManager;
import webflow.identity.impl.DummyUserIdentityManager;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.MembershipIdentityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;

import java.util.ArrayList;
import java.util.List;

public class ReplaceMembershipManager implements StartEngineEventListener
{
	IdentityMembershipManager _customMembershipManager;

	@Override
	public void afterStartEngine(ProcessEngineConfigurationImpl conf, ProcessEngine processEngine)
	{
	}

	@Override
	public void beforeStartEngine(ProcessEngineConfigurationImpl conf)
	{
		List<SessionFactory> sessionFactories = new ArrayList<SessionFactory>();
		sessionFactories.add(new SessionedEntityManagerFactory(UserIdentityManager.class, new DummyUserIdentityManager(
				_customMembershipManager)));
		sessionFactories.add(new SessionedEntityManagerFactory(GroupIdentityManager.class,
				new DummyGroupIdentityManager(_customMembershipManager)));
		sessionFactories.add(new SessionedEntityManagerFactory(MembershipIdentityManager.class,
				new DummyMembershipIdentityManager()));

		conf.setCustomSessionFactories(sessionFactories);
	}

	public IdentityMembershipManager getCustomMembershipManager()
	{
		return _customMembershipManager;
	}

	public void setCustomMembershipManager(IdentityMembershipManager customMembershipManager)
	{
		_customMembershipManager = customMembershipManager;
	}

}
