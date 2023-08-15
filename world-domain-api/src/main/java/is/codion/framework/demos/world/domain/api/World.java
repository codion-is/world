/*
 * This file is part of Codion World Demo.
 *
 * Codion World Demo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Codion World Demo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Codion World Demo.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2023, Björn Darri Sigurðsson.
 */
package is.codion.framework.demos.world.domain.api;

import is.codion.common.db.operation.FunctionType;
import is.codion.framework.db.EntityConnection;
import is.codion.framework.domain.DomainType;
import is.codion.framework.domain.entity.ColorProvider;
import is.codion.framework.domain.entity.DefaultEntityValidator;
import is.codion.framework.domain.entity.Entity;
import is.codion.framework.domain.entity.EntityType;
import is.codion.framework.domain.entity.attribute.Attribute;
import is.codion.framework.domain.entity.attribute.Column;
import is.codion.framework.domain.entity.attribute.DerivedAttribute;
import is.codion.framework.domain.entity.attribute.DerivedAttribute.SourceValues;
import is.codion.framework.domain.entity.attribute.ForeignKey;
import is.codion.framework.domain.entity.exception.ValidationException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import static is.codion.common.NullOrEmpty.notNull;
import static is.codion.common.db.operation.FunctionType.functionType;

/**
 * World domain api.
 */
// tag::world[]
public interface World {

  DomainType DOMAIN = DomainType.domainType(World.class);
  // end::world[]

  // tag::city[]
  interface City extends Entity {
    EntityType TYPE = DOMAIN.entityType("world.city", City.class);

    Column<Integer> ID = TYPE.integerColumn("id");
    Column<String> NAME = TYPE.stringColumn("name");
    Column<String> COUNTRY_CODE = TYPE.stringColumn("countrycode");
    Column<String> DISTRICT = TYPE.stringColumn("district");
    Column<Integer> POPULATION = TYPE.integerColumn("population");
    Column<Location> LOCATION = TYPE.column("location", Location.class);

    ForeignKey COUNTRY_FK = TYPE.foreignKey("country", COUNTRY_CODE, Country.CODE);

    String name();
    int population();
    Country country();
    void location(Location location);

    default boolean isInCountry(Entity country) {
      return country != null && Objects.equals(country(), country);
    }

    default boolean isCapital() {
      return Objects.equals(get(City.ID), country().get(Country.CAPITAL));
    }
  }
  // end::city[]

  // tag::country[]
  interface Country extends Entity {
    EntityType TYPE = DOMAIN.entityType("world.country", Country.class);

    Column<String> CODE = TYPE.stringColumn("code");
    Column<String> NAME = TYPE.stringColumn("name");
    Column<String> CONTINENT = TYPE.stringColumn("continent");
    Column<String> REGION = TYPE.stringColumn("region");
    Column<Double> SURFACEAREA = TYPE.doubleColumn("surfacearea");
    Column<Integer> INDEPYEAR = TYPE.integerColumn("indepyear");
    Column<String> INDEPYEAR_SEARCHABLE = TYPE.stringColumn("indepyear_searchable");
    Column<Integer> POPULATION = TYPE.integerColumn("population");
    Column<Double> LIFE_EXPECTANCY = TYPE.doubleColumn("lifeexpectancy");
    Column<Double> GNP = TYPE.doubleColumn("gnp");
    Column<Double> GNPOLD = TYPE.doubleColumn("gnpold");
    Column<String> LOCALNAME = TYPE.stringColumn("localname");
    Column<String> GOVERNMENTFORM = TYPE.stringColumn("governmentform");
    Column<String> HEADOFSTATE = TYPE.stringColumn("headofstate");
    Column<Integer> CAPITAL = TYPE.integerColumn("capital");
    Column<String> CODE_2 = TYPE.stringColumn("code2");
    Attribute<Integer> CAPITAL_POPULATION = TYPE.integerAttribute("capital_population");
    Column<Integer> NO_OF_CITIES = TYPE.integerColumn("no_of_cities");
    Column<Integer> NO_OF_LANGUAGES = TYPE.integerColumn("no_of_languages");
    Column<byte[]> FLAG = TYPE.byteArrayColumn("flag");

    ForeignKey CAPITAL_FK = TYPE.foreignKey("capital_fk", CAPITAL, City.ID);

    FunctionType<EntityConnection, String, Double> AVERAGE_CITY_POPULATION = functionType("average_city_population");

    String name();
    String continent();
    String region();
    double surfacearea();
    int population();
  }
  // end::country[]

  // tag::country_language[]
  interface CountryLanguage extends Entity {
    EntityType TYPE = DOMAIN.entityType("world.countrylanguage", CountryLanguage.class);

    Column<String> COUNTRY_CODE = TYPE.stringColumn("countrycode");
    Column<String> LANGUAGE = TYPE.stringColumn("language");
    Column<Boolean> IS_OFFICIAL = TYPE.booleanColumn("isofficial");
    Column<Double> PERCENTAGE = TYPE.doubleColumn("percentage");
    Attribute<Integer> NO_OF_SPEAKERS = TYPE.integerAttribute("noOfSpeakers");

    ForeignKey COUNTRY_FK = TYPE.foreignKey("country_fk", COUNTRY_CODE, Country.CODE);

    String language();
    int noOfSpeakers();
  }
  // end::country_language[]

  // tag::continent[]
  interface Continent extends Entity {
    EntityType TYPE = DOMAIN.entityType("continent", Continent.class);

