package webflow.mgr.common;

import webflow.identity.UserDetailsEntity;

public abstract class UserDetailsEntitySupport implements UserDetailsEntity
{
	public void copyProperties(UserDetailsEntity src)
	{
		for (String name : src.getPropertyNames())
		{
			setProperty(name, src.getProperty(name));
		}
	}
}
