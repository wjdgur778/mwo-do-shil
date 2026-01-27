import { motion } from "framer-motion";
import { CATEGORIES } from "@shared/schema";

interface AILoadingProps {
  category: string;
}
// const CATEGORIES = [
//   { id: "soju", label: "소주", icon: "🍶" },
//   { id: "beer", label: "맥주", icon: "🍺" },
//   { id: "wine", label: "와인", icon: "🍷" },
//   { id: "whiskey", label: "위스키", icon: "🥃" },
//   { id: "sake", label: "사케", icon: "🍶" },
//   { id: "makgeolli", label: "막걸리", icon: "🥣" },
//   { id: "cocktail", label: "칵테일", icon: "🍸" }
// ];
export function AILoading({ category }: AILoadingProps) {
  const categoryName = CATEGORIES.find(c => c.id === category)?.name || category;

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-white/80 backdrop-blur-sm">
      <div className="text-center px-6">
        <motion.div
          animate={{
            rotate: 360,
          }}
          transition={{
            duration: 2,
            repeat: Infinity,
            ease: "linear"
          }}
          className="w-16 h-16 border-4 border-orange-200 border-t-primary rounded-full mx-auto mb-6"
        />
        
        <motion.h3 
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-xl md:text-2xl font-bold text-gray-800 mb-2"
        >
          AI 분석 중...
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
      </div>
    </div>
  );
}