    Column<String> NAME = TYPE.stringColumn("continent");
    Column<Integer> SURFACE_AREA = TYPE.integerColumn("surface_area");
    Column<Long> POPULATION = TYPE.longColumn("population");
    Column<Double> MIN_LIFE_EXPECTANCY = TYPE.doubleColumn("min_life_expectancy");
    Column<Double> MAX_LIFE_EXPECTANCY = TYPE.doubleColumn("max_life_expectancy");
    Column<Integer> MIN_INDEPENDENCE_YEAR = TYPE.integerColumn("min_indep_year");
    Column<Integer> MAX_INDEPENDENCE_YEAR = TYPE.integerColumn("max_indep_year");
    Column<Double> GNP = TYPE.doubleColumn("gnp");

    String name();
    int surfaceArea();
    long population();
    double minLifeExpectancy();
    double maxLifeExpectancy();
    double gnp();
  }
  // end::continent[]

  // tag::lookup[]
  interface Lookup {
    EntityType TYPE = DOMAIN.entityType("world.country_city_lookup");

    Column<String> COUNTRY_CODE = TYPE.stringColumn("country.code");
    Column<String> COUNTRY_NAME = TYPE.stringColumn("country.name");
    Column<String> COUNTRY_CONTINENT = TYPE.stringColumn("country.continent");
    Column<String> COUNTRY_REGION = TYPE.stringColumn("country.region");
    Column<Double> COUNTRY_SURFACEAREA = TYPE.doubleColumn("country.surfacearea");
    Column<Integer> COUNTRY_INDEPYEAR = TYPE.integerColumn("country.indepyear");
    Column<Integer> COUNTRY_POPULATION = TYPE.integerColumn("country.population");
    Column<Double> COUNTRY_LIFEEXPECTANCY = TYPE.doubleColumn("country.lifeexpectancy");
    Column<Double> COUNTRY_GNP = TYPE.doubleColumn("country.gnp");
    Column<Double> COUNTRY_GNPOLD = TYPE.doubleColumn("country.gnpold");
    Column<String> COUNTRY_LOCALNAME = TYPE.stringColumn("country.localname");
    Column<String> COUNTRY_GOVERNMENTFORM = TYPE.stringColumn("country.governmentform");
    Column<String> COUNTRY_HEADOFSTATE = TYPE.stringColumn("country.headofstate");
    Column<String> COUNTRY_CODE2 = TYPE.stringColumn("country.code2");
    Column<byte[]> COUNTRY_FLAG = TYPE.byteArrayColumn("country.flag");
    Column<Integer> CITY_ID = TYPE.integerColumn("city.id");
    Column<String> CITY_NAME = TYPE.stringColumn("city.name");
    Column<String> CITY_DISTRICT = TYPE.stringColumn("city.district");
    Column<Integer> CITY_POPULATION = TYPE.integerColumn("city.population");
    Column<Location> CITY_LOCATION = TYPE.column("city.location", Location.class);
  }
  // end::lookup[]

  // tag::location[]
  record Location(double latitude, double longitude) implements Serializable {

    @Override
    public String toString() {
      return "[" + latitude + "," + longitude + "]";
    }
  }
  // end::location[]

  // tag::cityColorProvider[]
  final class CityColorProvider implements ColorProvider {

    @Serial
    private static final long serialVersionUID = 1;

    private static final String YELLOW = "#ffff00";
    private static final String GREEN = "#00ff00";

    @Override
    public Object color(Entity cityEntity, Attribute<?> attribute) {
      if (attribute.equals(City.POPULATION) &&
              cityEntity.get(City.POPULATION) > 1_000_000) {
        return YELLOW;
      }
      City city = cityEntity.castTo(City.class);
      if (attribute.equals(City.NAME) && city.isCapital()) {
        return GREEN;
      }

      return null;
    }
  }
  // end::cityColorProvider[]

  // tag::noOfSpeakersProvider[]
  final class NoOfSpeakersProvider implements DerivedAttribute.Provider<Integer> {

    @Serial
    private static final long serialVersionUID = 1;

    @Override
    public Integer get(SourceValues sourceValues) {
      Double percentage = sourceValues.get(CountryLanguage.PERCENTAGE);
      Entity country = sourceValues.get(CountryLanguage.COUNTRY_FK);
      if (notNull(percentage, country) && country.isNotNull(Country.POPULATION)) {
        return Double.valueOf(country.get(Country.POPULATION) * (percentage / 100)).intValue();
      }

      return null;
    }
  }
  // end::noOfSpeakersProvider[]

  // tag::cityValidator[]
  final class CityValidator extends DefaultEntityValidator implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    @Override
    public void validate(Entity city) throws ValidationException {
      super.validate(city);
      //after a call to super.validate() values that are not nullable
      //(such as country and population) are guaranteed to be non-null
      Entity country = city.get(City.COUNTRY_FK);
      Integer cityPopulation = city.get(City.POPULATION);
      Integer countryPopulation = country.get(Country.POPULATION);
      if (countryPopulation != null && cityPopulation > countryPopulation) {
        throw new ValidationException(City.POPULATION,
                cityPopulation, "City population can not exceed country population");
      }
    }
  }
  // end::cityValidator[]

  // tag::locationComparator[]
  final class LocationComparator implements Comparator<Location>, Serializable {

    @Override
    public int compare(Location l1, Location l2) {
      int result = Double.compare(l1.latitude(), l2.latitude());
      if (result == 0) {
        return Double.compare(l1.longitude(), l2.longitude());
      }

      return result;
    }
  }
  // end::locationComparator[]
}