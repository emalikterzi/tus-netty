FROM adoptopenjdk/openjdk11

MAINTAINER Enes Malik Terzi

ENV JAVA_OPTS="-Xms256m -Xmx256m"
ENV STATIC_PATH="/root/static"
ENV VOLUME_PATH="/data"

ADD target/tus-netty-server-examples-0.0.1-jar-with-dependencies.jar /root
ADD entry-point.sh /root

WORKDIR /root

RUN mkdir static

COPY src/main/resources/static /root/static

RUN chmod +x entry-point.sh

EXPOSE 1080

VOLUME /data

ENTRYPOINT ["./entry-point.sh"]
