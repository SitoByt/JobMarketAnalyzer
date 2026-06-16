package data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Job {
	private String id;		
	private String title;
	private String company;	
	
	private Instant created;
	private String location;
	
	private String minSalary;
	private String maxSalary;
	private String predictedSalary;

	private ContractType contractType; // contract/permanent 
	private ContractTime contractTime; // full_time/part_time

	private String url;			
	private String description; 
	
	// private List<String> requestedSkills;
	
	public Job(String id, String url, String title, String company, String description) {
		this.id = id;
		this.title = title;
		this.company = company;
		this.url = url;
		this.description = description;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	public void setCreated(Instant date) {
		created = date;
	}
	
	public void setSalary(String min, String max, String predicted) {
		this.minSalary = min;
		this.maxSalary = max;
		this.predictedSalary = predicted;
	}
	
	public void setContract(ContractTime contractTime, ContractType contractType) {
		this.contractTime = contractTime;
		this.contractType = contractType;
	}

	
	// prints the information of the company in a pre-defined, readable format
	public String getHeader() {
		String header = title;
		if(hasData(company)) {header += " (" + company + ") ";}
		return header;
	}

	public String getBody() {		
		String link = "\nLink: " + url;
		
		String salary = ", \nSalary offered: " ;
		if(!hasData(minSalary) && !hasData(maxSalary) && !hasData(predictedSalary)) {salary += "Unspecified";}
		else {
			if(hasData(minSalary)) {salary += "\t min:" + minSalary;}
			if(hasData(maxSalary)) {salary += "\t max:" + maxSalary;}
			if(hasData(predictedSalary)) {salary += "\t estimated:" + predictedSalary;}
		}
		
		String located = hasData(location) ? ", \nIn: " + location : "";
		
		String published = "";
		if(created != null) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'at' HH:mm")
						.withZone(ZoneId.systemDefault());
				published = ", \nPublished On: " + formatter.format(created);
			} catch (Exception e) {
				System.err.println("couldn't read the published date");
			}
			 
		}
		
		String contract = ", \nContract: \t" + ContractTime.toString(contractTime) 
						+ "   \t" + ContractType.toString(contractType);
		
		String descr = "\n\nDescription: " + description;
		
		return (located + published + salary + contract + descr + link);
	}
	
	// Sorts the Jobs based on Salary (based on min.Salary, otherwise predicted Salary)
	public static List<Job> sortBySalaryDesc(List<Job> jobs) {
		return jobs.stream()
				.sorted(Comparator.comparingDouble(Job::getSalary).reversed())
				.collect(Collectors.toList());
	}
	
	public static List<Job> sortByMostRecent(List<Job> jobs) {
		return jobs.stream()
				.sorted(Comparator.comparing(Job::getCreated, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
				.collect(Collectors.toList());
	}
	
	public static List<Job> sortByContract(List<Job> jobs, ContractType contractType, ContractTime contractTime) {
		return jobs.stream()
				.sorted(Comparator.comparing((Job job) -> job.getContractTime() == contractTime).reversed()
			    .thenComparing(Comparator.comparing((Job job) -> job.getContractType() == contractType).reversed()))
				.collect(Collectors.toList());
	}
	
	// Map all Job offers depending on Company
	public static Map<String, List<Job>> mapCompaniesToJobs(List<Job> jobs){
		return jobs.stream()
				.collect(Collectors.groupingBy(
						job -> job.hasData(job.getCompany()) ? job.getCompany() : "no company"
					));
	}

	// filter the first offer of each company in the list
	public static List<Job> getCompaniesFirstOffer(List<Job> jobs) {
		Set<String> companySet = new HashSet<>();
		List<Job> uniqueJobs = jobs.stream()
				.filter(job -> {
					if(!job.hasData(job.getCompany())) {return true;}
					return companySet.add(job.getCompany());
				})
				.collect(Collectors.toList());
		uniqueJobs.sort(Comparator.comparing(job -> !job.hasData(job.getCompany())));
		return uniqueJobs;
	}
	
	public double getSalary() {
		try {
			if(hasData(minSalary)) {
				return Double.parseDouble(minSalary);
			}
			if (hasData(predictedSalary)) {
				return Double.parseDouble(predictedSalary);
			}
		} catch (NumberFormatException e) {
			
		}
		return 0.0;
	}
	
	
	// helper method -> no data provided
	private boolean hasData(String value) {
		return !(value == null || value.isBlank());
	}
	
	// getters

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getCompany() {
		return company;
	}

	public Instant getCreated() {
		return created;
	}

	public String getLocation() {
		return location;
	}

	public String getMinSalary() {
		return minSalary;
	}

	public String getMaxSalary() {
		return maxSalary;
	}

	public String getPredictedSalary() {
		return predictedSalary;
	}


	public String getUrl() {
		return url;
	}

	public String getDescription() {
		return description;
	}

	public ContractType getContractType() {
		return contractType;
	}

	public ContractTime getContractTime() {
		return contractTime;
	}
}
