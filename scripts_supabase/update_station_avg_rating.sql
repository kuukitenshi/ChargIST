CREATE OR REPLACE FUNCTION update_station_rating() RETURNS trigger AS $update_station_rating$
    BEGIN
        IF (TG_OP = 'DELETE') THEN
          UPDATE stations SET "avgRating" = (SELECT AVG(rating) FROM reviews WHERE "stationId" = OLD."stationId")
          WHERE id = OLD."stationId";
        ELSE
          UPDATE stations SET "avgRating" = (SELECT AVG(rating) FROM reviews WHERE "stationId" = NEW."stationId")
          WHERE id = NEW."stationId";
        END IF;

        RETURN NULL;
    END;
$update_station_rating$ LANGUAGE plpgsql;

create or replace trigger on_reviews_change
after insert or update or delete on reviews
for each row
execute function update_station_rating();