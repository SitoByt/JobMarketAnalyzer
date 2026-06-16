package data.applicant;

import java.util.List;

import data.ContractTime;
import data.ContractType;

public class Criteria {
	
	private int desiredSalary;	// targeted wage
	
	private String industrySector; // e.g.: IT, Informatik,
	private ContractType contractType; // for e.g.: full-time, part-time, student,...
	private ContractTime contractTime;
	private List<String> keywords;
	
	private String country; // not integrated yet
	private String city;
	private int radiusInKm;
	
	public String getCountry() {
		return country;
	}

	public String getCity() {
		return city;
	}

	public int getRadiusInKm() {
		return radiusInKm;
	}

	public Criteria() {
		
	}

	public int getDesiredSalary() {
		return desiredSalary;
	}

	public void setDesiredSalary(int salary) {
		this.desiredSalary = salary;
	}

	public ContractType getContractType() {
		return contractType;
	}

	public void setContractType(ContractType contractType) {
		this.contractType = contractType;
	}
	
	

	public String getIndustrySector() {
		return industrySector;
	}

	public void setIndustrySector(String industrySector) {
		this.industrySector = industrySector;
	}

	public void setLocation(String country, String city, int radiusInKm) {
		this.city = city;
		this.country = country;
		this.radiusInKm = radiusInKm;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public ContractTime getContractTime() {
		return contractTime;
	}

	public void setContractTime(ContractTime contractTime) {
		this.contractTime = contractTime;
	}

	public ContractType getEmploymentType() {
		return contractType;
	}
	
	

}
