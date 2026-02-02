import axios from "axios";
import { auth } from "../config/firebase-config"; // 설정 임포트
import { signInAnonymously } from "firebase/auth";

// 1. 일반 API (인증 필요 없음)
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { "Content-Type": "application/json" },
  withCredentials: true,
});

// 2. 인증 전용 API (Firebase 토큰 포함)
export const authApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { "Content-Type": "application/json" },
  withCredentials: true,
});

// [핵심] authApi 전용 인터셉터 설정
authApi.interceptors.request.use(
  async (config) => {
    let user = auth.currentUser;

    // 사용자가 없으면 익명 로그인 시도
    if (!user) {
      const userCredential = await signInAnonymously(auth);
      user = userCredential.user;
    }

    // 최신 토큰 가져오기
    const token = await user.getIdToken();
    config.headers.Authorization = `Bearer ${token}`;
    
    return config;
  },
  (error) => Promise.reject(error)
);