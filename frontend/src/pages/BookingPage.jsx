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

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user?.userId) {
        setError("User not authenticated");
        return;
      }

      const res = await fetch(`/backend/reservation?personId=${user.userId}`);
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
      const response = await fetch("/backend/station");
      if (!response.ok) throw new Error("Failed to fetch stations");
      const data = await response.json();
      
      // Ensure we have proper UUIDs and charger types
      const formattedStations = data.map(station => ({
        ...station,
        id: station.id || UUID(), // Ensure ID exists
        chargerTypes: station.chargerTypes || [] // Ensure array exists
      }));
      
      setStations(formattedStations);
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    fetchBookings();
    fetchStations();
  }, []);

  const handleInputChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user?.userId) {
        throw new Error("User not authenticated");
      }

      // Validate the selected station
      const selectedStation = stations.find(s => s.id === formData.stationId);
      if (!selectedStation) {
        throw new Error("Invalid station selected");
      }

      // Prepare the reservation data according to your DTO structure
      const reservationData = {
        stationId: formData.stationId,
        personId: user.userId,
        startTime: new Date(formData.startTime).toISOString(),
        endTime: new Date(formData.endTime).toISOString(),
        // Include any additional required fields from your Reservation DTO
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
      
      // Refresh data
      await fetchBookings();
    } catch (err) {
      setError(err.message);
    }
  };

  // Helper to generate UUIDs if needed
  const UUID = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
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
              <div>
                <label className="block mb-1">Station</label>
                <select
                  name="stationId"
                  value={formData.stationId}
                  onChange={handleInputChange}
                  required
                  className="w-full border rounded p-2"
                >
                  <option value="">Select a station</option>
                  {stations.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block mb-1">Start Time</label>
                <input
                  type="datetime-local"
                  name="startTime"
                  value={formData.startTime}
                  onChange={handleInputChange}
                  required
                  min={new Date().toISOString().slice(0, 16)}
                  className="w-full border rounded p-2"
                />
              </div>

              <div>
                <label className="block mb-1">End Time</label>
                <input
                  type="datetime-local"
                  name="endTime"
                  value={formData.endTime}
                  onChange={handleInputChange}
                  required
                  min={formData.startTime || new Date().toISOString().slice(0, 16)}
                  className="w-full border rounded p-2"
                />
              </div>

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
                  disabled={loading}
                >
                  {loading ? "Processing..." : "Book"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {loading && bookings.length === 0 ? (
        <p>Loading bookings...</p>
      ) : bookings.length === 0 ? (
        <p>No bookings found.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full table-auto border border-gray-300 mt-4">
            <thead className="bg-gray-100">
              <tr>
                <th className="border px-4 py-2">Station</th>
                <th className="border px-4 py-2">Start Time</th>
                <th className="border px-4 py-2">End Time</th>
                <th className="border px-4 py-2">Status</th>
                <th className="border px-4 py-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((booking) => (
                <tr key={booking.id}>
                  <td className="border px-4 py-2">
                    {stations.find(s => s.id === booking.stationId)?.name || "Unknown Station"}
                  </td>
                  <td className="border px-4 py-2">
                    {new Date(booking.startTime).toLocaleString()}
                  </td>
                  <td className="border px-4 py-2">
                    {new Date(booking.endTime).toLocaleString()}
                  </td>
                  <td className="border px-4 py-2">
                    {booking.paid ? "Paid" : "Pending"}
                  </td>
                  <td className="border px-4 py-2 text-center">
                    <button
                      onClick={() => handleDelete(booking.id)}
                      className="text-red-600 hover:text-red-800"
                      disabled={loading}
                    >
                      {loading ? "Processing..." : "Delete"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default BookingPage;