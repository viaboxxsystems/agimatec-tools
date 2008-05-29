package com.agimatec.annomark.example.model;

import com.agimatec.annotations.DTO;
import com.agimatec.annotations.DTOAttribute;

import javax.persistence.*;

/**
 * postal codes in a country
 */
@Entity
@Table(name = "CV_Postcode")
@DTO(usage = "View", dtoClass = "TransferPostcodeLight")
public class CvPostcode implements java.io.Serializable {
    // Fields
    /**
     * column postcode_id
     **/
    @DTOAttribute(usage = "View")
    private long postcodeId;
    /**
     * column version
     **/
    private int version;
    /**
     * ZIP-code
     **/
    @DTOAttribute(usage = "View")
    private String zip;
    /**
     * ZIP-code description
     **/
    @DTOAttribute(usage = "View")
    private String description;
    /**
     * The ZIP-code is valid from this date
     **/
    private java.sql.Timestamp validFrom;

    // Relationships
    private CvCountry country;

    // Constructors

    /**
     * default constructor
     */
    public CvPostcode() {
    }

    // Property accessors

    @Id
    @GeneratedValue
    @Column(name="postcode_id", nullable=false, unique=true)
    public long getPostcodeId() {
        return postcodeId;
    }

    public void setPostcodeId(long postcodeId) {
        this.postcodeId = postcodeId;
    }

    @Version
    @Column(name="version", nullable=false)
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Column(name="zip", nullable=false, length=40)
    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Column(name="description", length=100)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name="valid_from")
    public java.sql.Timestamp getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(java.sql.Timestamp validFrom) {
        this.validFrom = validFrom;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name="country", nullable=false)
    public CvCountry getCountry() {
        return country;
    }

    public void setCountry(CvCountry country) {
        this.country = country;
    }
}
