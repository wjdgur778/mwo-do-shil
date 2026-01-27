
import { z } from 'zod';
import { insertRestaurantSchema, restaurants } from './schema';

export const errorSchemas = {
  validation: z.object({
    message: z.string(),
    field: z.string().optional(),
  }),
  notFound: z.object({
    message: z.string(),
  }),
  internal: z.object({
    message: z.string(),
  }),
};

export const api = {
  restaurants: {
    list: {
      method: 'GET' as const,
      path: '/api/restaurants',
      input: z.object({
        category: z.string().optional(),
        search: z.string().optional(),
      }).optional(),
      responses: {
        200: z.array(z.custom<typeof restaurants.$inferSelect>()),
      },
    },
    get: {
      method: 'GET' as const,
      path: '/api/restaurants/:id',
      responses: {
        200: z.custom<typeof restaurants.$inferSelect>(),
        404: errorSchemas.notFound,
      },
    },
    // Keep create/update for potential admin usage later
    create: {
      method: 'POST' as const,
      path: '/api/restaurants',
      input: insertRestaurantSchema,
      responses: {
        201: z.custom<typeof restaurants.$inferSelect>(),
        400: errorSchemas.validation,
      },
    },
  },
};

export function buildUrl(path: string, params?: Record<string, string | number>): string {
  let url = path;
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (url.includes(`:${key}`)) {
        url = url.replace(`:${key}`, String(value));
      }
    });
  }
  return url;
}
