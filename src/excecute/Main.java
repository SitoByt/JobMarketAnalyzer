package excecute;

import data.JobSearchDB;

public class Main {
	public static void main(String[] args) {
		JobSearchDB db = JobSearchDB.getJobSearchDB();
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> db.close()));   // close the database connection when the program ends (!)
		
		JobMarketCommandLine cmdl = new JobMarketCommandLine();
		cmdl.start();
	
	
	}
}
