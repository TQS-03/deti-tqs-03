import React from "react";

export const Select = ({ label, options, isMulti = false, onChange, value = [], ...props }) => {
  // For multi-select, value is an array of selected values
  // For single select, value is a single value or empty string

  const handleChange = (e) => {
    if (isMulti) {
      const selectedOptions = Array.from(e.target.selectedOptions).map(opt => {
        return options.find(o => o.value === opt.value);
      });
      onChange(selectedOptions);
    } else {
      const selectedOption = options.find(o => o.value === e.target.value);
      onChange(selectedOption ? [selectedOption] : []);
    }
  };

  return (
    <div className="flex flex-col">
      {label && (
        <label className="mb-1 font-medium text-gray-700">
          {label}
        </label>
      )}
      <select
        multiple={isMulti}
        value={isMulti ? value.map(v => v.value) : (value[0]?.value || "")}
        onChange={handleChange}
        className="border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
        {...props}
      >
        {!isMulti && <option value="">Select...</option>}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  );
};
