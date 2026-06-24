ALTER TABLE travel_plans
ADD COLUMN transit_fare INT DEFAULT 0 AFTER total_distance,
ADD COLUMN taxi_fare INT DEFAULT 0 AFTER transit_fare;
