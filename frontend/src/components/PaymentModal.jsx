// src/components/PaymentModal.jsx
import { useState } from "react";
import { Modal } from "./ui/Modal";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";

const PaymentModal = ({ isOpen, onClose, amount, onPaymentSuccess }) => {
  const [cardDetails, setCardDetails] = useState({
    cardNumber: "",
    expiryDate: "",
    cvv: "",
    saveCard: false
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setCardDetails(prev => ({ ...prev, [name]: value }));
  };

  const handleCheckboxChange = (e) => {
    setCardDetails(prev => ({ ...prev, saveCard: e.target.checked }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user?.userId) {
        throw new Error("User not authenticated");
      }

      const paymentData = {
        ...cardDetails,
        amount: amount
      };

      const response = await fetch(`backend/driver/payments/${user.userId}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(paymentData)
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.paymentStatus || "Payment failed");
      }

      const result = await response.json();
      if (result.paymentStatus === "SUCCESS") {
        onPaymentSuccess();
        onClose();
      } else {
        throw new Error(result.paymentStatus);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Payment Details">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="bg-blue-50 p-4 rounded mb-4">
          <h3 className="font-bold text-lg">Total Amount: €{amount.toFixed(2)}</h3>
        </div>

        <Input
          label="Card Number"
          name="cardNumber"
          value={cardDetails.cardNumber}
          onChange={handleInputChange}
          placeholder="1234 5678 9012 3456"
          required
        />

        <div className="grid grid-cols-2 gap-4">
          <Input
            label="Expiry Date (MM/YY)"
            name="expiryDate"
            value={cardDetails.expiryDate}
            onChange={handleInputChange}
            placeholder="MM/YY"
            required
          />

          <Input
            label="CVV"
            name="cvv"
            value={cardDetails.cvv}
            onChange={handleInputChange}
            placeholder="123"
            type="password"
            required
          />
        </div>

        <div className="flex items-center">
          <input
            type="checkbox"
            id="saveCard"
            checked={cardDetails.saveCard}
            onChange={handleCheckboxChange}
            className="mr-2"
          />
          <label htmlFor="saveCard">Save this card for future payments</label>
        </div>

        {error && <div className="text-red-500">{error}</div>}

        <div className="flex justify-end gap-2 pt-4">
          <Button
            type="button"
            variant="outline"
            onClick={onClose}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button type="submit" disabled={loading}>
            {loading ? "Processing..." : "Pay Now"}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default PaymentModal;