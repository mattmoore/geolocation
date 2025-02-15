CREATE TABLE IF NOT EXISTS addresses(
  id SERIAL PRIMARY KEY,
  street VARCHAR,
  city VARCHAR,
  state VARCHAR,
  coords GEOMETRY
);

INSERT INTO addresses(id, street, city, state, coords) VALUES
(1, '20 W 34th St.', 'New York', 'NY', 'POINT(-74.044502 40.689247)'),
(2, '5908 US-31', 'Williamsburg', 'MI', 'POINT(-85.50011694235425 44.78319626355651)')
;
