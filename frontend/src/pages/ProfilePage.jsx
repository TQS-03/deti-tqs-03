import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "../components/ui/Button.jsx";

function ProfilePage() {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const userData = JSON.parse(localStorage.getItem("user"));
        if (!userData) {
            navigate("/login");
        } else {
            setUser(userData);
        }
    }, [navigate]);

    if (!user) {
        return <div>Loading...</div>;
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-lg shadow-md">
                <div className="text-center">
                    <div className="mx-auto flex items-center justify-center h-20 w-20 rounded-full bg-blue-100 mb-4">
            <span className="text-2xl font-bold text-blue-600">
              {user.email.charAt(0).toUpperCase()}
            </span>
                    </div>
                    <h2 className="mt-6 text-2xl font-extrabold text-gray-900">
                        User Profile
                    </h2>
                </div>

                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Email</label>
                        <div className="mt-1 p-2 bg-gray-50 rounded-md">
                            {user.email}
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700">User ID</label>
                        <div className="mt-1 p-2 bg-gray-50 rounded-md">
                            {user.userId}
                        </div>
                    </div>

                    <div className="pt-4">
                        <Button
                            className="w-full"
                            onClick={() => navigate("/map")}
                        >
                            Back to Map
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ProfilePage;