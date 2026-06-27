package excecute.example;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import data.applicant.Criteria;
import data.job.ContractTime;
import data.job.ContractType;
import data.job.Job;
import io.IOManager;
import network.ApiProvider;
import network.NetworkManager;
import network.provider.AdzunaProvider;
import network.provider.Provider;

public class IOExample {
	
	public static void main() {
		Criteria myCriteria = setUpExampleCriteria();
		
		try {
			JsonObject root = JsonParser.parseReader(new FileReader("dataa/api_providers.json")).getAsJsonObject();
            JsonArray providersArray = root.getAsJsonArray("providers");
            
            // 1. SCHRITT: Rohe JSON-Daten in die ApiProvider-Datencontainer einlesen
            Type listType = new TypeToken<List<ApiProvider>>(){}.getType();
            List<ApiProvider> configList = new Gson().fromJson(providersArray, listType);
            
            // 2. SCHRITT: Die echten, funktionalen Logik-Klassen (Provider) bauen
            List<Provider> activeProviders = new java.util.ArrayList<>();
            for(ApiProvider config : configList) {
                if(config.getName().equalsIgnoreCase("Adzuna")) {
                    activeProviders.add(new AdzunaProvider(config)); 
                }
                // Hier kommen später weitere Provider rein (z.B. else if Indeed...)
            }
            
            NetworkManager analyzer = new NetworkManager();
            for (Provider provider : activeProviders) {
                System.out.println("Start Job-Search for Provider: " + provider.getName() + "...");
                String url = provider.buildUrl(myCriteria, 100);
                System.out.println("Seraching: " + url);
                String rawJson = analyzer.fetchRawJson(url);
                List<Job> foundJobs = provider.parseResponse(rawJson); 
                System.out.println(foundJobs.size() + " jobs found at " + provider.getName());
                List<Job> sortedJobs = Job.sortBySalaryDesc(
                		Job.sortByMostRecent(
                		Job.sortByContract(
                		foundJobs, ContractType.CONTRACT, ContractTime.STUDENT))
                );
                
                // 5. In eine Markdown-Datei schreiben
                IOManager.setFilePrefix(provider.getName());
                IOManager.exportToMarkdown(sortedJobs);
            }
		} catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
	}

	private static Criteria setUpExampleCriteria() {
		var criteria = new Criteria();
		criteria.setLocation("de", "Ulm", 15);
		//criteria.setContractType(ContractType.CONTRACT);
		//criteria.setContractTime(ContractTime.PART_TIME);
		//criteria.setDesiredSalary(15);
		criteria.setIndustrySector("Informatik");
		criteria.setKeywords(List.of());
		return criteria;
	}

}
