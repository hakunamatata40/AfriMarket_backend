package com.example.AfriMarket_backend.dto;

import com.example.AfriMarket_backend.model.RelayPoint;
import com.example.AfriMarket_backend.model.Zone;

public class ZoneDto {
    private Long id;
    private String name;
    private String city;
    private String region;

    public static ZoneDto from(Zone z) {
        ZoneDto d = new ZoneDto();
        d.id = z.getId();
        d.name = z.getName();
        d.city = z.getCity();
        d.region = z.getRegion();
        return d;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getRegion() { return region; }

    public static class RelayDto {
        private Long id;
        private String name;
        private String address;
        private String phone;
        private Double gpsLat;
        private Double gpsLng;
        private String zoneName;
        private Double capacityKg;

        public static RelayDto from(RelayPoint r) {
            RelayDto d = new RelayDto();
            d.id = r.getId();
            d.name = r.getName();
            d.address = r.getAddress();
            d.phone = r.getPhone();
            d.gpsLat = r.getGpsLat();
            d.gpsLng = r.getGpsLng();
            d.zoneName = r.getZone() != null ? r.getZone().getName() : null;
            d.capacityKg = r.getCapacityKg();
            return d;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public Double getGpsLat() { return gpsLat; }
        public Double getGpsLng() { return gpsLng; }
        public String getZoneName() { return zoneName; }
        public Double getCapacityKg() { return capacityKg; }
    }
}
