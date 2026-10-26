package com.edu.spring.ai.dto;

import java.util.List;

/**
 * @author Ramanjaneya Reddy Sreeyapu Reddy
 */
public class Response {
    private List<Messages> choices;

    public List<Messages> getChoices() {
        return choices;
    }

    public void setChoices(List<Messages> choices) {
        this.choices = choices;
    }
}
