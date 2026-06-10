package com.devsecops.demo;

import com.devsecops.demo.controller.HomeController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleTest {

    @Test
    void testHealth() {

        HomeController controller =
                new HomeController();

        assertEquals(
                "UP",
                controller.health()
        );
    }

    @Test
    void testWelcome() {

        HomeController controller =
                new HomeController();

        assertEquals(
                "DevSecOps Demo Running",
                controller.welcome()
        );
    }
}
