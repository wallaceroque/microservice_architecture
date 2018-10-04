FROM danieldggomes/alpine-jdk8-oracle

LABEL maintainer="Daniel Gomes <danieldggomes@gmail.com>" 

ADD target/sales-0.0.1-SNAPSHOT.war /sales-0.0.1-SNAPSHOT.war

CMD java $JAVA_OPTIONS -jar /sales-0.0.1-SNAPSHOT.war

EXPOSE 4444