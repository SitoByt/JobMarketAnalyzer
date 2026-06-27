package excecute;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import data.JobSearchDB;
import data.applicant.Criteria;
import data.job.ContractTime;
import data.job.ContractType;
import io.IOManager;
import network.ApiProvider;
import network.provider.AdzunaProvider;
import network.provider.Provider;

public class JobMarketCommandLine {

	private Scanner scanner;
	private JobSearchDB db;
	private SearchEngine searchEngine;
	
	private List<Provider> providers;
	private List<Criteria> criteria;
	
	public JobMarketCommandLine() {		
		scanner = new Scanner(System.in);
		db = JobSearchDB.getJobSearchDB();
		searchEngine = new SearchEngine();
		providers = loadAllProviders();
	}
	
	public void start() {
		clearScreen();
		System.out.println("Welcome to the Job-Market Web Analyzer:\n");
		while(true){
			System.out.println("\nEnter a Number to select an action:");
			System.out.println("[1] Full Scan (all Criteria & Providers) + DB Update");
            System.out.println("[2] Search for custom selected Providers & Criteria");
            System.out.println("[3] Transform Criteria-List");
            System.out.println("[4] Exit");
            System.out.println("Enter your Choice: ");
            String input = scanner.nextLine();
            int choice = -1;
            try {
            	choice = Integer.parseInt(input);
            } catch(NumberFormatException e) {
            	
            }
            clearScreen();
            switch(choice) {
            case 1:
        		fullScan();
        		break;
            case 2:
            	partialScan();
            	break;
            case 3:
            	changeCriteria();
            	break;
            case 4:
            	changeProviders();
            	break;
            case 5:
            	return;
            default:
            	System.out.println("Please enter a valid Number");
            }
		}
	}
	
	private void changeProviders() {
		while(true){
			System.out.println("\nEnter a Number to select an action:");
			System.out.println("[1] Show Providers Only");
            System.out.println("[2] Add a Provider");
            System.out.println("[3] Delete Providers");
            System.out.println("[4] Return");
            System.out.println("Enter your Choice: ");
            String input = scanner.nextLine();
            int choice = -1;
            try {
            	choice = Integer.parseInt(input);
            } catch(NumberFormatException e) {
            	
            }
            clearScreen();
            switch(choice) {
            case 1:
        		printProviders();
        		break;
            case 2:
            	printProviders();
            	addProvider();
            	break;
            case 3:
            	printProviders();
            	deleteProviders();
            	break;
            case 4:
            	return; // returns to main menu
            default:
            	System.out.println("Please enter a valid Number");
            }
		}
	}

	private void deleteProviders() {
		printProviders();
		System.out.println("Enter the providers ids to continue:"
				+ "\n[e.g: '1, 4, 5' ]");
		String deleteInput = scanner.nextLine();
		if (!deleteInput.isEmpty()) {
			String[] arr = deleteInput.split(",");
			List<Integer> ids = Arrays.stream(arr)
					.map(x -> Integer.parseInt(x.strip()))
					.collect(Collectors.toList());
			if(ids.isEmpty()) { return; }
			
			for(int id : ids) {
			    int idx = id - 1;
			    if (idx >= 0 && idx < this.criteria.size()) {
			        int pId = this.criteria.get(idx).getId();
			        String name = this.providers.get(idx).getName();
					try {
						String pathString = "data/api_providers.json";
						Path path = Paths.get(pathString);
						if (Files.exists(path)) {
							System.out.println("couldn't find: " + path.toString());
							return;
						}
						
						JsonObject root = JsonParser.parseReader(new FileReader(pathString)).getAsJsonObject();
						JsonArray providersArray = root.getAsJsonArray("providers");
						JsonArray updatedArray = new JsonArray();
						
						for (JsonElement je : providersArray) {
							if(je.getAsJsonObject().get("name").getAsString().equalsIgnoreCase(name)) {
								updatedArray.add(je);
							}
						}
						root.add("providers", updatedArray);
						Files.writeString(path, new Gson().toJson(root), StandardCharsets.UTF_8);
						
						this.providers = loadAllProviders();
						System.out.println(name + " has been removed");
					} catch (Exception e) {
						System.err.println("Error removing provider: " + name);
					}
			    }
			}
		}
	}

	private void addProvider() {
		// TODO Auto-generated method stub
		
	}

