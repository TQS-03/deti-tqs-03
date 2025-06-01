import React, { useState } from "react";
import { Button } from "../components/ui/Button.jsx";
import { Input } from "../components/ui/Input.jsx";
import { useNavigate } from "react-router-dom";

const LoginPage = ({ setIsAuthenticated }) => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
      
        try {
          const response = await fetch("backend/auth/login", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ email: username, password }),
          });
      
          if (!response.ok) {
            const message = await response.text();
            throw new Error(message || "Login failed");
          }
      
          const data = await response.json();
          console.log("Login successful:", data);
      
          // Save user data in localStorage for later use
          localStorage.setItem("user", JSON.stringify(data));
      
          setIsAuthenticated(true);
          navigate("/map");
        } catch (err) {
          setError(err.message);
        }
      };
      
      

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
            <form onSubmit={handleSubmit} className="bg-white p-8 rounded shadow-md w-full max-w-md">
                <h1 className="text-2xl font-bold mb-6 text-center">Login</h1>
                <Input
                    type="email"
                    placeholder="Email"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
                <Input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    className="mt-4"
                />
                {error && <p className="text-red-500 text-sm mt-2">{error}</p>}
                <Button type="submit" className="mt-6 w-full">
                    Login
                </Button>
            </form>
        </div>
    );
};

export default LoginPage;
