import { Link, useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import { Modal } from "./ui/Modal";
import styles from '../css/NavBar.module.css';

function NavBar({
                  logoText = "Electro",
                  logoTo = "/",
                  className = "",
                  showActive = true,
                }) {
  const location = useLocation();
  const navigate = useNavigate();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const user = JSON.parse(localStorage.getItem("user"));

  const navLinks = [
    { path: "/", text: "Home", exact: true },
    { path: "/map", text: "Interactive Map" },
    ...(user ? [{ path: "/bookings", text: "My Bookings" }] : []),
    { path: "/about", text: "About Us" }
  ];

  const isActive = (path, exact = false) => {
    return exact
      ? location.pathname === path
      : location.pathname.startsWith(path);
  };

  return (
    <nav className={`${styles.navbar} ${className}`} aria-label="Main navigation">
      <div className={styles.navbarContainer}>
        <Link
          to={logoTo}
          className={styles.navbarLogo}
          aria-label="Home page"
        >
          {logoText}
        </Link>

        <ul className={styles.navMenu}>
          {navLinks.map((link, index) => (
            <li key={`${link.path}-${index}`} className={styles.navItem}>
              <Link
                id={`nav-${link.text.replace(/\s+/g, "-").toLowerCase()}`}
                to={link.path}
                className={`${styles.navLinks} ${showActive && isActive(link.path, link.exact) ? styles.active : ""
                  }`}
                aria-current={
                  showActive && isActive(link.path, link.exact) ? "page" : undefined
                }
              >
                Cancel
              </button>
              <button
                  onClick={handleLogout}
                  className="px-4 py-2 bg-red-600 text-white rounded-md text-sm font-medium hover:bg-red-700"
              >
                Logout
              </button>
            </div>
          </div>
        </Modal>
      </>
  );
}

export default NavBar;
