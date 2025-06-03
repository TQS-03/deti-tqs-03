import { useEffect, useState } from "react";

const BookingPage = () => {
  const [bookings, setBookings] = useState([]);
  const [stations, setStations] = useState([]);
  const [error, setError] = useState("");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    stationId: "",
    startTime: "",
    endTime: "",
  });
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPaymentForm, setShowPaymentForm] = useState(false);
  const [selectedBookingId, setSelectedBookingId] = useState(null);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [paymentData, setPaymentData] = useState({
    cardNumber: "",
    expiryDate: "",
    cvv: "",
    saveCard: false,
  });
  const [consumptionData, setConsumptionData] = useState({
    energyUsed: "",
    pricePerKWh: 0.15,
  });

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user?.userId) throw new Error("User not authenticated");
      const res = await fetch(`backend/reservation?personId=${user.userId}`);
      if (!res.ok) throw new Error("Failed to fetch bookings");
      const data = await res.json();
      setBookings(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const fetchStations = async () => {
    try {
      const response = await fetch("backend/station");
      if (!response.ok) throw new Error("Failed to fetch stations");
      const data = await response.json();
      setStations(data.map(s => ({
        ...s,
        chargerTypes: s.chargerTypes || [],
        pricePerKWh: s.pricePerKWh || 0.15
      })));
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    fetchBookings();
    fetchStations();
  }, []);

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setPaymentData(prev => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleBookingInput = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    
    if (name === "stationId") {
      const selectedStation = stations.find(s => s.id === value);
      if (selectedStation) {
        setConsumptionData(prev => ({
          ...prev,
          pricePerKWh: selectedStation.pricePerKWh
        }));
      }
    }
  };

  const handleConsumptionChange = (e) => {
    const { name, value } = e.target;
    setConsumptionData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const calculateCost = () => {
    const energy = parseFloat(consumptionData.energyUsed) || 0;
    const price = parseFloat(consumptionData.pricePerKWh) || 0;
    return (energy * price).toFixed(2);
  };

  const handleDelete = async (bookingId) => {
    setLoading(true);
    setError("");
    setMessage("");
    try {
      const res = await fetch(`/backend/reservation/${bookingId}`, {
        method: "DELETE",
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || "Failed to delete booking");
      }

      setMessage("Booking deleted successfully");
      await fetchBookings();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    setLoading(true);

    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user?.userId) throw new Error("User not authenticated");
      const selectedStation = stations.find(s => s.id === formData.stationId);
      if (!selectedStation) throw new Error("Invalid station selected");

      const toLocalDateTimeString = (dateStr) => new Date(dateStr).toISOString().slice(0, 19);

      const reservationData = {
        stationId: formData.stationId,
        personId: user.userId,
        startTime: toLocalDateTimeString(formData.startTime),
        endTime: toLocalDateTimeString(formData.endTime),
      };

      const res = await fetch("/backend/reservation", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(reservationData),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || "Booking failed");
      }

      setMessage("Booking successful!");
      setIsModalOpen(false);
      setFormData({ stationId: "", startTime: "", endTime: "" });
      await fetchBookings();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handlePay = async (bookingId) => {
    setLoading(true);
    setError("");
    setMessage("");
  
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user?.userId) throw new Error("User not authenticated");
  
      if (!selectedBooking?.station) throw new Error("Station not found");
      
      const pricePerKWh = selectedBooking.station.pricePerKWh || 0.15;
  
      const consumptionPayload = {
        station: { id: selectedBooking.station.id },
        startTime: selectedBooking.startTime,
        endTime: new Date().toISOString(),
        energyUsed: parseFloat(consumptionData.energyUsed),
        pricePerKWh: pricePerKWh
      };
  
      const consumptionResponse = await fetch("/backend/consumption", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(consumptionPayload),
      });
  
      if (!consumptionResponse.ok) {
        throw new Error("Failed to record consumption");
      }
  
      const consumption = await consumptionResponse.json();
      const amount = calculateCost();
  
      const paymentResponse = await fetch(`/backend/driver/payments/${user.userId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...paymentData,
          amount,
          bookingId,
          consumptionId: consumption.id
        }),
      });
  
      if (!paymentResponse.ok) {
        throw new Error("Payment failed");
      }
  
      setMessage("Payment and consumption recorded successfully");
      setShowPaymentForm(false);
      setSelectedBookingId(null);
      setConsumptionData({
        energyUsed: "",
        pricePerKWh: pricePerKWh
      });
      await fetchBookings();
  
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">My Bookings</h1>
      {message && <p className="text-green-600 mb-4">{message}</p>}
      {error && <p className="text-red-500 mb-4">{error}</p>}

      <button
        onClick={() => setIsModalOpen(true)}
        className="mb-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
      >
        New Booking
      </button>

      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex justify-center items-center z-50">
          <div className="bg-white p-6 rounded shadow-lg w-full max-w-md relative">
            <h2 className="text-xl font-semibold mb-4">Create New Booking</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <select
                name="stationId"
                value={formData.stationId}
                onChange={handleBookingInput}
                required
                className="w-full border rounded p-2"
              >
                <option value="">Select a station</option>
                {stations.map(s => (
                  <option key={s.id} value={s.id}>
                    {s.name} (€{s.pricePerKWh?.toFixed(2) || "0.15"}/kWh)
                  </option>
                ))}
              </select>
              <input
                type="datetime-local"
                name="startTime"
                value={formData.startTime}
                onChange={handleBookingInput}
                required
                min={new Date().toISOString().slice(0, 16)}
                className="w-full border rounded p-2"
              />
              <input
                type="datetime-local"
                name="endTime"
                value={formData.endTime}
                onChange={handleBookingInput}
                required
                min={formData.startTime || new Date().toISOString().slice(0, 16)}
                className="w-full border rounded p-2"
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 bg-gray-300 rounded"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-green-600 text-white rounded"
                >
                  Book
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showPaymentForm && selectedBooking && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex justify-center items-center z-50">
          <div className="bg-white p-6 rounded shadow-lg w-full max-w-md relative">
            <h2 className="text-xl font-semibold mb-4">Payment Information</h2>
            <div className="mb-4">
              <h3 className="font-medium mb-2">Consumption Details</h3>
              <div className="mb-2">
                <label className="block mb-1">Energy Used (kWh)</label>
                <input
                  type="number"
                  name="energyUsed"
                  value={consumptionData.energyUsed}
                  onChange={handleConsumptionChange}
                  className="w-full border rounded p-2"
                  step="0.01"
                  min="0"
                  placeholder="Enter kWh used"
                  required
                />
              </div>
              <div className="mb-2">
                <label className="block mb-1">Price per kWh (€)</label>
                <input
                  type="number"
                  name="pricePerKWh"
                  value={consumptionData.pricePerKWh}
                  onChange={handleConsumptionChange}
                  className="w-full border rounded p-2"
                  step="0.01"
                  min="0"
                  required
                  readOnly
                />
              </div>
              <div className="font-semibold text-lg">
                Total Cost: €{calculateCost()}
              </div>
            </div>

            <h3 className="font-medium mb-2">Payment Info</h3>
            <input
              type="text"
              name="cardNumber"
              placeholder="Card Number"
              value={paymentData.cardNumber}
              onChange={handleInputChange}
              className="w-full mb-2 p-2 border rounded"
              required
            />
            <input
              type="text"
              name="expiryDate"
              placeholder="MM/YY"
              value={paymentData.expiryDate}
              onChange={handleInputChange}
              className="w-full mb-2 p-2 border rounded"
              required
            />
            <input
              type="text"
              name="cvv"
              placeholder="CVV"
              value={paymentData.cvv}
              onChange={handleInputChange}
              className="w-full mb-2 p-2 border rounded"
              required
            />
            <label className="flex items-center mb-4">
              <input
                type="checkbox"
                name="saveCard"
                checked={paymentData.saveCard}
                onChange={handleInputChange}
                className="mr-2"
              />
              Save card for future payments
            </label>

            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={() => {
                  setShowPaymentForm(false);
                  setConsumptionData({
                    energyUsed: "",
                    pricePerKWh: selectedBooking.station?.pricePerKWh || 0.15
                  });
                }}
                className="px-4 py-2 bg-gray-300 rounded"
              >
                Cancel
              </button>
              <button
                onClick={() => handlePay(selectedBookingId)}
                className="px-4 py-2 bg-green-600 text-white rounded"
                disabled={loading || !consumptionData.energyUsed}
              >
                {loading ? "Processing..." : `Pay €${calculateCost()}`}
              </button>
            </div>
          </div>
        </div>
      )}

      <table className="w-full table-auto border border-gray-300 mt-4">
        <thead className="bg-gray-100">
          <tr>
            <th className="border px-4 py-2 text-center">Station</th>
            <th className="border px-4 py-2 text-center">Start Time</th>
            <th className="border px-4 py-2 text-center">End Time</th>
            <th className="border px-4 py-2 text-center">Status</th>
            <th className="border px-4 py-2 text-center">Pay</th>
            <th className="border px-4 py-2 text-center">Delete</th>
          </tr>
        </thead>
        <tbody>
          {bookings.map((booking) => (
            <tr key={booking.id}>
              <td className="border px-4 py-2 text-center">
                {booking.station?.name || "Unknown"}
              </td>
              <td className="border px-4 py-2 text-center">
                {new Date(booking.startTime).toLocaleString("en-GB")}
              </td>
              <td className="border px-4 py-2 text-center">
                {new Date(booking.endTime).toLocaleString("en-GB")}
              </td>
              <td className="border px-4 py-2 text-center">
                {booking.paid ? "Paid" : "Pending"}
              </td>
              <td className="border px-4 py-2 text-center">
                {!booking.paid && (
                  <button
                    className="text-blue-600 hover:underline"
                    onClick={() => {
                      setSelectedBooking(booking);
                      setSelectedBookingId(booking.id);
                      setShowPaymentForm(true);
                      setConsumptionData({
                        energyUsed: "",
                        pricePerKWh: booking.station?.pricePerKWh || 0.15
                      });
                    }}
                  >
                    Pay Now
                  </button>
                )}
              </td>
              <td className="border px-4 py-2 text-center">
                <button
                  className="text-red-600 hover:underline"
                  onClick={() => handleDelete(booking.id)}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {loading && <div className="mt-4">Loading...</div>}
    </div>
  );
};

export default BookingPage;