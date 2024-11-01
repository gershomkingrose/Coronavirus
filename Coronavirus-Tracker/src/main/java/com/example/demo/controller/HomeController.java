package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.model.LocationStates;
import com.example.demo.services.CoronaVirusDataServices;
import com.example.demo.services.CoronaVirusDataServicesRepository;

@Controller
public class HomeController 
{
	
	CoronaVirusDataServices crnService;
	
	@Autowired
	CoronaVirusDataServicesRepository repository;
	
	@Autowired
	public void setCrnService(CoronaVirusDataServices crnService) {
		this.crnService = crnService;
	}
	
		@GetMapping("/")
		public String home(Model model)
		{
			List<LocationStates> allstates=crnService.getAllstates();
			int totalDeathsReported=allstates.stream().mapToInt(stat->stat.getLatestTotalDeaths()).sum();
			model.addAttribute("LocationStates",allstates);
			model.addAttribute("totalDeathsReported",totalDeathsReported);
			return "home";
		}
	
		@RequestMapping(path = "/collectChartData", produces = "application/json")
	    @ResponseBody
	    public List<LocationStates> collectChartData(Model model) {
	        System.out.println("Here View Chart Data...");
	        List<LocationStates> allStates = crnService.getAllstates();
	        int totalDeathsReported = allStates.stream().mapToInt(stat -> stat.getLatestTotalDeaths()).sum();
	
	        model.addAttribute("LocationStates", allStates);
	        model.addAttribute("totalDeathsReported", totalDeathsReported);
	
	        return allStates;
	    }
		
		@RequestMapping(path = "/collectChartData/{id}", produces = {"application/json"})
		@ResponseBody
		public ResponseEntity<List<LocationStates>> collectChartDataByCountryID(@PathVariable("id") int countryId) {
		    System.out.println("Fetching chart data for country ID: " + countryId);
		    
		    // Fetch the location states based on the country ID
		    Optional<LocationStates> locationStatesOptional = repository.findById(countryId);
	
		    if (locationStatesOptional.isPresent()) {
		        LocationStates locationStates = locationStatesOptional.get();
		        
		        // Return as a list for compatibility with frontend expectations
		        return ResponseEntity.ok(List.of(locationStates));
		    } else {
		        // Return a 404 Not Found response if no data is available
		        return ResponseEntity.notFound().build();
		    }
		}

		// Collect chart data by country name
	    @RequestMapping(path = "/collectChartData/country={countryName}", produces = "application/json")
	    @ResponseBody
	    public ResponseEntity<List<LocationStates>> collectChartDataByCountryName(@PathVariable("countryName") String countryName) {
	        List<LocationStates> locationStates = repository.findByCountryName(countryName);
	        return locationStates.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(locationStates);
	    }
	 	
    	// Collect chart data for top countries
	    @RequestMapping(path = "/collectChartData/top={count}", produces = "application/json")
	    @ResponseBody
	    public ResponseEntity<List<LocationStates>> collectChartDataByTopCount(@PathVariable("count") int count) {
	        List<LocationStates> locationStates = repository.findTopCountriesByLatestTotalDeaths(count);
	        return locationStates.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(locationStates);
	    }
	    // Endpoint to view chart with model and view
	 	@RequestMapping(value = "/viewChart", method = RequestMethod.GET)
	    public ModelAndView viewChart() {
	        return new ModelAndView("ViewChart").addObject("myURL",new String( "http://localhost:8070/collectChartData"));
	    }

	    // Endpoint to view chart by ID
	    @GetMapping("/viewChart/{id}")
	    public String viewChartByID(@PathVariable("id") int id, Model m) {
	        m.addAttribute("id", id);
	        m.addAttribute("myURL", "http://localhost:8070/collectChartData/" + id);
	        return "ViewChart";
	    }

	    // Endpoint to view chart by country name
	    @GetMapping("/viewChart/country={name}")
	    public String viewChartByCountryName(@PathVariable("name") String name, Model m) {
	        m.addAttribute("countryName", name);
	        m.addAttribute("myURL", "http://localhost:8070/collectChartData/country=" + name);
	        return "ViewChart";
	    }
	    
	    @GetMapping("/viewChart/top={count}")
	    public String viewChartByTopCount(@PathVariable("count") int count, Model m) {
	        m.addAttribute("count", count);
	        m.addAttribute("myURL", "http://localhost:8070/collectChartData/top=" + count);
	        return "ViewChart";
	    }
//	@GetMapping("/viewChart")
//	public String viewChart(Model model) {
//	    List<LocationStates> allStates = crnService.getAllstates();
//	    
//	    // Aggregate deaths per country
//	    Map<String, Integer> deathsPerCountry = allStates.stream()
//	            .collect(Collectors.groupingBy(
//	                    LocationStates::getCountry,
//	                    Collectors.summingInt(LocationStates::getLatestTotalDeaths)
//	            ));
//	    
//	    // Filter out countries with 1000 or fewer deaths
//	    Map<String, Integer> filteredDeathsPerCountry = deathsPerCountry.entrySet().stream()
//	            .filter(entry -> entry.getValue() > 5000) // Only keep entries with deaths > 1000
//	            .collect(Collectors.toMap(
//	                    Map.Entry::getKey, 
//	                    Map.Entry::getValue,
//	                    (e1, e2) -> e1, 
//	                    LinkedHashMap::new // To maintain insertion order
//	            ));
//	    
//	    // Sort remaining countries alphabetically
//	    Map<String, Integer> sortedDeathsPerCountry = filteredDeathsPerCountry.entrySet().stream()
//	            .sorted(Map.Entry.comparingByKey())
//	            .collect(Collectors.toMap(
//	                    Map.Entry::getKey, 
//	                    Map.Entry::getValue,
//	                    (e1, e2) -> e1, 
//	                    LinkedHashMap::new // To maintain insertion order
//	            ));
//
//	    model.addAttribute("countries", sortedDeathsPerCountry.keySet());
//	    model.addAttribute("deathCounts", sortedDeathsPerCountry.values());
//	    
//	    return "viewChart";
//	}



}
