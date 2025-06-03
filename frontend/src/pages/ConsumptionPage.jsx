import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

const ConsumptionPage = () => {
  const { id } = useParams();  // <-- change here
  const [consumptions, setConsumptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!id) return; // safety check to avoid fetching with undefined

    const fetchConsumptions = async () => {
      try {
        const response = await fetch(`/backend/consumption/station/${id}`);
        if (!response.ok) {
          throw new Error("Failed to fetch consumption data");
        }
        const data = await response.json();

        console.log(data)
        setConsumptions(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchConsumptions();
  }, [id]);

  if (loading) return <div>Loading consumption data...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Consumption History for Station {id}</h1>
  
      <table className="w-full table-auto border border-gray-300">
        <thead className="bg-gray-100">
          <tr>
            <th className="border px-4 py-2">Start Time</th>
            <th className="border px-4 py-2">End Time</th>
            <th className="border px-4 py-2">Energy Used (kWh)</th>
            <th className="border px-4 py-2">Price per kWh (€)</th>
            <th className="border px-4 py-2">Total Cost (€)</th>
          </tr>
        </thead>
        <tbody>
          {consumptions.map((consumption) => (
            <tr key={consumption.id}>
              <td className="border px-4 py-2 text-center">
                {new Date(consumption.startTime).toLocaleString()}
              </td>
              <td className="border px-4 py-2 text-center">
                {new Date(consumption.endTime).toLocaleString()}
              </td>
              <td className="border px-4 py-2 text-center">
                {consumption.energyUsed.toFixed(2)}
              </td>
              <td className="border px-4 py-2 text-center">
                {consumption.pricePerKWh.toFixed(2)}
              </td>
              <td className="border px-4 py-2 text-center">
                {(consumption.energyUsed * consumption.pricePerKWh).toFixed(2)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
  
};

export default ConsumptionPage;
