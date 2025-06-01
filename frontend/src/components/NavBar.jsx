import { Link, useLocation } from "react-router-dom";
import styles from '../css/NavBar.module.css'; // CSS Module import

function NavBar({
  logoText = "MyApp",
  logoTo = "/",
  links = [],
  className = "",
  showActive = true,
}) {
  const location = useLocation();
  const defaultLinks = [
    { path: "/", text: "Home", exact: true },
    { path: "/map", text: "Map" },
  ];

  const navLinks = links.length > 0 ? links : defaultLinks;

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
                {link.onClick ? (
                    <button
                        onClick={link.onClick}
                        className={`${styles.navLinks}`}
                    >
                      {link.icon && <span className={styles.navIcon}>{link.icon}</span>}
                      {link.text}
                    </button>
                ) : (
                    <Link
                        to={link.path}
                        className={`${styles.navLinks} ${
                            showActive && isActive(link.path, link.exact) ? styles.active : ""
                        }`}
                        aria-current={
                          showActive && isActive(link.path, link.exact) ? "page" : undefined
                        }
                    >
                      {link.icon && <span className={styles.navIcon}>{link.icon}</span>}
                      {link.text}
                    </Link>
                )}
              </li>
          ))}
        </ul>
      </div>
    </nav>
  );
}

export default NavBar;