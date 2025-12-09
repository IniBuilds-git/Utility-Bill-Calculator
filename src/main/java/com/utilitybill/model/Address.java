package com.utilitybill.model;

import java.io.Serializable;
import java.util.Objects;

public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    private String houseNumber;
    private String street;
    private String city;
    private String county;
    private String postcode;
    private String country;

    public Address() {
        this.country = "United Kingdom";
    }

    public Address(String houseNumber, String street, String city, String county, String postcode) {
        this();
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.county = county;
        this.postcode = postcode;
    }

    public Address(String houseNumber, String street, String city, String county,
                   String postcode, String country) {
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.county = county;
        this.postcode = postcode;
        this.country = country;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddressLine1() {
        return String.format("%s %s", houseNumber, street).trim();
    }

    public String getAddressLine2() {
        if (county != null && !county.isEmpty()) {
            return String.format("%s, %s", city, county);
        }
        return city;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(getAddressLine1()).append("\n");
        sb.append(getAddressLine2()).append("\n");
        sb.append(postcode).append("\n");
        sb.append(country);
        return sb.toString();
    }

    public String getInlineAddress() {
        return String.format("%s %s, %s, %s %s",
                houseNumber, street, city, county, postcode).trim();
    }

    public boolean isValid() {
        return houseNumber != null && !houseNumber.trim().isEmpty() &&
                street != null && !street.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                postcode != null && !postcode.trim().isEmpty();
    }

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

