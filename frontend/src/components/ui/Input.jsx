import React from "react";

export const Input = ({ label, name, value, onChange, type = "text", required = false, ...props }) => {
  return (
    <div className="flex flex-col">
      {label && (
        <label htmlFor={name} className="mb-1 font-medium text-gray-700">
          {label}
        </label>
      )}
      <input
        id={name}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        required={required}
        className="border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
        {...props}
      />
    </div>
  );
};
