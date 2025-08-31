package com.workintech.s17d2.rest;

import com.workintech.s17d2.model.*;
import com.workintech.s17d2.tax.Taxable;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/developers") // final path: /workintech/developers
public class DeveloperController {

    private final Taxable taxable;

    // TEST doğrudan bu alana erişiyor: public olmalı
    public Map<Integer, Developer> developers;

    public DeveloperController(Taxable taxable) {
        this.taxable = taxable;
    }

    @PostConstruct
    public void init() {
        developers = new ConcurrentHashMap<>();
    }

    // --- helpers ---
    private double netSalary(double gross, Experience exp) {
        double rate = switch (exp) {
            case JUNIOR -> taxable.getSimpleTaxRate();
            case MID    -> taxable.getMiddleTaxRate();
            case SENIOR -> taxable.getUpperTaxRate();
        };
        return gross - (gross * rate / 100.0);
    }

    private Developer typed(Integer id, String name, double gross, Experience exp) {
        double afterTax = netSalary(gross, exp);
        return switch (exp) {
            case JUNIOR -> new JuniorDeveloper(id, name, afterTax);
            case MID    -> new MidDeveloper(id, name, afterTax);
            case SENIOR -> new SeniorDeveloper(id, name, afterTax);
        };
    }

    // --- CRUD ---
    @GetMapping
    public ResponseEntity<List<Developer>> findAll() {
        return ResponseEntity.ok(new ArrayList<>(developers.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Developer> findById(@PathVariable Integer id) {
        Developer dev = developers.get(id);
        return (dev == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(dev);
    }

    @PostMapping
    public ResponseEntity<Developer> create(@RequestBody Developer req) {
        if (req.getId() == null || req.getName() == null || req.getExperience() == null) {
            return ResponseEntity.badRequest().build();
        }
        Developer created = typed(req.getId(), req.getName(), req.getSalary(), req.getExperience());
        developers.put(created.getId(), created);
        // TEST: 201 Created bekliyor
        return ResponseEntity.created(URI.create("/developers/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Developer> update(@PathVariable Integer id, @RequestBody Developer req) {
        if (!developers.containsKey(id)) return ResponseEntity.notFound().build();

        Developer current = developers.get(id);
        Experience exp = (req.getExperience() != null) ? req.getExperience() : current.getExperience();
        String name = (req.getName() != null) ? req.getName() : current.getName();
        double salary = (req.getSalary() > 0) ? req.getSalary() : current.getSalary();

        Developer updated = typed(id, name, salary, exp);
        developers.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Developer removed = developers.remove(id);
        // TEST: 200 OK bekliyor
        return (removed == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok().build();
    }
}
