package com.oms.fix.config;

import com.oms.fix.application.OmsFixApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quickfix.*;

import java.io.InputStream;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FixSessionConfig {

    private final OmsFixApplication application;

    @Value("${fix.config.path:order-service.cfg}")
    private String configPath;

    @Bean
    public SessionSettings sessionSettings() throws ConfigError {
        InputStream is = getClass().getClassLoader().getResourceAsStream(configPath);
        if (is == null) throw new ConfigError("FIX config file not found: " + configPath);
        return new SessionSettings(is);
    }

    @Bean
    public MessageStoreFactory messageStoreFactory(SessionSettings settings) {
        return new FileStoreFactory(settings);
    }

    @Bean
    public LogFactory logFactory(SessionSettings settings) {
        return new FileLogFactory(settings);
    }

    @Bean
    public MessageFactory messageFactory() {
        return new DefaultMessageFactory();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Acceptor socketAcceptor(SessionSettings sessionSettings,
                                   MessageStoreFactory messageStoreFactory,
                                   LogFactory logFactory,
                                   MessageFactory messageFactory) throws ConfigError {
        return new SocketAcceptor(application, messageStoreFactory,
                sessionSettings, logFactory, messageFactory);
    }
}
