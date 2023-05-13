package is.codion.framework.demos.world.model;

import is.codion.common.db.exception.DatabaseException;
import is.codion.framework.db.EntityConnectionProvider;
import is.codion.framework.demos.world.domain.api.World.City;
import is.codion.framework.demos.world.domain.api.World.Location;
import is.codion.framework.domain.entity.exception.ValidationException;
import is.codion.swing.framework.model.SwingEntityEditModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

public final class CityEditModel extends SwingEntityEditModel {

  private static final String OPENSTREETMAP_ORG_SEARCH = "https://nominatim.openstreetmap.org/search/";

  public CityEditModel(EntityConnectionProvider connectionProvider) {
    super(City.TYPE, connectionProvider);
    initializeComboBoxModels(City.COUNTRY_FK);
  }

  public void setLocation() throws IOException, DatabaseException, ValidationException {
    Location location = lookupLocation(entity().castTo(City.class))
            .orElseThrow(() -> new RuntimeException("Location not found for city: " + entity()));
    put(City.LOCATION, location);
    if (isModified()) {
      update();
    }
  }

  void setLocation(City city) throws IOException, DatabaseException, ValidationException {
    lookupLocation(city).ifPresent(location -> city.put(City.LOCATION, location));
    if (city.isModified()) {
      update(singletonList(city));
    }
  }

  private Optional<Location> lookupLocation(City city)  throws IOException {
    JSONArray jsonArray = toJSONArray(new URL(OPENSTREETMAP_ORG_SEARCH +
            URLEncoder.encode(city.name(), UTF_8) + "," +
            URLEncoder.encode(city.country().name(), UTF_8) + "?format=json"));
    if (jsonArray.length() > 0) {
      JSONObject cityInformation = (JSONObject) jsonArray.get(0);

      return Optional.of(new Location(cityInformation.getDouble("lat"), cityInformation.getDouble("lon")));
    }

    return Optional.empty();
  }

  private static JSONArray toJSONArray(URL url) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), UTF_8))) {
      return new JSONArray(reader.lines().collect(joining()));
    }
  }
}