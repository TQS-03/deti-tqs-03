// src/pages/PaymentHistoryPage.jsx
import { useState, useEffect } from "react";
import { Table } from "../components/ui/Table";

const PaymentHistoryPage = () => {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchPaymentHistory = async () => {
      try {
        const user = JSON.parse(localStorage.getItem("user"));
        if (!user?.userId) {
          throw new Error("User not authenticated");
        }

        const response = await fetch(`/backend/driver/payments/${user.userId}`);
        if (!response.ok) {
          throw new Error("Failed to fetch payment history");
        }
        const data = await response.json();
        setPayments(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchPaymentHistory();
  }, []);

  if (loading) return <div>Loading payment history...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">Payment History</h1>
      
      <div className="bg-white rounded-lg shadow-md p-6">
        {payments.length === 0 ? (
          <p>No payment history found.</p>
        ) : (
          <Table
            columns={[
              { header: "Date", accessor: "date" },
              { header: "Transaction ID", accessor: "id" },
              { header: "Amount", accessor: "amount" },
              { header: "Card", accessor: "card" },
              { header: "Status", accessor: "status" }
            ]}
            data={payments.map(payment => ({
              date: new Date(payment.timestamp).toLocaleString(),
              id: payment.id,
              amount: `€${payment.amount.toFixed(2)}`,
              card: `•••• ${payment.last4Digits}`,
              status: "Completed"
            }))}
          />
        )}
      </div>
    </div>
  );
};

export default PaymentHistoryPage;
