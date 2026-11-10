package com.guseva.conference_system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ConferenceSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
