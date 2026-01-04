package com.um.helpdesk.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("STAFF")
public class Staff extends User {
    
    private String staffId;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}