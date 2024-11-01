package com.example.demo.services;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.LocationStates;

@Repository
public interface CoronaVirusDataServicesRepository extends JpaRepository<LocationStates, Integer> {

    // Method to find LocationStates by country name
    LocationStates findByCountry(String countryName);
    
    @Query(value = "SELECT * FROM location_states ORDER BY latest_total_deaths DESC LIMIT :top", nativeQuery = true)
    List<LocationStates> findTopCountriesByLatestTotalDeaths(@Param("top") int topN);
    
    @Query(value = "SELECT * FROM location_states WHERE country = :countryName", nativeQuery = true)
    List<LocationStates> findByCountryName(@Param("countryName") String countryName);
    // Method to get the top 'count' LocationStates by latest total deaths
    List<LocationStates> findCountryByLatestTotalDeaths(int count);
}
