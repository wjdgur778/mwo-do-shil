import { Restaurant, CATEGORIES } from "@shared/schema";

// Mock Data - Realistic Seoul Locations
const MOCK_RESTAURANTS: Restaurant[] = [
  {
    id: 1,
    name: "우래옥 본점",
    category: "soju",
    pairingReason: "진한 육향의 평양냉면 국물은 소주의 알싸함을 부드럽게 감싸주며, 불고기의 달큰한 맛이 소주 한 잔을 부릅니다.",
    signatureMenu: "평양냉면 & 불고기",
    lat: 37.5682,
    lng: 126.9987,
    address: "서울 중구 창경궁로 62-29",
    imageUrl: "https://images.unsplash.com/photo-1596452292375-7b58c7349941?auto=format&fit=crop&q=80&w=800"
  },
  {
    id: 2,
    name: "몽탄",
    category: "wine",
    pairingReason: "짚불 훈연 향이 배어있는 우대갈비의 기름진 풍미는 타닌감이 있는 레드와인과 환상적인 마리아주를 이룹니다.",
    signatureMenu: "우대갈비",
    lat: 37.5318,
    lng: 126.9715,
    address: "서울 용산구 백범로99길 50",
    imageUrl: "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&q=80&w=800"
  },
  {
    id: 3,
    name: "코블러",
    category: "whiskey",
    pairingReason: "묵직한 바디감의 위스키는 달콤하고 꾸덕한 코블러 파이와 함께할 때 그 스모키한 향이 더욱 돋보입니다.",
    signatureMenu: "코블러 파이",
    lat: 37.5796,
    lng: 126.9723,
    address: "서울 종로구 사직로12길 1-23",
    imageUrl: "https://images.unsplash.com/photo-1514362545857-3bc16549766b?auto=format&fit=crop&q=80&w=800"
  },
  {
    id: 4,
    name: "크래프트루",
    category: "beer",
    pairingReason: "바삭한 피쉬앤칩스의 기름진 맛을 시원하고 청량한 수제 맥주가 깔끔하게 씻어줍니다.",
    signatureMenu: "수제버거 & 칩스",
    lat: 37.5744,
    lng: 126.9895,
    address: "서울 종로구 익선동 166-31",
    imageUrl: "https://images.unsplash.com/photo-1571091718767-18b5b1457add?auto=format&fit=crop&q=80&w=800"
  },
  {
    id: 5,
    name: "미장플라스",
    category: "cocktail",
    pairingReason: "상큼한 시트러스 향의 칵테일은 가벼운 타파스 요리의 풍미를 해치지 않으면서 입맛을 돋워줍니다.",
    signatureMenu: "제철 과일 타파스",
    lat: 37.5558,
    lng: 126.9234,
    address: "서울 마포구 와우산로 35",
    imageUrl: "https://images.unsplash.com/photo-1514362545857-3bc16549766b?auto=format&fit=crop&q=80&w=800"
  },
  {
    id: 6,
    name: "느린마을 양조장",
    category: "makgeolli",
    pairingReason: "부드럽고 크리미한 막걸리는 매콤달콤한 김치전이나 보쌈과 함께할 때 최고의 밸런스를 보여줍니다.",
    signatureMenu: "수제 막걸리 & 모듬전",
    lat: 37.4987,
    lng: 127.0276,
    address: "서울 서초구 강남대로 419",
    imageUrl: "https://images.unsplash.com/photo-1626202378949-d759654d216d?auto=format&fit=crop&q=80&w=800"
  }
];

export const mockApi = {
  fetchRestaurants: async (category?: string): Promise<Restaurant[]> => {
    // Simulate API delay for "AI Analysis" effect
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    if (!category) return MOCK_RESTAURANTS;
    
    // Filter by category
    const filtered = MOCK_RESTAURANTS.filter(r => r.category === category);
    
    // If no exact match (e.g., sake), return random recommendation to ensure demo works
    if (filtered.length === 0) {
      return [MOCK_RESTAURANTS[Math.floor(Math.random() * MOCK_RESTAURANTS.length)]];
    }
    
    return filtered;
  },
  
  getRestaurant: async (id: number): Promise<Restaurant | undefined> => {
    await new Promise(resolve => setTimeout(resolve, 500));
    return MOCK_RESTAURANTS.find(r => r.id === id);
  }
};
