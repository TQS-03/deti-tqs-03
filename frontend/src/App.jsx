import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { useState } from "react";
import NavBar from "./components/NavBar";
import HomePage from "./pages/HomePage";
import MapPage from "./pages/MapPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ElectroIcon from "./assets/ElectroIcon.png";

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    const PrivateRoute = ({ element }) => {
        return isAuthenticated ? element : <Navigate to="/login" />;
    };

    return (
        <Router>
            <div className="app-container">
                <NavBar
                    logoText={
                        <div className="flex items-center gap-2">
                            <img src={ElectroIcon} alt="Electro Logo" className="w-6 h-6" />
                            <span>Electro</span>
                        </div>
                    }
                    links={[
                        { path: "/", text: "Home", exact: true },
                        { path: "/map", text: "Interactive Map" },
                        { path: "/about", text: "About Us" },
                        isAuthenticated
                            ? { path: "/logout", text: "Logout", onClick: () => setIsAuthenticated(false) }
                            : { path: "/login", text: "Login" }
                    ].filter(Boolean)}
                />

                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/login" element={<LoginPage setIsAuthenticated={setIsAuthenticated} />} />
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path="/map" element={<PrivateRoute element={<MapPage />} />} />
                    </Routes>
                </main>

                <footer className="app-footer">
                    {/* Footer content */}
                </footer>
            </div>
        </Router>
    );
}

export default App;