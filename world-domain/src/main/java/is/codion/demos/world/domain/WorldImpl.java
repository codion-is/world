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
 * Copyright (c) 2023 - 2025, Björn Darri Sigurðsson.
 */
package is.codion.demos.world.domain;

import is.codion.common.db.operation.DatabaseFunction;
import is.codion.common.item.Item;
import is.codion.framework.db.EntityConnection;
import is.codion.framework.domain.DomainModel;
import is.codion.framework.domain.entity.EntityDefinition;
import is.codion.framework.domain.entity.OrderBy;
import is.codion.framework.domain.entity.attribute.Column.Converter;
import is.codion.framework.domain.entity.query.EntitySelectQuery;

import java.sql.Statement;
import java.util.List;

import static is.codion.common.item.Item.item;
import static is.codion.demos.world.domain.api.World.*;
import static is.codion.framework.db.EntityConnection.Select.where;
import static is.codion.framework.domain.entity.KeyGenerator.sequence;
import static is.codion.framework.domain.entity.OrderBy.ascending;
import static java.lang.Double.parseDouble;

// tag::world[]
public final class WorldImpl extends DomainModel {

	private static final List<Item<String>> CONTINENT_ITEMS = List.of(
					item("Africa"), item("Antarctica"), item("Asia"),
					item("Europe"), item("North America"), item("Oceania"),
					item("South America"));

	public WorldImpl() {
		super(DOMAIN);
		//By default, you can't define a foreign key referencing an entity which
		//has not been defined, to prevent mistakes. But sometimes we have to
		//deal with cyclical dependencies, such as here, where city references
		//country and country references city. In these cases we can simply
		//disable foreign key validation.
		validateForeignKeys(false);

		add(city(), country(), countryLanguage(), lookup(), continent());
		add(Country.AVERAGE_CITY_POPULATION, new AverageCityPopulation());
	}
	// end::world[]

	// tag::city[]
	EntityDefinition city() {
		return City.TYPE.define(
										City.ID.define()
														.primaryKey(),
										City.NAME.define()
														.column()
														.caption("Name")
														.searchable(true)
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
														.numberGrouping(true),
										City.LOCATION.define()
														.column()
														.caption("Location")
														.converter(String.class, new LocationConverter())
														.comparator(new LocationComparator()))
						.keyGenerator(sequence("world.city_seq"))
						.validator(new CityValidator())
						.orderBy(ascending(City.NAME))
						.formatter(City.NAME)
						.caption("City")
						.build();
	}
	// end::city[]

	// tag::country[]
	EntityDefinition country() {
		return Country.TYPE.define(
										Country.CODE.define()
														.primaryKey()
														.caption("Country code")
														.updatable(true)
														.maximumLength(3),
										Country.NAME.define()
														.column()
														.caption("Name")
														.searchable(true)
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
														.numberGrouping(true)
														.fractionDigits(2),
										Country.INDEPYEAR.define()
														.column()
														.caption("Indep. year")
														.range(-2000, 2500),
										Country.INDEPYEAR_SEARCHABLE.define()
														.column()
														.expression("to_char(indepyear)")
														.searchable(true)
														.readOnly(true),
										Country.POPULATION.define()
														.column()
														.caption("Population")
														.nullable(false)
														.numberGrouping(true),
										Country.LIFE_EXPECTANCY.define()
														.column()
														.caption("Life expectancy")
														.fractionDigits(1)
														.range(0, 99),
										Country.GNP.define()
														.column()
														.caption("GNP")
														.numberGrouping(true)
														.fractionDigits(2),
										Country.GNPOLD.define()
														.column()
														.caption("GNP old")
														.numberGrouping(true)
														.fractionDigits(2),
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
														.numberGrouping(true),
										Country.NO_OF_CITIES.define()
														.subquery("""
																		SELECT COUNT(*)
																		FROM world.city
																		WHERE city.countrycode = country.code""")
														.caption("No. of cities"),
										Country.NO_OF_LANGUAGES.define()
														.subquery("""
																		SELECT COUNT(*)
																		FROM world.countrylanguage
																		WHERE countrylanguage.countrycode = country.code""")
														.caption("No. of languages"),
										Country.FLAG.define()
														.column()
														.caption("Flag")
														.selected(false),
										Country.CODE_2.define()
														.column()
														.caption("Code2")
														.nullable(false)
														.maximumLength(2))
						.orderBy(ascending(Country.NAME))
						.formatter(Country.NAME)
						.caption("Country")
						.build();
	}
	// end::country[]

