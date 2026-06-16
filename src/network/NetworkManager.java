package network;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NetworkManager {

	public String fetchRawJson(String targetUrl) {
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(new URI(targetUrl))
					.GET()
					.build();	
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return response.body();
			
		} catch (IOException | InterruptedException | URISyntaxException e)  {
			System.err.println("Exception during API-Request: " + e.getMessage());
			return null;
		}
	}
	
}
