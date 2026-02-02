// apis/api.js에서 설정한 인스턴스를 가져옵니다.
import { authApi } from "../apis/api"; 

const recommendService = {
  /**
   * @param {string} alcohol - 'soju', 'wine', 'beer' 등
   * @param {object} bounds - { minX, minY, maxX, maxY }
   */
  getRecommendations: async (alcohol, bounds) => {
    try {
      // 이미 api 인스턴스에 baseURL이 설정되어 있으므로 상대 경로만
      const response = await authApi.get(`/api/recommend/${alcohol}`, {
        params: {
          minX: bounds.minX,
          minY: bounds.minY,
          maxX: bounds.maxX,
          maxY: bounds.maxY
        }
      });

      // Spring Boot의 ResponseEntity<Result> 구조에서 실제 데이터 리스트 반환
      return response.data.data; 
    } catch (error) {
      throw error;
    }
  },

  // 1. 페이지 로드 시 또는 필요할 때 남은 횟수 조회
  getRemainingCount: async () => {
    try {
      // authApi 인터셉터가 Firebase 토큰을 자동으로 헤더에 넣어줍니다.
      const response = await authApi.get("/api/recommend/remaining");
      return response.data.data; // 예: 2
    } catch (error) {
      console.error("횟수 조회 실패:", error);
      return 0;
    }
  },

  // 1. 페이지 로드 시 또는 필요할 때 남은 횟수 조회
  checkRemainingCount: async () => {
    try {
      // authApi 인터셉터가 Firebase 토큰을 자동으로 헤더에 넣어줍니다.
      const response = await authApi.get("/api/recommend/check");
      console.log(response.data);
      return response.data.remaining; // 예: 2
    } catch (error) {
      console.error("횟수 조회 실패:", error);
      return 0;
    }
  },
  
};

export default recommendService;