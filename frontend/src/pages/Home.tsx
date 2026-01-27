import { useState, useEffect, useRef } from "react";
import { Map, MapMarker as KakaoMapMarker } from "react-kakao-maps-sdk";
import confetti from "canvas-confetti";
import { Search, MapPin, Wine } from "lucide-react";
import { CategoryBar } from "@/components/CategoryBar";
import { MapMarker } from "@/components/MapMarker";
import { RestaurantCard } from "@/components/RestaurantCard";
import { AILoading } from "@/components/AILoading";
import { AnimatePresence, motion } from "framer-motion";
import recommendService from "../services/recommendService"; // 서비스 임포트

const SEOUL_CENTER = { lat: 37.5665, lng: 126.9780 };

export default function Home() {
  const mapRef = useRef<kakao.maps.Map>(null); // 지도 객체 참조를 위한 ref
  
  const [center, setCenter] = useState<{lat: number, lng: number} | null>(null);
  const [mapCenter, setMapCenter] = useState<{lat: number, lng: number}>(SEOUL_CENTER);
  const [mapLevel, setMapLevel] = useState(3);
  
  const [isInitialLoading, setIsInitialLoading] = useState(true); 
  const [isLocating, setIsLocating] = useState(false); 

  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [selectedRestaurantId, setSelectedRestaurantId] = useState<number | null>(null);
  const [showAILoader, setShowAILoader] = useState(false);
  
  // 서버에서 받아온 추천 식당 데이터 상태
  const [restaurants, setRestaurants] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // 현재 지도 영역의 좌표(Bounds)를 가져오는 함수
  const getMapBounds = () => {
    if (!mapRef.current) return null;
    const bounds = mapRef.current.getBounds();
    const sw = bounds.getSouthWest(); // 남서쪽 (minX, minY)
    const ne = bounds.getNorthEast(); // 북동쪽 (maxX, maxY)
    
    return {
      minX: sw.getLng(),
      minY: sw.getLat(),
      maxX: ne.getLng(),
      maxY: ne.getLat()
    };
  };

  // 위치 정보 가져오기
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

  useEffect(() => {
    getCurrentLocation(true);
  }, []);

  // 카테고리 클릭 시 호출되는 핵심 함수
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

    try {
      // API 호출: 선택한 카테고리와 현재 지도 좌표 전달
      const data = await recommendService.getRecommendations(categoryId, bounds);
      console.log(data);
      setRestaurants(data);
      
      // AI 효과를 위해 약간의 지연 후 데이터 표시
      setTimeout(() => {
        setShowAILoader(false);
        setIsLoading(false);
        if (data.length > 0) {
          // 데이터가 있다면 첫 번째 장소 선택 및 폭죽 효과
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
      console.error("추천 정보를 가져오는데 실패했습니다.", error);
      setShowAILoader(false);
      setIsLoading(false);
    }
  };

  // 선택된 식당 객체 찾기 (데이터 구조에 맞춰 item.place.id 비교)
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
      {/* 초기 로딩 화면 */}
      <AnimatePresence>
        {isInitialLoading && (
          <motion.div 
            key="initial-loader"
            initial={{ opacity: 1 }}
            exit={{ opacity: 0, transition: { duration: 0.8 } }}
            className="absolute inset-0 z-[100] bg-white flex flex-col items-center justify-center px-6"
          >
            <motion.div initial={{ scale: 0.8, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} className="flex flex-col items-center">
              <Wine className="w-16 h-16 text-primary animate-bounce mb-8" />
              <h1 className="text-2xl font-bold text-gray-800 mb-2 text-center break-keep">오늘 기분에 딱 맞는 술 한 잔, <br/> 어디가 좋을까요?</h1>
              <p className="text-gray-400 text-center animate-pulse">실시간 위치 정보를 확인 중입니다...</p>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Header (Search & Category) */}
      <div className="absolute top-0 left-0 right-0 z-20 pt-4 pb-2 bg-gradient-to-b from-white/95 via-white/80 to-transparent backdrop-blur-[2px]">
        <div className="max-w-md mx-auto px-4 w-full mb-3">
          <div className="relative group">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400 group-focus-within:text-primary transition-colors" />
            <input 
              type="text" 
              placeholder="원하는 주종을 검색해주세요" 
              className="w-full pl-10 pr-4 py-3 bg-white rounded-2xl shadow-lg border border-gray-100 focus:outline-none focus:ring-2 focus:ring-primary/50 text-gray-700 font-medium"
            />
          </div>
        </div>
        
        <div className="w-full overflow-x-auto scrollbar-hide">
          <CategoryBar 
            selectedCategory={selectedCategory} 
            onSelectCategory={handleSelectCategory} 
          />
        </div>
      </div>

      {/* Map Section */}
      <div className="flex-1 w-full h-full relative">
        <Map
          ref={mapRef} // 지도 객체에 접근하기 위해 ref 설정
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
                src: "/my-location-marker1.png", // public 폴더 경로는 /로 시작
                size: { width: 35, height: 35 }
              }}
            />
          )}

          {/* 추천 결과 마커 표시 */}
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

      {/* Overlay UI (Loading & Card) */}
      <AnimatePresence mode="wait">
        {showAILoader && selectedCategory && (
          <AILoading key="loader" category={selectedCategory} />
        )}
        
        {selectedRestaurant && !showAILoader && (
          <RestaurantCard 
            key="card" 
            // DTO 구조를 기존 Card 컴포넌트 Props에 맞춰 매핑
            restaurant={{
              id: selectedRestaurant.place.id,
              name: selectedRestaurant.place.placeName,
              address: selectedRestaurant.place.addressName,
              pairingReason: selectedRestaurant.reason,
              lat: parseFloat(selectedRestaurant.place.y),
              lng: parseFloat(selectedRestaurant.place.x),
              imageUrl: selectedRestaurant.place.imageUrl
            }} 
            onClose={() => setSelectedRestaurantId(null)} 
          />
        )}
      </AnimatePresence>

      {/* 가이드 메시지 */}
      {(!selectedCategory || !selectedRestaurantId) && !showAILoader && !isInitialLoading && (
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="absolute bottom-24 left-0 right-0 z-50 px-4 flex justify-center pointer-events-none"
        >
          <div className="bg-white/95 backdrop-blur-md px-6 py-3 rounded-full shadow-2xl border border-white/50 text-center pointer-events-auto">
            <p className="text-sm font-medium text-gray-600 whitespace-nowrap">
              {selectedCategory 
                ? <><span className="text-primary font-bold">지도의 마커</span>를 누르거나 다른 <span className="text-primary font-bold">주종</span>을 선택해보세요!</>
                : <>상단의 <span className="text-primary font-bold">주종</span>을 선택해보세요!</>
              }
            </p>
          </div>
        </motion.div>
      )}
    </div>
  );
}