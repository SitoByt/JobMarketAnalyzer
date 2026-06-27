package data.job;

public enum ContractType {
	PERMANENT, CONTRACT;
	
	public static String toString(ContractType type){
		if (type == null) {
			return "";
		}
		
		return switch(type) {
			case PERMANENT -> "Permanent";
			case CONTRACT -> "Contract";
			default -> "";
		};		
	}
}