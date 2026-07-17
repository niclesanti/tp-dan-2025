package edu.utn.frsf.isi.dan.reservas_svc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@Disabled("Disabled in unit-test-focused suite")
class ReservasSvcApplicationTests {

	@Container
	static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7");

	@Container
	static RabbitMQContainer rabbitContainer =
			new RabbitMQContainer("rabbitmq:3-management-alpine");

	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri",
				() -> mongoContainer.getConnectionString() + "/reservas");
		registry.add("spring.rabbitmq.host", rabbitContainer::getHost);
		registry.add("spring.rabbitmq.port",
				() -> rabbitContainer.getMappedPort(5672));
		registry.add("spring.rabbitmq.username", () -> "guest");
		registry.add("spring.rabbitmq.password", () -> "guest");
	}

	@Test
	void contextLoads() {
	}

}
