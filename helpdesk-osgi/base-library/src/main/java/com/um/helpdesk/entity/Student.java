package com.um.helpdesk.entity;

import javax.persistence.*;

@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {
    
    private String studentId;
    private String faculty;
    private String program;
    
    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getFaculty() {
        return faculty;
    }
    
    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
    
    public String getProgram() {
        return program;
    }
    
    public void setProgram(String program) {
        this.program = program;
    }
}
