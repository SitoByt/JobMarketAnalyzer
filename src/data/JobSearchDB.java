package data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import data.applicant.Criteria;
import data.job.ContractTime;
import data.job.ContractType;
import data.job.Job;

public class JobSearchDB {
	
	private static JobSearchDB db = new JobSearchDB();
	private Connection conn;
	
	private JobSearchDB() {
		new File("data").mkdirs();
		connect();
		createTables();
	}
	
	public static JobSearchDB getJobSearchDB() {
		return db;
	}

	private void connect() {
		String url = "jdbc:sqlite:data/jobmarket.db";
		try {
			conn = DriverManager.getConnection(url);
			System.out.println("Connection to SQLite successfully established.");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void close() {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch ( SQLException e) {
				e.printStackTrace();
		}
	}
	
	//
	private void createTables() {
		String sqlJobs = "CREATE TABLE IF NOT EXISTS jobs ("
				+ " id TEXT PRIMARY KEY,"
				+ " title TEXT,"
				+ " company TEXT,"
				+ " location TEXT,"
				+ " created TEXT,"
				+ " minSalary TEXT,"
				+ " maxSalary TEXT,"
				+ " predictedSalary TEXT,"
				+ " contractType TEXT,"
				+ " contractTime TEXT,"
				+ " url TEXT,"
				+ " description TEXT,"
				+ " provider_name TEXT"
				+ ");";
		
		String sqlCriteria = "CREATE TABLE IF NOT EXISTS criteria ("
				+ " id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " country TEXT,"
				+ " city TEXT,"
				+ " radius INTEGER,"
				+ " sector TEXT,"
				+ " contractType TEXT,"
				+ " contractTime TEXT,"
				+ " keywords TEXT"
				+ ");";
		
		try (Statement stmt = conn.createStatement()) {
			stmt.execute(sqlJobs);
			stmt.execute(sqlCriteria);
		} catch (SQLException e) {
			System.out.println("Exception during executing sql statements: " + e.getMessage());
		}
	}
	
	// --- Jobs --- //
	
	// Check whether the Job already exists in the database
	public boolean isKnown(String id) {
		String sql = "Select id FROM jobs WHERE id = ?";
		try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			return pstmt.executeQuery().next();
		} catch (Exception e) {
			return false;
		}
	}
	
	public void insertJob(Job job, String providerName) {
		String sql = "INSERT INTO jobs(id, title, company, location, created, minSalary, maxSalary, predictedSalary, contractType, contractTime, description, url, provider_name) "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, job.getId());
			pstmt.setString(2, job.getTitle());
			pstmt.setString(3, job.getCompany());
			pstmt.setString(4, job.getLocation());
			pstmt.setString(5, job.getCreated() != null ? job.getCreated().toString() : null);
			pstmt.setString(6, job.getMinSalary());
			pstmt.setString(7, job.getMaxSalary());
			pstmt.setString(8, job.getPredictedSalary());
			pstmt.setString(9, job.getContractType() != null ? job.getContractType().name() : null);
			pstmt.setString(10, job.getContractTime() != null ? job.getContractTime().name() : null);
			pstmt.setString(11, job.getUrl());
			pstmt.setString(13, providerName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Error inserting Job: " + e.getMessage());
		}
	}
	
