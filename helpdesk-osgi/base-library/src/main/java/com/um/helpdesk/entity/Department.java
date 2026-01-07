package com.um.helpdesk.entity;

import javax.persistence.*;

@Entity
@Table(name = "departments")
public class Department extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    private String code;
    private String description;
    private boolean active = true;
    
    // Constructors
    public Department() {
    }
    
    public Department(String name, String code) {
        this.name = name;
        this.code = code;
        this.active = true;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
