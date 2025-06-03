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

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
    setShowLogoutModal(false);
    window.location.reload(); // Força o refresh para atualizar a navbar
  };

  return (
      <>
        <nav className={`${styles.navbar} ${className}`} aria-label="Main navigation">
          <div className={styles.navbarContainer}>
            <Link to={logoTo} className={styles.navbarLogo} aria-label="Home page">
              {logoText}
            </Link>

            <ul className={styles.navMenu}>
              {navLinks.map((link, index) => (
                  <li key={`${link.path}-${index}`} className={styles.navItem}>
                    <Link
                        to={link.path}
                        className={`${styles.navLinks} ${
                            showActive && isActive(link.path, link.exact) ? styles.active : ""
                        }`}
                        aria-current={
                          showActive && isActive(link.path, link.exact) ? "page" : undefined
                        }
                    >
                      {link.text}
                    </Link>
                  </li>
              ))}

              {user ? (
                  <li className={`${styles.navItem} ${styles.dropdownContainer}`}>
                    <button
                        className={`${styles.navLinks} ${styles.profileButton}`}
                        onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                        aria-expanded={isDropdownOpen}
                    >
                  <span className={styles.userInitial}>
                    {user.email.charAt(0).toUpperCase()}
                  </span>
                      <span className={styles.userEmail}>
                    {user.email}
                  </span>
                    </button>

                    {isDropdownOpen && (
                        <div className={styles.dropdownMenu}>
                          <Link
                              to="/profile"
                              className={styles.dropdownItem}
                              onClick={() => setIsDropdownOpen(false)}
                          >
                            <i className="fas fa-user mr-2"></i> Profile
                          </Link>
                          <button
                              className={`${styles.dropdownItem} ${styles.logoutItem}`}
                              onClick={() => {
                                setIsDropdownOpen(false);
                                setShowLogoutModal(true);
                              }}
                          >
                            <i className="fas fa-sign-out-alt mr-2"></i> Logout
                          </button>
                        </div>
                    )}
                  </li>
              ) : (
                  <li className={styles.navItem}>
                    <Link
                        to="/login"
                        className={`${styles.navLinks} ${
                            showActive && isActive("/login", true) ? styles.active : ""
                        }`}
                    >
                      Login
                    </Link>
                  </li>
              )}
            </ul>
          </div>
        </nav>

        <Modal
            isOpen={showLogoutModal}
            onClose={() => setShowLogoutModal(false)}
            title="Confirm Logout"
        >
          <div className="space-y-4">
            <p className="text-gray-700">Are you sure you want to logout?</p>
            <div className="flex justify-end space-x-3">
              <button
                  onClick={() => setShowLogoutModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium hover:bg-gray-50"
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