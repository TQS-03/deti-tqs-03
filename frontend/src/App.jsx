// src/App.jsx
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import NavBar from "./components/NavBar";
import HomePage from "./pages/HomePage";
import MapPage from "./pages/MapPage";
import ElectroIcon from "./assets/ElectroIcon.png"

function App() {
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
            { path: "/about", text: "About Us" }
          ]}
        />
        
        <main className="main-content">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/map" element={<MapPage />} />
          </Routes>
        </main>
        
        <footer className="app-footer">
        
        </footer>
      </div>
    </Router>
  );
}

export default App;