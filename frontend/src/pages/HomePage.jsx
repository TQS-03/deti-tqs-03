import React from "react";
import ElectroIcon from "../assets/ElectroIcon.png";

function HomePage() {
  return (
    <div className="min-h-screen flex flex-col px-4 py-12">

      <header>
        <h1 className="text-4xl font-bold mb-8 text-center">Welcome to Electro</h1>
      </header>

      {/* Center images horizontally and vertically */}
      <main className="flex-grow flex items-center justify-center">
        <div className="flex gap-40">
          {[1, 2, 3].map((i) => (
            <div key={i} className="flex flex-col items-center">
              <img
                src={ElectroIcon}
                alt={`Electro Icon ${i}`}
                className="w-24 h-24 object-contain"
              />
              <p className="mt-6 text-center text-gray-700">Description for image {i}</p>
            </div>
          ))}
        </div>
      </main>

    </div>
  );
}

export default HomePage;
