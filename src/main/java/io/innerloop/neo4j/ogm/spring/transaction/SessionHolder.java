package io.innerloop.neo4j.ogm.spring.transaction;

import io.innerloop.neo4j.ogm.Session;
import io.innerloop.neo4j.ogm.Transaction;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

/**
 * Created by markangrish on 08/05/2015.
 */
public class SessionHolder extends ResourceHolderSupport
{
    private Session session;

    private Transaction transaction;


    public SessionHolder(Session session)
    {
        Assert.notNull(session, "Session must not be null");
        this.session = session;
    }

    public Session getSession()
    {
        return this.session;
    }

    public void setTransaction(Transaction transaction)
    {
        this.transaction = transaction;
    }

    public Transaction getTransaction()
    {
        return this.transaction;
    }


    @Override
    public void clear()
    {
        super.clear();
        this.transaction = null;
    }
}
