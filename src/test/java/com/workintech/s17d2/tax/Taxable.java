package com.workintech.s17d2.tax;

public interface Taxable {
    double getSimpleTaxRate();   // 15d
    double getMiddleTaxRate();   // 25d
    double getUpperTaxRate();    // 35d
}