	// tag::country_language[]
	EntityDefinition countryLanguage() {
		return CountryLanguage.TYPE.define(
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
														.hasDatabaseDefault(true)
														.nullable(false),
										CountryLanguage.NO_OF_SPEAKERS.define()
														.derived(CountryLanguage.COUNTRY_FK, CountryLanguage.PERCENTAGE)
														.value(new NoOfSpeakers())
														.caption("No. of speakers")
														.numberGrouping(true),
										CountryLanguage.PERCENTAGE.define()
														.column()
														.caption("Percentage")
														.nullable(false)
														.fractionDigits(1)
														.range(0, 100))
						.orderBy(OrderBy.builder()
										.ascending(CountryLanguage.LANGUAGE)
										.descending(CountryLanguage.PERCENTAGE)
										.build())
						.caption("Language")
						.build();
	}
	// end::country_language[]

	// tag::lookup[]
	EntityDefinition lookup() {
		return Lookup.TYPE.define(
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
														.numberGrouping(true),
										Lookup.COUNTRY_INDEPYEAR.define()
														.column()
														.caption("Indep. year"),
										Lookup.COUNTRY_POPULATION.define()
														.column()
														.caption("Country population")
														.numberGrouping(true),
										Lookup.COUNTRY_LIFEEXPECTANCY.define()
														.column()
														.caption("Life expectancy"),
										Lookup.COUNTRY_GNP.define()
														.column()
														.caption("GNP")
														.numberGrouping(true),
										Lookup.COUNTRY_GNPOLD.define()
														.column()
														.caption("GNP old")
														.numberGrouping(true),
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
														.column()
														.caption("Flag")
														.selected(false),
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
														.numberGrouping(true),
										Lookup.CITY_LOCATION.define()
														.column()
														.caption("City location")
														.converter(String.class, new LocationConverter())
														.comparator(new LocationComparator()))
						.selectQuery(EntitySelectQuery.builder()
										.from("world.country LEFT OUTER JOIN world.city ON city.countrycode = country.code")
										.build())
						.orderBy(OrderBy.builder()
										.ascending(Lookup.COUNTRY_NAME)
										.descending(Lookup.CITY_POPULATION)
										.build())
						.readOnly(true)
						.caption("Lookup")
						.build();
	}
	// end::lookup[]

	// tag::continent[]
	EntityDefinition continent() {
		return Continent.TYPE.define(
										Continent.NAME.define()
														.column()
														.caption("Continent")
														.groupBy(true),
										Continent.SURFACE_AREA.define()
														.column()
														.caption("Surface area")
														.expression("sum(surfacearea)")
														.aggregate(true)
														.numberGrouping(true),
										Continent.POPULATION.define()
														.column()
														.caption("Population")
														.expression("sum(population)")
														.aggregate(true)
														.numberGrouping(true),
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
														.numberGrouping(true))
						.table("world.country")
						.readOnly(true)
						.caption("Continent")
						.build();
	}
	// end::continent[]

	// tag::locationConverter[]
	private static final class LocationConverter implements Converter<Location, String> {

		@Override
		public String toColumnValue(Location location,
																Statement statement) {
			return "POINT (" + location.longitude() + " " + location.latitude() + ")";
		}

		@Override
		public Location fromColumnValue(String columnValue) {
			String[] latLon = columnValue
							.replace("POINT (", "")
							.replace(")", "")
							.split(" ");

			return new Location(parseDouble(latLon[1]), parseDouble(latLon[0]));
		}
	}
	// end::locationConverter[]

	// tag::averageCityPopulation[]
	private static final class AverageCityPopulation implements DatabaseFunction<EntityConnection, String, Double> {

		@Override
		public Double execute(EntityConnection connection, String countryCode) {
			return connection.select(where(City.COUNTRY_CODE.equalTo(countryCode))
											.attributes(City.POPULATION)
											.build()).stream()
							.map(city -> city.get(City.POPULATION))
							.mapToInt(Integer::intValue)
							.average()
							.orElse(0d);
		}
	}
	// end::averageCityPopulation[]
}
