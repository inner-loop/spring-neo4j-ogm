package io.innerloop.neo4j.ogm.spring.transaction;

import io.innerloop.neo4j.ogm.Session;
import io.innerloop.neo4j.ogm.SessionFactory;
import io.innerloop.neo4j.ogm.Transaction;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
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
        implements ResourceTransactionManager, BeanFactoryAware, InitializingBean
{
    private SessionFactory sessionFactory;

    /**
     * Just needed for entityInterceptorBeanName.
     */
    private BeanFactory beanFactory;

    public Neo4jTransactionManager()
    {
    }

    /**
     * Create a new Neo4jTransactionManager instance.
     *
     * @param sessionFactory
     *         SessionFactory to manage transactions for
     */
    public Neo4jTransactionManager(SessionFactory sessionFactory)
    {
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
            if (logger.isDebugEnabled())
            {
                logger.debug("Found thread-bound Session [" + sessionHolder.getSession() +
                             "] for Neo4J OGM transaction");
            }
            txObject.setSessionHolder(sessionHolder);
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
                    logger.debug("Opened new Session [" + newSession + "] for Neo4j OGM transaction");
                }
                txObject.setSession(newSession);
            }

            session = txObject.getSessionHolder().getSession();


            Transaction neo4jTransaction = session.getTransaction();
            neo4jTransaction.begin();

            txObject.getSessionHolder().setTransaction(neo4jTransaction);


            // Bind the session holder to the thread.
            if (txObject.isNewSessionHolder())
            {
                TransactionSynchronizationManager.bindResource(getSessionFactory(), txObject.getSessionHolder());
            }
            txObject.getSessionHolder().setSynchronizedWithTransaction(true);
        }

        catch (Throwable ex)
        {
            if (txObject.isNewSession())
            {
                try
                {
                    if (session != null && session.getTransaction().isOpen())
                    {
                        session.getTransaction().rollback();
                    }
                }
                catch (Throwable ex2)
                {
                    logger.debug("Could not rollback Session after failed transaction begin", ex);
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
                    txObject.setSessionHolder(null);
                }
            }
            throw new CannotCreateTransactionException("Could not open OGM Session for transaction", ex);
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException
    {
        Neo4jTransactionObject txObject = (Neo4jTransactionObject) status.getTransaction();
        if (status.isDebug())
        {
            logger.debug("Committing Neo4j OGM transaction on Session [" +
                         txObject.getSessionHolder().getSession() + "]");
        }
        txObject.getSessionHolder().getTransaction().commit();
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        Neo4jTransactionObject txObject = (Neo4jTransactionObject) transaction;

        return txObject.getSessionHolder() != null && txObject.getSessionHolder().getTransaction().isActive();
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException
    {
        logger.info("Rolling back a transaction.");
        Neo4jTransactionObject txObject = (Neo4jTransactionObject) status.getTransaction();
        if (status.isDebug())
        {
            logger.debug("Rolling back Neo4j OGM transaction on Session [" +
                         txObject.getSessionHolder().getSession() + "]");
        }
        try
        {
            txObject.getSessionHolder().getTransaction().rollback();
        }
        finally
        {
            if (!txObject.isNewSession())
            {
                // Clear all pending inserts/updates/deletes in the Session.
                // Necessary for pre-bound Sessions, to avoid inconsistent state.
                txObject.getSessionHolder().getSession().clear();
            }
        }
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

        if (txObject.isNewSession())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Closing Neo4j OGM Session [" + session + "] after transaction");
            }
            if (session != null)
            {
                try
                {
                    session.close();
                }
                catch (Throwable ex)
                {
                    logger.debug("Unexpected exception on closing Neo4j OGM Session", ex);
                }
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Not closing pre-bound Neo4j OGM  Session [" + session + "] after transaction");
            }
        }
        txObject.getSessionHolder().clear();
    }


    /**
     * The bean factory just needs to be known for resolving entity interceptor bean names. It does not need to be set
     * for any other mode of operation.
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory)
    {
        this.beanFactory = beanFactory;
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

        private boolean newSession;

        private Integer previousHoldability;


        public void setSession(Session session)
        {
            this.sessionHolder = new SessionHolder(session);
            this.newSessionHolder = true;
            this.newSession = true;
        }

        public void setExistingSession(Session session)
        {
            this.sessionHolder = new SessionHolder(session);
            this.newSessionHolder = true;
            this.newSession = false;
        }

        public void setSessionHolder(SessionHolder sessionHolder)
        {
            this.sessionHolder = sessionHolder;
            this.newSessionHolder = false;
            this.newSession = false;
        }

        public SessionHolder getSessionHolder()
        {
            return this.sessionHolder;
        }

        public boolean isNewSessionHolder()
        {
            return this.newSessionHolder;
        }

        public boolean isNewSession()
        {
            return this.newSession;
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
