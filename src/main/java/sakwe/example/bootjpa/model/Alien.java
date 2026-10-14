package sakwe.example.bootjpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// This class represents one alien record in the database.
@Entity
@Table(name = "alien")
public class Alien {
    @Id
    @NotNull(message = "Alien ID is required.")
    @Min(value = 1, message = "Alien ID must be greater than zero.")
    private Integer aid;

    @NotBlank(message = "Alien name is required.")
    @Size(max = 255, message = "Alien name must be 255 characters or fewer.")
    private String aname;

    @NotBlank(message = "Alien technology is required.")
    @Size(max = 255, message = "Alien technology must be 255 characters or fewer.")
    private String tech;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserAccount owner;

    public Integer getAid() {
        return aid;
    }

    public void setAid(Integer aid) {
        this.aid = aid;
    }

    public String getAname() {
        return aname;
    }

    public void setAname(String aname) {
        this.aname = aname;
    }

    public String getTech() {
        return tech;
    }

    public void setTech(String tech) {
        this.tech = tech;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Alien [aid=" + aid + ", aname=" + aname + ", tech=" + tech + "]";
    }
}
