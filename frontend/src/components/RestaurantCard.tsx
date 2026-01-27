import { Restaurant } from "@shared/schema";
import { motion } from "framer-motion";
import { X, MapPin, Sparkles, Utensils } from "lucide-react";

interface RestaurantCardProps {
  restaurant: Restaurant;
  onClose: () => void;
}

export function RestaurantCard({ restaurant, onClose }: RestaurantCardProps) {
  return (
    <motion.div
      initial={{ y: "100%", opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      exit={{ y: "100%", opacity: 0 }}
      transition={{ type: "spring", damping: 25, stiffness: 300 }}
      className="fixed bottom-0 left-0 right-0 z-50 p-4 md:p-6"
    >
      <div className="bg-white rounded-3xl shadow-2xl border border-gray-100 overflow-hidden max-w-md mx-auto relative">
        {/* Close Button */}
        <button 
          onClick={onClose}
          className="absolute top-4 right-4 z-10 p-2 bg-black/5 hover:bg-black/10 rounded-full transition-colors"
        >
          <X className="w-5 h-5 text-gray-700" />
        </button>

        {/* Image Header */}
        <div className="h-48 relative overflow-hidden bg-gray-100">
          {restaurant.imageUrl ? (
            <>
              {/* Unsplash image with descriptive comment */}
              {/* Restaurant food or interior based on category */}
              <img 
                src={restaurant.imageUrl} 
                alt={restaurant.name}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
            </>
          ) : (
            <div className="w-full h-full flex items-center justify-center bg-orange-100">
              <Utensils className="w-12 h-12 text-primary/40" />
            </div>
          )}
          
          <div className="absolute bottom-4 left-6 text-white">
            <div className="flex items-center space-x-2 mb-1">
              <span className="px-2 py-0.5 rounded-md bg-primary text-xs font-bold uppercase tracking-wider">
                {restaurant.category.toUpperCase()} PAIRING
              </span>
            </div>
            <h2 className="text-2xl font-bold font-display">{restaurant.name}</h2>
          </div>
        </div>

        {/* Content */}
        <div className="p-6 space-y-5">
          {/* AI Pairing Reason - Highlighted */}
          <div className="bg-orange-50 rounded-2xl p-4 border border-orange-100">
            <div className="flex items-center space-x-2 text-primary font-bold mb-2">
              <Sparkles className="w-4 h-4" />
              <span className="text-sm uppercase tracking-wide">AI Pairing Analysis</span>
            </div>
            <p className="text-gray-700 font-medium leading-relaxed text-sm md:text-base">
              "{restaurant.pairingReason}"
            </p>
          </div>

          <div className="space-y-3">
            <div className="flex items-start space-x-3 text-gray-600">
              <Utensils className="w-5 h-5 mt-0.5 shrink-0 text-gray-400" />
              <div>
                <p className="text-xs text-gray-400 font-bold uppercase tracking-wide">Signature Menu</p>
                <p className="font-semibold text-gray-900">{restaurant.signatureMenu}</p>
              </div>
            </div>

            <div className="flex items-start space-x-3 text-gray-600">
              <MapPin className="w-5 h-5 mt-0.5 shrink-0 text-gray-400" />
              <div>
                <p className="text-xs text-gray-400 font-bold uppercase tracking-wide">Location</p>
                <p className="font-medium">{restaurant.address || "서울 특별시"}</p>
              </div>
            </div>
          </div>

          <button className="w-full py-3.5 bg-gray-900 text-white font-bold rounded-xl shadow-lg hover:bg-gray-800 active:scale-[0.98] transition-all flex items-center justify-center space-x-2">
            <span>길찾기</span>
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H3" />
            </svg>
          </button>
        </div>
      </div>
    </motion.div>
  );
}
