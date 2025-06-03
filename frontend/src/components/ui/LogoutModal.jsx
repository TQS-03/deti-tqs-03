import { Modal } from "./Modal";

export const LogoutConfirmationModal = ({ isOpen, onClose, onConfirm }) => {
    const handleConfirm = () => {
        onConfirm();
        onClose();
        window.location.reload();
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Confirm Logout">
            <div className="space-y-4 p-4">
                <p className="text-gray-700">Are you sure you want to logout?</p>
                <div className="flex justify-end space-x-3">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium hover:bg-gray-50"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleConfirm}
                        className="px-4 py-2 bg-red-600 text-white rounded-md text-sm font-medium hover:bg-red-700"
                    >
                        Logout
                    </button>
                </div>
            </div>
        </Modal>
    );
};