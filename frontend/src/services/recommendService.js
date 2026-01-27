// apis/api.js에서 설정한 인스턴스를 가져옵니다.
import api from "../apis/api"; 

const recommendService = {
  /**
   * @param {string} alcohol - 'soju', 'wine', 'beer' 등
   * @param {object} bounds - { minX, minY, maxX, maxY }
   */
  getRecommendations: async (alcohol, bounds) => {
    try {
      // 이미 api 인스턴스에 baseURL이 설정되어 있으므로 상대 경로만
      const response = await api.get(`/recommend/${alcohol}`, {
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
      console.error("추천 API 호출 중 오류 발생:", error);
      // 에러를 던져서 컴포넌트단에서 처리하게 하거나, 빈 배열을 반환
      throw error;
    }
  }
};

export default recommendService;