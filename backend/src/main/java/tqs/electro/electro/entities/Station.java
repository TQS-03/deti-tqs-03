package tqs.electro.electro.entities;

import jakarta.persistence.*;
import tqs.electro.electro.utils.ChargerType;

import java.util.List;
import java.util.UUID;

@Entity
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String address;
    private int maxOccupation;
    private int currentOccupation;
    private String latitude;
    private String longitude;

    @ElementCollection(targetClass = ChargerType.class)
    @Enumerated(EnumType.STRING)
    private List<ChargerType> chargerTypes;

    public Station(UUID id, String name, String address, int maxOccupation, int currentOccupation, String latitude, String longitude, List<ChargerType> chargerTypes) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.maxOccupation = maxOccupation;
        this.currentOccupation = currentOccupation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.chargerTypes = chargerTypes;
    }

    public Station() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public List<ChargerType> getChargerTypes() {
        return chargerTypes;
    }

    public void setChargerTypes(List<ChargerType> chargerTypes) {
        this.chargerTypes = chargerTypes;
    }

}
