package com.utilitybill.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a physical address in the Utility Bill Management System.
 * This class is used as a component of the Customer class (Composition pattern).
 *
 * <p>Design Pattern: Value Object - Address is immutable after creation,
 * representing a value rather than an entity.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class Address implements Serializable {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** House/building number */
    private String houseNumber;

    /** Street name */
    private String street;

    /** City or town */
    private String city;

    /** County or state */
    private String county;

    /** Postal code */
    private String postcode;

    /** Country (defaults to UK) */
    private String country;

    /**
     * Default constructor required for JSON deserialization.
     */
    public Address() {
        this.country = "United Kingdom";
    }

    /**
     * Constructs a new Address with all fields.
     *
     * @param houseNumber the house or building number
     * @param street      the street name
     * @param city        the city or town
     * @param county      the county or state
     * @param postcode    the postal code
     */
    public Address(String houseNumber, String street, String city, String county, String postcode) {
        this();
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.county = county;
        this.postcode = postcode;
    }

    /**
     * Constructs a new Address with country specified.
     *
     * @param houseNumber the house or building number
     * @param street      the street name
     * @param city        the city or town
     * @param county      the county or state
     * @param postcode    the postal code
     * @param country     the country
     */
    public Address(String houseNumber, String street, String city, String county,
                   String postcode, String country) {
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.county = county;
        this.postcode = postcode;
        this.country = country;
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the house number.
     *
     * @return the house number
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * Sets the house number.
     *
     * @param houseNumber the house number to set
     */
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    /**
     * Gets the street name.
     *
     * @return the street name
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the street name.
     *
     * @param street the street name to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Gets the city.
     *
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city.
     *
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the county.
     *
     * @return the county
     */
    public String getCounty() {
        return county;
    }

    /**
     * Sets the county.
     *
     * @param county the county to set
     */
    public void setCounty(String county) {
        this.county = county;
    }

    /**
     * Gets the postcode.
     *
     * @return the postcode
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the postcode.
     *
     * @param postcode the postcode to set
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Gets the country.
     *
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country.
     *
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    // ==================== Business Methods ====================

    /**
     * Gets the first line of the address.
     *
     * @return house number and street
     */
    public String getAddressLine1() {
        return String.format("%s %s", houseNumber, street).trim();
    }

    /**
     * Gets the second line of the address.
     *
     * @return city and county
     */
    public String getAddressLine2() {
        if (county != null && !county.isEmpty()) {
            return String.format("%s, %s", city, county);
        }
        return city;
    }

    /**
     * Gets the full formatted address.
     *
     * @return the complete address as a formatted string
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(getAddressLine1()).append("\n");
        sb.append(getAddressLine2()).append("\n");
        sb.append(postcode).append("\n");
        sb.append(country);
        return sb.toString();
    }

    /**
     * Gets a single-line representation of the address.
     *
     * @return the address on one line
     */
    public String getInlineAddress() {
        return String.format("%s %s, %s, %s %s",
                houseNumber, street, city, county, postcode).trim();
    }

    /**
     * Validates that all required fields are present.
     *
     * @return true if the address is complete
     */
    public boolean isValid() {
        return houseNumber != null && !houseNumber.trim().isEmpty() &&
                street != null && !street.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                postcode != null && !postcode.trim().isEmpty();
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(houseNumber, address.houseNumber) &&
                Objects.equals(street, address.street) &&
                Objects.equals(city, address.city) &&
                Objects.equals(postcode, address.postcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseNumber, street, city, postcode);
    }

    @Override
    public String toString() {
        return getInlineAddress();
    }
}

