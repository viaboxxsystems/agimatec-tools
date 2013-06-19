package com.agimatec.annomark.example.model;

import javax.persistence.*;

/**
 * Contains currency available for selection
 */
@Entity
@org.hibernate.annotations.Entity(mutable = false)
@Table(name = "CV_Currency")
public class CvCurrency implements java.io.Serializable {
    // Fields
    /**
     * ISO 4217 Currency Code
     **/
    private String code;
    /**
     * currency name (as description)
     **/
    private String name;

    // Constructors

    /**
     * default constructor
     */
    public CvCurrency() {
    }

    // Property accessors

    @Id
    @GeneratedValue
    @Column(name="code", nullable=false, unique=true, length=3)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name="name", length=40)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

