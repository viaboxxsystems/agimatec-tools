package com.agimatec.annomark.example.model;


import com.agimatec.annotations.DTO;
import com.agimatec.annotations.DTOAttribute;
import com.agimatec.annotations.DTOs;
import com.agimatec.annotations.ToString;

import javax.persistence.*;

@Entity
@Table(name = "address")
@DTOs({@DTO(usage = "Edit"), @DTO(usage = "View")})
public class Address implements java.io.Serializable {
    // Fields    

    @DTOAttribute(type = "Long")
    @ToString
    private long addressId;
    private int version;

    private CvCountry country;
    /** street */
    @DTOAttribute(property = "street")
    @ToString
    private String field1;
    @DTOAttribute(usage = "Edit")
    @ToString
    private String field2;
    @DTOAttribute(usage = "Edit")
    @ToString
    private String field3;
    /** PLZ */
    @DTOAttribute
    @ToString
    private String zip;
    @DTOAttribute
    private String city;

    // Constructors

    /** default constructor */
    public Address() {
    }

    // Property accessors
    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "address_id", unique = true, nullable = false)
    public long getAddressId() {
        return this.addressId;
    }

    public void setAddressId(long addressId) {
        this.addressId = addressId;
    }

    @Version
    @Column(name = "version", nullable = false)
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country", nullable = false)
    public CvCountry getCountry() {
        return this.country;
    }

    public void setCountry(CvCountry country) {
        this.country = country;
    }

    @Column(name = "field_1")
    public String getField1() {
        return this.field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    @Column(name = "field_2")
    public String getField2() {
        return this.field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    @Column(name = "field_3")
    public String getField3() {
        return this.field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    @Column(name = "zip", length = 40)
    public String getZip() {
        return this.zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Column(name = "city", length = 40)
    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}


