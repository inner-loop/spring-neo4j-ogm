package io.innerloop.neo4j.ogm.spring.repository;

import io.innerloop.neo4j.ogm.SessionFactory;
import io.innerloop.neo4j.ogm.spring.repository.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

/**
 * Created by markangrish on 18/12/2014.
 */
@Transactional(propagation = Propagation.REQUIRED)
public abstract class DefaultNeo4jRepositoryImpl<T> implements GenericRepository<T>
{
    protected final SessionFactory sessionFactory;

    private Class<T> persistentClass;

    @Autowired
    public DefaultNeo4jRepositoryImpl(SessionFactory sessionFactory)
    {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.sessionFactory = sessionFactory;
    }

    protected Class<T> getPersistentClass()
    {
        return persistentClass;
    }

    @Override
    public Iterable<T> findAll()
    {
        return sessionFactory.getCurrentSession().loadAll(getPersistentClass());
    }

    @Override
    public T findBy(Map<String, Object> parameters)
    {
        return sessionFactory.getCurrentSession().load(getPersistentClass(), parameters);
    }

    @Override
    public T findBy(String propertyName, Object propertyValue)
    {
        return sessionFactory.getCurrentSession().load(getPersistentClass(), propertyName, propertyValue);
    }

    @Override
    public void save(T entity)
    {
        sessionFactory.getCurrentSession().save(entity);
    }

    @Override
    public void delete(T entity)
    {
        sessionFactory.getCurrentSession().delete(entity);
    }
}
