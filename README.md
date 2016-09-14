spring-neo4j-ogm
===============

***NOTE: This project is no longer actively developed supported however, I am an active developer now on the [the official Spring project](https://github.com/spring-projects/spring-data-neo4j) that integrates with [the Neo4j OGM](https://github.com/neo4j/neo4j-ogm) and recommend developers use those from now on.***


Spring integration for [Java Neo4J OGM](https://github.com/inner-loop/java-neo4j-ogm).

[![Build Status](https://travis-ci.org/inner-loop/spring-neo4j-ogm.svg?branch=master)](https://travis-ci.org/inner-loop/spring-neo4j-ogm)


#Quick Start
This module adds support for Spring @Transactional.

Use this module instead of the [Java Neo4J OGM](https://github.com/inner-loop/java-neo4j-ogm) for 
Spring projects.

## Install from Maven

Add the following to your ```<dependencies> .. </dependencies>``` section.

```maven
<dependency>
    <groupId>io.innerloop</groupId>
    <artifactId>spring-neo4j-ogm/artifactId>
    <version>0.2.0</version>
</dependency>
```

## Install from Gradle

Add the following to your ```dependencies { .. }``` section.

```gradle
compile group: 'io.innerloop', name: 'spring-neo4j-ogm', version: '0.2.0'
```

... or more simply:

```gradle
compile: 'io.innerloop:spring-neo4j-ogm:0.2.0'
```


See [Java Neo4J OGM](https://github.com/inner-loop/java-neo4j-ogm) for more details on how to use this library.

# Examples

## Using applicationContext.xml

```
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                            http://www.springframework.org/schema/beans/spring-beans-4.1.xsd
                            http://www.springframework.org/schema/context
                            http://www.springframework.org/schema/context/spring-context-4.1.xsd
                            http://www.springframework.org/schema/tx
                            http://www.springframework.org/schema/tx/spring-tx-4.1.xsd">


    <tx:annotation-driven transaction-manager="txManager" />
    <context:spring-configured/>
    <context:annotation-config/>
    <context:component-scan base-package="io.innerloop"/>

    <bean id="txManager" class="io.innerloop.neo4j.ogm.spring.transaction.Neo4jTransactionManager">
        <constructor-arg ref="sessionFactory"/>
    </bean>

    <bean id="neo4jClient" class="io.innerloop.neo4j.client.Neo4jClient">
        <constructor-arg value="#{systemProperties['neo4j.rest.url']}" index="0"/>
        <constructor-arg value="#{systemProperties['neo4j.rest.username']}" index="1"/>
        <constructor-arg value="#{systemProperties['neo4j.rest.password']}" index="2"/>
    </bean>

    <bean id="sessionFactory" class="io.innerloop.neo4j.ogm.SessionFactory">
        <constructor-arg ref="neo4jClient"/>
        <constructor-arg value="io.innerloop.insight.domain"/>
    </bean>
</beans>
```