	private void printProviders() {
		this.providers = loadAllProviders();
		System.out.println("Active Providers:");
		for (int i = 0; i < providers.size(); i++) {
			System.out.println("[" + (i + 1) + "] " + providers.get(i).getName());
		}
	}

	private void fullScan() {
		criteria = db.loadAllCriteria();
		if(criteria.isEmpty()) {
			System.out.println("No Criteria specified - create them first.");
			return;
		}
		
		var searchResults = searchEngine.executeSearch(providers, criteria);
		var newJobs = searchEngine.filterForNewJobs(searchResults,  db);
		db.updateJobs(searchResults);
		
		IOManager.setFileName("JobOffers");
		IOManager.exportToMarkdown(searchResults);
		IOManager.setFileName("NewJobOffers");
		IOManager.exportToMarkdown(newJobs);
	}

	private void partialScan() {
		// Load Providers
		this.providers = loadAllProviders();
		for(int i = 0; i < providers.size(); i++) {
			Provider provider = providers.get(i);
			System.out.println("[" + (i+1) + "]" + provider.getName());
		}
		
		// Select Providers
		System.out.println("Select the provider's ids you want to analyze:"
    			+ "\n[e.g: '1, 4, 5' ; or leave Empty to select all]");
		String providerInput = scanner.nextLine().trim();
		List<Provider> providerSelection = new ArrayList<>();
		if (providerInput.isEmpty()) {
			providerSelection = this.providers;
		} else {
			String[] arr =  providerInput.split(",");
	    	List<Integer> ids = Arrays.stream(arr)
	    			.map(x -> Integer.parseInt(x.trim()))
	    			.collect(Collectors.toList());
			providerSelection = loadProviders(ids);
		}
		
		// Do the same for the Criteria
		printCriteria();
		System.out.println("Select the provider's ids you want to analyze:"
    			+ "\n[e.g: '1, 4, 5' ; or leave Empty to select all]");
		String criteriaInput = scanner.nextLine().trim();
		List<Criteria> criteriaSelection = new ArrayList<>();
		if (criteriaInput.isEmpty()) {
			this.criteria = db.loadAllCriteria();
		} else {
			String[] arr =  criteriaInput.split(",");
	    	List<Integer> ids = Arrays.stream(arr)
	    			.map(x -> Integer.parseInt(x.trim()))
	    			.collect(Collectors.toList());
			criteriaSelection = loadCriteria(ids);
		}
		
		
		List<String> providerStrings = providerSelection.stream()
				.map(x -> x.getName())
				.toList();
		String providerString = String.join("_", providerStrings);
		IOManager.setFilePrefix(providerString);
		
		var jobs = searchEngine.executeSearch(providerSelection, criteriaSelection);
		IOManager.exportToMarkdown(jobs);
		System.out.println("Finished Partial Scan - found " + jobs.size() + " Jobs.");
	}

	private List<Criteria> loadCriteria(List<Integer> idxs) {
		List<Criteria> selected = new ArrayList<>();
		for(int num : idxs) {
			int idx = num - 1;
			if (idx >= 0 && idx < this.criteria.size()) {
				selected.add(this.criteria.get(idx));
			}
		}
		return selected;
	}
	
	private List<Provider> loadAllProviders() {
		List<Provider> providers = new ArrayList<>();
		try {
			JsonObject root = JsonParser.parseReader(new FileReader("data/api_providers.json")).getAsJsonObject();
			JsonArray providersArray = root.getAsJsonArray("providers");
			Type listType = new TypeToken<List<ApiProvider>>(){}.getType();
			List<ApiProvider> configList = new Gson().fromJson(providersArray, listType);
			
			for(ApiProvider config : configList) {
				if(config.getName().equals("Adzuna")) {
					providers.add(new AdzunaProvider(config));
				}
			}
		
		} catch (Exception e) {
			System.err.println("Exception when loading Provider: " + e.getMessage());
		}
		return providers;
	}

	private List<Provider> loadProviders(List<Integer> ids) {
		List<Provider> providers = new ArrayList<>();
		for(int id : ids) {
			int idx = id - 1;
			if (idx >= 0 && idx < providers.size()) {
				providers.add(this.providers.get(idx));
			}
		}
		return providers;
	}

