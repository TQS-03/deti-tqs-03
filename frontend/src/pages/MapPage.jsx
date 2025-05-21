// src/pages/MapPage.jsx
import { useState, useEffect } from "react";
import Map from "../components/Map/Map";
import { Dropdown } from "../components/ui/dropdown/Dropdown";
import { Button } from "../components/ui/Button.jsx";
import { Modal } from "../components/ui/Modal.jsx";
import { Input } from "../components/ui/Input.jsx";
import { Select } from "../components/ui/Select.jsx";
import { Marker, Popup } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

import iconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png';
import iconUrl from 'leaflet/dist/images/marker-icon.png';
import shadowUrl from 'leaflet/dist/images/marker-shadow.png';


// Fix for default marker icons
delete L.Icon.Default.prototype._getIconUrl;

L.Icon.Default.mergeOptions({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
});

const locations = [
  { value: "london", label: "London", coords: [51.505, -0.09] },
  { value: "newyork", label: "New York", coords: [40.7128, -74.006] },
  { value: "tokyo", label: "Tokyo", coords: [35.6762, 139.6503] },
  { value: "sydney", label: "Sydney", coords: [-33.8688, 151.2093] },
];

const chargerTypes = [
  { value: "TYPE_1", label: "Type 1" },
  { value: "TYPE_2", label: "Type 2" },
  { value: "CCS", label: "CCS" },
  { value: "CHAdeMO", label: "CHAdeMO" },
  { value: "TESLA", label: "Tesla" },
];

const MapPage = () => {
  const [selectedLocation, setSelectedLocation] = useState(locations[0].coords);
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newStation, setNewStation] = useState({
    name: "",
    address: "",
    maxOccupation: 0,
    currentOccupation: 0,
    latitude: "",
    longitude: "",
    chargerTypes: []
  });

  useEffect(() => {
    fetchStations();
  }, []);

  const fetchStations = async () => {
    try {
      const response = await fetch('http://localhost:8080/station');
      if (!response.ok) {
        throw new Error('Failed to fetch stations');
      }
      const data = await response.json();
      setStations(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleLocationChange = (value) => {
    const location = locations.find((loc) => loc.value === value);
    if (location) {
      setSelectedLocation(location.coords);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewStation(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleChargerTypeChange = (selectedOptions) => {
    setNewStation(prev => ({
      ...prev,
      chargerTypes: selectedOptions.map(option => option.value)
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('http://localhost:8080/station', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newStation),
      });

      if (!response.ok) {
        throw new Error('Failed to add station');
      }

      const addedStation = await response.json();
      setStations(prev => [...prev, addedStation]);
      setIsModalOpen(false);
      setNewStation({
        name: "",
        address: "",
        maxOccupation: 0,
        currentOccupation: 0,
        latitude: "",
        longitude: "",
        chargerTypes: []
      });
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="home-page">
        <h1>Welcome to Electro</h1>
      </div>

      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Interactive Map</h1>
        <Button onClick={() => setIsModalOpen(true)}>
          Add New Station
        </Button>
      </div>

      <div className="mb-6 w-full max-w-xs relative z-20">
        <Dropdown
          options={locations.map((loc) => ({ value: loc.value, label: loc.label }))}
          onSelect={handleLocationChange}
          placeholder="Select a location"
        />
      </div>

      {loading && <p className="text-center">Loading charging stations...</p>}
      {error && <p className="text-center text-red-500">Error: {error}</p>}

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Add New Charging Station">
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Station Name"
            name="name"
            value={newStation.name}
            onChange={handleInputChange}
            required
          />
          
          <Input
            label="Address"
            name="address"
            value={newStation.address}
            onChange={handleInputChange}
            required
          />
          
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Latitude"
              name="latitude"
              type="number"
              step="any"
              value={newStation.latitude}
              onChange={handleInputChange}
              required
            />
            
            <Input
              label="Longitude"
              name="longitude"
              type="number"
              step="any"
              value={newStation.longitude}
              onChange={handleInputChange}
              required
            />
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Max Capacity"
              name="maxOccupation"
              type="number"
              value={newStation.maxOccupation}
              onChange={handleInputChange}
              required
              min="1"
            />
            
            <Input
              label="Current Occupancy"
              name="currentOccupation"
              type="number"
              value={newStation.currentOccupation}
              onChange={handleInputChange}
              required
              min="0"
              max={newStation.maxOccupation}
            />
          </div>
          
          <Select
            label="Charger Types"
            options={chargerTypes}
            isMulti
            onChange={handleChargerTypeChange}
            value={chargerTypes.filter(option => 
              newStation.chargerTypes.includes(option.value)
            )}
          />
          
          <div className="flex justify-end space-x-2 pt-4">
            <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit">
              Add Station
            </Button>
          </div>
        </form>
      </Modal>

      <Map center={selectedLocation} className="h-[calc(100vh-200px)] relative z-10">
        {stations.map((station) => (
          <Marker 
            key={station.id} 
            position={[parseFloat(station.latitude), parseFloat(station.longitude)]}
          >
            <Popup>
              <div className="station-popup">
                <h3 className="font-bold">{station.name}</h3>
                <p>{station.address}</p>
                <p>Available: {station.maxOccupation - station.currentOccupation}/{station.maxOccupation}</p>
                <p>Charger Types: {station.chargerTypes?.join(', ') || 'N/A'}</p>
              </div>
            </Popup>
          </Marker>
        ))}
      </Map>
    </div>
  );
};

export default MapPage;