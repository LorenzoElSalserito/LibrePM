import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

/**
 * NotificationConfig - Configurazione sistema notifiche toast
 * 
 * Wrapper per react-toastify con tema custom LibrePM
 * 
 * @author Lorenzo DM
 * @since 0.2.0
 */
export default function NotificationConfig() {
    return (
        <ToastContainer
            position="bottom-right"
            autoClose={3000}
            hideProgressBar={false}
            newestOnTop
            closeOnClick
            rtl={false}
            pauseOnFocusLoss
            draggable
            pauseOnHover
            theme="dark"
            style={{
                fontSize: "0.9rem",
            }}
            toastStyle={{
                backgroundColor: "#212529",
                color: "#f8f9fa",
                borderRadius: "12px",
                border: "1px solid rgba(255, 255, 255, 0.1)",
            }}
        />
    );
}
