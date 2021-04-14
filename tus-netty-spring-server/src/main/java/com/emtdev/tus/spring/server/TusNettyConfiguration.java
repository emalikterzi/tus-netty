package com.emtdev.tus.spring.server;

import com.emtdev.tus.core.TusLocationProvider;
import com.emtdev.tus.netty.handler.TusConfiguration;
import com.emtdev.tus.spring.server.custom.DefaultFileIdGenerator;
import com.emtdev.tus.spring.server.custom.DefaultLocationProvider;
import com.emtdev.tus.spring.server.custom.InMemoryConfigStore;
import com.emtdev.tus.store.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TusSpringProperties.class)
public class TusNettyConfiguration {

    private static Logger logger  = LoggerFactory.getLogger(TusNettyConfiguration.class);

    @Autowired
    private TusSpringProperties tusSpringProperties;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public TusNettyServer tusNettyServer() {
        return new TusNettyServer(tusSpringProperties, tusChannelInitializer());
    }

    @Bean
    public TusChannelInitializer tusChannelInitializer() {
        return new TusChannelInitializer(tusConfiguration());
    }

    @Bean
    public TusConfiguration tusConfiguration() {
        logger.info("Creation Location Provider Path : " + tusSpringProperties.getLocationPrefix());
        FileStore fileStore = new FileStore(tusSpringProperties.getBasePath(), new InMemoryConfigStore());
        TusLocationProvider tusLocationProvider = new DefaultLocationProvider(tusSpringProperties.getLocationPrefix());
        TusConfiguration tusConfiguration = new TusConfiguration("/files", fileStore, tusLocationProvider, new DefaultFileIdGenerator(), 0);
        return tusConfiguration;
    }

}
