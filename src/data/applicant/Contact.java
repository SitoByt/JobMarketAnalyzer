package data.applicant;

public class Contact {
	private String email;
	private String phoneNumber;
	private String residence;
	
	public Contact(String email, String phoneNumber, String residence) {
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.residence = residence;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getResidence() {
		return residence;
	}
	public void setResidence(String residence) {
		this.residence = residence;
	}
}
