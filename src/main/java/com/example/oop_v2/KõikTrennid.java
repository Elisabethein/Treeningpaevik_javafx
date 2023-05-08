package com.example.oop_v2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KõikTrennid {
    private Map<LocalDate, ArrayList<Trenn>> trennideMap;

    public KõikTrennid(Map<LocalDate, ArrayList<Trenn>> trennideMap) {
        this.trennideMap = trennideMap;
    }

    public Map<LocalDate, ArrayList<Trenn>> getTrennideMap() {
        return trennideMap;
    }

    public void setTrennideMap(Map<LocalDate, ArrayList<Trenn>> trennideMap) {
        this.trennideMap = trennideMap;
    }
}
