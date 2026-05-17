package com.example.demo.contract;

import com.example.demo.DemoApplication;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base class for Spring Cloud Contract tests.
 * Configures RestAssuredMockMvc with the Spring WebApplicationContext.
 * Seeds test data and cleans up before each test.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Sql(scripts = {"/sql/cleanup/clean-up.sql", "/sql/init/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class ContractBaseTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    /** Sets up RestAssuredMockMvc before each contract test. */
    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(
                MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        );
    }
}
