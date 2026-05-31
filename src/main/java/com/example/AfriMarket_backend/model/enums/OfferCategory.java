package com.example.AfriMarket_backend.model.enums;

public enum OfferCategory {
    VEGETABLES("Légumes"),
    FRUITS("Fruits"),
    TUBERS("Tubercules"),
    CEREALS("Céréales"),
    LIVESTOCK("Élevage"),
    OTHER("Autre");

    private final String label;
    OfferCategory(String label) { this.label = label; }
    public String getLabel() { return label; }
}
