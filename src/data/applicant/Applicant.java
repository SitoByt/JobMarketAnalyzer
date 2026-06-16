package data.applicant;

import java.time.LocalDate;
import java.time.Period;

public class Applicant {
	
	private String name;
	private LocalDate birthDate;
	
	private String Degree;
	private String currentOccupation;	
	
	private Contact contact;
	private Skills skills;
	private Criteria criteria;
	
	public Applicant(String name, LocalDate birthDate) {
		this.name = name;
		this.birthDate = birthDate;
	}
	
	
	// getters and Setters
	public String getName() {
		return name;
	}
	
	public int getAge() {
		return Period.between(birthDate, LocalDate.now()).getYears();
	}
	
	public Criteria getCriteria() {
		return criteria;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	public String getDegree() {
		return Degree;
	}

	public void setDegree(String degree) {
		Degree = degree;
	}

	public String getCurrentOccupation() {
		return currentOccupation;
	}

	public void setCurrentOccupation(String currentOccupation) {
		this.currentOccupation = currentOccupation;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public Skills getSkills() {
		return skills;
	}

	public void setSkills(Skills skills) {
		this.skills = skills;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

}
