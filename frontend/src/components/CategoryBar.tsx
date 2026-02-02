import { motion } from "framer-motion";
// import { CATEGORIES } from "@shared/schema";

interface CategoryBarProps {
  selectedCategory: string | null;
  onSelectCategory: (id: string) => void;
}
export const CATEGORIES = [
  { id: 'soju', name: '소주', icon: '🍶' },
  { id: 'beer', name: '맥주', icon: '🍺' },
  { id: 'wine', name: '와인', icon: '🍷' },
  { id: 'whiskey', name: '위스키', icon: '🥃' },
  { id: 'sake', name: '사케', icon: '🍶' },
  { id: 'makgeolli', name: '막걸리', icon: '🥣' },
  { id: 'cocktail', name: '칵테일', icon: '🍸' }
] as const;

export function CategoryBar({ selectedCategory, onSelectCategory }: CategoryBarProps) {
  return (
    // 1. justify-center를 제거하여 왼쪽부터 정렬되게 합니다.
    // 2. flex-nowrap을 추가하여 한 줄로 유지합니다.
    <div className="w-full overflow-x-auto no-scrollbar py-2 bg-transparent flex flex-nowrap overflow-y-hidden">
      {/* 3. mx-auto를 제거하고 px-4로 좌우 여백만 확보합니다. */}
      <div className="flex space-x-3 px-4 flex-nowrap ">
        {CATEGORIES.map((cat) => (
          <motion.button
            key={cat.id}
            onClick={() => onSelectCategory(cat.name)}
            whileTap={{ scale: 0.95 }}
            // 4. flex-shrink-0을 추가하여 아이콘이 찌그러지지 않게 고정합니다.
            className={`
              flex-shrink-0 flex items-center space-x-2 px-4 py-2 rounded-full border shadow-sm transition-all duration-300
              ${
                selectedCategory === cat.id
                  ? "bg-primary text-white border-primary shadow-lg shadow-primary/30"
                  : "bg-white text-gray-700 border-gray-100 hover:border-primary/30 hover:bg-orange-50"
              }
            `}
          >
            <span className="text-xl filter drop-shadow-sm">{cat.icon}</span>
            <span className="font-medium text-sm whitespace-nowrap">{cat.name}</span>
          </motion.button>
        ))}
      </div>
    </div>
  );
}

