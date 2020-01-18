package org.oruko.dictionary.auth.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Hafiz on 1/18/2020.
 */
@Configuration
public class ServerConfiguration {

    @ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true")
    @Bean
    public EmbeddedServletContainerFactory servletContainer(@Value("${server.port}") Integer serverPort,
                                                            @Value("${http.port:#{null}}") Integer httpPort) {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(getHttpConnector(serverPort, httpPort));
        return tomcat;
    }

    private Connector getHttpConnector(Integer serverPort, Integer httpPort) {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(serverPort);
        connector.setSecure(false);
        connector.setPort(httpPort);
        connector.setRedirectPort(serverPort);
        return connector;
    }
}
