FROM openjdk:8

WORKDIR /opt/project
COPY target/performance-platform-0.0.1-SNAPSHOT.jar /opt/project/
#ADD target/smart-dm-inter-boot-1.0-SNAPSHOT.jar /opt/his/smart-dm-inter-boot-1.0-SNAPSHOT.jar
#RUN apt-get update \
#    && apt-get install -y maven \
#    && cd /project \
#    && mvn package

CMD ["java", "-jar", "/opt/project/performance-platform-0.0.1-SNAPSHOT.jar"]