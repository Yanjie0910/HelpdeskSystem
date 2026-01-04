package com.um.helpdesk.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ADMIN")
public class Administrator extends User {
    
    private String adminLevel;

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }
}