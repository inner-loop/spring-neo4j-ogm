package io.innerloop.neo4j.ogm.spring;

import io.innerloop.neo4j.ogm.spring.config.AppConfig;
import io.innerloop.neo4j.ogm.spring.domain.Person;
import io.innerloop.neo4j.ogm.spring.domain.PersonRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
