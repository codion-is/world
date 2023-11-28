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
package is.codion.framework.demos.world.domain;

import is.codion.common.db.exception.DatabaseException;
import is.codion.common.db.operation.DatabaseFunction;
import is.codion.common.item.Item;
import is.codion.framework.db.EntityConnection;
import is.codion.framework.demos.world.domain.api.World;
import is.codion.framework.domain.DefaultDomain;
import is.codion.framework.domain.entity.OrderBy;
import is.codion.framework.domain.entity.attribute.Column.Converter;
import is.codion.framework.domain.entity.query.SelectQuery;

import java.sql.Statement;
import java.util.List;

import static is.codion.common.item.Item.item;
import static is.codion.framework.domain.entity.KeyGenerator.sequence;
import static is.codion.framework.domain.entity.OrderBy.ascending;
import static java.lang.Double.parseDouble;

// tag::world[]
public final class WorldImpl extends DefaultDomain implements World {

  private static final List<Item<String>> CONTINENT_ITEMS = List.of(
          item("Africa"), item("Antarctica"), item("Asia"),
          item("Europe"), item("North America"), item("Oceania"),
          item("South America"));

  public WorldImpl() {
    super(World.DOMAIN);
    //By default, you can't define a foreign key referencing an entity which
    //has not been defined, to prevent mistakes. But sometimes we have to
    //deal with cyclical dependencies, such as here, where city references
    //country and country references city. In these cases we can simply
    //disable strict foreign keys.
    setStrictForeignKeys(false);

    city();
    country();
    countryLanguage();
    lookup();
    continent();
  }
  // end::world[]

  // tag::city[]
  void city() {
    add(City.TYPE.define(
            City.ID.define()
                    .primaryKey(),
            City.NAME.define()
                    .column()
                    .caption("Name")
                    .searchColumn(true)
                    .nullable(false)
                    .maximumLength(35),
            City.COUNTRY_CODE.define()
                    .column()
                    .nullable(false),
            City.COUNTRY_FK.define()
                    .foreignKey()
                    .caption("Country"),
            City.DISTRICT.define()
                    .column()
                    .caption("District")
                    .nullable(false)
                    .maximumLength(20),
            City.POPULATION.define()
                    .column()
                    .caption("Population")
                    .nullable(false)
                    .numberFormatGrouping(true),
            City.LOCATION.define()
                    .column()
                    .caption("Location")
                    .columnClass(String.class, new LocationConverter())
                    .comparator(new LocationComparator()))
            .keyGenerator(sequence("world.city_seq"))
            .validator(new CityValidator())
            .orderBy(ascending(City.NAME))
            .stringFactory(City.NAME)
            .foregroundColorProvider(new CityColorProvider())
            .caption("City"));
  }
  // end::city[]

