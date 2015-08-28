package io.innerloop.neo4j.ogm.spring.config;

import io.innerloop.neo4j.client.Neo4jClient;
import io.innerloop.neo4j.ogm.SessionFactory;
import io.innerloop.neo4j.ogm.spring.domain.PersonRepository;
import io.innerloop.neo4j.ogm.spring.repository.impl.Neo4jPersonRepositoryImpl;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.configuration.ServerConfigurator;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by markangrish on 28/08/2015.
 */
@Configuration
public class AppConfig
{
    private static final int DEFAULT_NEO_PORT = 7575;

    private int neoServerPort = -1;

    @Bean
    public GraphDatabaseService getDatabase()
    {
        return new TestGraphDatabaseFactory().newImpermanentDatabase();
    }


    @Bean
    public SessionFactory getSessionFactory(Neo4jClient neo4jClient)
    {
        return new SessionFactory(neo4jClient, "io.innerloop.neo4j.ogm.spring.domain");
    }

    @Bean
    public Neo4jClient getClient(GraphDatabaseService database)
    {
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
        WrappingNeoServerBootstrapper bootstrapper = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) database, configurator);
        bootstrapper.start();
        while (!bootstrapper.getServer().getDatabase().isRunning())
        {
            // It's ok to spin here.. it's not production code.
            try
            {
                Thread.sleep(250);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        return new Neo4jClient("http://localhost:" + port + "/db/data");
    }

    @Bean
    public PersonRepository getPersonRepository(SessionFactory sessionFactory)
    {
        return new Neo4jPersonRepositoryImpl(sessionFactory);
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

    protected int neoServerPort()
    {
        if (neoServerPort < 0)
        {
            neoServerPort = findOpenLocalPort();
        }
        return neoServerPort;
    }
}
