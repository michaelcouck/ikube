Sample Vaadin application that uses Spring Framework.

Eclipse
=======

Recommended plugins:
- Subclipse
- m2e (update site: http://m2eclipse.sonatype.org/sites/m2e)
- m2e integration for Subclipse (http://m2eclipse.sonatype.org/sites/m2e-extras)
- Vaadin Eclipse plugin

You can checkout project from SVN Repository Exploring perspective via "Check out as Maven project".

Or

svn co http://dev.vaadin.com/svn/incubator/SpringApplication
mvn eclipse:eclipse
Import project to Eclipse

Testing with Jetty
==================

- install maven2
- execute command mvn compile jetty:run
- open web browser on url http://localhost:8080/test

Testing clustering with Terracotta and Jetty
============================================

- change properties of src/main/resources/database.properties to non memory database (for example MySql)
- add database driver to pom.xml (MySql driver already included)
- execute command mvn -Pjetty6x package tc:run
- open web browser on url http://localhost:8080/test or http://localhost:8081/test (change browser address and see that other server is up to date)
- execute command mvn -Pjetty6x tc:stop to stop terracotta server
 