  // tag::country[]
  void country() {
    add(Country.TYPE.define(
            Country.CODE.define()
                    .primaryKey()
                    .caption("Country code")
                    .updatable(true)
                    .maximumLength(3),
            Country.NAME.define()
                    .column()
                    .caption("Name")
                    .searchColumn(true)
                    .nullable(false)
                    .maximumLength(52),
            Country.CONTINENT.define()
                    .column()
                    .caption("Continent")
                    .items(CONTINENT_ITEMS)
                    .nullable(false),
            Country.REGION.define()
                    .column()
                    .caption("Region")
                    .nullable(false)
                    .maximumLength(26),
            Country.SURFACEAREA.define()
                    .column()
                    .caption("Surface area")
                    .nullable(false)
                    .numberFormatGrouping(true)
                    .maximumFractionDigits(2),
            Country.INDEPYEAR.define()
                    .column()
                    .caption("Indep. year")
                    .valueRange(-2000, 2500),
            Country.INDEPYEAR_SEARCHABLE.define()
                    .column()
                    .expression("to_char(indepyear)")
                    .searchColumn(true)
                    .readOnly(true),
            Country.POPULATION.define()
                    .column()
                    .caption("Population")
                    .nullable(false)
                    .numberFormatGrouping(true),
            Country.LIFE_EXPECTANCY.define()
                    .column()
                    .caption("Life expectancy")
                    .maximumFractionDigits(1)
                    .valueRange(0, 99),
            Country.GNP.define()
                    .column()
                    .caption("GNP")
                    .numberFormatGrouping(true)
                    .maximumFractionDigits(2),
            Country.GNPOLD.define()
                    .column()
                    .caption("GNP old")
                    .numberFormatGrouping(true)
                    .maximumFractionDigits(2),
            Country.LOCALNAME.define()
                    .column()
                    .caption("Local name")
                    .nullable(false)
                    .maximumLength(45),
            Country.GOVERNMENTFORM.define()
                    .column()
                    .caption("Government form")
                    .nullable(false),
            Country.HEADOFSTATE.define()
                    .column()
                    .caption("Head of state")
                    .maximumLength(60),
            Country.CAPITAL.define()
                    .column(),
            Country.CAPITAL_FK.define()
                    .foreignKey()
                    .caption("Capital"),
            Country.CAPITAL_POPULATION.define()
                    .denormalized(Country.CAPITAL_FK, City.POPULATION)
                    .caption("Capital pop.")
                    .numberFormatGrouping(true),
            Country.NO_OF_CITIES.define()
                    .subquery("""
                            select count(*)
                            from world.city
                            where city.countrycode = country.code""")
                    .caption("No. of cities"),
            Country.NO_OF_LANGUAGES.define()
                    .subquery("""
                            select count(*)
                            from world.countrylanguage
                            where countrylanguage.countrycode = country.code""")
                    .caption("No. of languages"),
            Country.FLAG.define()
                    .blobColumn()
                    .caption("Flag")
                    .eagerlyLoaded(true),
            Country.CODE_2.define()
                    .column()
                    .caption("Code2")
                    .nullable(false)
                    .maximumLength(2))
            .orderBy(ascending(Country.NAME))
            .stringFactory(Country.NAME)
            .caption("Country"));

    add(Country.AVERAGE_CITY_POPULATION, new AverageCityPopulationFunction());
  }
  // end::country[]

  // tag::country_language[]
  void countryLanguage() {
    add(CountryLanguage.TYPE.define(
            CountryLanguage.COUNTRY_CODE.define()
                    .primaryKey(0)
                    .updatable(true),
            CountryLanguage.LANGUAGE.define()
                    .primaryKey(1)
                    .caption("Language")
                    .updatable(true),
            CountryLanguage.COUNTRY_FK.define()
                    .foreignKey()
                    .caption("Country"),
            CountryLanguage.IS_OFFICIAL.define()
                    .column()
                    .caption("Is official")
                    .columnHasDefaultValue(true)
                    .nullable(false),
            CountryLanguage.NO_OF_SPEAKERS.define()
                    .derived(new NoOfSpeakersProvider(),
                            CountryLanguage.COUNTRY_FK, CountryLanguage.PERCENTAGE)
                    .caption("No. of speakers")
                    .numberFormatGrouping(true),
            CountryLanguage.PERCENTAGE.define()
                    .column()
                    .caption("Percentage")
                    .nullable(false)
                    .maximumFractionDigits(1)
                    .valueRange(0, 100))
            .orderBy(OrderBy.builder()
                    .ascending(CountryLanguage.LANGUAGE)
                    .descending(CountryLanguage.PERCENTAGE)
                    .build())
            .caption("Language"));
  }
  // end::country_language[]

