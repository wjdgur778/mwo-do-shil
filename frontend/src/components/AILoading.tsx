import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { CATEGORIES } from "@shared/schema";

interface AILoadingProps {
  category: string;
}

const LOADING_TIPS = [
  "🔍 실시간 구글 검색으로 최신 데이터를 분석 중입니다.",
  "📍 지도를 확대하면 주변의 더 상세한 결과를 얻을 수 있어요.",
  "✨ AI가 메뉴판 가격과 미식 리뷰를 대조하고 있습니다.",
  "🕰️ 실측 정보를 가져오느라 시간이 조금 걸릴 수 있습니다.",
  "⚠️ 검색 결과는 실제 정보와 약간의 차이가 있을 수 있습니다."
];


export function AILoading({ category }: AILoadingProps) {
  const [tipIndex, setTipIndex] = useState(0);
  const [stepIndex, setStepIndex] = useState(0);
  const [dots, setDots] = useState(""); // 마침표 상태

  const categoryName = CATEGORIES.find(c => c.id === category)?.name || category;
  // 3.5초마다 팁 변경
  useEffect(() => {
    const timer = setInterval(() => {
      setTipIndex((prev) => (prev + 1) % LOADING_TIPS.length);
    }, 2500);
    return () => clearInterval(timer);
  }, []);
  
  // 1. 마침표 애니메이션 (.) -> (..) -> (...)
  useEffect(() => {
    const timer = setInterval(() => {
      setDots((prev) => (prev.length >= 3 ? "" : prev + "."));
    }, 500);
    return () => clearInterval(timer);
  }, []);


  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-white/80 backdrop-blur-sm">
      {/* 요구사항 3: 전체적으로 조금 아래에 위치 (translate-y-8) */}
      <div className="text-center px-8 w-full max-w-sm transform translate-y-8">
        
        {/* 요구사항 1: 요란하지 않은 미니멀 스피너 */}
        <div className="relative w-12 h-12 mx-auto mb-8">
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 1.8, repeat: Infinity, ease: "linear" }}
            className="w-full h-full border-[1.8px] border-gray-100 border-t-primary rounded-full"
          />
        </div>
        
        {/* 요구사항 2: 더 작고 부드러운 텍스트 스타일링 */}
        <motion.h3 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="text-lg font-semibold text-gray-700 mb-2"
        >
          AI 페어링 분석 중<span className="inline-block w-6 text-left">{dots}</span>
        </motion.h3>
          <motion.p 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2 }}
          className="text-gray-500 font-medium"
        >
          <span className="text-primary font-bold">{categoryName}</span>와 가장 잘 어울리는<br/>
          최적의 맛집을 찾고 있습니다.
        </motion.p>
        {/* UX 강화: 현재 진행 단계를 아주 작게 표시 */}
        <div className="flex flex-col items-center gap-4">
          <div className="h-[1px] bg-gray-100 w-12" />
          
          <div className="min-h-[40px] flex items-center justify-center">
            <AnimatePresence mode="wait">
              <motion.p 
                key={tipIndex}
                initial={{ opacity: 0, y: 3 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -3 }}
                transition={{ duration: 0.5 }}
                className="text-gray-500 text-sm font-normal leading-relaxed break-keep"
              >
                {LOADING_TIPS[tipIndex]}
              </motion.p>
            </AnimatePresence>
          </div>
        </div>
      </div>
    </div>
  );
}

