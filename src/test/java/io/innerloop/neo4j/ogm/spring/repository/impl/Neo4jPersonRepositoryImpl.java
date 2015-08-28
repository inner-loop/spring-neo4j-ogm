package io.innerloop.neo4j.ogm.spring.repository.impl;

import io.innerloop.neo4j.ogm.SessionFactory;
import io.innerloop.neo4j.ogm.spring.domain.Person;
import io.innerloop.neo4j.ogm.spring.domain.PersonRepository;
import io.innerloop.neo4j.ogm.spring.repository.DefaultNeo4jRepositoryImpl;
import org.springframework.stereotype.Repository;

/**
 * Created by markangrish on 28/08/2015.
 */
@Repository
public class Neo4jPersonRepositoryImpl extends DefaultNeo4jRepositoryImpl<Person> implements PersonRepository
{
    public Neo4jPersonRepositoryImpl(SessionFactory sessionFactory)
    {
        super(sessionFactory);
    }
}
