import React from "react";

export const Button = ({ children, onClick, type = "button", variant = "primary", ...props }) => {
  const baseClasses = "px-4 py-2 rounded font-semibold transition";
  const variants = {
    primary: "bg-blue-600 text-white hover:bg-blue-700",
    outline: "border border-gray-400 text-gray-700 hover:bg-gray-100",
  };

  return (
    <button
      type={type}
      onClick={onClick}
      className={`${baseClasses} ${variants[variant] || variants.primary}`}
      {...props}
    >
      {children}
    </button>
  );
};
