package com.example.demo.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.model.LocationStates;
import com.example.demo.services.CoronaVirusDataServicesRepository;


@Service
public class CoronaVirusDataServices 
{

	private List<LocationStates> allstates = new ArrayList<LocationStates>();
	
	 @Autowired
	    private CoronaVirusDataServicesRepository repository;
	public List<LocationStates> getAllstates() {
		return allstates;
	}
	public void setAllstates(List<LocationStates> allstates) {
		this.allstates = allstates;
	}
	private static String VIRUS_DATA_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";
	@PostConstruct
	@Scheduled(cron = "* * * 1 * *")
	public void fetchVirusData() throws IOException, InterruptedException
	{
		List<LocationStates> newstates=new ArrayList<LocationStates>();
	HttpClient client=HttpClient.newHttpClient();
	HttpRequest request=HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build();
	HttpResponse<String>httpResponse=client.send(request, HttpResponse.BodyHandlers.ofString());
	
	StringReader csvBodyreader=new StringReader(httpResponse.body());
	Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyreader);
	for (CSVRecord record : records) 
	{
		LocationStates losta=new LocationStates();
	    losta.setState(record.get("Province/State"));
	    losta.setCountry(record.get("Country/Region"));
	    int latestCase=Integer.parseInt(record.get(record.size()-1));
	    int PrevCase=Integer.parseInt(record.get(record.size()-2));
	    losta.setLatestTotalDeaths(latestCase);
	    losta.setDifferFromPrevay(latestCase-PrevCase);
	    System.out.println(losta);
	    
        repository.save(losta);
	    newstates.add(losta);
	    
	}
	this.allstates=newstates;
		
	}
	 /**
     * Aggregates total deaths per country.
     * @return Map of country names to total death counts.
     */
	
    public Map<String, Integer> getDeathsPerCountry() {
        return allstates.stream()
                .collect(Collectors.groupingBy(
                        LocationStates::getCountry,
                        Collectors.summingInt(LocationStates::getLatestTotalDeaths)
                ));
    }

    /**
     * Retrieves the top N countries with the highest death counts.
     * @param topN Number of top countries to retrieve.
     * @return LinkedHashMap of top N countries and their death counts.
     */
    public LinkedHashMap<String, Integer> getTopNDeathsPerCountry(int topN) {
        return getDeathsPerCountry().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, 
                        Map.Entry::getValue,
                        (e1, e2) -> e1, 
                        LinkedHashMap::new
                ));
    }
}


/*
 * https://www.interviewbit.com/blog/java-11-features/
 * HttpClient, HttpRequest, HttpResponse
 */
