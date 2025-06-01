import { useState, useEffect, useRef, useMemo } from "react";
import Map from "../components/Map/Map";
import { Dropdown } from "../components/ui/dropdown/Dropdown";
import { Button } from "../components/ui/Button.jsx";
import { Modal } from "../components/ui/Modal.jsx";
import { Input } from "../components/ui/Input.jsx";
import { Select } from "../components/ui/Select.jsx";
import { Marker, Popup } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

import iconRetinaUrl from "leaflet/dist/images/marker-icon-2x.png";
import iconUrl from "leaflet/dist/images/marker-icon.png";
import shadowUrl from "leaflet/dist/images/marker-shadow.png";

delete L.Icon.Default.prototype._getIconUrl;

L.Icon.Default.mergeOptions({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
});

const chargerTypes = [
  { value: "Type 1", label: "Type 1" },
  { value: "Type 2", label: "Type 2" },
  { value: "CCS", label: "CCS" },
  { value: "CHAdeMO", label: "CHAdeMO" },
  { value: "TESLA", label: "Tesla" },
  { value: "Schuko", label: "Schuko" },
];

const MapPage = () => {
  const [selectedLocation, setSelectedLocation] = useState([51.505, -0.09]);
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedChargerType, setSelectedChargerType] = useState(null);
  const [newStation, setNewStation] = useState({
    name: "",
    address: "",
    maxOccupation: 0,
    currentOccupation: 0,
    latitude: "",
    longitude: "",
    pricePerKWh: 0,
    chargerTypes: [],
  });
  const markerRefs = useRef({});

  // Booking modal states
  const [isBookingModalOpen, setIsBookingModalOpen] = useState(false);
  const [stationToBook, setStationToBook] = useState(null);
  const [bookingStart, setBookingStart] = useState("");
  const [bookingEnd, setBookingEnd] = useState("");
  const [userReservations, setUserReservations] = useState([]);

  useEffect(() => {
    fetchStations();
    fetchUserReservations();
  }, []);

  const fetchStations = async () => {
    try {
      const response = await fetch("backend/station");
      if (!response.ok) {
        throw new Error("Failed to fetch stations");
      }
      const data = await response.json();
      setStations(data);

      if (data.length > 0) {
        setSelectedLocation([parseFloat(data[0].latitude), parseFloat(data[0].longitude)]);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const fetchUserReservations = async () => {
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (user?.userId) {
        const response = await fetch(`backend/reservation?personId=${user.userId}`);
        if (response.ok) {
          const data = await response.json();
          setUserReservations(data);
        }
      }
    } catch (err) {
      console.error("Failed to fetch user reservations:", err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user || !user.userId) {
        throw new Error("User not authenticated");
      }

      const stationData = {
        name: newStation.name,
        address: newStation.address,
        maxOccupation: Number(newStation.maxOccupation),
        currentOccupation: Number(newStation.currentOccupation),
        latitude: newStation.latitude,
        longitude: newStation.longitude,
        pricePerKWh: Number(newStation.pricePerKWh || 0),
        chargerTypes: newStation.chargerTypes,
        personId: user.userId
      };

      const response = await fetch("backend/station", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(stationData),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || "Failed to add station");
      }

      const addedStation = await response.json();
      setStations((prev) => [...prev, addedStation]);
      setIsModalOpen(false);
      setNewStation({
        name: "",
        address: "",
        maxOccupation: 0,
        currentOccupation: 0,
        latitude: "",
        longitude: "",
        pricePerKWh: 0,
        chargerTypes: [],
      });
      setError("");
    } catch (err) {
      setError(err.message);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewStation((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleChargerTypeChange = (selectedOptions) => {
    setNewStation((prev) => ({
      ...prev,
      chargerTypes: selectedOptions.map((option) => option.value),
    }));
  };

  const filteredStations = useMemo(() => {
    if (!selectedChargerType) return stations;
    return stations.filter((station) =>
        station.chargerTypes?.includes(selectedChargerType.value)
    );
  }, [stations, selectedChargerType]);

  const handleLocationChange = (stationId) => {
    Object.values(markerRefs.current).forEach((ref) => {
      if (ref && ref.closePopup) {
        ref.closePopup();
      }
    });

    const station = filteredStations.find((s) => s.id === stationId);
    if (station) {
      setSelectedLocation([parseFloat(station.latitude), parseFloat(station.longitude)]);
    }
  };

  const stationOptions = useMemo(
      () =>
          filteredStations.map((station) => ({
            value: station.id,
            label: `${station.name} (${station.address})`,
            coords: [parseFloat(station.latitude), parseFloat(station.longitude)],
          })),
      [filteredStations]
  );

  const handleBookStation = (station) => {
    setStationToBook(station);
    setBookingStart("");
    setBookingEnd("");
    setIsBookingModalOpen(true);
  };

  const checkStationAvailability = async (stationId, startTime, endTime) => {
    try {
      const date = new Date(startTime).toISOString().split('T')[0];
      const response = await fetch(`backend/reservation?stationId=${stationId}&date=${date}`);

      if (!response.ok) {
        throw new Error("Failed to check availability");
      }

      const reservations = await response.json();
      const newStart = new Date(startTime);
      const newEnd = new Date(endTime);

      const isAvailable = !reservations.some(reservation => {
        const resStart = new Date(reservation.startTime);
        const resEnd = new Date(reservation.endTime);
        return (newStart < resEnd && newEnd > resStart);
      });

      return isAvailable;
    } catch (err) {
      console.error("Availability check error:", err);
      return false;
    }
  };

  const handleBookingSubmit = async (e) => {
    e.preventDefault();

    if (!bookingStart || !bookingEnd) {
      alert("Please select both start and end date/time");
      return;
    }

    if (new Date(bookingEnd) <= new Date(bookingStart)) {
      alert("End date/time must be after start date/time");
      return;
    }

    try {
      const isAvailable = await checkStationAvailability(
          stationToBook.id,
          bookingStart,
          bookingEnd
      );

      if (!isAvailable) {
        throw new Error("The station is not available during the selected time slot");
      }

      const user = JSON.parse(localStorage.getItem("user"));
      if (!user || !user.userId) {
        throw new Error("User not authenticated");
      }

      const reservationData = {
        personId: user.userId,
        stationId: stationToBook.id,
        startTime: bookingStart,
        endTime: bookingEnd
      };

      const response = await fetch("backend/reservation", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(reservationData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Booking failed");
      }

      const reservation = await response.json();
      alert(`Reservation created successfully! ID: ${reservation.id}`);
      setIsBookingModalOpen(false);

      fetchStations();
      fetchUserReservations();
    } catch (err) {
      alert(`Booking error: ${err.message}`);
    }
  };

  const cancelReservation = async (reservationId) => {
    try {
      const response = await fetch(`backend/reservation/${reservationId}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        throw new Error("Failed to cancel reservation");
      }

      alert("Reservation cancelled successfully");
      fetchUserReservations();
      fetchStations();
    } catch (err) {
      alert(`Error cancelling reservation: ${err.message}`);
    }
  };

  return (
      <div className="container mx-auto px-4 py-8">
        <div className="home-page">
          <h1>Welcome to Electro</h1>
        </div>

        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Interactive Map</h1>
          <Button onClick={() => setIsModalOpen(true)}>Add New Station</Button>
        </div>

        <div className="flex flex-col md:flex-row gap-4 mb-6">
          <div className="w-full md:w-1/3 relative z-20">
            <Dropdown
                options={stationOptions}
                onSelect={handleLocationChange}
                placeholder="Select a station"
            />
          </div>
          <div className="w-full md:w-1/3 relative z-20">
            <Select
                options={chargerTypes}
                placeholder="Filter by charger type"
                isClearable
                onChange={(selected) => setSelectedChargerType(selected)}
                value={selectedChargerType}
            />
          </div>
        </div>

        {loading && <p className="text-center">Loading charging stations...</p>}
        {error && <p className="text-center text-red-500">Error: {error}</p>}

        <Modal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            title="Add New Charging Station"
        >
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
                  type="text"
                  value={newStation.latitude}
                  onChange={handleInputChange}
                  required
              />

              <Input
                  label="Longitude"
                  name="longitude"
                  type="text"
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

            <Input
                label="Price per kWh (€)"
                name="pricePerKWh"
                type="number"
                step="0.01"
                value={newStation.pricePerKWh}
                onChange={handleInputChange}
                min="0"
            />

            <Select
                label="Charger Types"
                options={chargerTypes}
                isMulti
                onChange={handleChargerTypeChange}
                value={chargerTypes.filter((option) =>
                    newStation.chargerTypes.includes(option.value)
                )}
            />

            <div className="flex justify-end space-x-2 pt-4">
              <Button
                  type="button"
                  variant="outline"
                  onClick={() => setIsModalOpen(false)}
              >
                Cancel
              </Button>
              <Button type="submit">Add Station</Button>
            </div>
          </form>
        </Modal>

        <Modal
            isOpen={isBookingModalOpen}
            onClose={() => setIsBookingModalOpen(false)}
            title={`Book Station: ${stationToBook?.name || ""}`}
        >
          <form onSubmit={handleBookingSubmit} className="space-y-4">
            <p>Address: {stationToBook?.address}</p>
            <p>Available: {stationToBook?.maxOccupation - stationToBook?.currentOccupation}/{stationToBook?.maxOccupation}</p>
            <p>Price: €{stationToBook?.pricePerKWh?.toFixed(2) || '0.00'} per kWh</p>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Start Time</label>
                <Input
                    type="datetime-local"
                    value={bookingStart}
                    onChange={(e) => setBookingStart(e.target.value)}
                    required
                    min={new Date().toISOString().slice(0, 16)}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">End Time</label>
                <Input
                    type="datetime-local"
                    value={bookingEnd}
                    onChange={(e) => setBookingEnd(e.target.value)}
                    required
                    min={bookingStart || new Date().toISOString().slice(0, 16)}
                />
              </div>
            </div>

            <div className="flex justify-end space-x-2 pt-4">
              <Button
                  type="button"
                  variant="outline"
                  onClick={() => setIsBookingModalOpen(false)}
              >
                Cancel
              </Button>
              <Button type="submit">Confirm Booking</Button>
            </div>
          </form>
        </Modal>

        <Map center={selectedLocation} className="h-[calc(100vh-200px)] relative z-10">
          {filteredStations.map((station) => (
              <Marker
                  key={station.id}
                  position={[parseFloat(station.latitude), parseFloat(station.longitude)]}
                  ref={(ref) => {
                    markerRefs.current[station.id] = ref;
                  }}
              >
                <Popup>
                  <div className="station-popup space-y-2">
                    <h3 className="font-bold">{station.name}</h3>
                    <p>{station.address}</p>
                    <p>
                      Available: {station.maxOccupation - station.currentOccupation}/{station.maxOccupation}
                    </p>
                    <p>Price: €{station.pricePerKWh?.toFixed(2) || '0.00'} per kWh</p>
                    <p>Charger Types: {station.chargerTypes?.join(", ") || "N/A"}</p>
                    <Button onClick={() => handleBookStation(station)}>
                      Book This Station
                    </Button>
                  </div>
                </Popup>
              </Marker>
          ))}
        </Map>

        <div className="mt-8">
          <h2 className="text-2xl font-bold mb-4">Your Reservations</h2>
          {userReservations.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {userReservations.map((reservation) => (
                    <div key={reservation.id} className="border rounded-lg p-4">
                      <h3 className="font-bold">{reservation.station?.name || 'Unknown Station'}</h3>
                      <p>Start: {new Date(reservation.startTime).toLocaleString()}</p>
                      <p>End: {new Date(reservation.endTime).toLocaleString()}</p>
                      <p>Status: {reservation.paid ? 'Paid' : 'Pending Payment'}</p>
                      <div className="flex space-x-2 mt-2">
                        <Button
                            variant="outline"
                            onClick={() => {
                              if (reservation.station) {
                                setSelectedLocation([
                                  parseFloat(reservation.station.latitude),
                                  parseFloat(reservation.station.longitude)
                                ]);
                              }
                            }}
                        >
                          View on Map
                        </Button>
                        <Button
                            variant="destructive"
                            onClick={() => cancelReservation(reservation.id)}
                        >
                          Cancel
                        </Button>
                      </div>
                    </div>
                ))}
              </div>
          ) : (
              <p>You have no active reservations.</p>
          )}
        </div>
      </div>
  );
};

export default MapPage;