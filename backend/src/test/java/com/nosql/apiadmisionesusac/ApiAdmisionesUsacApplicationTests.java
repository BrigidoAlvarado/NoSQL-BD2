package com.nosql.apiadmisionesusac;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "MONGODB_USERNAME=test",
        "MONGODB_PASSWORD=test",
        "NEO4J_PASSWORD=test"
})
class ApiAdmisionesUsacApplicationTests {

    @Test
    void contextLoads() {
    }

}
