import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button } from "../components/ui/Button.jsx";
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

const ConsumptionPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [rawData, setRawData] = useState([]);
  const [processedData, setProcessedData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [timeRange, setTimeRange] = useState('day');
  const [stationInfo, setStationInfo] = useState(null);
  const [selectedDate, setSelectedDate] = useState(null);
  const [availableDates, setAvailableDates] = useState({
    days: [],
    weeks: [],
    months: []
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        const stationResponse = await fetch(`/backend/station/${id}`);
        if (!stationResponse.ok) throw new Error('Failed to fetch station data');
        const station = await stationResponse.json();
        setStationInfo(station);

        const consumptionResponse = await fetch(`/backend/consumption/station/${id}`);
        if (!consumptionResponse.ok) throw new Error('Failed to fetch consumption data');
        const data = await consumptionResponse.json();

        setRawData(data);
        extractAvailableDates(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  const extractAvailableDates = (data) => {
    const days = new Set();
    const weeks = new Set();
    const months = new Set();

    data.forEach(item => {
      const date = new Date(item.startTime);
      const dayKey = date.toISOString().split('T')[0];
      const weekKey = `${date.getFullYear()}-W${getWeekNumber(date)}`;
      const monthKey = `${date.getFullYear()}-${date.getMonth()}`;

      days.add(dayKey);
      weeks.add(weekKey);
      months.add(monthKey);
    });

    setAvailableDates({
      days: Array.from(days).map(day => new Date(day)),
      weeks: Array.from(weeks).map(week => {
        const [year, weekNum] = week.split('-W');
        return getDateFromWeek(year, weekNum);
      }),
      months: Array.from(months).map(month => {
        const [year, monthNum] = month.split('-');
        return new Date(year, monthNum);
      })
    });

    // Set initial date to the most recent available
    if (data.length > 0) {
      const lastDate = new Date(data[data.length - 1].startTime);
      setSelectedDate(lastDate);
    }
  };

  const getWeekNumber = (date) => {
    const firstDayOfYear = new Date(date.getFullYear(), 0, 1);
    const pastDaysOfYear = (date - firstDayOfYear) / 86400000;
    return Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7);
  };

  const getDateFromWeek = (year, week) => {
    const simple = new Date(year, 0, 1 + (week - 1) * 7);
    const dow = simple.getDay();
    const ISOweekStart = simple;
    if (dow <= 4) {
      ISOweekStart.setDate(simple.getDate() - simple.getDay() + 1);
    } else {
      ISOweekStart.setDate(simple.getDate() + 8 - simple.getDay());
    }
    return ISOweekStart;
  };

  const filterDataByDate = (date) => {
    if (!date) return rawData;

    let start, end;

    if (timeRange === 'day') {
      start = new Date(date);
      start.setHours(0, 0, 0, 0);
      end = new Date(date);
      end.setHours(23, 59, 59, 999);
    }
    else if (timeRange === 'week') {
      start = new Date(date);
      start.setDate(start.getDate() - start.getDay()); // Start of week (Sunday)
      start.setHours(0, 0, 0, 0);
      end = new Date(start);
      end.setDate(start.getDate() + 6); // End of week (Saturday)
      end.setHours(23, 59, 59, 999);
    }
    else { // month
      start = new Date(date.getFullYear(), date.getMonth(), 1);
      end = new Date(date.getFullYear(), date.getMonth() + 1, 0);
      end.setHours(23, 59, 59, 999);
    }

    return rawData.filter(item => {
      const itemDate = new Date(item.startTime);
      return itemDate >= start && itemDate <= end;
    });
  };

  const processData = (filteredData) => {
    if (timeRange === 'day') {
      return processDailyData(filteredData);
    } else if (timeRange === 'week') {
      return processWeeklyData(filteredData);
    } else {
      return processMonthlyData(filteredData);
    }
  };

  const processDailyData = (data) => {
    const hourlyData = Array(24).fill().map((_, hour) => ({
      hour: `${hour}:00`,
      energyUsed: 0,
      cost: 0,
      count: 0
    }));

    data.forEach(item => {
      const hour = new Date(item.startTime).getHours();
      hourlyData[hour].energyUsed += item.energyUsed;
      hourlyData[hour].cost += item.energyUsed * item.pricePerKWh;
      hourlyData[hour].count++;
    });

    return hourlyData.map(hour => ({
      time: hour.hour,
      energyUsed: hour.energyUsed,
      cost: parseFloat(hour.cost.toFixed(2)),
      avgCost: hour.count > 0 ? parseFloat((hour.cost / hour.count).toFixed(2)) : 0
    }));
  };

  const processWeeklyData = (data) => {
    const dailyData = Array(7).fill().map((_, i) => {
      const dayName = new Date(selectedDate);
      dayName.setDate(dayName.getDate() - dayName.getDay() + i);
      return {
        day: dayName.toLocaleDateString('en-US', { weekday: 'short' }),
        energyUsed: 0,
        cost: 0,
        count: 0
      };
    });

    data.forEach(item => {
      const dayIndex = new Date(item.startTime).getDay(); // 0 (Sun) to 6 (Sat)
      dailyData[dayIndex].energyUsed += item.energyUsed;
      dailyData[dayIndex].cost += item.energyUsed * item.pricePerKWh;
      dailyData[dayIndex].count++;
    });

    return dailyData.map(day => ({
      day: day.day,
      energyUsed: day.energyUsed,
      cost: parseFloat(day.cost.toFixed(2)),
      avgCost: day.count > 0 ? parseFloat((day.cost / day.count).toFixed(2)) : 0
    }));
  };

  const processMonthlyData = (data) => {
    const weeksInMonth = Math.ceil((new Date(selectedDate.getFullYear(), selectedDate.getMonth() + 1, 0).getDate() +
        new Date(selectedDate.getFullYear(), selectedDate.getMonth(), 1).getDay()) / 7);

    const weeklyData = Array(weeksInMonth).fill().map((_, i) => ({
      week: `Week ${i + 1}`,
      energyUsed: 0,
      cost: 0,
      count: 0
    }));

    data.forEach(item => {
      const itemDate = new Date(item.startTime);
      const weekIndex = Math.floor((itemDate.getDate() + new Date(itemDate.getFullYear(), itemDate.getMonth(), 1).getDay() - 1) / 7);
      weeklyData[weekIndex].energyUsed += item.energyUsed;
      weeklyData[weekIndex].cost += item.energyUsed * item.pricePerKWh;
      weeklyData[weekIndex].count++;
    });

    return weeklyData.map(week => ({
      week: week.week,
      energyUsed: week.energyUsed,
      cost: parseFloat(week.cost.toFixed(2)),
      avgCost: week.count > 0 ? parseFloat((week.cost / week.count).toFixed(2)) : 0
    }));
  };

  useEffect(() => {
    if (selectedDate && rawData.length > 0) {
      const filtered = filterDataByDate(selectedDate);
      const processed = processData(filtered);
      setProcessedData(processed);
    }
  }, [selectedDate, timeRange, rawData]);

  const handleDateChange = (date) => {
    setSelectedDate(date);
  };

  const handleTimeRangeChange = (range) => {
    setTimeRange(range);
    // Adjust selected date to nearest available date in new range
    const dates = availableDates[range === 'day' ? 'days' : range === 'week' ? 'weeks' : 'months'];
    if (dates.length > 0 && (!selectedDate || !dates.some(d => d.getTime() === selectedDate.getTime()))) {
      setSelectedDate(dates[dates.length - 1]); // Default to most recent date
    }
  };

  if (loading) return <div className="text-center py-8">Loading consumption data...</div>;
  if (error) return <div className="text-center py-8 text-red-500">Error: {error}</div>;
  if (!selectedDate) return <div className="text-center py-8">No consumption data available</div>;

  return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <Button variant="outline" onClick={() => navigate(-1)}>
            Back to Map
          </Button>
          <div className="flex gap-2">
            <Button
                variant={timeRange === 'day' ? 'default' : 'outline'}
                onClick={() => handleTimeRangeChange('day')}
            >
              Day
            </Button>
            <Button
                variant={timeRange === 'week' ? 'default' : 'outline'}
                onClick={() => handleTimeRangeChange('week')}
            >
              Week
            </Button>
            <Button
                variant={timeRange === 'month' ? 'default' : 'outline'}
                onClick={() => handleTimeRangeChange('month')}
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
              <p className="text-lg"><span className="font-semibold">Charger Types:</span> {stationInfo.chargerTypes?.join(', ') || 'N/A'}</p>
              <p className="text-lg"><span className="font-semibold">Current Price:</span> €{stationInfo.pricePerKWh?.toFixed(2) || '0.00'} per kWh</p>
            </div>
        )}

        <div className="mb-6">
          {timeRange === 'day' && (
              <div className="flex items-center gap-4">
                <span className="font-medium">Select Date:</span>
                <DatePicker
                    selected={selectedDate}
                    onChange={handleDateChange}
                    className="border rounded px-3 py-2"
                    includeDates={availableDates.days}
                    dateFormat="MMMM d, yyyy"
                    placeholderText="Select a day with data"
                />
              </div>
          )}
          {timeRange === 'week' && (
              <div className="flex items-center gap-4">
                <span className="font-medium">Select Week:</span>
                <DatePicker
                    selected={selectedDate}
                    onChange={handleDateChange}
                    className="border rounded px-3 py-2"
                    includeDates={availableDates.weeks}
                    dateFormat="MMMM d, yyyy"
                    placeholderText="Select a week with data"
                    showWeekPicker
                />
              </div>
          )}
          {timeRange === 'month' && (
              <div className="flex items-center gap-4">
                <span className="font-medium">Select Month:</span>
                <DatePicker
                    selected={selectedDate}
                    onChange={handleDateChange}
                    className="border rounded px-3 py-2"
                    includeDates={availableDates.months}
                    dateFormat="MMMM yyyy"
                    placeholderText="Select a month with data"
                    showMonthYearPicker
                />
              </div>
          )}
        </div>

        {/* Rest of your component remains the same */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
          {/* Energy Consumption Chart */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">Energy Consumption (kWh)</h2>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                {timeRange === 'day' ? (
                    <BarChart data={processedData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="time" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="energyUsed" fill="#8884d8" />
                    </BarChart>
                ) : timeRange === 'week' ? (
                    <BarChart data={processedData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="day" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="energyUsed" fill="#8884d8" />
                    </BarChart>
                ) : (
                    <BarChart data={processedData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="week" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="energyUsed" fill="#8884d8" />
                    </BarChart>
                )}
              </ResponsiveContainer>
            </div>
          </div>

          {/* Cost Chart */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-xl font-semibold mb-4">Cost (€)</h2>
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                {timeRange === 'day' ? (
                    <LineChart data={processedData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="time" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Line type="monotone" dataKey="cost" stroke="#82ca9d" />
                    </LineChart>
                ) : timeRange === 'week' ? (
                    <LineChart data={processedData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="day" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Line type="monotone" dataKey="cost" stroke="#82ca9d" />
                    </LineChart>
                ) : (
                    <LineChart data={processedData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="week" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Line type="monotone" dataKey="cost" stroke="#82ca9d" />
                    </LineChart>
                )}
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Detailed Data Table */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-xl font-semibold mb-4">Detailed Data</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
              <tr>
                {timeRange === 'day' && (
                    <>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Hour</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Energy (kWh)</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Cost</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Avg. Cost/kWh</th>
                    </>
                )}
                {timeRange === 'week' && (
                    <>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Day</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Energy (kWh)</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Cost</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Avg. Cost/kWh</th>
                    </>
                )}
                {timeRange === 'month' && (
                    <>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Week</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Energy (kWh)</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Cost</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Avg. Cost/kWh</th>
                    </>
                )}
              </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
              {processedData.filter(item => item.energyUsed > 0).map((item, index) => (
                  <tr key={index}>
                    {timeRange === 'day' && (
                        <>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.time}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.energyUsed.toFixed(2)}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">€{item.cost.toFixed(2)}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">€{item.avgCost.toFixed(2)}</td>
                        </>
                    )}
                    {timeRange === 'week' && (
                        <>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.day}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.energyUsed.toFixed(2)}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">€{item.cost.toFixed(2)}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">€{item.avgCost.toFixed(2)}</td>
                        </>
                    )}
                    {timeRange === 'month' && (
                        <>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.week}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.energyUsed.toFixed(2)}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">€{item.cost.toFixed(2)}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">€{item.avgCost.toFixed(2)}</td>
                        </>
                    )}
                  </tr>
              ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
  );
};

export default ConsumptionPage;