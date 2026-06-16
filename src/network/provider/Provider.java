package network.provider;

import java.util.List;

import data.Job;
import data.applicant.Criteria;

public interface Provider {
	String getName();
	String buildUrl(Criteria criteria, int maxResults);
	List<Job> parseResponse(String jsonResponse);
}
