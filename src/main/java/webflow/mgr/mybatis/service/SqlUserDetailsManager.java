package webflow.mgr.mybatis.service;

import webflow.identity.UserDetailsEntity;
import webflow.identity.UserDetailsManager;
import webflow.mgr.common.SimpleUserDetailsEntity;
import webflow.mgr.ext.UserDetailsManagerEx;
import webflow.mgr.mybatis.mapper.SqlUserDetailsEntityMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class SqlUserDetailsManager extends SqlMapperBasedServiceBase<SqlUserDetailsEntityMapper> implements
		UserDetailsManager, UserDetailsManagerEx
{
	public UserDetailsEntity findUserDetails(String userId)
	{
		return _mapper.findUserDetailsById(userId);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void removeAll()
	{
		_mapper.deleteAll();
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveUserDetails(UserDetailsEntity userDetails)
	{
		_mapper.saveUserDetails(new SimpleUserDetailsEntity(userDetails));
	}
}
