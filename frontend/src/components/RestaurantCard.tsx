import { Restaurant } from "@shared/schema";
import { motion } from "framer-motion";
import { X, MapPin, Sparkles, Utensils, ArrowRight } from "lucide-react";

interface RestaurantCardProps {
  restaurant: Restaurant;
  onClose: () => void;
}
const handleMobileNavi = (placeUrl: string) => {
  window.location.href = placeUrl;  // 같은탭 이동
};
export function RestaurantCard({ restaurant, onClose }: RestaurantCardProps) {
  return (
    <motion.div
      initial={{ y: "100%", opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      exit={{ y: "100%", opacity: 0 }}
      transition={{ type: "spring", damping: 25, stiffness: 300 }}
      // 1. 컨테이너: 화면 전체에서 하단 중앙 정렬을 담당
      className="fixed inset-x-0 bottom-0 z-50 flex justify-center p-4 pointer-events-none"
    >
      <div 
        // 2. 카드 본체: pointer-events-auto로 클릭 가능하게 설정
        // max-h-[45dvh]: 화면 높이의 45%를 넘지 않도록 설정 (절반보다 약간 작게)
        // w-full max-w-md: 모바일에선 꽉 차게, 데스크탑에선 적절한 너비
        className="bg-white rounded-[2.5rem] shadow-[0_-10px_40px_rgba(0,0,0,0.12)] border border-gray-100 
                   overflow-hidden w-full max-w-md pointer-events-auto flex flex-col max-h-[45dvh]"
      >
        {/* 상단 이미지 영역 - 높이를 비율(flex-shrink-0)로 조절 */}
        <div className="relative h-32 sm:h-40 shrink-0 overflow-hidden bg-gray-100">
          <button 
            onClick={onClose}
            className="absolute top-4 right-4 z-20 p-2 bg-white/20 hover:bg-black/40 backdrop-blur-md rounded-full transition-all"
          >
            <X className="w-4 h-4 text-gray" />
          </button>

          {restaurant.imageUrl ? (
            <>
              <img 
                src={restaurant.imageUrl} 
                alt={restaurant.place_name}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>
            </>
          ) : (
            <div className="w-full h-full flex items-center justify-center bg-orange-50">
              <Utensils className="w-10 h-10 text-primary/30" />
            </div>
          )}
          
          <div className="absolute bottom-3 left-6 text-gray">
            <span className="px-2 py-0.5 rounded-md bg-primary text-[10px] font-black uppercase tracking-widest mb-1 inline-block">
              PAIRING 
            </span>
            <h2 className="text-xl sm:text-2xl font-bold font-display truncate max-w-[280px]">
              {restaurant.place_name}
            </h2>
          </div>
        </div>

        {/* 하단 콘텐츠 영역 - 내용이 길어지면 내부 스크롤 발생 */}
        <div className="p-5 sm:p-6 space-y-4 overflow-y-auto scrollbar-hide flex-1">
          {/* AI Pairing Reason */}
          <div className="bg-orange-50/50 rounded-2xl p-4 border border-orange-100/50">
            <div className="flex items-center space-x-2 text-primary font-bold mb-1.5">
              <Sparkles className="w-3.5 h-3.5" />
              <span className="text-[10px] uppercase tracking-wider">AI Pairing Analysis</span>
            </div>
            <p className="text-gray-700 font-medium leading-relaxed text-sm">
              "{restaurant.reason}"
            </p>
          </div>

          <div className="grid grid-cols-2 gap-4 pt-1">
            <div className="flex items-start space-x-2.5">
              <div className="p-2 bg-gray-50 rounded-lg">
                <Sparkles className="w-4 h-4 text-gray-400" />
              </div>
              <div className="min-w-0">
                <p className="text-[10px] text-gray-400 font-bold uppercase">AI Match</p>
                <p className="font-bold text-gray-900 text-sm">{restaurant.score}점</p>
              </div>
            </div>

            <div className="flex items-start space-x-2.5">
              <div className="p-2 bg-gray-50 rounded-lg">
                <MapPin className="w-4 h-4 text-gray-400" />
              </div>
              <div className="min-w-0">
                <p className="text-[10px] text-gray-400 font-bold uppercase">Location</p>
                <p className="font-medium text-gray-900 text-xs truncate">
                  {restaurant.address_name || "서울 특별시"}
                </p>
              </div>
            </div>
          </div>

      <button 
          onClick={() => handleMobileNavi(restaurant.place_url)}
          className="w-full py-3.5 bg-gray-950 text-white font-bold rounded-2xl shadow-xl hover:bg-gray-800 active:scale-[0.97] transition-all flex items-center justify-center space-x-2 group"
        >
          <span className="text-sm">카카오맵 바로가기</span>
          <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
        </button>
        </div>
      </div>
    </motion.div>
  );
}