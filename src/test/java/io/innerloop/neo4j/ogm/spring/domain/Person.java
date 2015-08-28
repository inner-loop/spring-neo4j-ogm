package io.innerloop.neo4j.ogm.spring.domain;

import io.innerloop.neo4j.ogm.annotations.Id;
import io.innerloop.neo4j.ogm.generators.UuidGenerator;

import java.util.UUID;

/**
 * Created by markangrish on 28/08/2015.
 */
public class Person
{
    private Long id;

    @Id
    private UUID uuid;

    private String firstName;

    private String secondName;

    private String lastName;

    public Person()
    {
        this.uuid = UuidGenerator.generate();
    }

    public Person(String firstName, String secondName)
    {
        this.uuid = UuidGenerator.generate();
        this.firstName = firstName;
        this.secondName = secondName;
    }

    public Long getId()
    {
        return id;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }
}
