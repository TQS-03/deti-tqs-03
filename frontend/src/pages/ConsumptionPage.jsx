import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button } from "../components/ui/Button.jsx";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const ConsumptionPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [consumptionData, setConsumptionData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [timeRange, setTimeRange] = useState('week'); // 'day', 'week', 'month'
  const [stationInfo, setStationInfo] = useState(null);

  useEffect(() => {
    if (!id) return;

    const fetchData = async () => {
      try {
        setLoading(true);

        // Fetch station info
        const stationResponse = await fetch(`/backend/station/${id}`);
        if (!stationResponse.ok) throw new Error('Failed to fetch station info');
        const station = await stationResponse.json();
        setStationInfo(station);

        // Fetch consumption data
        const consumptionResponse = await fetch(`/backend/consumption/station/${id}`);
        if (!consumptionResponse.ok) throw new Error('Failed to fetch consumption data');
        const data = await consumptionResponse.json();

        setConsumptionData(processData(data, timeRange));
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id, timeRange]);

  const processData = (data, range) => {
    // Group data by date and calculate totals
    const groupedData = data.reduce((acc, item) => {
      const date = new Date(item.startTime).toLocaleDateString();
      if (!acc[date]) {
        acc[date] = {
          date,
          energyUsed: 0,
          cost: 0,
          sessions: 0
        };
      }
      acc[date].energyUsed += item.energyUsed;
      acc[date].cost += item.energyUsed * (item.pricePerKWh || stationInfo?.pricePerKWh || 0);
      acc[date].sessions += 1;
      return acc;
    }, {});

    return Object.values(groupedData).sort((a, b) => new Date(a.date) - new Date(b.date));
  };

  if (loading) return <div className="text-center py-8">Loading consumption data...</div>;
  if (error) return <div className="text-center py-8 text-red-500">Error: {error}</div>;

  return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <Button variant="outline" onClick={() => navigate(-1)}>
            Back to Map
          </Button>
          <div className="flex gap-2">
            <Button
                variant={timeRange === 'day' ? 'default' : 'outline'}
                onClick={() => setTimeRange('day')}
            >
              Day
            </Button>
            <Button
                variant={timeRange === 'week' ? 'default' : 'outline'}
                onClick={() => setTimeRange('week')}
            >
              Week
            </Button>
            <Button
                variant={timeRange === 'month' ? 'default' : 'outline'}
                onClick={() => setTimeRange('month')}
            >
              Month
            </Button>
          </div>
        </div>

        <h1 className="text-3xl font-bold mb-6">
          {stationInfo?.name || 'Station'} Consumption
        </h1>
        {stationInfo && (
            <div className="mb-8 p-4 bg-gray-50 rounded-lg">
              <p className="text-lg"><span className="font-semibold">Address:</span> {stationInfo.address}</p>
              <p className="text-lg"><span className="font-semibold">Current Price:</span> €{stationInfo.pricePerKWh?.toFixed(2) || '0.00'} per kWh</p>
              <p className="text-lg"><span className="font-semibold">Charger Types:</span> {stationInfo.chargerTypes?.join(", ") || "N/A"}</p>
            </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">Energy Consumption (kWh)</h2>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={consumptionData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip
                      formatter={(value) => [`${value} kWh`, "Energy Used"]}
                      labelFormatter={(date) => `Date: ${date}`}
                  />
                  <Legend />
                  <Line
                      type="monotone"
                      dataKey="energyUsed"
                      stroke="#8884d8"
                      activeDot={{ r: 8 }}
                      name="Energy Used"
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">Cost (€)</h2>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={consumptionData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip
                      formatter={(value) => [`€${value.toFixed(2)}`, "Total Cost"]}
                      labelFormatter={(date) => `Date: ${date}`}
                  />
                  <Legend />
                  <Line
                      type="monotone"
                      dataKey="cost"
                      stroke="#82ca9d"
                      name="Total Cost"
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-xl font-semibold mb-4">Detailed Consumption</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sessions</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Energy (kWh)</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Cost (€)</th>
              </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
              {consumptionData.map((item, index) => (
                  <tr key={index}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.date}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.sessions}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.energyUsed.toFixed(2)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">€{item.cost.toFixed(2)}</td>
                  </tr>
              ))}
              {consumptionData.length > 1 && (
                  <tr className="bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">Total</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {consumptionData.reduce((sum, item) => sum + item.sessions, 0)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {consumptionData.reduce((sum, item) => sum + item.energyUsed, 0).toFixed(2)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      €{consumptionData.reduce((sum, item) => sum + item.cost, 0).toFixed(2)}
                    </td>
                  </tr>
              )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
  );
};

export default ConsumptionPage;