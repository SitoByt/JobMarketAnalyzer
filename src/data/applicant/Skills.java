package data.applicant;

import java.util.Map;

public class Skills {
	
	private Map<String, String> programmingSkills;
	private Map<String, String> linguisticSkills;
	private Map<String, String> officeSkills;
	
	public Skills() {
		
	}

	public Map<String, String> getProgrammingSkills() {
		return programmingSkills;
	}

	public void setProgrammingSkills(Map<String, String> programmingSkills) {
		this.programmingSkills = programmingSkills;
	}

	public Map<String, String> getLinguisticSkills() {
		return linguisticSkills;
	}

	public void setLinguisticSkills(Map<String, String> linguisticSkills) {
		this.linguisticSkills = linguisticSkills;
	}

	public Map<String, String> getOfficeSkills() {
		return officeSkills;
	}

	public void setOfficeSkills(Map<String, String> officeSkills) {
		this.officeSkills = officeSkills;
	}
	
}
