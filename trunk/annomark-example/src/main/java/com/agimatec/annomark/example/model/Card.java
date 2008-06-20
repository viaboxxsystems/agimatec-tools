package com.agimatec.annomark.example.model;

import com.agimatec.annotations.DTO;
import com.agimatec.annotations.DTOAttribute;
import com.agimatec.annotations.ToString;

import javax.persistence.*;

/**
 * identification cards of a user
 */
@Entity
@Table(name = "card")
@DTO(usage="Edit")
public class Card implements java.io.Serializable {
    // Fields
    @DTOAttribute(type="Long")
    @ToString
    private long cardId;
    private int version;
    private UserCore userCore;
    @DTOAttribute
    private String track1;
    @DTOAttribute(usage = "Edit")
    private String track2;
    @DTOAttribute(usage = "Edit")
    private String track3;

    // Constructors

    /**
     * default constructor
     */
    public Card() {
    }

    // Property accessors
    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "card_id", unique = true, nullable = false)
    public long getCardId() {
        return this.cardId;
    }

    public void setCardId(long cardId) {
        this.cardId = cardId;
    }

    @Version
    @Column(name="version", nullable = false)
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @ManyToOne(optional = false,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public UserCore getUserCore() {
        return this.userCore;
    }

    public void setUserCore(UserCore userCore) {
        this.userCore = userCore;
    }

    @Column(name = "track_1", nullable = true, length = 40)
    public String getTrack1() {
        return this.track1;
    }

    public void setTrack1(String track1) {
        this.track1 = track1;
    }

    @Column(name = "track_2", nullable = true, length = 40)
    public String getTrack2() {
        return this.track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    @Column(name = "track_3", nullable = true, length = 40)
    public String getTrack3() {
        return this.track3;
    }

    public void setTrack3(String track3) {
        this.track3 = track3;
    }


}


