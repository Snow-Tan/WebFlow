package webflow.mgr.mybatis.service;

import webflow.identity.IdentityMembershipManager;
import webflow.mgr.ext.IdentityMembershipManagerEx;
import webflow.mgr.mybatis.entity.SqlMembershipEntity;
import webflow.mgr.mybatis.mapper.SqlMembershipEntityMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(readOnly = true)
public class SqlMembershipManager extends SqlMapperBasedServiceBase<SqlMembershipEntityMapper> implements
		IdentityMembershipManager, IdentityMembershipManagerEx
{
	public List<String> findGroupIdsByUser(String userId)
	{
		Map<String, Object> names = new HashMap<String, Object>();
		for (SqlMembershipEntity ms : _mapper.findMembershipsByUserId(userId))
		{
			names.put(ms.getGroupId(), 0);
		}

		return new ArrayList<String>(names.keySet());
	}

	public List<String> findUserIdsByGroup(String groupId)
	{
		Map<String, Object> names = new HashMap<String, Object>();
		for (SqlMembershipEntity ms : _mapper.findMembershipsByGroupId(groupId))
		{
			names.put(ms.getUserId(), 0);
		}

		return new ArrayList<String>(names.keySet());
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void removeAll()
	{
		_mapper.deleteAll();
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void saveMembership(String userId, String groupId)
	{
		SqlMembershipEntity mse = new SqlMembershipEntity();
		mse.setGroupId(groupId);
		mse.setUserId(userId);
		_mapper.saveMembership(mse);
	}
}
