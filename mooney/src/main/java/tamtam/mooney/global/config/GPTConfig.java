package tamtam.mooney.global.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Getter
@Configuration
//@ConfigurationProperties(prefix = "server.openai.api", ignoreUnknownFields = false)
public class GPTConfig {
    // application.yml에서 값 주입
    @Value("${server.openai.api.key}")
    private String key;

    @Value("${server.openai.api.url}")
    private String url;

    @Value("${server.openai.model}")
    private String model;

    @PostConstruct
    public void logConfig() {
        System.out.println("OpenAI API Key: " + key);
        System.out.println("OpenAI API URL: " + url);
        System.out.println("OpenAI Model: " + model);
    }
//    public void setKey(String key) {
//        this.key = key;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }

    @Bean
    public RestTemplate template(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + key);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
