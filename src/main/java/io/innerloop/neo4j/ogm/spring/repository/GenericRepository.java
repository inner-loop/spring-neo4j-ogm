package io.innerloop.neo4j.ogm.spring.repository;

import java.util.Map;

public interface GenericRepository<T>
{
    T findBy(Map<String, Object> properties);

    T findBy(String propertyName, Object propertyValue);

    Iterable<T> findAll();

    void save(T entity);

    void delete(T entity);
}
