package tamtam.mooney.global.openai;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class OpenAIConfig {
    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
}
