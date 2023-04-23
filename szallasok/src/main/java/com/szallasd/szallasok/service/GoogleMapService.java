package com.szallasd.szallasok.service;

import org.springframework.stereotype.Service;

@Service
public class GoogleMapService {
    private static final String linkStart = "https://maps.google.com/maps?q=";
    private static final String linkEnd = ".&output=embed";
    public String getGoogleMapLink(String cim){
        return linkStart + cim.replace(" ","+") + linkEnd;
    }
}
