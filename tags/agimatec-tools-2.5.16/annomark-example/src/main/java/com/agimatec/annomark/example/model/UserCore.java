package com.agimatec.annomark.example.model;

import com.agimatec.annotations.DTO;
import com.agimatec.annotations.DTOAttribute;
import com.agimatec.annotations.DTOs;
import com.agimatec.annotations.Default;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * a User entity
 */
@Entity
@Table(name = "user_core")
@DTOs({@DTO(usage = "Edit", dtoClass = "TransferUser"), @DTO(usage = "View",
        dtoClass = "TransferUserLight")})
public class UserCore implements java.io.Serializable {

    // Fields    
    @DTOAttribute(type = "Long")
    private long userId;
    private int version;
    @DTOAttribute(property = "emailAddress", usage = "Edit")
    private String email;
    /**
     * the mobile number incl. prefix
     */
    @DTOAttribute(usage = "Edit")
    private String mobileNumber;
    @DTOAttribute
    private String firstName;
    @DTOAttribute
    private String lastName;
    @DTOAttribute
    private String userIdentification;
    @DTOAttribute(usage = "Edit")
    private Timestamp registrationTime;
    @DTOAttribute(usage = "Edit")
    @Default("REGISTERED")
    private String type;
    @DTOAttribute(oneWay = true, usage = "Edit")
    private String gender;
    @DTOAttribute(usage = "Edit")
    private String localeCode;
    @DTOAttribute(usage = "Edit")
    @Default("OK")
    private String state;

    // Relationships
    @DTOAttribute(usage = "Edit", type = "com.agimatec.annomark.example.transfer.TransferRoleLight")
    private Role role;
    @DTOAttribute
    private Address address;
    @DTOAttribute(addMethod = "addCard", usage = "Edit")
    private List<Card> cards = new ArrayList<Card>(0);

    // Constructors

    /** default constructor */
    public UserCore() {
    }

    /** method (especially for dozer) to set BOTH SIDES of the relationship to Card */
    public void addCard(Card aCard) {
        cards.add(aCard);
        if (aCard != null) aCard.setUserCore(this);
    }

    public void removeCard(Card aCard) {
        cards.remove(aCard);
        aCard.setUserCore(null);
    }

    // Property accessors
    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", unique = true, nullable = false)
    public long getUserId() {
        return this.userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Version
    @Column(name = "version", nullable = false)
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @ManyToOne(optional = false,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @OneToOne(optional = true, cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = true, unique = true)
    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address theAddress) {
       this.address = theAddress;
    }

    @Column(name = "email", length = 250)
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "mobile_number", length = 40)
    public String getMobileNumber() {
        return this.mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Column(name = "first_name", nullable = true, length = 40)
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "last_name", nullable = true, length = 40)
    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "user_identification", nullable = false, unique = true, length = 40)
    public String getUserIdentification() {
        return this.userIdentification;
    }

    public void setUserIdentification(String userIdentification) {
        this.userIdentification = userIdentification;
    }

    @Column(name = "registration_time", nullable = false,
            length = 8)
    public Timestamp getRegistrationTime() {
        return this.registrationTime;
    }

    public void setRegistrationTime(Timestamp registrationTime) {
        this.registrationTime = registrationTime;
    }

    @Column(name = "type", nullable = false, length = 20)
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "userCore")
    public List<Card> getCards() {
        return this.cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    @Column(name = "gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Column(name = "state", nullable = false)
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "locale_code")
    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String aLocaleCode) {
        localeCode = aLocaleCode;
    }
}


