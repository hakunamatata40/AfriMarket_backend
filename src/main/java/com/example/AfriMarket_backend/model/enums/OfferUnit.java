package com.example.AfriMarket_backend.model.enums;

public enum OfferUnit {
    KG("kg"), LITER("litre"), BUNCH("botte"), BOX("carton"), PIECE("pièce");

    private final String label;
    OfferUnit(String label) { this.label = label; }
    public String getLabel() { return label; }
}
