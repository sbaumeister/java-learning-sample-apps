package io.github.sbaumeister.productcrawler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ProductCrawlerAppTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    public void blub() {
        String result = testRestTemplate.getForObject("/", String.class);
        assertNotNull(result);
    }
}