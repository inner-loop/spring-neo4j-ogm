package io.innerloop.neo4j.ogm.spring.config;

import io.innerloop.neo4j.client.Neo4jClient;
import io.innerloop.neo4j.ogm.SessionFactory;
import io.innerloop.neo4j.ogm.spring.transaction.Neo4jTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

/**
 * Created by markangrish on 08/05/2015.
 */
@Configuration
@EnableTransactionManagement
public abstract class Neo4jOgmConfiguration
{
    private final Logger LOG = LoggerFactory.getLogger(Neo4jOgmConfiguration.class);

    @Bean
    public PlatformTransactionManager transactionManager() throws Exception
    {
        LOG.info("Initialising Neo4jTransactionManager");
        SessionFactory sessionFactory = sessionFactory();
        Assert.notNull(sessionFactory,
                       "You must provide a Session Factory instance in your Spring configuration classes");
        return new Neo4jTransactionManager(sessionFactory);
    }

    @Bean
    public abstract SessionFactory sessionFactory();

    @Bean
    public abstract Neo4jClient neo4jDriver();
}