	public void deleteJob(String id) {
		String sql = "DELETE FROM jobs WHERE id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, id);
			pstmt.executeUpdate();
		} catch (SQLException e) { e.printStackTrace(); }
	}
	
	public List<Job> loadAllJobs() {
		List<Job> list = new ArrayList<>();
		String sql = "SELECT * FROM jobs";
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			while(rs.next()) {
				Job j = new Job(rs.getString("id"), rs.getString("url"), rs.getString("title"), rs.getString("company"), rs.getString("description"));
				j.setLocation(rs.getString("location"));
				j.setSalary(rs.getString("minSalary"), rs.getString("maxSalary"), rs.getString("predictedSalary"));
				
				String createdStr = rs.getString("created");
				if(createdStr != null) {
					j.setCreated(Instant.parse(createdStr));
				}
				
				String cTimeStr = rs.getString("contractTime");
				ContractTime cTime = cTimeStr != null ? ContractTime.valueOf(cTimeStr) : null;
				String cTypeStr = rs.getString("contractType");
				ContractType cType = cTypeStr != null ? ContractType.valueOf(cTypeStr) : null;
				j.setContract(cTime, cType);
				
				list.add(j);
			}
		} catch (Exception e) { e.printStackTrace(); }
		return list;
	}
	
	public void updateJobs(List<Job> jobs) {
		String upsertSql = "INSERT OR REPLACE INTO jobs(id, title, company, location, created, minSalary, maxSalary, predictedSalary, contractType, contractTime, url, description, provider_name) "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		String deleteSql = "DELETE FROM jobs WHERE id = ?";

		try {
			conn.setAutoCommit(false); // We use Batch-Commit for faster commits (!)
			try (PreparedStatement pstmtUpsert = conn.prepareStatement(upsertSql)) {
				for (Job job : jobs) {
					pstmtUpsert.setString(1, job.getId());
					pstmtUpsert.setString(2, job.getTitle());
					pstmtUpsert.setString(3, job.getCompany());
					pstmtUpsert.setString(4, job.getLocation());
					pstmtUpsert.setString(5, job.getCreated() != null ? job.getCreated().toString() : null);
					pstmtUpsert.setString(6, job.getMinSalary());
					pstmtUpsert.setString(7, job.getMaxSalary());
					pstmtUpsert.setString(8, job.getPredictedSalary());
					pstmtUpsert.setString(9, job.getContractType() != null ? job.getContractType().name() : null);
					pstmtUpsert.setString(10, job.getContractTime() != null ? job.getContractTime().name() : null);
					pstmtUpsert.setString(11, job.getUrl());
					pstmtUpsert.setString(12, job.getDescription());
					pstmtUpsert.setString(13, ""); 
					pstmtUpsert.addBatch(); 
				}
				pstmtUpsert.executeBatch(); 
			}

			Set<String> activeIds = jobs.stream()
					.map(Job::getId)
					.collect(Collectors.toSet());
			
			List<Job> allDbJobs = loadAllJobs();
			int deletedCount = 0;
			
			try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteSql)) {
				for (Job dbJob : allDbJobs) {
					if (!activeIds.contains(dbJob.getId())) {
						pstmtDelete.setString(1, dbJob.getId());
						pstmtDelete.addBatch();
						deletedCount++;
					}
				}
				pstmtDelete.executeBatch();
			}

			conn.commit(); 
			
			System.out.println("Datenbank synchronisiert: " + jobs.size() + " Jobs aktualisiert/eingefügt.");
			if (deletedCount > 0) {
				System.out.println("-> " + deletedCount + " abgelaufene Jobs aus der Datenbank entfernt.");
			}

		} catch (SQLException e) {
			System.err.println("Fehler beim Synchronisieren der Jobs: " + e.getMessage());
			try {
				if (conn != null)
				 {
					conn.rollback();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {

			}
		}
	}
	
	// --- Criteria --- //
	
	public void insertCriteria(Criteria c) {
		String sql = "INSERT INTO criteria(country, city, radius, sector, contractType, contractTime, keywords) VALUES(?,?,?,?,?,?,?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, c.getCountry());
            pstmt.setString(2, c.getCity());
            pstmt.setInt(3, c.getRadiusInKm());
            pstmt.setString(4, c.getIndustrySector());
            pstmt.setString(5, c.getContractType() != null ? c.getContractType().name() : null);
			pstmt.setString(6, c.getContractTime() != null ? c.getContractTime().name() : null);
            pstmt.setString(7, String.join(",", c.getKeywords()));
            
            pstmt.executeUpdate();
            System.out.println("Successfully saved Criteria to DataBase");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void deleteCriteria(int id) {
		String sql = "DELETE FROM criteria WHERE id = ?";
		try(var pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
			System.out.println("Successfully deleted Criteria");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public List<Criteria> loadAllCriteria() {
		List<Criteria> list = new ArrayList<>();
		String sql = "Select * From criteria";
		try (Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while(rs.next()) {
				Criteria c = new Criteria();
				c.setId(rs.getInt("id"));
				c.setLocation(rs.getString("country"), rs.getString("city"), rs.getInt("radius"));
				c.setIndustrySector(rs.getString("sector"));
				String cTypeStr = rs.getString("contractType");
				if (cTypeStr != null && !cTypeStr.isBlank()) {
					c.setContractType(ContractType.valueOf(cTypeStr));
				}
				String cTimeStr = rs.getString("contractTime");
				if (cTimeStr != null && !cTimeStr.isBlank()) {
					c.setContractTime(ContractTime.valueOf(cTimeStr));
				}
				String kwString = rs.getString("keywords");
				if (kwString != null && !kwString.isBlank()) {
				    c.setKeywords(Arrays.asList(kwString.split(",")));
				} else {
				    c.setKeywords(new ArrayList<>());
				}
				list.add(c);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return list;
	}
	
	public Connection getConnection() {
	    return conn;
	}
	
}
