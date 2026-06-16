package io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import data.Job;
	
public class IOManager {
	
	public static void exportToMarkdown(Map<String, List<Job>> jobs, String filePrefix) {
		Path filePath = createFilePath(filePrefix);
		StringBuilder content = new StringBuilder();
		writeMappedJobLists(jobs, content);
		writeToMd(filePath, content);
	}
	
	public static void exportToMarkdown(List<Job> jobs, String filePrefix) {
		Path filePath = createFilePath(filePrefix);
		StringBuilder content = new StringBuilder();
		writeJobList(jobs, content);
		writeToMd(filePath, content);
	}
	
	public static void exportToMarkdown(Job job, String filePrefix) {
		Path filePath = createFilePath(filePrefix);
		StringBuilder content = new StringBuilder();
		writeJob(job, content);
		writeToMd(filePath, content);
		
	}

	private static void writeToMd(Path filePath, StringBuilder content) {
		try {
			Files.writeString(filePath, content.toString(), StandardCharsets.UTF_8);
			System.out.println("Exported File to: " + filePath.toAbsolutePath());
		} catch(IOException e) {
			System.err.println("Exception while writing File: ");
		}
	}

	private static void writeJob(Job job, StringBuilder content) {
		content.append("### ").append(job.getHeader()).append("\n");
		content.append("```\n");
		content.append(job.getBody()).append("\n");
		content.append("```\n\n");
	}
	
	private static void writeJobList(List<Job> jobs, StringBuilder content) {
		content.append("## Found ").append(jobs.size()).append(" Jobs the provided Criteras.\n\n");
		for (Job job : jobs) {
			writeJob(job, content);
		}
	}
	
	private static void writeMappedJobLists(Map<String, List<Job>> jobs, StringBuilder content) {
		content.append("Found ").append(jobs.size()).append(" categories.\n\n");
		for(String s : jobs.keySet()) {
			content.append("# ").append(s);
			var jobList = jobs.get(s);
			writeJobList(jobList, content);
		}
	}

	private static Path createFilePath(String filePrefix) {
		String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
		String fileName =  "out/" + filePrefix + "_" + dateString + ".md";
		Path filePath = Paths.get(fileName);
		return filePath;
	}

}
