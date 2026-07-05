package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.Location;
import ru.practicum.explorewithme.entity.LocationEmbeddable;

public class LocationMapper {
    public static Location mapToLocation(double lat, double lon) {
        return new Location(lat, lon);
    }

    public static LocationEmbeddable mapToLocationEmbeddable(double lat, double lon) {
        return new LocationEmbeddable(lat, lon);
    }
}
