package com.example.oop_v2;

import java.time.LocalDate;

public class Trenn {
    private String nimetus;
    private LocalDate kuupöev;
    private double kestvus;
    private String sisu;

    public Trenn(String nimetus, LocalDate kuupöev, double kestvus, String sisu) {
        this.nimetus = nimetus;
        this.kuupöev = kuupöev;
        this.kestvus = kestvus;
        this.sisu = sisu;
    }

    public String getNimetus() {
        return nimetus;
    }

    public void setNimetus(String nimetus) {
        this.nimetus = nimetus;
    }

    public LocalDate getKuupöev() {
        return kuupöev;
    }

    public void setKuupöev(LocalDate kuupöev) {
        this.kuupöev = kuupöev;
    }

    public double getKestvus() {
        return kestvus;
    }

    public void setKestvus(double kestvus) {
        this.kestvus = kestvus;
    }

    public String getSisu() {
        return sisu;
    }

    public void setSisu(String sisu) {
        this.sisu = sisu;
    }

    @Override
    public String toString() {
        return this.nimetus+";"+this.kuupöev+";"+this.kestvus+";"+this.sisu;
    }
}
