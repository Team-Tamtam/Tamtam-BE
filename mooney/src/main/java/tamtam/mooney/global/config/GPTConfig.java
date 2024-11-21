package tamtam.mooney.global.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "server.openai.api", ignoreUnknownFields = false)
public class GPTConfig {
    public String key;
    public String url;

    @PostConstruct
    public void logConfig() {
        System.out.println("OpenAI API Key: " + key);
        System.out.println("OpenAI API URL: " + url);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
