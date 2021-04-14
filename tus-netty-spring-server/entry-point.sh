#!/usr/bin/env bash

java ${JAVA_OPTS} -jar -Dspring.profiles.active=docker tus-netty-spring-server-0.0.1.jar
