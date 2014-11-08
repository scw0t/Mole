package Entities;


import java.util.ArrayList;

public class Person extends Artist{
    
    private ArrayList<String> instruments;
    private String period;

    public Person(String name) {
        super(name);
        period = "";
        instruments = new ArrayList<>();
    }

    public Person(String name, ArrayList<String> instruments, String period) {
        super(name);
        this.instruments = instruments;
        this.period = period;
    }
    
    public ArrayList<String> getInstruments() {
        return instruments;
    }

    public void setInstruments(ArrayList<String> instruments) {
        this.instruments = instruments;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
    
    
    
}
