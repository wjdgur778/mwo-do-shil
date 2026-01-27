
import { pgTable, text, serial, real, varchar } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod";

export const restaurants = pgTable("restaurants", {
  id: serial("id").primaryKey(),
  name: text("name").notNull(),
  category: varchar("category", { length: 50 }).notNull(), // e.g., 'Soju', 'Wine', 'Whiskey'
  pairingReason: text("pairing_reason").notNull(),
  signatureMenu: text("signature_menu").notNull(),
  lat: real("lat").notNull(),
  lng: real("lng").notNull(),
  address: text("address"),
  imageUrl: text("image_url"),
});

export const insertRestaurantSchema = createInsertSchema(restaurants).omit({ id: true });

export type Restaurant = typeof restaurants.$inferSelect;
export type InsertRestaurant = z.infer<typeof insertRestaurantSchema>;

export const CATEGORIES = [
  { id: 'soju', name: '소주', icon: '🍶' },
  { id: 'beer', name: '맥주', icon: '🍺' },
  { id: 'wine', name: '와인', icon: '🍷' },
  { id: 'whiskey', name: '위스키', icon: '🥃' },
  { id: 'sake', name: '사케', icon: '🍶' },
  { id: 'makgeolli', name: '막걸리', icon: '🥣' },
  { id: 'cocktail', name: '칵테일', icon: '🍸' },
] as const;