	private void changeCriteria() {
		while(true){
			System.out.println("\nEnter a Number to select an action:");
			System.out.println("[1] Show Criteria Only");
            System.out.println("[2] Add Criteria");
            System.out.println("[3] Delete Criteria");
            System.out.println("[4] Return");
            System.out.println("Enter your Choice: ");
            String input = scanner.nextLine();
            int choice = -1;
            try {
            	choice = Integer.parseInt(input);
            } catch(NumberFormatException e) {
            	
            }
            clearScreen();
            switch(choice) {
            case 1:
        		printCriteria();
        		break;
            case 2:
            	printCriteria();
            	addCriteria();
            	break;
            case 3:
            	printCriteria();
            	deleteCriteria();
            	break;
            case 4:
            	return; // returns to main menu
            default:
            	System.out.println("Please enter a valid Number");
            }
		}
	}

	private void deleteCriteria() {
		System.out.println("Enter the criterias ids to continue:"
				+ "\n[e.g: '1, 4, 5' ]");
		String deleteInput = scanner.nextLine();
		if (!deleteInput.isEmpty()) {
			String[] arr = deleteInput.split(",");
			List<Integer> ids = Arrays.stream(arr)
					.map(x -> Integer.parseInt(x.strip()))
					.collect(Collectors.toList());
			deleteCriteria(ids);
		}
	}
	
	private void deleteCriteria(List<Integer> ids) {
		if(ids.isEmpty()) { return; }
		
		for(int id : ids) {
		    int idx = id - 1;
		    if (idx >= 0 && idx < this.criteria.size()) {
		        int dbId = this.criteria.get(idx).getId();
		        db.deleteCriteria(dbId);
		    }
		}
	}

	private void addCriteria() {
		while (true){
			System.out.println("Create a new Criteria: ");
			Criteria criteria = new Criteria();
			
			System.out.println("Country [abreviated] (e.g. US, DE, UK,...): ");
			String country = scanner.nextLine();
			if (country.isEmpty()) { return; }
			System.out.println("City (e.g. London): ");
			String city = scanner.nextLine();
			System.out.println("Radius [km] (e.g. 50): ");
			int radius;
			try {
				radius = Integer.parseInt(scanner.nextLine());
			} catch (NumberFormatException e) {
				radius = 15;
			}
			criteria.setLocation(country, city, radius);
			
			System.out.println("Sector [Optional] (e.g: Computer Science, Finance,...");
			String sector = scanner.nextLine().trim();
			criteria.setIndustrySector(sector.isEmpty() ? null : sector);
			
			System.out.println("Contract Time [Optional] ('parttime', 'fulltime', 'student')");
			String ctime = scanner.nextLine().trim();
			switch(ctime) {
				case "parttime":
					criteria.setContractTime(ContractTime.PART_TIME);
					break;
				case "fulltime":
					criteria.setContractTime(ContractTime.FULL_TIME);
					break;
				case "student":
					criteria.setContractTime(ContractTime.STUDENT);
					break;
				default:
					criteria.setContractTime(null);
			}
			
			System.out.println("Contract Type [Optional] ('permanent', 'contract')");
			String ctype = scanner.nextLine().trim();
			switch(ctype) {
				case "permanent":
					criteria.setContractType(ContractType.PERMANENT);
					break;
				case "contract":
					criteria.setContractType(ContractType.CONTRACT);
					break;
				default:
					criteria.setContractType(null);
			}
			
			System.out.println("Keyword (e.g. IT): \n[Enter Nothing to stop]");
			List<String> keywords = new ArrayList<>();
			while(true) {
				System.out.print("> ");
				String keyword = scanner.nextLine().trim();
				if(keyword.isEmpty()) { break; }
				keywords.add(keyword);
			}
			criteria.setKeywords(keywords);
			
			db.insertCriteria(criteria);
			System.out.println("Add another Criteria? (y/n): ");
			if(!scanner.nextLine().trim().equalsIgnoreCase("y")) {
				break;
			}
		}
	}

	private void printCriteria() {
		this.criteria = db.loadAllCriteria();
		if (this.criteria.isEmpty()) {
		    System.out.println("No Criteria created yet.");
		    return;
		}
		for(int i = 0; i < this.criteria.size(); i++) {
		    Criteria c = this.criteria.get(i);
			System.out.println("[" + (i + 1) + "] - " + c.toString());
		}	
	}
	
	private static void clearScreen() {  
		for (int i = 0; i < 50; ++i) { System.out.println(); } // temporary to show "Empty Console" in IDE
	    System.out.print("\033[H\033[2J");
	    System.out.flush();
	}  
}
