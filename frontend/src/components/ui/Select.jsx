import React from "react";

export const Select = ({ label, options, isMulti = false, onChange, value = null, ...props }) => {
  const { isClearable, ...selectProps } = props;

  const handleChange = (e) => {
    if (isMulti) {
      const selectedOptions = Array.from(e.target.selectedOptions).map(opt =>
        options.find(o => o.value === opt.value)
      );
      onChange(selectedOptions);
    } else {
      const selectedOption = options.find(o => o.value === e.target.value);
      onChange(selectedOption || null); // ✅ Return a single object or null
    }
  };

  const getValue = () => {
    if (isMulti) {
      return Array.isArray(value) ? value.map(v => v.value) : [];
    } else {
      return value?.value || "";
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
        value={getValue()}
        onChange={handleChange}
        className="border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
        {...selectProps}
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
