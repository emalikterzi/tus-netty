FROM adoptopenjdk/openjdk11

MAINTAINER Enes Malik Terzi

ENV JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1082"
ENV LOCATION_URI="http://localhost:1080/files/"

ADD target/tus-netty-spring-server-0.0.1.jar /root
ADD entry-point.sh /root

RUN mkdir data

WORKDIR /root

RUN chmod +x entry-point.sh

EXPOSE 1080
EXPOSE 1081

VOLUME /data

ENTRYPOINT ["./entry-point.sh"]

