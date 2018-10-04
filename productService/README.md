# spring-mvc-crud-reference
keywords:  spring-boot, thymeleaf, swagger-ui, repository, data, jpa, h2, datasource, rest e mvc

# Requirement

- Java 8

- Maven 3.3.9

teste

# Objective 
This  a reference to start a spring-boot project with a minimal requirements.
Have in this project the basic requirement to mvc pattern.
Have  components like thymeleaf(view), swagger-ui, jpa e rest

# Execute Project

## download the project
    git clone git@github.com:danieldggomes/spring-mvc-crud-reference.git

    cd  spring-mvc-crud-reference

## execute and test the service

    mvn spring-boot:run


## build e create a war file

    cd spring-mvc-crud-reference

    Change in the pom.xml to build a artifact war.
    Just change in the tag packaging with the value to war value
    <!--<packaging>war</packaging>-->

    mvn clean package

    cd spring-mvc-crud-reference/target

    ls -la

    Just observe if the jar and the war must be there.

# Access Aplication

## App 
http://localhost:8089/

## API App
http://localhost:8089/swagger-ui.html#/

## h2 Database
http://localhost:8089/h2/
