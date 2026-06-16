package network.provider;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import data.ContractTime;
import data.ContractType;
import data.Job;
import data.applicant.Criteria;
import network.ApiProvider;

public class AdzunaProvider implements Provider {
	
	private ApiProvider config;

	public AdzunaProvider(ApiProvider config) {
		this.config = config;
	}
	
	@Override
	public String getName() {
		return config.getName();
	}

	@Override
	public String buildUrl(Criteria criteria, int maxResults) {
		StringBuilder url = new StringBuilder(config.getUrlTemplate());
		List<String> terms = new ArrayList<>();
		
		url.append(1).append("?app_id=").append(config.getAppId())
			.append("&app_key=").append(config.getAppKey())
			.append("&results_per_page=").append(maxResults);
		
		if(criteria.getCity() != null && !criteria.getCity().isEmpty()) {
			url.append("&where=").append(URLEncoder.encode(criteria.getCity(), StandardCharsets.UTF_8));
			if (criteria.getRadiusInKm() > 0) {
				url.append("&distance=").append(criteria.getRadiusInKm());
			}
		}
		
		if (criteria.getContractTime() != null) {
			switch(criteria.getContractTime()) {
				case FULL_TIME:
					url.append("&full_time=1");
					break;
				case PART_TIME:
					url.append("&part_time=1");
					break;
				case STUDENT:
					url.append("&part_time=1");
					terms.add("student");
			}
		}
		
		if (criteria.getContractType() != null) {
			switch(criteria.getContractType()) {
				case CONTRACT:
					url.append("&contract=1");
					break;
				case PERMANENT:
					url.append("&permanent=1");
			}
		}
		
		
		if(criteria.getIndustrySector() != null && !criteria.getIndustrySector().isEmpty()) {
			terms.add(criteria.getIndustrySector());
		}
        if(criteria.getKeywords() != null) {
			terms.addAll(criteria.getKeywords());
		}
        
        if (!terms.isEmpty()) {
            String what = URLEncoder.encode(String.join(" ", terms), StandardCharsets.UTF_8);
            url.append("&what=").append(what);
        }
		
		
		return url.toString();
	}

	@Override
	public List<Job> parseResponse(String jsonResponse) {
		List<Job> jobs = new ArrayList<Job>();
		if (jsonResponse == null || jsonResponse.isEmpty()) {return jobs;}
		
		try {
			JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
			if(!root.has("results")) { return jobs; }
			
			JsonArray results = root.getAsJsonArray("results");
			
			for(JsonElement element : results) {
				JsonObject jobJson = element.getAsJsonObject();
				
				String id = jobJson.get("id").getAsString();
				String title = jobJson.get("title").getAsString();
				String description = jobJson.get("description").getAsString();
				String rawDate = jobJson.get("created").getAsString();
				Instant created = null;
				if(rawDate != null && !rawDate.isBlank()) {
					try {
						created = Instant.parse(rawDate);
					} catch (Exception e) {
						System.err.println("Couldn't parse date: " + rawDate);
					}
				}
				
				String company = (jobJson.has("company") && jobJson.getAsJsonObject("company").has("display_name"))?
						jobJson.getAsJsonObject("company").get("display_name").getAsString() : null;
				String location = (jobJson.has("location") && jobJson.getAsJsonObject("location").has("display_name")) ?
						jobJson.getAsJsonObject("location").get("display_name").getAsString() : null;
				
				String minSalary = jobJson.has("salary_min") ? jobJson.get("salary_min").getAsString() : null;
				String maxSalary = jobJson.has("salary_max") ? jobJson.get("salary_max").getAsString() : null;
				String predictedSalary = jobJson.has("salary_is_predicted") ? jobJson.get("salary_is_predicted").getAsString() : null;
				
				String rawContractTime = jobJson.has("contract_time") ? jobJson.get("contract_time").getAsString() : "";
				ContractTime contractTime = null;
				if (rawContractTime.contains("full_time")) {
					contractTime = ContractTime.FULL_TIME;
				} else if (rawContractTime.contains("part_time")) {
					contractTime = ContractTime.PART_TIME;
				}

				String rawContractType = jobJson.has("contract_type") ? jobJson.get("contract_type").getAsString() : "";
				ContractType contractType = null;
				if (rawContractType.contains("permanent")) {
					contractType = ContractType.PERMANENT;
				} else if (rawContractType.contains("contract")) {
					contractType = ContractType.CONTRACT;
				}
				
				String url = jobJson.has("redirect_url")? jobJson.get("redirect_url").getAsString() : null;
				
				Job job = new Job(id, url, title, company, description);
				job.setContract(contractTime, contractType);
				job.setSalary(minSalary, maxSalary, predictedSalary);
				job.setLocation(location);
				job.setCreated(created);
				
				jobs.add(job);
			}
		} catch (Exception e) {
			System.err.println("Fehler beim Parsen von Adzuna: " + e.getMessage());
		}
		
		return jobs;
	}
	
	
	
}
