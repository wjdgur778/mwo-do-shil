import { useState, useEffect, useRef } from "react";
import { Map, MapMarker as KakaoMapMarker } from "react-kakao-maps-sdk";
import confetti from "canvas-confetti";
import { Search, MapPin, Wine, X, SearchX, AlertCircle } from "lucide-react";
import { CategoryBar } from "@/components/CategoryBar";
import { MapMarker } from "@/components/MapMarker";
import { RestaurantCard } from "@/components/RestaurantCard";
import { AILoading } from "@/components/AILoading";
import { AnimatePresence, motion } from "framer-motion";
import { LIQUOR_LIST } from "../data/liquorData"; // 주종 데이터 임포트
import recommendService from "../services/recommendService"; // 서비스 임포트

const SEOUL_CENTER = { lat: 37.5665, lng: 126.9780 };
const MOCK_DATA = [
  {
    place: {
      id: 16768193,
      place_name: "전가",
      category_name: "음식점 > 술집 > 호프,요리주점",
      address_name: "서울 강남구 대치동 936-33",
      phone: "02-562-0337",
      x: "127.05295793692005",
      y: "37.497265978265595",
      place_url: "http://place.map.kakao.com/16768193"
    },
    reason: "여기 '전가' 안주 퀄리티 미쳤어! 특히 튀김류는 진짜 겉바속촉 제대로던데? 🍢 맥주랑도 물론 잘 어울리지만, 얼큰한 국물 요리 시켜서 소주랑 같이 먹으면 추운 날씨에도 딱이지! 🔥",
    score: 900
  },
  {
    place: {
      id: 1338149995,
      place_name: "주교리3구",
      category_name: "음식점 > 한식 > 육류,고기",
      address_name: "서울 강남구 대치동 936-33",
      phone: "",
      x: "127.0531",
      y: "37.4975"
    },
    reason: "여긴 '주교리3구'인데, 육류 전문점이라 소주랑 곁들일 고기 메뉴가 많아! 👍 특히 숙성 삼겹살이 그렇게 맛있대. 기름진 고기랑 짭짤한 소주 한잔이면 스트레스 확 풀리지~ 😉",
    score: 850
  },
  {
    place: {
      id: 7829718,
      place_name: "대치골한우곱창",
      category_name: "음식점 > 한식 > 육류,고기 > 곱창,막창",
      address_name: "서울 강남구 대치동 922-17",
      phone: "02-501-7418",
      x: "127.0520",
      y: "37.4965"
    },
    reason: "'대치골한우곱창'은 곱창 대창 막창 안주로 소주 마시기 딱이야! 💯 쫄깃한 식감이 살아있는 곱창구이랑 시원한 소주 한 잔이면 천국이 따로 없어! ✨",
    score: 800
  },
  {
    place: {
      id: 26829587,
      place_name: "하남돼지집 한티역점",
      category_name: "음식점 > 한식 > 육류,고기 > 삼겹살 > 하남돼지집",
      address_name: "서울 강남구 대치동 922-21",
      phone: "02-553-9232",
      x: "127.0515",
      y: "37.4980"
    },
    reason: "'하남돼지집'은 뭐 말해 뭐해~ 🐷 숯불 향 가득한 삼겹살에 소주 한 잔은 국룰이지! 특히 갓 구운 고기랑 같이 마시는 소주 한 잔이 최고야. 👍",
    score: 750
  },
  {
    place: {
      id: 1026601630,
      place_name: "동호마을냉삼겹",
      category_name: "음식점 > 한식 > 육류,고기 > 삼겹살",
      address_name: "서울 강남구 대치동 936-25",
      phone: "02-557-0204",
      x: "127.0525",
      y: "37.4990"
    },
    reason: "'동호마을냉삼겹'에서 추억의 냉삼에 소주 한잔 어때? 😋 바삭하게 구운 냉삼이랑 톡 쏘는 소주의 조합은 언제나 옳지! 🍻",
    score: 700
  }
];
export default function Home() {
  const mapRef = useRef<kakao.maps.Map>(null);
  
  const [center, setCenter] = useState<{lat: number, lng: number} | null>(null);
  const [mapCenter, setMapCenter] = useState<{lat: number, lng: number}>(SEOUL_CENTER);
  const [mapLevel, setMapLevel] = useState(3);
  
  const [isInitialLoading, setIsInitialLoading] = useState(true); 
  const [isLocating, setIsLocating] = useState(false); 

  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [selectedRestaurantId, setSelectedRestaurantId] = useState<number | null>(null);
  const [showAILoader, setShowAILoader] = useState(false);
  
  const [restaurants, setRestaurants] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // --- 검색 관련 상태 ---
  const [searchQuery, setSearchQuery] = useState("");
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const [filteredLiquors, setFilteredLiquors] = useState<string[]>([]);

  const getMapBounds = () => {
    if (!mapRef.current) return null;
    const bounds = mapRef.current.getBounds();
    const sw = bounds.getSouthWest();
    const ne = bounds.getNorthEast();
    return { minX: sw.getLng(), minY: sw.getLat(), maxX: ne.getLng(), maxY: ne.getLat() };
  };

  const getCurrentLocation = (isFirstTime = false) => {
    if (typeof window !== "undefined" && navigator.geolocation) {
      if (isFirstTime) setIsInitialLoading(true);
      else setIsLocating(true);
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          const newPos = { lat: latitude, lng: longitude };
          setCenter(newPos);
          setMapCenter(newPos); 
          setIsInitialLoading(false);
          setIsLocating(false);
        },
        () => {
          if (isFirstTime) setMapCenter(SEOUL_CENTER);
          setIsInitialLoading(false);
          setIsLocating(false);
        },
        { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
      );
    }
  };

  useEffect(() => { getCurrentLocation(true); }, []);

  // 검색어 입력 시 자동완성 필터링
  useEffect(() => {
    if (searchQuery.trim() === "") {
      setFilteredLiquors([]);
    } else {
      const filtered = LIQUOR_LIST.filter(item => 
        item.toLowerCase().includes(searchQuery.toLowerCase())
      );
      setFilteredLiquors(filtered);
    }
  }, [searchQuery]);

  // 카테고리 및 검색어 선택 핵심 로직
  const handleSelectCategory = async (categoryId: string) => {
    if (selectedCategory === categoryId) {
      setSelectedCategory(null);
      setSelectedRestaurantId(null);
      setRestaurants([]);
      return;
    }

    const bounds = getMapBounds();
    if (!bounds) return;

    setSelectedCategory(categoryId);
    setSelectedRestaurantId(null);
    setShowAILoader(true);
    setIsLoading(true);
    setIsSearchFocused(false); // 검색창 닫기

    try {
      // const data = await recommendService.getRecommendations(categoryId, bounds);
      const data = MOCK_DATA;
      setRestaurants(data);
      
      setTimeout(() => {
        setShowAILoader(false);
        setIsLoading(false);
        if (data.length > 0) {
          setSelectedRestaurantId(data[0].place.id);
          confetti({
            particleCount: 100,
            spread: 70,
            origin: { y: 0.6 },
            colors: ['#FF9F43', '#FFC078', '#FFD8A8']
          });
        }
      }, 1500);
    } catch (error) {
      console.error("추천 실패", error);
      setShowAILoader(false);
      setIsLoading(false);
    }
  };

  const selectedRestaurant = restaurants?.find(r => r.place.id === selectedRestaurantId);
  
  useEffect(() => {
    if (selectedRestaurant) {
      setMapCenter({ 
        lat: parseFloat(selectedRestaurant.place.y), 
        lng: parseFloat(selectedRestaurant.place.x) 
      });
    }
  }, [selectedRestaurantId]);

  return (
    <div className="relative w-full h-[100dvh] overflow-hidden bg-gray-50 flex flex-col">
      <AnimatePresence>
        {isInitialLoading && (
          <motion.div 
            key="initial-loader"
            initial={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="absolute inset-0 z-[100] bg-white flex flex-col items-center justify-center px-6"
          >
            <Wine className="w-16 h-16 text-primary animate-bounce mb-8" />
            <h1 className="text-2xl font-bold text-gray-800 mb-2 text-center">오늘 기분에 딱 맞는 술 한 잔, <br/> 어디가 좋을까요?</h1>
          </motion.div>
        )}
      </AnimatePresence>

      {/* --- 상단 UI (조건부 렌더링) --- */}
      <div className="absolute top-0 left-0 right-0 z-[60] pt-4 pb-2">
        <AnimatePresence mode="wait">
          {mapLevel > 3 ? (
            /* 지도 확대 유도 메시지 */
            <motion.div
              key="zoom-message"
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="max-w-md mx-auto px-4"
            >
              <div className="bg-primary/90 backdrop-blur-md px-6 py-3 rounded-2xl shadow-xl border border-white/20 text-center">
                <p className="text-white font-bold flex items-center justify-center gap-2">
                  <MapPin className="w-4 h-4 animate-pulse" />
                  원하는 구역을 찾아 지도를 확대해 주세요!
                </p>
                <p className="text-white/80 text-xs mt-1">지도를 확대하면 검색창이 나타납니다 🔍</p>
              </div>
            </motion.div>
          ) : (
            /* 검색 및 카테고리 바 */
            <motion.div
              key="search-ui"
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
            >
              <div className="max-w-md mx-auto px-4 w-full mb-3 relative">
                <div className="relative group">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                  <input 
                    type="text" 
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onFocus={() => setIsSearchFocused(true)}
                    placeholder="원하는 주종을 검색해주세요" 
                    className="w-full pl-10 pr-10 py-3 bg-white rounded-2xl shadow-lg border border-gray-100 focus:outline-none focus:ring-2 focus:ring-primary/50 text-gray-700 font-medium"
                  />
                  {searchQuery && (
                    <button 
                      onClick={() => setSearchQuery("")}
                      className="absolute right-3 top-1/2 -translate-y-1/2"
                    >
                      <X className="h-4 h-4 text-gray-400" />
                    </button>
                  )}
                </div>

                {/* 자동완성 드로워(드롭다운) */}
                <AnimatePresence>
                  {isSearchFocused && (
                    <motion.div
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: 10 }}
                      className="absolute top-full left-4 right-4 mt-2 bg-white rounded-2xl shadow-2xl border border-gray-100 overflow-hidden z-[70] max-h-[60vh] overflow-y-auto"
                    >
                      {filteredLiquors.length > 0 ? (
                        filteredLiquors.map((liquor) => (
                          <button
                            key={liquor}
                            onClick={() => {
                              setSearchQuery(liquor);
                              handleSelectCategory(liquor);
                            }}
                            className="w-full px-5 py-4 text-left border-b border-gray-50 last:border-none active:bg-gray-50 flex items-center gap-3 transition-colors"
                          >
                            <Wine className="w-4 h-4 text-primary/60" />
                            <span className="text-gray-700 font-medium">{liquor}</span>
                          </button>
                        ))
                      ) : (
                        <div className="p-8 text-center text-gray-400">
                          <SearchX className="w-8 h-8 mx-auto mb-2 opacity-20" />
                          <p className="text-sm">찾으시는 주종이 없나요?<br/>다른 검색어를 입력해 보세요.</p>
                        </div>
                      )}
                      <button 
                        onClick={() => setIsSearchFocused(false)}
                        className="w-full py-3 bg-gray-50 text-xs text-gray-400 font-bold uppercase tracking-wider"
                      >
                        닫기
                      </button>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
              
              {!isSearchFocused && (
                <div className="w-full overflow-x-auto scrollbar-hide">
                  <CategoryBar 
                    selectedCategory={selectedCategory} 
                    onSelectCategory={handleSelectCategory} 
                  />
                </div>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Map Section */}
      <div className="flex-1 w-full h-full relative">
        <Map
          ref={mapRef}
          center={mapCenter}
          isPanto={true}
          style={{ width: "100%", height: "100%" }}
          level={mapLevel}
          onDragEnd={(map) => {
            const latlng = map.getCenter();
            setMapCenter({ lat: latlng.getLat(), lng: latlng.getLng() });
          }}
          onZoomChanged={(map) => setMapLevel(map.getLevel())}
        >
          {center && (
            <KakaoMapMarker 
              position={center} 
              image={{
                src: "/my-location-marker1.png",
                size: { width: 35, height: 35 }
              }}
            />
          )}

          {!isInitialLoading && restaurants?.map((item) => (
            <MapMarker
              key={item.place.id}
              restaurant={{
                ...item.place,
                lat: parseFloat(item.place.y),
                lng: parseFloat(item.place.x),
                name: item.place.placeName
              }}
              isSelected={selectedRestaurantId === item.place.id}
              onClick={() => setSelectedRestaurantId(item.place.id)}
            />
          ))}
        </Map>
        
        <div className="absolute bottom-6 right-4 z-10 flex flex-col gap-2">
          <button 
            onClick={() => getCurrentLocation(false)}
            className="w-11 h-11 bg-white rounded-full shadow-lg flex items-center justify-center border border-gray-100 active:scale-90 transition-all"
          >
            <MapPin className={`w-5 h-5 ${isLocating ? 'text-primary animate-spin' : 'text-gray-600'}`} />
          </button>
        </div>
      </div>

      {/* Overlay UI */}
      <AnimatePresence mode="wait">
        {showAILoader && selectedCategory && (
          <AILoading key="loader" category={selectedCategory} />
        )}
        
        {selectedRestaurant && !showAILoader && (
          <RestaurantCard 
            key="card" 
            restaurant={{
              id: selectedRestaurant.place.id,
              place_name: selectedRestaurant.place.place_name,
              address_name: selectedRestaurant.place.address_name,
              reason: selectedRestaurant.reason,
              place_url:selectedRestaurant.place.place_url,
              score :selectedRestaurant.score,
              lat: parseFloat(selectedRestaurant.place.y),
              lng: parseFloat(selectedRestaurant.place.x),
            }} 
            onClose={() => setSelectedRestaurantId(null)} 
          />
        )}
      </AnimatePresence>

      {/* 가이드 메시지 (검색 중이 아닐 때만 노출) */}
      {!isSearchFocused && (!selectedCategory || !selectedRestaurantId) && !showAILoader && !isInitialLoading && (
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="absolute bottom-16 left-0 right-0 z-50 px-4 flex justify-center pointer-events-none"
        >
          <div className="bg-white/95 backdrop-blur-md px-6 py-3 rounded-full shadow-2xl border border-white/50 text-center pointer-events-auto">
            <div className="text-sm font-medium text-gray-600 whitespace-nowrap">
              {selectedCategory 
                ? <><span className="text-primary font-bold">지도의 마커</span>를 누르거나<p>다른 <span className="text-primary font-bold">주종</span>을 선택해보세요!</p> </>
                : <><p>🍽️지도 내의 <span className="text-primary font-bold">페어링 맛집</span>을 추천해드려요!</p>상단의 <span className="text-primary font-bold">주종</span>을 선택해보세요!</>
              }
            </div>
          </div>
        </motion.div>
      )}
    </div>
  );
}