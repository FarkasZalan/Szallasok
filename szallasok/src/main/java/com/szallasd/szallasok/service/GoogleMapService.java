package com.szallasd.szallasok.service;

import org.springframework.stereotype.Service;

@Service
public class GoogleMapService {
    // Base URL for Google Maps embed link
    private static final String LINK_START = "https://maps.google.com/maps?q=";
    // Suffix for Google Maps embed link
    private static final String LINK_END = ".&output=embed";

    /**
     * Constructs a Google Maps embed link for a given address.
     *
     * @param address The address to be displayed on the map.
     * @return A Google Maps embed link with the specified address.
     */
    public String getGoogleMapLink(String address) {
        // Replace spaces in the address with '+' for URL encoding
        String encodedAddress = address.replace(" ", "+");
        // Construct and return the full Google Maps embed link
        return LINK_START + encodedAddress + LINK_END;
    }
}
