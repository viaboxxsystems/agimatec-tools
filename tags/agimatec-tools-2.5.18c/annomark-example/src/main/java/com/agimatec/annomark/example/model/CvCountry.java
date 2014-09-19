package com.agimatec.annomark.example.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cv_country")
@org.hibernate.annotations.Entity(mutable = false)
public class CvCountry implements java.io.Serializable {
    // Fields
    private String code;
    /** country name (as description) */
    private String name;

    // Property accessors
    @Id
    @Column(name = "code", nullable = false, unique = true, length = 2)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
        
    @Column(name = "name", length = 40)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