  // tag::lookup[]
  void lookup() {
    add(Lookup.TYPE.define(
            Lookup.COUNTRY_CODE.define()
                    .primaryKey(0)
                    .caption("Country code"),
            Lookup.COUNTRY_NAME.define()
                    .column()
                    .caption("Country name"),
            Lookup.COUNTRY_CONTINENT.define()
                    .column()
                    .caption("Continent")
                    .items(CONTINENT_ITEMS),
            Lookup.COUNTRY_REGION.define()
                    .column()
                    .caption("Region"),
            Lookup.COUNTRY_SURFACEAREA.define()
                    .column()
                    .caption("Surface area")
                    .numberFormatGrouping(true),
            Lookup.COUNTRY_INDEPYEAR.define()
                    .column()
                    .caption("Indep. year"),
            Lookup.COUNTRY_POPULATION.define()
                    .column()
                    .caption("Country population")
                    .numberFormatGrouping(true),
            Lookup.COUNTRY_LIFEEXPECTANCY.define()
                    .column()
                    .caption("Life expectancy"),
            Lookup.COUNTRY_GNP.define()
                    .column()
                    .caption("GNP")
                    .numberFormatGrouping(true),
            Lookup.COUNTRY_GNPOLD.define()
                    .column()
                    .caption("GNP old")
                    .numberFormatGrouping(true),
            Lookup.COUNTRY_LOCALNAME.define()
                    .column()
                    .caption("Local name"),
            Lookup.COUNTRY_GOVERNMENTFORM.define()
                    .column()
                    .caption("Government form"),
            Lookup.COUNTRY_HEADOFSTATE.define()
                    .column()
                    .caption("Head of state"),
            Lookup.COUNTRY_FLAG.define()
                    .blobColumn()
                    .caption("Flag"),
            Lookup.COUNTRY_CODE2.define()
                    .column()
                    .caption("Code2"),
            Lookup.CITY_ID.define()
                    .primaryKey(1),
            Lookup.CITY_NAME.define()
                    .column()
                    .caption("City"),
            Lookup.CITY_DISTRICT.define()
                    .column()
                    .caption("District"),
            Lookup.CITY_POPULATION.define()
                    .column()
                    .caption("City population")
                    .numberFormatGrouping(true),
            Lookup.CITY_LOCATION.define()
                    .column()
                    .caption("City location")
                    .columnClass(String.class, new LocationConverter())
                    .comparator(new LocationComparator()))
            .selectQuery(SelectQuery.builder()
                    .from("world.country left outer join world.city on city.countrycode = country.code")
                    .build())
            .orderBy(OrderBy.builder()
                    .ascending(Lookup.COUNTRY_NAME)
                    .descending(Lookup.CITY_POPULATION)
                    .build())
            .readOnly(true)
            .caption("Lookup"));
  }
  // end::lookup[]

  // tag::continent[]
  void continent() {
    add(Continent.TYPE.define(
            Continent.NAME.define()
                    .column()
                    .caption("Continent")
                    .groupBy(true)
                    .beanProperty("name"),
            Continent.SURFACE_AREA.define()
                    .column()
                    .caption("Surface area")
                    .expression("sum(surfacearea)")
                    .aggregate(true)
                    .numberFormatGrouping(true),
            Continent.POPULATION.define()
                    .column()
                    .caption("Population")
                    .expression("sum(population)")
                    .aggregate(true)
                    .numberFormatGrouping(true),
            Continent.MIN_LIFE_EXPECTANCY.define()
                    .column()
                    .caption("Min. life expectancy")
                    .expression("min(lifeexpectancy)")
                    .aggregate(true),
            Continent.MAX_LIFE_EXPECTANCY.define()
                    .column()
                    .caption("Max. life expectancy")
                    .expression("max(lifeexpectancy)")
                    .aggregate(true),
            Continent.MIN_INDEPENDENCE_YEAR.define()
                    .column()
                    .caption("Min. ind. year")
                    .expression("min(indepyear)")
                    .aggregate(true),
            Continent.MAX_INDEPENDENCE_YEAR.define()
                    .column()
                    .caption("Max. ind. year")
                    .expression("max(indepyear)")
                    .aggregate(true),
            Continent.GNP.define()
                    .column()
                    .caption("GNP")
                    .expression("sum(gnp)")
                    .aggregate(true)
                    .numberFormatGrouping(true))
            .tableName("world.country")
            .readOnly(true)
            .caption("Continent"));
  }
  // end::continent[]

  // tag::locationConverter[]
  private static final class LocationConverter implements Converter<Location, String> {

    @Override
    public String toColumnValue(Location location,
                                Statement statement) {
      if (location == null) {
        return null;
      }

      return "POINT (" + location.longitude() + " " + location.latitude() + ")";
    }

    @Override
    public Location fromColumnValue(String columnValue) {
      if (columnValue == null) {
        return null;
      }

      String[] latLon = columnValue
              .replace("POINT (", "")
              .replace(")", "")
              .split(" ");

      return new Location(parseDouble(latLon[1]), parseDouble(latLon[0]));
    }
  }
  // end::locationConverter[]

  // tag::averageCityPopulationFunction[]
  private static final class AverageCityPopulationFunction implements DatabaseFunction<EntityConnection, String, Double> {

    @Override
    public Double execute(EntityConnection connection, String countryCode) throws DatabaseException {
      return connection.select(City.POPULATION,
                      City.COUNTRY_CODE.equalTo(countryCode))
              .stream()
              .mapToInt(Integer::intValue)
              .average()
              .orElse(0d);
    }
  }
  // end::averageCityPopulationFunction[]
}
