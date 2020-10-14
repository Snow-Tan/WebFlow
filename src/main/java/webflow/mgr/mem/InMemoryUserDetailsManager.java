package webflow.mgr.mem;


import webflow.identity.UserDetailsEntity;
import webflow.identity.UserDetailsManager;
import webflow.mgr.ext.UserDetailsManagerEx;

import java.util.HashMap;
import java.util.Map;

public class InMemoryUserDetailsManager implements UserDetailsManager, UserDetailsManagerEx
{
	Map<String, UserDetailsEntity> _users = new HashMap<String, UserDetailsEntity>();

	@Override
	public UserDetailsEntity findUserDetails(String userId)
	{
		return _users.get(userId);
	}

	@Override
	public void removeAll()
	{
		_users.clear();
	}

	public void saveUserDetails(UserDetailsEntity userDetails)
	{
		_users.put(userDetails.getUserId(), userDetails);
	}
}
