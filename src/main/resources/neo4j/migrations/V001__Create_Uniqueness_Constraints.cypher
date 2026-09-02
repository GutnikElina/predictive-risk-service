CREATE CONSTRAINT constraint_container_id_unique IF NOT EXISTS
FOR (c:Container) REQUIRE c.id IS UNIQUE;

CREATE CONSTRAINT constraint_destination_id_unique IF NOT EXISTS
FOR (d:Destination) REQUIRE d.id IS UNIQUE;

CREATE CONSTRAINT constraint_port_id_unique IF NOT EXISTS
FOR (p:Port) REQUIRE p.id IS UNIQUE;

CREATE CONSTRAINT constraint_shipment_id_unique IF NOT EXISTS
FOR (s:Shipment) REQUIRE s.id IS UNIQUE;

CREATE CONSTRAINT constraint_truck_id_unique IF NOT EXISTS
FOR (t:Truck) REQUIRE t.id IS UNIQUE;

CREATE CONSTRAINT constraint_vessel_id_unique IF NOT EXISTS
FOR (v:Vessel) REQUIRE v.id IS UNIQUE;