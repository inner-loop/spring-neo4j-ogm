package io.innerloop.neo4j.ogm.spring.config;

import io.innerloop.neo4j.client.Neo4jClient;
import io.innerloop.neo4j.ogm.SessionFactory;
import io.innerloop.neo4j.ogm.spring.domain.PersonRepository;
import io.innerloop.neo4j.ogm.spring.repository.impl.Neo4jPersonRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by markangrish on 28/08/2015.
 */
@Configuration
public class AppConfig
{
    @Bean
    public SessionFactory getSessionFactory(Neo4jClient neo4jClient)
    {
        return new SessionFactory(neo4jClient, "io.innerloop.neo4j.ogm.spring.domain");
    }

    @Bean
    public Neo4jClient getClient()
    {
        return new Neo4jClient("http://localhost:7474/db/data", "neo4j", "admin");
    }

    @Bean
    public PersonRepository getPersonRepository(SessionFactory sessionFactory)
    {
        return new Neo4jPersonRepositoryImpl(sessionFactory);
    }
}
