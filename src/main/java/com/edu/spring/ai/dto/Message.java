package com.edu.spring.ai.dto;

/**
 * @author Ramanjaneya Reddy Sreeyapu Reddy
 */
public class Message{
    String role = "user";
    String content;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
