package tamtam.mooney.service.aiService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tamtam.mooney.global.config.GPTConfig;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;


@Service
@RequiredArgsConstructor
public class GPTService {
    private final GPTConfig gptConfig;
    //private final RestTemplate restTemplate;

    String apiUrl = gptConfig.url;
    String apiKey = gptConfig.key;


    public void testOpenAiConnection() {
        try {

            if (apiUrl == null || apiKey == null) {
                System.out.println("API URL 또는 API Key가 설정되지 않았습니다.");
                if(apiUrl != null){
                    System.out.println("apiurl은 존재합니다.");
                }
                else if(apiKey != null){
                    System.out.println("apiKey는 존재합니다.");
                }
                else{
                    System.out.println("apikey와 url을 모두 가져오지 못했습니다. ");
                }
                return;
            }

            // Test Prompt
            String systemContent = "You are a helpful assistant.";
            String userContent = "Hello!";

            // Create the JSON payload
            String jsonPayload = """
            {
              "model": "o1-mini-2024-09-12",
              "messages": [
                {
                  "role": "system",
                  "content": "%s"
                },
                {
                  "role": "user",
                  "content": "%s"
                }
              ],
              "max_tokens": 50
            }
        """.formatted(systemContent, userContent);

            // Set up the connection
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            // Send the request
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(jsonPayload.getBytes());
                outputStream.flush();
            }

            // Read the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println("OpenAI API Response: " + response);
                }
            } else {
                System.out.println("OpenAI API Error: HTTP " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
