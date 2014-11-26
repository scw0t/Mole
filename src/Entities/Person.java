package Entities;

import java.util.ArrayList;

public class Person extends Artist {

    private ArrayList<String> instruments;
    private String period;

    /**
     *
     * @param name
     */
    public Person(String name) {
        super(name);
        period = "";
        instruments = new ArrayList<>();
    }

    /**
     *
     * @param name
     * @param instruments
     * @param period
     */
    public Person(String name, ArrayList<String> instruments, String period) {
        super(name);
        this.instruments = instruments;
        this.period = period;
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getInstruments() {
        return instruments;
    }

    /**
     *
     * @param instruments
     */
    public void setInstruments(ArrayList<String> instruments) {
        this.instruments = instruments;
    }

    /**
     *
     * @return
     */
    public String getPeriod() {
        return period;
    }

    /**
     *
     * @param period
     */
    public void setPeriod(String period) {
        this.period = period;
    }
}