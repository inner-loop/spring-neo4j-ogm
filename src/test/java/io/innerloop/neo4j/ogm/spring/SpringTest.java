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

    private static final int DEFAULT_NEO_PORT = 7575;

    private Neo4jClient client;

    private int neoServerPort = -1;

    private GraphDatabaseService database;

    private WrappingNeoServerBootstrapper bootstrapper;

    @BeforeClass
    public static void oneTimeSetUp()
    {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("io.innerloop.neo4j");
        rootLogger.setLevel(Level.DEBUG);
    }

    private static int findOpenLocalPort()
    {
        try (ServerSocket socket = new ServerSocket(0))
        {
            return socket.getLocalPort();
        }
        catch (IOException e)
        {
            System.err.println("Unable to establish local port due to IOException: " + e.getMessage() +
                               "\nDefaulting instead to use: " + DEFAULT_NEO_PORT);
            e.printStackTrace(System.err);

            return DEFAULT_NEO_PORT;
        }
    }

    @Before
    public void setUp() throws IOException, InterruptedException
    {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                database.shutdown();
            }
        });
        ServerConfigurator configurator = new ServerConfigurator((GraphDatabaseAPI) database);
        int port = neoServerPort();
        configurator.configuration().addProperty(Configurator.WEBSERVER_PORT_PROPERTY_KEY, port);
        configurator.configuration().addProperty("dbms.security.auth_enabled", false);
        bootstrapper = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) database, configurator);
        bootstrapper.start();
        while (!bootstrapper.getServer().getDatabase().isRunning())
        {
            // It's ok to spin here.. it's not production code.
            Thread.sleep(250);
        }
        client = new Neo4jClient("http://localhost:" + port + "/db/data");
    }

    protected int neoServerPort()
    {
        if (neoServerPort < 0)
        {
            neoServerPort = findOpenLocalPort();
        }
        return neoServerPort;
    }

    @After
    public void tearDown() throws IOException, InterruptedException
    {
        bootstrapper.stop();
        database.shutdown();
        client = null;
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
