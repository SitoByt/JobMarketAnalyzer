package excecute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import data.JobSearchDB;
import data.applicant.Criteria;
import data.job.Job;
import network.NetworkManager;
import network.provider.Provider;

public class SearchEngine {

	private NetworkManager networkManager;
	
	public SearchEngine() {
		networkManager = new NetworkManager();
	}
	
	public List<Job> executeSearch(List<Provider> providers, List<Criteria> criteriaList) {
		List<Job> foundJobs = new ArrayList<>();
		
		for(Provider provider : providers) {
			System.out.println("Searching on: " + provider.getName() + "\n");
			for(Criteria criteria : criteriaList) {
				String url = provider.buildUrl(criteria, 100);
				String rawJson = networkManager.fetchRawJson(url);
				if (rawJson != null) {
					List<Job> jobs = provider.parseResponse(rawJson);
					foundJobs.addAll(jobs);
				}
			}
		}
		return foundJobs;
	}

	public List<Job> filterForNewJobs(List<Job> foundJobs, JobSearchDB database) {
		Map<String, List<Job>> splitJobs = Job.splitNewAndOldResults(foundJobs, database);
		List<Job> newJobs = splitJobs.getOrDefault("NEW", new ArrayList<>());
		
		System.out.println("Of " + foundJobs.size() + " found Jobs, found " + newJobs.size() + " new Jobs.");
		return newJobs;
	}
}
