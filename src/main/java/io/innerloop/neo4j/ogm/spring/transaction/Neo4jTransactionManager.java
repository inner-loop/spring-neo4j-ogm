package io.innerloop.neo4j.ogm.spring.transaction;

import io.innerloop.neo4j.ogm.Session;
import io.innerloop.neo4j.ogm.SessionFactory;
import io.innerloop.neo4j.ogm.Transaction;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Created by markangrish on 08/05/2015.
 */
public class Neo4jTransactionManager extends AbstractPlatformTransactionManager
        implements ResourceTransactionManager, InitializingBean
{
    private SessionFactory sessionFactory;

    public Neo4jTransactionManager()
    {
        setGlobalRollbackOnParticipationFailure(false);
    }

    /**
     * Create a new Neo4jTransactionManager instance.
     *
     * @param sessionFactory
     *         SessionFactory to manage transactions for
     */
    public Neo4jTransactionManager(SessionFactory sessionFactory)
    {
        this();
        this.sessionFactory = sessionFactory;
        afterPropertiesSet();
    }

    /**
     * Return the SessionFactory that this instance should manage transactions for.
     */
    public SessionFactory getSessionFactory()
    {
        return this.sessionFactory;
    }

    /**
     * Set the SessionFactory that this instance should manage transactions for.
     */
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected Object doGetTransaction() throws TransactionException
    {
        logger.info("Requesting to create or join a transaction");
        Neo4jTransactionObject txObject = new Neo4jTransactionObject();

        SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(getSessionFactory());
        if (sessionHolder != null)
        {
            txObject.setSessionHolder(sessionHolder, false);
        }

        return txObject;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException
    {
        Neo4jTransactionObject txObject = (Neo4jTransactionObject) transaction;

        Session session = null;
        try
        {
            if (txObject.getSessionHolder() == null || txObject.getSessionHolder().isSynchronizedWithTransaction())
            {
                Session newSession = getSessionFactory().getCurrentSession();

                if (logger.isDebugEnabled())
                {
                    logger.debug("Acquired Session [" + newSession + "] for Neo4j OGM transaction");
                }
                txObject.setSessionHolder(new SessionHolder(newSession), true);
            }

            txObject.getSessionHolder().setSynchronizedWithTransaction(true);
            session = txObject.getSessionHolder().getSession();

            Transaction neo4jTransaction = session.getTransaction();
            neo4jTransaction.begin();

            txObject.getSessionHolder().setTransaction(neo4jTransaction);

            // Bind the session holder to the thread.
            if (txObject.isNewSessionHolder())
            {
                TransactionSynchronizationManager.bindResource(getSessionFactory(), txObject.getSessionHolder());
            }
        }
        catch (Throwable ex)
        {
            if (txObject.isNewSessionHolder())
            {
                try
                {
                    if (session != null && session.getTransaction().isOpen())
                    {
                        session.getTransaction().rollback();
                    }
                }
                finally
                {
                    if (session != null)
                    {
                        try
                        {
                            session.close();
                        }
                        catch (Throwable ex2)
                        {
                            logger.debug("Unexpected exception on closing Neo4j OGM Session", ex2);
                        }
                    }
                    txObject.setSessionHolder(null, false);
                }
            }
            throw new CannotCreateTransactionException("Could not open OGM Session for transaction", ex);
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException
    {
        Neo4jTransactionObject txObject = (Neo4jTransactionObject) status.getTransaction();
        Session session = txObject.getSessionHolder().getSession();
        if (status.isDebug())
        {
            logger.debug("Committing Neo4j OGM transaction on Session [" +
                         session + "]");
        }
        session.getTransaction().commit();
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException
    {
        Neo4jTransactionObject txObject = (Neo4jTransactionObject) transaction;

        return txObject.getSessionHolder() != null && txObject.getSessionHolder().getTransaction().isActive();
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        status.setRollbackOnly();
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException
    {
        Neo4jTransactionObject txObject = (Neo4jTransactionObject) status.getTransaction();
        Session session = txObject.getSessionHolder().getSession();
        if (status.isDebug())
        {
            logger.debug("Rolling back Neo4j OGM transaction on Session [" +
                         session + "]");
        }
        session.getTransaction().rollback();
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction)
    {
        Neo4jTransactionObject txObject = (Neo4jTransactionObject) transaction;

        // Remove the session holder from the thread.
        if (txObject.isNewSessionHolder())
        {
            TransactionSynchronizationManager.unbindResource(getSessionFactory());
        }

        Session session = txObject.getSessionHolder().getSession();

        if (logger.isDebugEnabled())
        {
            logger.debug("Closing Neo4j OGM Session [" + session + "] after transaction");
        }
        if (session != null)
        {
            session.close();
        }

        txObject.getSessionHolder().clear();
    }


    @Override
    public void afterPropertiesSet()
    {
        if (getSessionFactory() == null)
        {
            throw new IllegalArgumentException("Property 'sessionFactory' is required");
        }
    }

    @Override
    public Object getResourceFactory()
    {
        return getSessionFactory();
    }


    /**
     */
    private class Neo4jTransactionObject implements SmartTransactionObject
    {
        private SessionHolder sessionHolder;

        private boolean newSessionHolder;


        public void setSession(Session session)
        {
            this.sessionHolder = new SessionHolder(session);
            this.newSessionHolder = true;
        }

        public void setExistingSession(Session session)
        {
            this.sessionHolder = new SessionHolder(session);
            this.newSessionHolder = true;
        }

        public void setSessionHolder(SessionHolder sessionHolder, boolean newConnectionHolder)
        {
            this.sessionHolder = sessionHolder;
            this.newSessionHolder = newConnectionHolder;
        }

        public SessionHolder getSessionHolder()
        {
            return this.sessionHolder;
        }

        public boolean isNewSessionHolder()
        {
            return this.newSessionHolder;
        }


        public void setRollbackOnly()
        {
            this.sessionHolder.setRollbackOnly();
        }

        @Override
        public boolean isRollbackOnly()
        {
            return this.sessionHolder.isRollbackOnly();
        }

        @Override
        public void flush()
        {
            this.sessionHolder.getSession().flush();
        }
    }
}
