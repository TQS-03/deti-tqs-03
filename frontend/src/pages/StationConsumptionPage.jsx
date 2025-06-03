import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button } from "../components/ui/Button.jsx";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const StationConsumptionPage = () => {
    const { stationId } = useParams();
    const navigate = useNavigate();
    const [consumptionData, setConsumptionData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [timeRange, setTimeRange] = useState('week'); // 'day', 'week', 'month'

    useEffect(() => {
        const fetchConsumptionData = async () => {
            try {
                setLoading(true);
                const response = await fetch(`backend/consumption/station/${stationId}`);

                if (!response.ok) {
                    throw new Error('Failed to fetch consumption data');
                }

                const data = await response.json();
                setConsumptionData(processData(data, timeRange));
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchConsumptionData();
    }, [stationId, timeRange]);

    const processData = (data, range) => {
        // Process raw data into chart-friendly format based on time range
        // This is a simplified example - you'd need to adapt to your actual data structure
        return data.map(item => ({
            date: new Date(item.startTime).toLocaleDateString(),
            energyUsed: item.energyUsed,
            cost: item.energyUsed * (item.pricePerKWh || 0)
        }));
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

            <h1 className="text-3xl font-bold mb-6">Station Consumption</h1>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
                <div className="bg-white p-6 rounded-lg shadow">
                    <h2 className="text-xl font-semibold mb-4">Energy Consumption (kWh)</h2>
                    <div className="h-80">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={consumptionData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="date" />
                                <YAxis />
                                <Tooltip />
                                <Legend />
                                <Line
                                    type="monotone"
                                    dataKey="energyUsed"
                                    stroke="#8884d8"
                                    activeDot={{ r: 8 }}
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
                                <Tooltip />
                                <Legend />
                                <Line
                                    type="monotone"
                                    dataKey="cost"
                                    stroke="#82ca9d"
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            <div className="bg-white p-6 rounded-lg shadow">
                <h2 className="text-xl font-semibold mb-4">Consumption Details</h2>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Duration</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Energy (kWh)</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Price/kWh</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Cost</th>
                        </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                        {consumptionData.map((item, index) => (
                            <tr key={index}>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.date}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.duration}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.energyUsed.toFixed(2)}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">€{item.pricePerKWh?.toFixed(2) || '0.00'}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">€{item.cost?.toFixed(2) || '0.00'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default StationConsumptionPage;