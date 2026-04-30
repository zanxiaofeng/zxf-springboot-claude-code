-- Add bio column to users table (CLOB/TEXT for user profile description)
ALTER TABLE users ADD COLUMN bio TEXT;
