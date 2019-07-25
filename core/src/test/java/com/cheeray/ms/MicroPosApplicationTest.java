package com.cheeray.ms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MsApplication.class, value = {
		"spring.cloud.bus.enabled=false", "spring.cloud.consul.config.enabled=false",
		"spring.cloud.discovery.enabled=false", "server.port = 9977",
		"spring.application.config.enabled=false", "spring.cloud.consul.enabled=false"
}, webEnvironment = WebEnvironment.DEFINED_PORT)
public class MicroPosApplicationTest {

	@Mock
	private ServiceInstance instance;

	@MockBean
	private SimpleDiscoveryClient discoveryClient;

	@MockBean
	private LoadBalancerClient loadBalancer;

	@Autowired
	private TestRestTemplate template;

	@Test
	public void testHealth() {
		String body = this.template.getForObject("/actuator/health", String.class);
		assertThat(body).isEqualTo("{\"status\":\"UP\"}");
	}

	@Test
	public void testInfo() {
		String body = this.template.getForObject("/actuator/info", String.class);
		assertThat(body).isEqualTo("{}");
	}

	@Test
	@Ignore
	public void testSelf() {
		given(this.discoveryClient.getInstances(Mockito.anyString()))
				.willReturn(Arrays.asList(instance));
		String body = this.template.getForObject("/self", String.class);
		assertThat(body).isEqualTo("{}");
	}

	@Test
	@Ignore
	public void testLb() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testInstances() {
		fail("Not yet implemented");
	}

}
