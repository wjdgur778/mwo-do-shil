import { MapMarker as KakaoMapMarker, CustomOverlayMap } from "react-kakao-maps-sdk";
import { Restaurant } from "@shared/schema";
import { motion, AnimatePresence } from "framer-motion";

interface MapMarkerProps {
  restaurant: Restaurant;
  isSelected: boolean;
  onClick: () => void;
}

export function MapMarker({ restaurant, isSelected, onClick }: MapMarkerProps) {
  return (
    <>
      <KakaoMapMarker
        position={{ lat: restaurant.lat, lng: restaurant.lng }}
        onClick={onClick}
        image={{
          src: "/public/shop-marker.png",
          size: { width: 35, height: 35 },
        }}
      />
      
      {isSelected && (
        <CustomOverlayMap
          position={{ lat: restaurant.lat, lng: restaurant.lng }}
          yAnchor={1.8}
        >
          <motion.div
            initial={{ opacity: 0, y: 10, scale: 0.9 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 10, scale: 0.9 }}
            className="bg-white px-4 py-2 rounded-xl shadow-lg border border-primary/20 relative"
          >
            <div className="font-bold text-gray-800 text-sm">{restaurant.place_name}</div>
            <div className="absolute -bottom-1.5 left-1/2 -translate-x-1/2 w-3 h-3 bg-white border-r border-b border-primary/20 rotate-45"></div>
          </motion.div>
        </CustomOverlayMap>
      )}
    </>
  );
}
