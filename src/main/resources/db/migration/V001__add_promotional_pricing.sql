-- Migration: Add promotional pricing columns to products table
-- Author: OpenSpec Implementation
-- Description: Adds promotional_price (DOUBLE, nullable) and is_promo_active (BOOLEAN, default false) columns

ALTER TABLE products ADD COLUMN promotional_price DOUBLE NULL;
ALTER TABLE products ADD COLUMN is_promo_active BOOLEAN DEFAULT false;
