--List num seats for flight:
SELECT P.seats - F.num_sold 
FROM Flight F, FlightInfo FI, Plane P 
WHERE (F.fnum = FI.flight_id) AND (FI.plane_id = P.id) AND fnum = /*[flightnum]*/ 10;

--List repairs per plane in desc order: 
SELECT count(*) as "# Repairs", plane_id as "Plane ID#"
FROM repairs group by "Plane ID#" ORDER BY "# Repairs" DESC;

--List years sorted lowest to highest by num repairs:
SELECT count(*) as "# Repairs", extract(year from repair_date) as "Year"
FROM repairs GROUP BY "Year" ORDER BY "# Repairs" ASC;

--List number of customers with each possible status:
SELECT status,count(*)
FROM reservation R
WHERE R.fid = /*flightnum*/10 GROUP BY status;
