import { useQuery } from "@tanstack/react-query";
import { api } from "@shared/routes";
import { mockApi } from "../services/mockApi";
import { Restaurant } from "@shared/schema";

export function useRestaurants(category?: string | null) {
  return useQuery({
    queryKey: [api.restaurants.list.path, category],
    queryFn: async () => {
      // In a real app, we would fetch from the backend:
      // const url = category 
      //   ? `${api.restaurants.list.path}?category=${category}` 
      //   : api.restaurants.list.path;
      // const res = await fetch(url);
      // return api.restaurants.list.responses[200].parse(await res.json());

      // Using Mock API for this demo
      return mockApi.fetchRestaurants(category || undefined);
    },
    enabled: true, // Always allow fetching, but UI will control when to show loading
    staleTime: 1000 * 60 * 5, // 5 minutes
  });
}

export function useRestaurant(id: number) {
  return useQuery({
    queryKey: [api.restaurants.get.path, id],
    queryFn: async () => {
      return mockApi.getRestaurant(id);
    },
    enabled: !!id,
  });
}
