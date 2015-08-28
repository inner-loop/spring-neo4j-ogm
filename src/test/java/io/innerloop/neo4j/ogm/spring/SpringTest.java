package io.innerloop.neo4j.ogm.spring;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.innerloop.neo4j.client.Neo4jClient;
import io.innerloop.neo4j.ogm.spring.config.AppConfig;
import io.innerloop.neo4j.ogm.spring.domain.Person;
import io.innerloop.neo4j.ogm.spring.domain.PersonRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.configuration.ServerConfigurator;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.ServerSocket;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by markangrish on 28/08/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@TransactionConfiguration(defaultRollback = true)
@Transactional
public class SpringTest
{
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(SpringTest.class);

    @BeforeClass
    public static void oneTimeSetUp()
    {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("io.innerloop.neo4j");
        rootLogger.setLevel(Level.DEBUG);
    }



    @Autowired
    private PersonRepository personRepository;

    @Test
    public void savePersonTest(){
        Person p = new Person("Mark", "Angrish");
        personRepository.save(p);

        Person p2 = personRepository.findBy("firstName", "Mark");

        assertEquals(p, p2);
    }
}
