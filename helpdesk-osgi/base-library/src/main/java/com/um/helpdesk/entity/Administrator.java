package com.um.helpdesk.entity;

import javax.persistence.*;

@Entity
@DiscriminatorValue("ADMIN")
public class Administrator extends User {
    
    private String adminLevel;
    
    // Getters and Setters
    public String getAdminLevel() {
        return adminLevel;
    }
    
    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }
}
