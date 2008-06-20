package com.agimatec.annomark.example.model;

import com.agimatec.annotations.DTO;
import com.agimatec.annotations.DTOAttribute;
import com.agimatec.annotations.DTOs;
import com.agimatec.annotations.ToString;

import javax.persistence.*;

/**
 * Privilege of a role
 */
@Entity
@Table(name = "privilege")
@DTOs({@DTO(usage = "Edit"), @DTO(usage = "View")})
public class Privilege implements java.io.Serializable {
    // Fields    
    @DTOAttribute(type = "Long")
    @ToString
    private long privilegeId;
    private int version;
    @DTOAttribute
    @ToString
    private String privilegeName;
    @DTOAttribute(usage = "Edit")
    private String privilegeDescription;

    // Constructors

    /** default constructor */
    public Privilege() {
    }

    // Property accessors
    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "privilege_id", unique = true, nullable = false)
    public long getPrivilegeId() {
        return this.privilegeId;
    }

    public void setPrivilegeId(long privilegeId) {
        this.privilegeId = privilegeId;
    }

    @Version
    @Column(name = "version", nullable = false)
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Column(name = "privilege_name", nullable = false, length = 40)
    public String getPrivilegeName() {
        return this.privilegeName;
    }

    public void setPrivilegeName(String privilegeName) {
        this.privilegeName = privilegeName;
    }

    @Column(name = "privilege_description")
    public String getPrivilegeDescription() {
        return this.privilegeDescription;
    }

    public void setPrivilegeDescription(String privilegeDescription) {
        this.privilegeDescription = privilegeDescription;
    }
}


