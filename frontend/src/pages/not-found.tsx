import { Link } from "wouter";
import { AlertCircle } from "lucide-react";

export default function NotFound() {
  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-gray-50">
      <div className="w-full max-w-md p-6 text-center">
        <div className="mb-6 flex justify-center">
          <div className="w-20 h-20 bg-orange-100 rounded-full flex items-center justify-center">
            <AlertCircle className="w-10 h-10 text-primary" />
          </div>
        </div>
        
        <h1 className="text-3xl font-bold text-gray-900 mb-2 font-display">404 Page Not Found</h1>
        <p className="text-gray-500 mb-8">
          찾으시는 페이지가 없습니다. 주소를 확인해주세요.
        </p>

        <Link href="/" className="inline-flex items-center justify-center w-full px-6 py-3 text-base font-bold text-white transition-all duration-200 bg-primary border border-transparent rounded-xl hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary shadow-lg shadow-primary/30">
          홈으로 돌아가기
        </Link>
      </div>
    </div>
  );
}
