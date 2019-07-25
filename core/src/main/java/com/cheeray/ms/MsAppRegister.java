package com.cheeray.ms;

import static org.springframework.boot.actuate.health.Status.OUT_OF_SERVICE;
import static org.springframework.boot.actuate.health.Status.UP;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.ReflectionUtils;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.health.model.Check;

/**
 * Service register and deregister.
 * <p>
 * Provision registration with given <code>RegisterInfo</code>.
 * </p>
 * 
 * @author Chengwei.Yan
 * @see RegisterInfo
 */
@Configuration
@ConditionalOnConsulEnabled
@AutoConfigureBefore(ServiceRegistryAutoConfiguration.class)
@ConditionalOnBean(RegisterInfo.class)
public class MsAppRegister {
	private static Logger LOG = LoggerFactory.getLogger(MsAppRegister.class);

	@Autowired(required = false)
	private TtlScheduler ttlScheduler;

	@Autowired(required = true)
	private RegisterInfo info;

	@Value("${spring.application.name:MicroApp}")
	private String appName;

	@Value("${server.servlet.context-path:/api}")
	private String contextPath;

	@Bean
	@ConditionalOnMissingBean
	public ConsulAutoServiceRegistration consulAutoServiceRegistration(ConsulServiceRegistry registry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties, ConsulDiscoveryProperties properties,
			ConsulAutoRegistration consulRegistration) {
		return new ConsulAutoServiceRegistration(registry, autoServiceRegistrationProperties, properties,
				consulRegistration);
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulAutoRegistration consulRegistration(
			AutoServiceRegistrationProperties autoServiceRegistrationProperties, ConsulDiscoveryProperties properties,
			List<ConsulRegistrationCustomizer> customizers, ApplicationContext context,
			HeartbeatProperties heartbeatProperties) {
		return ConsulAutoRegistration.registration(autoServiceRegistrationProperties, properties, context, customizers,
				heartbeatProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	public List<ConsulRegistrationCustomizer> consulRegistrationCustomizer(ConsulDiscoveryProperties properties) {
		final ConsulRegistrationCustomizer customizer = new ConsulRegistrationCustomizer() {
			@Override
			public void customize(ConsulRegistration registration) {
				final NewService service = registration.getService();
				List<String> tags = service.getTags();
				if (tags == null) {
					tags = new ArrayList<>();
				}
				tags.addAll(info.getTags());
				tags.add("contextPath=" + contextPath);
				service.setTags(tags);
			}
		};
		final List<ConsulRegistrationCustomizer> list = new ArrayList<>();
		list.add(customizer);
		return list;
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulServiceRegistry consulServiceRegistry(ConsulClient consulClient, ConsulDiscoveryProperties properties,
			HeartbeatProperties heartbeatProperties) {

		properties.setHostname(info.getHostName());

		properties.setInstanceGroup(info.getInstanceGroup());
		properties.setInstanceId(info.getInstanceId());
		properties.setDefaultQueryTag(info.getDefaultQueryTag());
		properties.setServiceName(info.getServiceName());
		properties.setServerListQueryTags(info.getServerListQueryTags());
		properties.setTags(info.getTags());
		// return new ConsulServiceRegistry(consulClient, properties, ttlScheduler,
		// heartbeatProperties);
		return new CatalogRegistry(consulClient, properties, ttlScheduler, heartbeatProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty("spring.cloud.consul.discovery.heartbeat.enabled")
	public TtlScheduler ttlScheduler(ConsulClient consulClient, HeartbeatProperties heartbeatProperties) {
		return new TtlScheduler(heartbeatProperties, consulClient);
	}

	@Bean
	@ConditionalOnMissingBean
	public HeartbeatProperties heartbeatProperties() {
		return new HeartbeatProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulDiscoveryProperties consulDiscoveryProperties(InetUtils inetUtils) {
		return new ConsulDiscoveryProperties(inetUtils);
	}

	@EventListener
	public void onUp(ApplicationReadyEvent e) {
		if (e.getApplicationContext() instanceof ServletWebServerApplicationContext) {
			LOG.info(e.toString());

		}
		// LOG.error("Application failed to start. Process will exit.");
		// System.exit(99);
	}

	@EventListener
	public void onDown(ContextClosedEvent e) {
		if (e != null) {
			ConsulServiceRegistry registry = e.getApplicationContext()
				.getBean(ConsulServiceRegistry.class);
			if (registry != null) {
				ConsulAutoRegistration reg = e.getApplicationContext()
					.getBean(ConsulAutoRegistration.class);
				if (reg != null) {
					LOG.info("Deregister service ...");
					registry.deregister(reg);
				}
			}
		}
	}

	private static final class CatalogRegistry extends ConsulServiceRegistry {

		private final ConsulClient client;

		private final ConsulDiscoveryProperties properties;

		private final TtlScheduler ttlScheduler;

		private final HeartbeatProperties heartbeatProperties;

		public CatalogRegistry(ConsulClient client, ConsulDiscoveryProperties properties, TtlScheduler ttlScheduler,
				HeartbeatProperties heartbeatProperties) {
			super(client, properties, ttlScheduler, heartbeatProperties);
			this.client = client;
			this.properties = properties;
			this.ttlScheduler = ttlScheduler;
			this.heartbeatProperties = heartbeatProperties;
		}

		@Override
		public void register(ConsulRegistration reg) {
			final NewService service = reg.getService();
			LOG.info("Registering service:{} with id:{}. ", service, service.getId());
			try {
				client.agentServiceRegister(service, properties.getAclToken());
				//@formatter:off
				/*
				 * CatalogRegistration cr = new CatalogRegistration();
				 * final Map<String, String> qt = properties.getServerListQueryTags();
				 * cr.setNode(qt.get("company"));
				 * cr.setAddress(reg.getService().getAddress());
				 * Service srv = new Service();
				 * srv.setId(reg.getInstanceId());
				 * srv.setService(reg.getService().getId());
				 * srv.setAddress(reg.getService().getAddress());
				 * srv.setPort(reg.getService().getPort());
				 * srv.setTags(reg.getService().getTags());
				 * cr.setService(srv );
				 * client.catalogRegister(cr);
				 */
				//@formatter:on
				if (heartbeatProperties.isEnabled() && ttlScheduler != null) {
					ttlScheduler.add(reg.getInstanceId());
				}
			} catch (ConsulException e) {
				if (this.properties.isFailFast()) {
					LOG.error("Error registering service with consul: " + service, e);
					ReflectionUtils.rethrowRuntimeException(e);
				}
				LOG.warn("Failfast is false. Error registering service with consul: " + service, e);
			}
		}

		@Override
		public void deregister(ConsulRegistration reg) {
			if (ttlScheduler != null) {
				ttlScheduler.remove(reg.getInstanceId());
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("Deregistering service with consul: " + reg.getInstanceId());
			}
			client.agentServiceDeregister(reg.getInstanceId());
		}

		@Override
		public void close() {

		}

		@Override
		public void setStatus(ConsulRegistration registration, String status) {
			if (status.equalsIgnoreCase(OUT_OF_SERVICE.getCode())) {
				client.agentServiceSetMaintenance(registration.getInstanceId(), true);
			} else if (status.equalsIgnoreCase(UP.getCode())) {
				client.agentServiceSetMaintenance(registration.getInstanceId(), false);
			} else {
				throw new IllegalArgumentException("Unknown status: " + status);
			}

		}

		@Override
		public Object getStatus(ConsulRegistration registration) {
			String serviceId = registration.getServiceId();
			Response<List<Check>> response = client.getHealthChecksForService(serviceId, QueryParams.DEFAULT);
			List<Check> checks = response.getValue();

			for (Check check : checks) {
				if (check.getServiceId()
					.equals(registration.getInstanceId())) {
					if (check.getName()
						.equalsIgnoreCase("Service Maintenance Mode")) {
						return OUT_OF_SERVICE.getCode();
					}
				}
			}

			return UP.getCode();
		}

	}
}
