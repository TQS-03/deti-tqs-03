package tqs.electro.electro.dtos;

import tqs.electro.electro.utils.ChargerType;

import java.util.List;
import java.util.UUID;

public class NewStationDTO {

    private String name;
    private String address;
    private int maxOccupation;
    private int currentOccupation;
    private String latitude;
    private String longitude;
    private double pricePerKWh;
    private UUID personId;
    private List<ChargerType> chargerTypes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getMaxOccupation() {
        return maxOccupation;
    }

    public void setMaxOccupation(int maxOccupation) {
        this.maxOccupation = maxOccupation;
    }

    public int getCurrentOccupation() {
        return currentOccupation;
    }

    public void setCurrentOccupation(int currentOccupation) {
        this.currentOccupation = currentOccupation;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public double getPricePerKWh() {
        return pricePerKWh;
    }

    public void setPricePerKWh(double pricePerKWh) {
        this.pricePerKWh = pricePerKWh;
    }

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public List<ChargerType> getChargerTypes() {
        return chargerTypes;
    }

    public void setChargerTypes(List<ChargerType> chargerTypes) {
        this.chargerTypes = chargerTypes;
    }
}
