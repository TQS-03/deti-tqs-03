import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import NavBar from "./components/NavBar";
import HomePage from "./pages/HomePage";
import MapPage from "./pages/MapPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ProfilePage from "./pages/ProfilePage";
import StationConsumptionPage from "./pages/StationConsumptionPage";
import ElectroIcon from "./assets/ElectroIcon.png";

function AppWrapper() {
    return (
        <Router>
            <App />
        </Router>
    );
}

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const checkAuth = () => {
            const userData = localStorage.getItem("user");
            if (userData) {
                const user = JSON.parse(userData);
                const isExpired = new Date().getTime() - user.timestamp > 604800000;
                if (!isExpired) {
                    setIsAuthenticated(true);
                } else {
                    localStorage.removeItem("user");
                }
            }
        };
        checkAuth();
    }, []);

    const updateAuth = (authStatus, userData = null) => {
        setIsAuthenticated(authStatus);
        if (userData) {
            localStorage.setItem("user", JSON.stringify({
                userId: userData.userId,
                email: userData.email,
                isWorker: userData.isWorker,
                timestamp: new Date().getTime()
            }));
        } else {
            localStorage.removeItem("user");
        }
    };

    const handleLogout = () => {
        updateAuth(false);
        navigate("/login");
    };

    const PrivateRoute = ({ element }) => {
        const user = localStorage.getItem("user");
        const userObj = user ? JSON.parse(user) : null;
        const isUserValid = userObj && (new Date().getTime() - userObj.timestamp <= 604800000);
        return isAuthenticated || isUserValid ? element : <Navigate to="/login" />;
    };

    const navLinks = [
        { path: "/", text: "Home", exact: true },
        { path: "/map", text: "Interactive Map" },
        ...(isAuthenticated || localStorage.getItem("user") ? [{ path: "/bookings", text: "My Bookings" }] : []),
        { path: "/about", text: "About Us" },
        isAuthenticated || localStorage.getItem("user")
            ? {
                path: "/logout",
                text: `Logout (${JSON.parse(localStorage.getItem("user"))?.email || 'User'})`,
                onClick: handleLogout
            }
            : { path: "/login", text: "Login" },
    ];

    return (
        <div className="app-container">
            <NavBar
                logoText={
                    <div className="flex items-center gap-2">
                        <img src={ElectroIcon} alt="Electro Logo" className="w-6 h-6" />
                        <span>Electro</span>
                    </div>
                }
                links={navLinks}
            />

            <main className="main-content">
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route
                        path="/login"
                        element={<LoginPage setIsAuthenticated={updateAuth} />}
                    />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/map" element={<PrivateRoute element={<MapPage />} />} />
                    <Route path="/profile" element={<PrivateRoute element={<ProfilePage />} />} />
                    <Route
                        path="/station/:stationId/consumption"
                        element={<PrivateRoute element={<StationConsumptionPage />} />}
                    />
                </Routes>
            </main>

            <footer className="app-footer">{/* Footer content */}</footer>
        </div>
    );
}

export default AppWrapper;