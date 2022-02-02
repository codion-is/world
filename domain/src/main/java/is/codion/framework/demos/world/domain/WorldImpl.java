package is.codion.framework.demos.world.domain;

import is.codion.framework.demos.world.domain.api.World;
import is.codion.framework.domain.DefaultDomain;
import is.codion.framework.domain.entity.query.SelectQuery;
import is.codion.framework.domain.property.ColumnProperty.ValueConverter;

import java.sql.SQLException;
import java.sql.Statement;

import static is.codion.common.item.Item.item;
import static is.codion.framework.domain.entity.KeyGenerator.sequence;
import static is.codion.framework.domain.entity.OrderBy.orderBy;
import static is.codion.framework.domain.entity.StringFactory.stringFactory;
import static is.codion.framework.domain.property.Properties.*;
import static java.lang.Double.parseDouble;
import static java.util.Arrays.asList;

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

  void city() {
    define(City.TYPE,
            primaryKeyProperty(City.ID),
            columnProperty(City.NAME, "Name")
                    .searchProperty()
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
                    .columnClass(String.class, new LocationConverter()))
            .keyGenerator(sequence("world.city_seq"))
            .validator(new CityValidator())
            .orderBy(orderBy().ascending(City.NAME))
            .stringFactory(stringFactory(City.NAME))
            .foregroundColorProvider(new CityColorProvider())
            .caption("City");
  }

  void country() {
    define(Country.TYPE,
            primaryKeyProperty(Country.CODE, "Country code")
                    .updatable(true)
                    .maximumLength(3),
            columnProperty(Country.NAME, "Name")
                    .searchProperty()
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
                    .minimumValue(-2000).maximumValue(2500),
            columnProperty(Country.INDEPYEAR_SEARCHABLE)
                    .columnExpression("to_char(indepyear)")
                    .searchProperty()
                    .readOnly(),
            columnProperty(Country.POPULATION, "Population")
                    .nullable(false)
                    .numberFormatGrouping(true),
            columnProperty(Country.LIFE_EXPECTANCY, "Life expectancy")
                    .maximumFractionDigits(1)
                    .minimumValue(0).maximumValue(99),
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
            denormalizedViewProperty(Country.CAPITAL_POPULATION, "Capital pop.",
                    Country.CAPITAL_FK, City.POPULATION)
                    .numberFormatGrouping(true),
            subqueryProperty(Country.NO_OF_CITIES, "No. of cities",
                    "select count(*) from world.city " +
                            "where city.countrycode = country.code"),
            subqueryProperty(Country.NO_OF_LANGUAGES, "No. of languages",
                    "select count(*) from world.countrylanguage " +
                            "where countrylanguage.countrycode = country.code"),
            blobProperty(Country.FLAG, "Flag")
                    .eagerlyLoaded(),
            columnProperty(Country.CODE_2, "Code2")
                    .nullable(false)
                    .maximumLength(2))
            .orderBy(orderBy().ascending(Country.NAME))
            .stringFactory(stringFactory(Country.NAME))
            .caption("Country");
  }

  void countryLanguage() {
    define(CountryLanguage.TYPE,
            columnProperty(CountryLanguage.COUNTRY_CODE)
                    .primaryKeyIndex(0)
                    .updatable(true),
            columnProperty(CountryLanguage.LANGUAGE, "Language")
                    .primaryKeyIndex(1)
                    .updatable(true),
            foreignKeyProperty(CountryLanguage.COUNTRY_FK, "Country"),
            columnProperty(CountryLanguage.IS_OFFICIAL, "Is official")
                    .columnHasDefaultValue()
                    .nullable(false),
            derivedProperty(CountryLanguage.NO_OF_SPEAKERS, "No. of speakers",
                    new NoOfSpeakersProvider(), CountryLanguage.COUNTRY_FK, CountryLanguage.PERCENTAGE)
                    .numberFormatGrouping(true),
            columnProperty(CountryLanguage.PERCENTAGE, "Percentage")
                    .nullable(false)
                    .maximumFractionDigits(1)
                    .minimumValue(0).maximumValue(100))
            .orderBy(orderBy().ascending(CountryLanguage.LANGUAGE).descending(CountryLanguage.PERCENTAGE))
            .caption("Language");
  }

  void lookup() {
    define(Lookup.TYPE,
            columnProperty(Lookup.COUNTRY_CODE, "Country code"),
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
            columnProperty(Lookup.CITY_ID),
            columnProperty(Lookup.CITY_NAME, "City"),
            columnProperty(Lookup.CITY_DISTRICT, "District"),
            columnProperty(Lookup.CITY_POPULATION, "City population")
                    .numberFormatGrouping(true))
            .selectQuery(SelectQuery.builder("world.country join world.city on city.countrycode = country.code")
                    .build())
            .orderBy(orderBy().ascending(Lookup.COUNTRY_NAME).descending(Lookup.CITY_POPULATION))
            .readOnly()
            .caption("Lookup");
  }

  void continent() {
    define(Continent.TYPE, "world.country",
            columnProperty(Continent.NAME, "Continent")
                    .groupingColumn()
                    .beanProperty("name"),
            columnProperty(Continent.SURFACE_AREA, "Surface area")
                    .columnExpression("sum(surfacearea)")
                    .aggregateColumn()
                    .numberFormatGrouping(true),
            columnProperty(Continent.POPULATION, "Population")
                    .columnExpression("sum(population)")
                    .aggregateColumn()
                    .numberFormatGrouping(true),
            columnProperty(Continent.MIN_LIFE_EXPECTANCY, "Min. life expectancy")
                    .columnExpression("min(lifeexpectancy)")
                    .aggregateColumn(),
            columnProperty(Continent.MAX_LIFE_EXPECTANCY, "Max. life expectancy")
                    .columnExpression("max(lifeexpectancy)")
                    .aggregateColumn(),
            columnProperty(Continent.MIN_INDEPENDENCE_YEAR, "Min. ind. year")
                    .columnExpression("min(indepyear)")
                    .aggregateColumn(),
            columnProperty(Continent.MAX_INDEPENDENCE_YEAR, "Max. ind. year")
                    .columnExpression("max(indepyear)")
                    .aggregateColumn(),
            columnProperty(Continent.GNP, "GNP")
                    .columnExpression("sum(gnp)")
                    .aggregateColumn()
                    .numberFormatGrouping(true))
            .readOnly()
            .caption("Continent");
  }

  private static final class LocationConverter implements ValueConverter<Location, String> {

    @Override
    public String toColumnValue(Location geoPosition,
                                Statement statement) throws SQLException {
      if (geoPosition == null) {
        return null;
      }

      return "POINT (" + geoPosition.longitude() + " " + geoPosition.latitude() + ")";
    }

    @Override
    public Location fromColumnValue(String columnValue) throws SQLException {
      if (columnValue == null) {
        return null;
      }

      String[] latLon = columnValue.replace("POINT (", "")
              .replace(")", "").split(" ");

      return new Location(parseDouble(latLon[1]), parseDouble(latLon[0]));
    }
  }
}
