import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "",
  headers: { "Content-Type": "application/json" },
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const data = err.response?.data;
    let message =
      data?.message ||
      data?.error ||
      "Error inesperado del servidor";

    // Extract field validation errors from @Valid
    if (data?.errors && typeof data.errors === "object") {
      const fieldMsgs = Object.values(data.errors).join("; ");
      if (fieldMsgs) message = fieldMsgs;
    }

    return Promise.reject(new Error(message));
  }
);

export default api;
