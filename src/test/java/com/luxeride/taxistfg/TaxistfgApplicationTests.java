package com.luxeride.taxistfg;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

// necesita mysql real y las env vars (DB_PASSWORD, JWT_SECRET, MAIL_USERNAME, MAIL_PASSWORD),
// se salta solo si no hay DB_PASSWORD (ej. este sandbox); en CI con la DB levantada corre normal
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class TaxistfgApplicationTests {

    @Test
    void contextLoads() {
    }

}
