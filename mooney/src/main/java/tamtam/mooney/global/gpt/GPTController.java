package tamtam.mooney.global.gpt;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import tamtam.mooney.global.config.GPTConfig;
import tamtam.mooney.global.gpt.dto.ChatGPTRequest;
import tamtam.mooney.global.gpt.dto.ChatGPTResponse;

@RestController
@RequiredArgsConstructor //final 붙은 것만 생성자 생성, 작업 순서가 필드 선언 -> 생성자 생성
@RequestMapping("/gpt")
public class GPTController {
    private final GPTService GPTService;
    private final GPTConfig gptConfig;

    @Autowired
    private RestTemplate template;

    private String apiURL;
    private String apiKey;
    private String model;

    @PostConstruct
    public void initialize() {
        this.apiURL = gptConfig.getUrl();
        this.apiKey = gptConfig.getKey();
        this.model = gptConfig.getModel();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(name = "prompt")String prompt){
        ChatGPTRequest request = new ChatGPTRequest(model, prompt);
        ChatGPTResponse chatGPTResponse =  template.postForObject(apiURL, request, ChatGPTResponse.class);
        return chatGPTResponse.getChoices().get(0).getMessage().getContent();
    }

}

