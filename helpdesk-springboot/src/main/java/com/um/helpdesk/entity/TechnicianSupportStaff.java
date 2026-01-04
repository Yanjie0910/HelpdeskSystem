package com.um.helpdesk.entity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("TECHNICIAN")
public class TechnicianSupportStaff extends User {
    
    private String staffId;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private String specialization;

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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}