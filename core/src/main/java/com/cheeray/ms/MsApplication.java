package com.cheeray.ms;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistryAutoConfiguration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.StandardEnvironment;

/**
 * Micro service application.
 * <p>
 * Instead auto registration, use {@link MsAppRegister}.
 * </p>
 * 
 * <pre>
 * Consul auto config:
 * {@link org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistryAutoConfiguration}
 * {@link org.springframework.cloud.consul.ConsulAutoConfiguration}
 * {@link org.springframework.cloud.consul.config.ConsulConfigAutoConfiguration}
 * {@link org.springframework.cloud.consul.discovery.configclient.ConsulConfigServerAutoConfiguration}
 * {@link org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration}
 * </pre>
 * 
 * @author Chengwei.Yan
 */
@SpringBootApplication(exclude = {
		ConsulServiceRegistryAutoConfiguration.class,
		/* ConsulConfigServerAutoConfiguration.class, */ /*
															 * ConsulAutoConfiguration.
															 * class,
															 */
		ConsulAutoServiceRegistrationAutoConfiguration.class
})
// @EnableDiscoveryClient
@ComponentScan("${ms.package}")
public class MsApplication {
	private static Logger LOG = LoggerFactory.getLogger(MsApplication.class);
	private static final String PROPERTY_SEPARATOR = "::";

	private static ConfigurableApplicationContext CTX = null;
	// @Autowired
	// private DiscoveryClient discoveryClient;
	//
	// @Autowired
	// private LoadBalancerClient loadBalancer;

	@Value("${spring.application.name:MicroApp}")
	private String appName;

	// @RequestMapping("/self")
	// public ServiceInstance self() {
	// return discoveryClient.getLocalServiceInstance();
	// }
	//
	// @RequestMapping("/")
	// public ServiceInstance lb() {
	// return loadBalancer.choose(appName);
	// }

	// @RequestMapping("/instances")
	// public List<ServiceInstance> instances() {
	// return discoveryClient.getInstances(appName);
	// }

	@EventListener
	public void listen(ApplicationEvent e) {
		LOG.info(e.toString());
		// LOG.error("Application failed to start. Process will exit.");
		// System.exit(99);
	}

	public static void main(String[] args) {
		final boolean stop = args != null
				&& Arrays.stream(args).anyMatch(c -> c.equalsIgnoreCase("stop"));
		final String pid = "Service {}, PID:"
				+ ManagementFactory.getRuntimeMXBean().getName();
		// Required to allow encoded forward slash
		System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH",
				"true");
		// Required to allow encoded back slash
		System.setProperty("org.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH",
				"true");

		if (stop) {
			// Stop the service ...
			LOG.info(pid, "stopping ...");
			if (CTX != null) {
				System.exit(SpringApplication.exit(CTX, new ExitCodeGenerator() {
					@Override
					public int getExitCode() {
						LOG.info("stopped");
						return 0;
					}
				}));
			} else {
				LOG.info("Not attached, invoke JMX");
				// Not attached, need to find and stop ...
			}
		} else {
			// Start the service ...
			LOG.info("Starting service PID:"
					+ ManagementFactory.getRuntimeMXBean().getName());

			setSystemProperty(args);

			// For the response to be sent back to the
			final SpringApplication app = new SpringApplication(MsApplication.class);
			final StandardEnvironment env = new StandardEnvironment();
			app.setEnvironment(env);
			CTX = app.run(args);
			LOG.info("Started service PID:"
					+ ManagementFactory.getRuntimeMXBean().getName());

			final ConsulServiceRegistry registry = CTX
					.getBean(ConsulServiceRegistry.class);
			if (registry != null) {
				final ConsulAutoRegistration reg = CTX
						.getBean(ConsulAutoRegistration.class);
				if (reg != null) {
					Runtime.getRuntime().addShutdownHook(new Thread() {

						@Override
						public void run() {
							LOG.info("Deregister service ...");
							registry.deregister(reg);
						}

					});

				}
			}

		}
	}

	private static void setSystemProperty(String[] args) {
		if (args != null) {
			LOG.info("Setting system property: " + args);
			for (final String arg : args) {
				if (arg.contains(PROPERTY_SEPARATOR)) {
					final String[] splits = arg.split(PROPERTY_SEPARATOR);
					if (splits.length != 2) {
						LOG.warn("Invalid property found:{}. Format: key{}value.", arg,
								PROPERTY_SEPARATOR);
						continue;
					} else {
						LOG.info("Setting system property {} with {}", splits[0],
								splits[1]);
						System.setProperty(splits[0], splits[1]);
					}
				}
			}
		} else {
			LOG.info("Ignore system property settings.");
		}
	}
}
