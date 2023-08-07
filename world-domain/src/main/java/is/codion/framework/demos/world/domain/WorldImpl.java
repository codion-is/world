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
import is.codion.framework.db.EntityConnection;
import is.codion.framework.demos.world.domain.api.World;
import is.codion.framework.domain.DefaultDomain;
import is.codion.framework.domain.entity.OrderBy;
import is.codion.framework.domain.entity.query.SelectQuery;
import is.codion.framework.domain.property.ColumnProperty.ValueConverter;

import java.sql.Statement;

import static is.codion.common.item.Item.item;
import static is.codion.framework.db.condition.Condition.attribute;
import static is.codion.framework.db.condition.Condition.where;
import static is.codion.framework.domain.entity.EntityDefinition.definition;
import static is.codion.framework.domain.entity.KeyGenerator.sequence;
import static is.codion.framework.domain.entity.OrderBy.ascending;
import static is.codion.framework.domain.property.Property.*;
import static java.lang.Double.parseDouble;
import static java.util.Arrays.asList;

// tag::world[]
public final class WorldImpl extends DefaultDomain implements World {

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
    add(definition(
            primaryKeyProperty(City.ID),
            columnProperty(City.NAME, "Name")
                    .searchProperty(true)
                    .nullable(false)
                    .maximumLength(35),
            columnProperty(City.COUNTRY_CODE)
                    .nullable(false),
            foreignKeyProperty(City.COUNTRY_FK, "Country"),
            columnProperty(City.DISTRICT, "District")
                    .nullable(false)
                    .maximumLength(20),
            columnProperty(City.POPULATION, "Population")
                    .nullable(false)
                    .numberFormatGrouping(true),
            columnProperty(City.LOCATION, "Location")
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
    add(definition(
            primaryKeyProperty(Country.CODE, "Country code")
                    .updatable(true)
                    .maximumLength(3),
            columnProperty(Country.NAME, "Name")
                    .searchProperty(true)
                    .nullable(false)
                    .maximumLength(52),
            itemProperty(Country.CONTINENT, "Continent", asList(
                    item("Africa"), item("Antarctica"), item("Asia"),
                    item("Europe"), item("North America"), item("Oceania"),
                    item("South America")))
                    .nullable(false),
            columnProperty(Country.REGION, "Region")
                    .nullable(false)
                    .maximumLength(26),
            columnProperty(Country.SURFACEAREA, "Surface area")
                    .nullable(false)
                    .numberFormatGrouping(true)
                    .maximumFractionDigits(2),
            columnProperty(Country.INDEPYEAR, "Indep. year")
                    .valueRange(-2000, 2500),
            columnProperty(Country.INDEPYEAR_SEARCHABLE)
                    .columnExpression("to_char(indepyear)")
                    .searchProperty(true)
                    .readOnly(true),
            columnProperty(Country.POPULATION, "Population")
                    .nullable(false)
                    .numberFormatGrouping(true),
            columnProperty(Country.LIFE_EXPECTANCY, "Life expectancy")
                    .maximumFractionDigits(1)
                    .valueRange(0, 99),
            columnProperty(Country.GNP, "GNP")
                    .numberFormatGrouping(true)
                    .maximumFractionDigits(2),
            columnProperty(Country.GNPOLD, "GNP old")
                    .numberFormatGrouping(true)
                    .maximumFractionDigits(2),
            columnProperty(Country.LOCALNAME, "Local name")
                    .nullable(false)
                    .maximumLength(45),
            columnProperty(Country.GOVERNMENTFORM, "Government form")
                    .nullable(false),
            columnProperty(Country.HEADOFSTATE, "Head of state")
                    .maximumLength(60),
            columnProperty(Country.CAPITAL),
            foreignKeyProperty(Country.CAPITAL_FK, "Capital"),
            denormalizedProperty(Country.CAPITAL_POPULATION, "Capital pop.",
                    Country.CAPITAL_FK, City.POPULATION)
                    .numberFormatGrouping(true),
            subqueryProperty(Country.NO_OF_CITIES, "No. of cities", """
                    select count(*)
                    from world.city
                    where city.countrycode = country.code"""),
            subqueryProperty(Country.NO_OF_LANGUAGES, "No. of languages", """
                    select count(*)
                    from world.countrylanguage
                    where countrylanguage.countrycode = country.code"""),
            blobProperty(Country.FLAG, "Flag")
                    .eagerlyLoaded(true),
            columnProperty(Country.CODE_2, "Code2")
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
    add(definition(
            columnProperty(CountryLanguage.COUNTRY_CODE)
                    .primaryKeyIndex(0)
                    .updatable(true),
            columnProperty(CountryLanguage.LANGUAGE, "Language")
                    .primaryKeyIndex(1)
                    .updatable(true),
            foreignKeyProperty(CountryLanguage.COUNTRY_FK, "Country"),
            columnProperty(CountryLanguage.IS_OFFICIAL, "Is official")
                    .columnHasDefaultValue(true)
                    .nullable(false),
            derivedProperty(CountryLanguage.NO_OF_SPEAKERS, "No. of speakers",
                    new NoOfSpeakersProvider(), CountryLanguage.COUNTRY_FK, CountryLanguage.PERCENTAGE)
                    .numberFormatGrouping(true),
            columnProperty(CountryLanguage.PERCENTAGE, "Percentage")
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
    add(definition(
            columnProperty(Lookup.COUNTRY_CODE, "Country code")
                    .primaryKeyIndex(0),
            columnProperty(Lookup.COUNTRY_NAME, "Country name"),
            columnProperty(Lookup.COUNTRY_CONTINENT, "Continent"),
            columnProperty(Lookup.COUNTRY_REGION, "Region"),
            columnProperty(Lookup.COUNTRY_SURFACEAREA, "Surface area")
                    .numberFormatGrouping(true),
            columnProperty(Lookup.COUNTRY_INDEPYEAR, "Indep. year"),
            columnProperty(Lookup.COUNTRY_POPULATION, "Country population")
                    .numberFormatGrouping(true),
            columnProperty(Lookup.COUNTRY_LIFEEXPECTANCY, "Life expectancy"),
            columnProperty(Lookup.COUNTRY_GNP, "GNP")
                    .numberFormatGrouping(true),
            columnProperty(Lookup.COUNTRY_GNPOLD, "GNP old")
                    .numberFormatGrouping(true),
            columnProperty(Lookup.COUNTRY_LOCALNAME, "Local name"),
            columnProperty(Lookup.COUNTRY_GOVERNMENTFORM, "Government form"),
            columnProperty(Lookup.COUNTRY_HEADOFSTATE, "Head of state"),
            blobProperty(Lookup.COUNTRY_FLAG, "Flag"),
            columnProperty(Lookup.COUNTRY_CODE2, "Code2"),
            columnProperty(Lookup.CITY_ID)
                    .primaryKeyIndex(1),
            columnProperty(Lookup.CITY_NAME, "City"),
            columnProperty(Lookup.CITY_DISTRICT, "District"),
            columnProperty(Lookup.CITY_POPULATION, "City population")
                    .numberFormatGrouping(true),
            columnProperty(Lookup.CITY_LOCATION, "City location")
                    .columnClass(String.class, new LocationConverter())
                    .comparator(new LocationComparator()))
            .selectQuery(SelectQuery.builder()
                    .from("world.country join world.city on city.countrycode = country.code")
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
    add(definition(
            columnProperty(Continent.NAME, "Continent")
                    .groupingColumn(true)
                    .beanProperty("name"),
            columnProperty(Continent.SURFACE_AREA, "Surface area")
                    .columnExpression("sum(surfacearea)")
                    .aggregateColumn(true)
                    .numberFormatGrouping(true),
            columnProperty(Continent.POPULATION, "Population")
                    .columnExpression("sum(population)")
                    .aggregateColumn(true)
                    .numberFormatGrouping(true),
            columnProperty(Continent.MIN_LIFE_EXPECTANCY, "Min. life expectancy")
                    .columnExpression("min(lifeexpectancy)")
                    .aggregateColumn(true),
            columnProperty(Continent.MAX_LIFE_EXPECTANCY, "Max. life expectancy")
                    .columnExpression("max(lifeexpectancy)")
                    .aggregateColumn(true),
            columnProperty(Continent.MIN_INDEPENDENCE_YEAR, "Min. ind. year")
                    .columnExpression("min(indepyear)")
                    .aggregateColumn(true),
            columnProperty(Continent.MAX_INDEPENDENCE_YEAR, "Max. ind. year")
                    .columnExpression("max(indepyear)")
                    .aggregateColumn(true),
            columnProperty(Continent.GNP, "GNP")
                    .columnExpression("sum(gnp)")
                    .aggregateColumn(true)
                    .numberFormatGrouping(true))
            .tableName("world.country")
            .readOnly(true)
            .caption("Continent"));
  }
  // end::continent[]

  // tag::locationConverter[]
  private static final class LocationConverter implements ValueConverter<Location, String> {

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
                      where(attribute(City.COUNTRY_CODE).equalTo(countryCode)))
              .stream()
              .mapToInt(Integer::intValue)
              .average()
              .orElse(0d);
    }
  }
  // end::averageCityPopulationFunction[]
}
