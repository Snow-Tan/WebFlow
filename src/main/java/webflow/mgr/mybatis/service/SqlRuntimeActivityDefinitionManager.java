package webflow.mgr.mybatis.service;

import webflow.ctrl.RuntimeActivityDefinitionEntity;
import webflow.ctrl.RuntimeActivityDefinitionManager;
import webflow.mgr.mybatis.mapper.SqlRuntimeActivityDefinitionManagerMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional(readOnly = true)
public class SqlRuntimeActivityDefinitionManager extends
		SqlMapperBasedServiceBase<SqlRuntimeActivityDefinitionManagerMapper> implements
		RuntimeActivityDefinitionManager
{
	public List<RuntimeActivityDefinitionEntity> list()
	{
		List<RuntimeActivityDefinitionEntity> list = new ArrayList<RuntimeActivityDefinitionEntity>();
		list.addAll(_mapper.findAll());
		return list;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void removeAll()
	{
		_mapper.deleteAll();
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void save(RuntimeActivityDefinitionEntity entity)
	{
		_mapper.save(entity);
	}
}
