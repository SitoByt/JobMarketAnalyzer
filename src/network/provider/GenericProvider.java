package network.provider;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import data.applicant.Criteria;
import data.job.Job;
import network.ApiProvider;

public class GenericProvider implements Provider {
	
	private ApiProvider config;
	
	public GenericProvider(ApiProvider config) {
		this.config = config;
	}

	@Override
	public String getName() {
		return config.getName();
	}

	@Override
	public String buildUrl(Criteria criteria, int page, int maxResults) {
		// TODO: Add radius and country (!)
		String template = config.getUrlTemplate();
		template = template.replace("{page}", String.valueOf(page));
		template = template.replace("{id}", config.getAppId() != null ? config.getAppId() : "");
		template = template.replace("{key}", config.getAppKey() != null ? config.getAppKey() : "");
		template = template.replace("{country}", URLEncoder.encode(criteria.getCountry().toLowerCase(), StandardCharsets.UTF_8));
		template = template.replace("{city}", URLEncoder.encode(criteria.getCity() != null ? criteria.getCity() : "", StandardCharsets.UTF_8));
		
		
		template = template.replace("{radius}", String.valueOf(criteria.getRadiusInKm()));
		
		List<String> queryTerms = new ArrayList<>();
		if (criteria.getIndustrySector() != null) {
			if(template.contains("{sector}")) {
				template.replace("{sector}", criteria.getIndustrySector());
			} else {
				queryTerms.add(criteria.getIndustrySector());
			}
		}
		if (criteria.getContractTime() != null) {
			if(template.contains("{contract_time}")) {
				template.replace("{contract_time}", "");// add the search term which we specify in the config, if existent(!)
			} else {
				queryTerms.add(criteria.getIndustrySector());
			}
		}else {
			template = template.replace("{sector}", "");
		}
		if (criteria.getContractType() != null) {
			if(template.contains("{contract_type}")) {
				template.replace("contract_type",""); // add the search term which we specify in the config, if existent(!)
			} else {
				queryTerms.add(criteria.getIndustrySector());
			}
		}else {
			template = template.replace("{contract_type}", "");
		}
        if (criteria.getKeywords() != null) {
			queryTerms.addAll(criteria.getKeywords());
		}
        template = template.replace("{keywords}", URLEncoder.encode(String.join(" ", queryTerms), StandardCharsets.UTF_8));
		
        return template;
	}

	@Override
	public List<Job> parseResponse(String jsonResponse) {
		List<Job> jobs = new ArrayList<>();
		if (jsonResponse == null || jsonResponse.isEmpty()) { return jobs; }
		
		try  {
			JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
			Map<String, String> mapping = config.getMapping();
			
			String arrayKey = mapping.getOrDefault("resultArray", "results");
			if(!root.has(arrayKey)) { return jobs; }
			
			JsonArray results = root.getAsJsonArray(arrayKey);
			for(JsonElement element : results) {
				JsonObject item = element.getAsJsonObject();
				String id = extractNestedField(item, mapping.get("id"));
				String title = extractNestedField(item, mapping.get("title"));
                String company = extractNestedField(item, mapping.get("company"));
                String description = extractNestedField(item, mapping.get("description"));
                String url = extractNestedField(item, mapping.get("url"));
                String location = extractNestedField(item, mapping.get("location"));
                
                Job job = new Job(url, title, company, description);
                job.setLocation(location);
                
                String minSalary = extractNestedField(item, mapping.get("salary"));
                job.setSalary(minSalary, null, null);
                
                jobs.add(job);
			}
			
		} catch(Exception e) {
			System.err.println("Parsing error for " + getName() + ": " + e.getMessage());
		}
		return jobs;		
	}

	private String extractNestedField(JsonObject obj, String path) {
		if (path == null || path.isEmpty()) {
			return null;
		}
        String[] parts = path.split("\\.");
        JsonElement current = obj;
        
        for (String part : parts) {
            if (current == null || !current.isJsonObject()) {
				return null;
			}
            current = current.getAsJsonObject().get(part);
        }
        return current != null ? current.getAsString() : null;
    }

}
