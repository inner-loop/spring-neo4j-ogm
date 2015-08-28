package io.innerloop.neo4j.ogm.spring.domain;

import io.innerloop.neo4j.ogm.spring.repository.GenericRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by markangrish on 28/08/2015.
 */
@Repository
public interface PersonRepository extends GenericRepository<Person>
{

}
