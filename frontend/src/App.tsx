import { Switch, Route } from "wouter";
import { queryClient } from "./lib/queryClient";
import { QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import NotFound from "@/pages/not-found";
import Home from "@/pages/Home";

// Load Kakao SDK Script
// Note: In a real production app, move this to index.html or a proper script loader
// For this environment, ensure VITE_KAKAO_MAP_API_KEY is set in .env
function KakaoScriptLoader() {
  const apiKey = import.meta.env.VITE_KAKAO_MAP_API_KEY;
  
  if (!apiKey) {
    console.warn("Kakao Map API Key is missing. Maps will not load.");
    return null;
  }

  return (
    <script
      type="text/javascript"
      src={`//dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&libraries=services&autoload=false`}
      async
    />
  );
}

function Router() {
  return (
    <Switch>
      <Route path="/" component={Home} />
      <Route component={NotFound} />
    </Switch>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <KakaoScriptLoader />
        <Toaster />
        <Router />
      </TooltipProvider>
    </QueryClientProvider>
  );
}

export default App;
