package com.agimatec.annomark.example.model;

import com.agimatec.annotations.DTO;
import com.agimatec.annotations.DTOAttribute;
import com.agimatec.annotations.DTOs;
import com.agimatec.annotations.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Role for users
 */
@Entity
@Table(name = "role")
@DTOs({@DTO(usage = "Edit"), @DTO(usage = "View")})
public class Role implements java.io.Serializable {
    // Fields

    @DTOAttribute(type = "Long")
    @ToString
    private long roleId;
    private int version;
    @DTOAttribute
    @ToString
    private String roleName;
    @DTOAttribute(usage="Edit")
    private String roleDescription;
    private List<UserCore> userCores = new ArrayList<UserCore>(0);
    @DTOAttribute(usage = "Edit",
            type = "java.util.List<com.agimatec.annomark.example.transfer.TransferPrivilegeLight>")
    private List<Privilege> privileges = new ArrayList<Privilege>(0);

    // Constructors

    /** default constructor */
    public Role() {
    }

    // Property accessors
    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "role_id", unique = true, nullable = false)
    public long getRoleId() {
        return this.roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    @Version
    @Column(name = "version", nullable = false)
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Column(name = "role_name", length = 40, unique = true, nullable = false)
    public String getRoleName() {
        return this.roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Column(name = "role_description")
    public String getRoleDescription() {
        return this.roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "role")
    public List<UserCore> getUserCores() {
        return this.userCores;
    }

    public void setUserCores(List<UserCore> userCores) {
        this.userCores = userCores;
    }

    @ManyToMany()
    @JoinTable(name = "Role_Privilege",
            joinColumns = {@JoinColumn(name = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "privilege_id")})
    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }

    /** method (especially for dozer) to set BOTH SIDES of the relationship */
    public void addPrivilege(Privilege aTarget) {
        privileges.add(aTarget);

    }

    public void removePrivilege(Privilege aTarget) {
        privileges.remove(aTarget);

    }

    public String toString() {
        return "Role{" + "roleId=" + roleId + ", roleName='" + roleName + '\'' + '}';
    }
}


