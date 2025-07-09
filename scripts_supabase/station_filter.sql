CREATE OR REPLACE FUNCTION stations_filter(
    only_available BOOLEAN DEFAULT FALSE,
    charger_types TEXT[] DEFAULT NULL,
    charger_speeds TEXT[] DEFAULT NULL,
    min_price DOUBLE PRECISION DEFAULT 0.0,
    max_price DOUBLE PRECISION DEFAULT 20.0,
    payment_methods TEXT[] DEFAULT NULL,
    nearby_services TEXT[] DEFAULT NULL,
    max_distance DOUBLE PRECISION DEFAULT 25.0,
    user_latitude DOUBLE PRECISION DEFAULT 0.0,
    user_longitude DOUBLE PRECISION DEFAULT 0.0,
    already_have bigint[] DEFAULT NULL
)
RETURNS SETOF JSON AS $$
BEGIN
  RETURN QUERY
  SELECT
    json_build_object(
      'station', json_build_object(
          'id', s.id,
          'name', s.name,
          'latitude', s.latitude,
          'longitude', s.longitude,
          'paymentMethods', s."paymentMethods",
          'nearbyServices', s."nearbyServices",
          'imageUrl', s."imageUrl",
          'avgRating', s."avgRating"
      ),
      'chargers', COALESCE(
          (
              SELECT json_agg(
                  json_build_object(
                      'id', c.id,
                      'stationId', c."stationId",
                      'type', c.type,
                      'power', c.power,
                      'price', c.price,
                      'status', c.status,
                      'issue', c.issue
                  )
              )
              FROM chargers c
              WHERE c."stationId" = s.id
          ),
          '[]'::json
      )
    )
  FROM
    stations s
  WHERE
    EXISTS (
        SELECT 1
        FROM public.chargers charger
        WHERE charger."stationId" = s.id
          AND (NOT only_available OR charger.status = 'FREE')
          AND (charger_types IS NULL OR array_length(charger_types, 1) IS NULL OR charger.type = ANY(charger_types))
          AND (min_price IS NULL OR charger.price >= min_price)
          AND (max_price IS NULL OR charger.price <= max_price)
          AND (charger_speeds IS NULL OR array_length(charger_speeds, 1) IS NULL OR charger.power = ANY(charger_speeds))
    )
    AND (payment_methods IS NULL OR array_length(payment_methods, 1) IS NULL OR string_to_array(s."paymentMethods", E',\\s*') && payment_methods)
    AND (nearby_services IS NULL OR array_length(nearby_services, 1) IS NULL OR string_to_array(s."nearbyServices", E',\\s*') && nearby_services)
    AND (already_have IS NULL OR array_length(already_have, 1) IS NULL OR NOT(s.id = ANY(already_have)))
  LIMIT 5;
END;
$$ LANGUAGE plpgsql;