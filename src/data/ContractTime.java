package data;

public enum ContractTime {
	PART_TIME, FULL_TIME, STUDENT;
	
	public static String toString(ContractTime time){
		if (time == null) {
			return "";
		}
		
		return switch(time) {
			case PART_TIME -> "Part Time";
			case FULL_TIME -> "Full Time";
			case STUDENT -> "Student Job";
			default -> "";
		};		
	}
}
