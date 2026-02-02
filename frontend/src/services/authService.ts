import { auth } from "../config/firebase-config";
import { signInAnonymously } from "firebase/auth";

export const getValidToken = async () => {
  try {
    // 1. 현재 로그인된 사용자가 있는지 확인
    let user = auth.currentUser;

    // 2. 없으면 익명 로그인 수행 (UID 발급)
    if (!user) {
      const userCredential = await signInAnonymously(auth);
      user = userCredential.user;
      console.log("Anonymous ID 발급 완료:", user.uid);
    }

    // 3. 서버 검증용 ID 토큰(JWT) 추출
    // forceRefresh를 true로 하면 만료 여부와 상관없이 새로 갱신된 토큰을 가져옵니다.
    const idToken = await user.getIdToken(false);
    return idToken;
  } catch (error) {
    console.error("Firebase Auth 에러:", error);
    throw error;
  }
};