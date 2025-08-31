package com.workintech.s17d2.model;

public class SeniorDeveloper extends Developer {
    public SeniorDeveloper(Integer id, String name, double salary) {
        super(id, name, salary, Experience.SENIOR);
    }
}
