package com.example.gracefulshutdown;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class JettyWebserverCustomizer implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {

	static Server staticServer;

	@Value("${server.shutdownWaitTime:30000}")
	private int shutdownWaitTime;

	@PostConstruct
	public void post() {
		System.out.println("POST Construct");
	}

	@Override
	public void customize(JettyServletWebServerFactory factory) {
		factory.addServerCustomizers(server -> {
			staticServer = server;

			/*
			 * StatisticsHandler has to be added for graceful shutdown to work (see
			 * https://github.com/eclipse/jetty.project/issues/1549#issuecomment-301102535)
			 */
			System.out.println("shutdownWaitTime is: " + shutdownWaitTime);
			if (shutdownWaitTime > 0) {
				StatisticsHandler statisticsHandler = new StatisticsHandler();
				statisticsHandler.setHandler(server.getHandler());
				server.setHandler(statisticsHandler);
				server.setStopTimeout(shutdownWaitTime);

				// We will stop it through JettyGracefulShutdown class.
				server.setStopAtShutdown(false);
			}
		});
	}

	@Bean
	public JettyGracefulShutdown jettyGracefulShutdown() { return new JettyGracefulShutdown(); }

	// SpringBoot closes application context before everything.
	private static class JettyGracefulShutdown implements ApplicationListener<ContextClosedEvent> {
		private static final Logger log = LoggerFactory.getLogger(JettyGracefulShutdown.class);

		@Override
		public void onApplicationEvent(ContextClosedEvent event) {
			if (staticServer == null) {
				log.error("Jetty server variable is null, this should not happen!");
				return;
			}
			log.info("Entering shutdown for Jetty.");
			if (!(staticServer.getHandler() instanceof StatisticsHandler)) {
				log.error("Root handler is not StatisticsHandler, graceful shutdown may not work at all!");
			} else {
				log.info("Active requests: " + ((StatisticsHandler) staticServer.getHandler()).getRequestsActive());
			}
			try {
				long begin = System.currentTimeMillis();
				staticServer.stop();
				log.info("Shutdown took " + (System.currentTimeMillis() - begin) + " ms.");
			} catch (Exception e) {
				log.error("Fail to shutdown gracefully.", e);
			}
		}
	}
}
