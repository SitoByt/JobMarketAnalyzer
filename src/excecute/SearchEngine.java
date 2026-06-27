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
				System.out.print("Scanning location: '" + criteria.getCity() + "' -> Pages: ");
				int page = 1;
				int jobsPerCriteria = 0;
				while (true) {
					String url = provider.buildUrl(criteria, page, 100);
					String rawJson = networkManager.fetchRawJson(url);
					if (rawJson == null) {break;}						
					List<Job> jobs = provider.parseResponse(rawJson);
					if(jobs.isEmpty()) {break;}
					foundJobs.addAll(jobs);
					jobsPerCriteria += jobs.size();
					System.out.print(page + " ");
					page++;
					
					if(page > 20) {
						System.out.println("[Stopped at Page 10 - specify your criterias to get better results!");
						break;
					}
					
					// spam prevention!
					try { Thread.sleep(100);} catch(Exception e) {}
				}
				System.out.println("(Found " + jobsPerCriteria + " Jobs)");
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
