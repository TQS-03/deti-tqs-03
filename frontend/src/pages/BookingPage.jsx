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

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem("user"));
    if (!user?.userId) {
      setError("User not authenticated");
      return;
    }

    const fetchBookings = async () => {
      try {
        const res = await fetch(`/backend/reservation?personId=${user.userId}`);
        const data = await res.json();
        setBookings(data);
      } catch (err) {
        setError("Failed to fetch bookings");
      }
    };
    const fetchStations = async () => {
        try {
          const response = await fetch("backend/station");
          if (!response.ok) {
            throw new Error("Failed to fetch stations");
          }
          const data = await response.json();
          setStations(data);
    
         
        } catch (err) {
          setError(err.message);
        } finally {
        }
      };
    

    fetchBookings();
    fetchStations();
  }, []);

  const handleInputChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const user = JSON.parse(localStorage.getItem("user"));
    if (!user?.userId) {
      setError("User not authenticated");
      return;
    }

    try {
      const res = await fetch("/backend/reservation", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...formData,
          personId: user.userId,
          paid: false,
        }),
      });

      if (!res.ok) throw new Error("Booking failed");

      const newBooking = await res.json();
      setBookings((prev) => [...prev, newBooking]);
      setMessage("Booking successful!");
      setIsModalOpen(false);
      setFormData({ stationId: "", startTime: "", endTime: "" });
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this booking?")) return;
  
    try {
      const res = await fetch(`/backend/reservation/${id}`, {
        method: "DELETE",
      });
  
      if (!res.ok) throw new Error("Failed to delete booking");
  
      setBookings((prev) => prev.filter((b) => b.id !== id));
      setMessage("Booking deleted successfully.");
    } catch (err) {
      setError(err.message);
    }
  };
  

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">My Bookings</h1>
      {message && <p className="text-green-600">{message}</p>}
      {error && <p className="text-red-500">{error}</p>}

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
                <button type="submit" className="px-4 py-2 bg-green-600 text-white rounded">
                  Book
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {bookings.length === 0 ? (
        <p>No bookings found.</p>
      ) : (
        <table className="w-full table-auto border border-gray-300 mt-4">
          <thead className="bg-gray-100">
            <tr>
              <th className="border px-4 py-2">Station</th>
              <th className="border px-4 py-2">Start Time</th>
              <th className="border px-4 py-2">End Time</th>
              <th className="border px-4 py-2">Paid</th>
            </tr>
          </thead>
          <tbody>
            {bookings.map((booking) => (
                <tr key={booking.id}>
                <td className="border px-4 py-2">{booking.station?.name || "Unknown"}</td>
                <td className="border px-4 py-2">{new Date(booking.startTime).toLocaleString()}</td>
                <td className="border px-4 py-2">{new Date(booking.endTime).toLocaleString()}</td>
                <td className="border px-4 py-2">{booking.paid ? "Yes" : "No"}</td>
                <td className="border px-4 py-2 text-center">
                    <button
                    onClick={() => handleDelete(booking.id)}
                    className="text-red-600 hover:text-red-800"
                    >
                    Delete
                    </button>
                </td>
                </tr>
            ))}
        </tbody>

        </table>
      )}
    </div>
  );
};

export default BookingPage;

