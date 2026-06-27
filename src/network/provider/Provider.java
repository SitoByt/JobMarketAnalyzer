package network.provider;

import java.util.List;

import data.applicant.Criteria;
import data.job.Job;

public interface Provider {
	String getName();
	String buildUrl(Criteria criteria, int page, int maxResults);
	List<Job> parseResponse(String jsonResponse);
}
