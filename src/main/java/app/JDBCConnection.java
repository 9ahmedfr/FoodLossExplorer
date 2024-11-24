
package app;

import java.util.ArrayList;

//import org.eclipse.jetty.io.ssl.SslHandshakeListener.Event;
//import java.lang.reflect.Array;
//import javax.swing.text.Highlighter;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCConnection {

    // Name of database file (contained in database folder)
    public static final String DATABASE = "jdbc:sqlite:database/foodloss.db";

    /**
     * This creates a JDBC Object so we can keep talking to the database
     */
    public JDBCConnection() {
        System.out.println("Created JDBC Connection Object");
    }

/* Methods for Page ST1A
<--- Collapse & Expand Here*/

    public static int getLowestYear() {

        int lowestYear = 0;

        Connection connection = null;

        try {
        connection = DriverManager.getConnection(JDBCConnection.DATABASE);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);

        // The Query
        String query = "SELECT MIN(year) as LowestYear from CountryLossEvent;";

        // Get Result
        ResultSet results = statement.executeQuery(query);

        lowestYear = results.getInt("LowestYear");
        
        statement.close();
        } 
        
        catch (SQLException e) {
            System.err.println(e.getMessage());
        } 
        
        finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } 
            
            catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return lowestYear;
    }

    public static int getHighestYear() {

        int highestYear = 0;

        Connection connection = null;

        try {
        connection = DriverManager.getConnection(JDBCConnection.DATABASE);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);

        // The Query
        String query = "SELECT MAX(year) as HighestYear from CountryLossEvent;";

        // Get Result
        ResultSet results = statement.executeQuery(query);

        highestYear = results.getInt("HighestYear");
        
        statement.close();
        } 
        
        catch (SQLException e) {
            System.err.println(e.getMessage());
        } 
        
        finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } 
            
            catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return highestYear;
    }

    public static FoodLossEvent getHighestLossForAYearRangeForCommodity(int startYear, int endYear) {

        double highestLossPercentage = 0;
        int highestLossYear = 0;
        String commodityName = "";
        FoodLossEvent foodLossEvent = null;

        Connection connection = null;

        try {
        connection = DriverManager.getConnection(JDBCConnection.DATABASE);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);

        // The Query
        String query = String.format("""
            SELECT ROUND(AVG(percentage), 1) as loss, co.commodityName AS commodityName, year
            FROM Country c
            JOIN CountryLossEvent cl ON cl.m49Code = c.m49Code
            JOIN Commodity co ON co.cpcCode = cl.cpcCode
            WHERE Year >= %d AND Year <= %d
            GROUP BY CommodityName, year
            Order BY loss DESC
            LIMIT 1;
            """, startYear, endYear);

        // Get Result
        ResultSet results = statement.executeQuery(query);

        while (results.next()) {
            highestLossPercentage = results.getDouble("loss");
            commodityName = results.getString("commodityName");
            highestLossYear = results.getInt("year");
            foodLossEvent = new FoodLossEvent(commodityName, highestLossYear, highestLossPercentage);
        }

        statement.close();
        } 
        
        catch (SQLException e) {
            System.err.println(e.getMessage());
        } 
        
        finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } 
            
            catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return foodLossEvent;
    }

/* Methods for Page ST2A
<--- Collapse & Expand Here*/

    public static ArrayList<Country> getAllCountries() {
        // Create the ArrayList of Country objects to return
        ArrayList<Country> countries = new ArrayList<Country>();

        // Setup the variable for the JDBC connection
        Connection connection = null;

        try {
        // Connect to JDBC data base
        connection = DriverManager.getConnection(DATABASE);

        // Prepare a new SQL Query & Set a timeout
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);

        // The Query
        String query = "SELECT * FROM Country ORDER BY countryName ASC";

        // Get Result
        ResultSet results = statement.executeQuery(query);

        // Process all of the results
        while (results.next()) {
            // Lookup the columns we need
            String m49Code = results.getString("m49Code");
            String name = results.getString("countryName");

            // Create a Country Object
            Country country = new Country(m49Code, name);

            // Add the Country object to the array
            countries.add(country);
        }

        // Close the statement because we are done with it
        statement.close();
        } catch (SQLException e) {
            // If there is an error, lets just pring the error
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
                }
            }

        // Finally we return all of the countries
        return countries;
    }

    public static ArrayList<String> getAllCountryNames() {

        ArrayList<String> countries = new ArrayList<String>();

        Connection connection = null;

        try {

            connection = DriverManager.getConnection(JDBCConnection.DATABASE);

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            String query = "SELECT * FROM Country ORDER BY countryName ASC";

            // Get Result
            ResultSet results = statement.executeQuery(query);

            // Process all of the results
            while (results.next()) {
                String countryName = results.getString("countryName");
                countries.add(countryName);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                }
            }

        return countries;
    }

    public static ArrayList<Integer> getYearRangeForACountry(String CountryName, int Startyear, int Endyear, boolean yearsInBetween) {
        
        ArrayList<Integer> years = new ArrayList<Integer>();
        PreparedStatement statement = null;

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(JDBCConnection.DATABASE);

            String query = "";

            if (!yearsInBetween) { query += "SELECT year FROM (";}
            
            query += " SELECT DISTINCT year FROM CountryLossEvent cl JOIN Country C on c.m49Code = cl.m49Code WHERE CountryName = ? AND  year>= ? and year <= ? Order By Year ";
            
            if (!yearsInBetween) { query += """
                LIMIT 1) UNION SELECT YEAR FROM 
                (SELECT DISTINCT year FROM CountryLossEvent cl JOIN Country C on c.m49Code = cl.m49Code
                WHERE CountryName = ? AND  year>= ? and year <= ? Order By Year DESC LIMIT 1) ORDER BY year;""";}

            statement = connection.prepareStatement(query);
            statement.setString(1, CountryName);
            statement.setInt(2, Startyear);
            statement.setInt(3, Endyear);
            if (!yearsInBetween)
            {statement.setString(4, CountryName);
            statement.setInt(5, Startyear);
            statement.setInt(6, Endyear);}

            ResultSet results = statement.executeQuery();

            while (results.next()) {
                int year = results.getInt("year");
                years.add(year);
            }

            statement.close();
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return years;
    }

    public static double getLossForAYear(String country, int year, boolean activity, boolean supply, boolean cause) {
            
        double value = 0;
        // Setup the variable for the JDBC connection
        Connection connection = null;

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            String query = String.format("""
                            SELECT ROUND(AVG(AveragePercentage), 2) AS LossValue FROM (

                                SELECT 
                                    AVG(cle.Percentage) AS AveragePercentage 
                                FROM 
                                    Country c 
                                JOIN 
                                    CountryLossEvent cle ON c.m49Code = cle.m49Code 
                                JOIN 
                                    Commodity co ON cle.CPCCode = co.CPCCode 
                                WHERE 
                                    c.CountryName = "%s" AND cle.Year = '%s'
                                GROUP BY 
                                    co.CommodityName
                            """, country, ""+year);

            if (activity) query += ", cle.Activity";
            if (supply) query += ", cle.supplyStage";
            if (cause) query += ", cle.Cause";
            
            query +=    ");";

            // Get Result
            ResultSet results = statement.executeQuery(query);


            while (results.next()) {
                value = results.getDouble("LossValue");  
            }

            // Close the statement because we are done with it
            statement.close();
        } catch (SQLException e) {
            // If there is an error, lets just print the error
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        
        return value;
    }

    public static int getMinOrMaxYearForACountryYearRange(String CountryName, int Startyear, int Endyear, String minOrMax) {
        
        PreparedStatement statement = null;
        int year = 0;
        

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(JDBCConnection.DATABASE);
            
            String query = String.format("""
                    SELECT DISTINCT year FROM CountryLossEvent cl JOIN Country C on c.m49Code = cl.m49Code
                    WHERE CountryName = ? AND  year>= ? and year <= ? Order By Year %s LIMIT 1;"""
                , minOrMax.equals("min") ? "" : "DESC");

            statement = connection.prepareStatement(query);
            statement.setString(1, CountryName);
            statement.setInt(2, Startyear);
            statement.setInt(3, Endyear);

            ResultSet results = statement.executeQuery();
            
            while (results.next()) {
                year = results.getInt("year");
            }

            statement.close();
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return year;
    }
    
    public static ArrayList<FoodLossEvent> getCommodityLossForAYear(String country, int year, boolean isCheckedActivity, boolean isCheckedSupply, boolean isCheckedCause) {
        // Create the ArrayList of Country objects to return
        
        ArrayList<FoodLossEvent> FoodLossEvents = new ArrayList<FoodLossEvent>();

        // Setup the variable for the JDBC connection
        Connection connection = null;

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            String query = String.format("""
                            SELECT 
                                COALESCE(NULLIF(co.CommodityName, ''), 'N/A') AS CommodityName, 
                                COALESCE(NULLIF(cle.Activity, ''), 'N/A') AS Activity, 
                                COALESCE(NULLIF(cle.supplyStage, ''), 'N/A') AS Stage,
                                COALESCE(NULLIF(cle.Cause, ''), 'N/A') AS Cause,
                                ROUND(AVG(cle.Percentage), 2) AS Percentage 
                            FROM 
                                Country c 
                            JOIN 
                                CountryLossEvent cle ON c.m49Code = cle.m49Code 
                            JOIN 
                                Commodity co ON cle.CPCCode = co.CPCCode 
                            WHERE 
                                c.CountryName = "%s" AND cle.Year = "%s"
                            GROUP BY 
                                COALESCE(NULLIF(co.CommodityName, ''), 'N/A')
                            """, country, ""+year);

                    if (isCheckedActivity) query += ", COALESCE(NULLIF(cle.Activity, ''), 'N/A')";
                    if (isCheckedSupply) query += ", COALESCE(NULLIF(cle.supplyStage, ''), 'N/A')";
                    if (isCheckedCause) query += ", COALESCE(NULLIF(cle.Cause, ''), 'N/A')";

                    query += "ORDER BY COALESCE(NULLIF(co.CommodityName, ''), 'N/A') ";

                    if (isCheckedActivity) query += ", COALESCE(NULLIF(cle.Activity, ''), 'N/A')";
                    if (isCheckedSupply) query += ", COALESCE(NULLIF(cle.supplyStage, ''), 'N/A')";
                    if (isCheckedCause) query += ", COALESCE(NULLIF(cle.Cause, ''), 'N/A')";

            
            // Get Result
            ResultSet results = statement.executeQuery(query);

            // Process all of the results
            while (results.next()) {
                // Lookup the columns we need
                String commodity  = results.getString("CommodityName");
                String activity = results.getString("Activity");
                String stage = results.getString("Stage");
                String cause = results.getString("Cause");
                Double percentage = Double.parseDouble(results.getString("Percentage"));

                FoodLossEvent foodLossEvent = new FoodLossEvent(commodity, percentage);

                if (isCheckedActivity) foodLossEvent.setActivity(activity); 
                if (isCheckedSupply) foodLossEvent.setStage(stage);
                if (isCheckedCause) foodLossEvent.setCause(cause);

                FoodLossEvents.add(foodLossEvent);
            }        

            // Close the statement because we are done with it
            statement.close();
        } catch (SQLException e) {
            // If there is an error, lets just print the error
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        // Finally we return all of the countries
        return FoodLossEvents;
    }

    public static ArrayList<String> getSortedCountryNames(String[] selectedCountries, int startyear, int endyear, String sort, boolean activity, boolean supply, boolean cause) {
        
        ArrayList<String> sortedCountriesList = new ArrayList<>();
        Connection connection = null;
        String baseQuery = "";
        String query = "";
        String selectedCountriesString = "";
    
        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(DATABASE);
            Statement statement = connection.createStatement();
    
            if (sort.equals("AZ") || sort.equals("ZA")) {
                
                query += 
                    "SELECT DISTINCT c.CountryName AS CountryName, 'N/A' AS LossValue " +
                    "FROM Country c " +
                    "JOIN CountryLossEvent cl ON cl.m49Code = c.m49Code " +
                    "WHERE c.CountryName IN (";
                
                for (int i = 0; i < selectedCountries.length; i++) {
                    
                    if (selectedCountries[i].equals("all_countries")) {
                        continue;
                    }

                    if (i != selectedCountries.length - 1) {
                        query += "'" + PageST2A.escapeSingleQuotesSQL(selectedCountries[i]) + "', ";
                    } else {
                        query += "'" + PageST2A.escapeSingleQuotesSQL(selectedCountries[i]) + "'";
                    }
                    
                }

                query += String.format("""
                    ) and cl.year BETWEEN %d AND %d
                    ORDER BY c.CountryName %s;
                    """, startyear, endyear, sort.equals("AZ") ? "ASC" : "DESC");
            }
            
            else {
                
                baseQuery = """
        
                        WITH YearPerCountry AS (
                            SELECT 
                                c.CountryName, 
                                %s(cle.Year) AS Year
                            FROM 
                                Country c
                            JOIN 
                                CountryLossEvent cle ON c.m49Code = cle.m49Code
                            WHERE 
                                c.CountryName IN (%s)
                                AND cle.Year >= %s
                                AND cle.Year <= %s
                            GROUP BY 
                                c.CountryName
                        )
                        SELECT 
                            CountryName,
                            AVG(AveragePercentage) AS LossValue 
                        FROM (
                            SELECT 
                                c.CountryName,
                                AVG(cle.Percentage) AS AveragePercentage 
                            FROM 
                                Country c 
                            JOIN 
                                CountryLossEvent cle ON c.m49Code = cle.m49Code 
                            JOIN 
                                Commodity co ON cle.CPCCode = co.CPCCode 
                            JOIN 
                                YearPerCountry ypc ON c.CountryName = ypc.CountryName AND cle.Year = ypc.Year
                            WHERE 
                                c.CountryName IN (%s)
                            GROUP BY 
                                c.CountryName, co.commodityName %s %s %s) AS grouped
                        GROUP BY
                            countryName
                        ORDER BY
                            lossValue %s, CountryName;
                        """;

                String yearFunction = sort.charAt(0) == 'S' ? "MIN" : "MAX";
                String order = sort.charAt(1) == 'H' ? "DESC" : "ASC";
                selectedCountriesString = String.join("\", \"", selectedCountries);
                

                query = String.format(baseQuery, yearFunction, "\"" + selectedCountriesString + "\"", 
                            startyear, endyear, "\"" + selectedCountriesString + "\"", activity? ", cle.Activity" : "", 
                            supply? ", cle.supplyStage" : "", cause? ", cle.Cause" : "", order);

            }

            // Get Result
            ResultSet results = statement.executeQuery(query);

            // Process all of the results
            while (results.next()) {
                // Lookup the columns we need
                String country = results.getString("CountryName");
                sortedCountriesList.add(country);
            }

            sortedCountriesList = getRemainingCountries(sortedCountriesList, selectedCountries, startyear, endyear, sort);

            // Close the statement because we are done with it
            statement.close();
        } catch (SQLException e) {
            // If there is an error, lets just print the error
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        // Convert the list back to an array
        return sortedCountriesList;
    }

    public static ArrayList<String> getRemainingCountries(ArrayList<String> sortedCountries, String[] selectedCountries, int startyear, int endyear, String sort) {

        Connection connection = null;
        String query = "";

        try {
            
            connection = DriverManager.getConnection(JDBCConnection.DATABASE);

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            query += """
                    SELECT DISTINCT c.CountryName AS CountryName, 'N/A' AS LossValue
                    FROM Country c
                    JOIN CountryLossEvent cl ON cl.m49Code = c.m49Code
                    WHERE c.CountryName IN (
                    
                    """;
                    
            for (int i = 0; i < selectedCountries.length; i++) {

                String country = PageST2A.escapeSingleQuotesSQL(selectedCountries[i]);

                if (country.equals("all_countries")) {
                    continue;
                }

                if (i != selectedCountries.length - 1) {
                    query += "'" + country + "', ";
                } else {
                    query += "'" + country + "'";
                }

            }
                         
            query += String.format("""
                    ) and c.CountryName NOT IN (
                        SELECT DISTINCT c.CountryName
                        FROM Country c
                        JOIN CountryLossEvent cl ON cl.m49Code = c.m49Code
                        WHERE cl.Year BETWEEN %d AND %d
                    )
                    ORDER BY c.CountryName %s;

                    """, startyear, endyear, sort.equals("ZA") ? "DESC" : "ASC");

            // Get Result
            ResultSet results = statement.executeQuery(query);

            // Process all of the results
            while (results.next()) {
                String countryName = results.getString("countryName");
                sortedCountries.add(countryName);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                }
            }

        return sortedCountries;
    }

    public static double getLossChangeForACountry(String country, int minYear, int maxYear, boolean activity, boolean supply, boolean cause) {
                
        double change = 0;
        // Setup the variable for the JDBC connection
        Connection connection = null;

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            String query = String.format("""

            WITH AvgMax AS (
            SELECT AVG(cle.Percentage) AS AveragePercentage
            FROM Country c 
            JOIN CountryLossEvent cle ON c.m49Code = cle.m49Code 
            JOIN Commodity co ON cle.CPCCode = co.CPCCode 
            WHERE c.CountryName = "%s" AND cle.Year = %d
            GROUP BY co.CommodityName
            """, country, maxYear);
            
            if (activity) query += ", cle.Activity";
            if (supply) query += ", cle.supplyStage";
            if (cause) query += ", cle.Cause";
            
            query += String.format("""
            ),
            AvgMin AS (
            SELECT AVG(cle.Percentage) AS AveragePercentage
            FROM Country c 
            JOIN CountryLossEvent cle ON c.m49Code = cle.m49Code 
            JOIN Commodity co ON cle.CPCCode = co.CPCCode 
            WHERE c.CountryName = "%s" AND cle.Year = %d
            GROUP BY co.CommodityName
            """, country, minYear);
            
            if (activity) query += ", cle.Activity";
            if (supply) query += ", cle.supplyStage";
            if (cause) query += ", cle.Cause";
            
            query += """
            )
            SELECT 
                ROUND((AvgMax.AveragePercentage - AvgMin.AveragePercentage), 2) AS LossChange
            FROM
                (SELECT AVG(AveragePercentage) AS AveragePercentage FROM AvgMax) AS AvgMax,
                (SELECT AVG(AveragePercentage) AS AveragePercentage FROM AvgMin) AS AvgMin;
                    """;

            // Get Result
            ResultSet results = statement.executeQuery(query);


            while (results.next()) {
                change = results.getDouble("LossChange");  
            }

            // Close the statement because we are done with it
            statement.close();
        } catch (SQLException e) {
            // If there is an error, lets just print the error
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        
        return change;
    }

    public static double getAverageLossForACountry(String country, int minYear, int maxYear, boolean activity, boolean supply, boolean cause, boolean yearsInBetween) {
                
        double average = 0;
        // Setup the variable for the JDBC connection
        Connection connection = null;

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            String query = "";
            
            if (!yearsInBetween) { 

                query = String.format("""

                    WITH AvgMax AS (
                    SELECT AVG(cle.Percentage) AS AveragePercentage
                    FROM Country c 
                    JOIN CountryLossEvent cle ON c.m49Code = cle.m49Code 
                    JOIN Commodity co ON cle.CPCCode = co.CPCCode 
                    WHERE c.CountryName = "%s" AND cle.Year = %d
                    GROUP BY co.CommodityName
                    """, country, maxYear);
                    
                    if (activity) query += ", cle.Activity";
                    if (supply) query += ", cle.supplyStage";
                    if (cause) query += ", cle.Cause";
                
                query += String.format("""
                    ),
                    AvgMin AS (
                    SELECT AVG(cle.Percentage) AS AveragePercentage
                    FROM Country c 
                    JOIN CountryLossEvent cle ON c.m49Code = cle.m49Code 
                    JOIN Commodity co ON cle.CPCCode = co.CPCCode 
                    WHERE c.CountryName = "%s" AND cle.Year = %d
                    GROUP BY co.CommodityName
                    """, country, minYear);
                    
                    if (activity) query += ", cle.Activity";
                    if (supply) query += ", cle.supplyStage";
                    if (cause) query += ", cle.Cause";
                
                query += """
                    )
                    SELECT 
                        Round((AvgMax.AveragePercentage + AvgMin.AveragePercentage)/2, 2) AS average
                    FROM
                        (SELECT AVG(AveragePercentage) AS AveragePercentage FROM AvgMax) AS AvgMax,
                        (SELECT AVG(AveragePercentage) AS AveragePercentage FROM AvgMin) AS AvgMin;
                            """;
            } else { 
            
                query = String.format("""

                    SELECT ROUND(AVG(LossValue), 2) as average FROM
                    (SELECT Year, AVG(AveragePercentage) AS LossValue 
                    FROM (
                        SELECT cle.Year, AVG(cle.Percentage) AS AveragePercentage
                        FROM Country c 
                        JOIN CountryLossEvent cle ON c.m49Code = cle.m49Code 
                        JOIN Commodity co ON cle.CPCCode = co.CPCCode 
                        WHERE c.CountryName = "%s" AND cle.Year >= %d AND cle.Year <= %d
                        GROUP BY cle.Year, co.CommodityName """, country, minYear, maxYear);
                        
                        if (activity) query += ", cle.Activity";
                        if (supply) query += ", cle.supplyStage";
                        if (cause) query += ", cle.Cause";

                query += """

                        ) AS AvgByYearAndCommodity
                        GROUP BY Year
                        ORDER BY Year) AS value;
                            """;   

            }

            // Get Result
            ResultSet results = statement.executeQuery(query);


            while (results.next()) {
                average = results.getDouble("average");  
            }

            // Close the statement because we are done with it
            statement.close();
        } catch (SQLException e) {
            // If there is an error, lets just print the error
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        
        return average;
    }

/* Methods for Page ST3A
<--- Collapse & Expand Here*/

    public static ArrayList<Region> getAllRegionNamesForACountry(String country) {

        ArrayList<Region> region = new ArrayList<Region>();

        Connection connection = null;

        try {

            connection = DriverManager.getConnection(JDBCConnection.DATABASE);

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            String query = "Select Distinct (RegionName) as region, c.m49Code as Code from Region r JOIN country c on c.m49Code = r.m49Code\n" +
                                "Where CountryName = \"" + country + "\"\n" +
                                "Order By regionName;";

            // Get Result
            ResultSet results = statement.executeQuery(query);

            // Process all of the results
            while (results.next()) {
                String regionName = results.getString("region");
                int m49Code = results.getInt("Code");
                Region regionNameObject = new Region(m49Code, regionName);
                region.add(regionNameObject);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                }
            }

        return region;
    }

    public static ArrayList<String> getCountriesInYear(int year, String selectedCountry) {

        Connection connection = null;
        String query = "";
        ArrayList<String> Countries = new ArrayList<String>();

        try {
            
            connection = DriverManager.getConnection(JDBCConnection.DATABASE);

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            query += String.format("""

                SELECT DISTINCT CountryName AS countryName
                FROM Country c1
                JOIN CountryLossEvent cl1 ON cl1.m49Code = c1.m49Code
                JOIN Commodity co1 ON co1.cpcCode = cl1.cpcCode
                WHERE YEAR = %d
                AND countryName != "%s"
                AND EXISTS (
                    SELECT 1
                    FROM Country c2
                    JOIN CountryLossEvent cl2 ON cl2.m49Code = c2.m49Code
                    WHERE c2.CountryName = "%s" AND cl2.YEAR = %d
                )
                ORDER BY countryName;
                
                    """, year, selectedCountry, selectedCountry, year);


            // Get Result
            ResultSet results = statement.executeQuery(query);

            // Process all of the results
            while (results.next()) {
                String countryName = results.getString("countryName");
                Countries.add(countryName);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                }
            }

        return Countries;
    }

    public static Country getOverallSimilarityScoreForCountry(Country countryObject, int year, String mainCountry, String similarity, String similarityType) {
        
        String country = countryObject.getName();

        Connection connection = null;
        String query = "";

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            query += String.format("""

            WITH SelectedCountryAvg AS (
                SELECT AVG(AveragePercentage) AS AvgPercentage 
                FROM (
                    SELECT 
                        c.CountryName,
                        AVG(cle.Percentage) AS AveragePercentage 
                    FROM 
                        Country c 
                    JOIN 
                        CountryLossEvent cle ON c.m49Code = cle.m49Code 
                    JOIN 
                        Commodity co ON cle.CPCCode = co.CPCCode 
                    WHERE 
                        YEAR = %d AND c.CountryName = "%s"
                    GROUP BY 
                        c.CountryName, co.commodityName
                )
            ),
                    """, year, mainCountry);


            query += String.format("""
                    
            OtherCountryAverage AS (
                SELECT AVG(AveragePercentage) AS AvgPercentage 
                FROM (
                    SELECT 
                        c.CountryName,
                        AVG(cle.Percentage) AS AveragePercentage 
                    FROM 
                        Country c 
                    JOIN 
                        CountryLossEvent cle ON c.m49Code = cle.m49Code 
                    JOIN 
                        Commodity co ON cle.CPCCode = co.CPCCode 
                    WHERE 
                        YEAR = %d AND c.CountryName = "%s"
                    GROUP BY 
                        c.CountryName, co.commodityName
                )
            ),
                    """, year, country);

                
            query += String.format("""

            ProductsInMainCountry AS (
                SELECT COUNT(DISTINCT co1.CommodityName) AS Count, c1.CountryName
                FROM Country c1
                JOIN CountryLossEvent cl1 ON cl1.m49Code = c1.m49Code
                JOIN Commodity co1 ON co1.CPCCode = cl1.CPCCode
                WHERE cl1.YEAR = %d AND c1.CountryName = "%s"
            ),
                
                """, year, mainCountry);

            query += String.format("""
                
            ProductsInOtherCountry AS (
                SELECT COUNT(DISTINCT co2.CommodityName) AS Count
                FROM Country c2
                JOIN CountryLossEvent cl2 ON cl2.m49Code = c2.m49Code
                JOIN Commodity co2 ON co2.CPCCode = cl2.CPCCode
                WHERE cl2.YEAR = %d AND c2.CountryName = "%s"
            ),

            """, year, country);

            query += String.format("""

            CommonProducts AS (
                SELECT COUNT(DISTINCT co1.CommodityName) AS Count
                FROM Country c1
                JOIN CountryLossEvent cl1 ON cl1.m49Code = c1.m49Code
                JOIN Commodity co1 ON co1.CPCCode = cl1.CPCCode
                WHERE cl1.YEAR = %d AND c1.CountryName = "%s"
                AND co1.CommodityName IN (
                    SELECT DISTINCT co2.CommodityName
                    FROM Country c2
                    JOIN CountryLossEvent cl2 ON cl2.m49Code = c2.m49Code
                    JOIN Commodity co2 ON co2.CPCCode = cl2.CPCCode
                    WHERE cl2.YEAR = %d AND c2.CountryName = "%s"
                )
            ),

            """, year, mainCountry, year, country);

            query += String.format("""

            TotalUniqueProducts AS (
                SELECT COUNT(DISTINCT CommodityName) AS Count
                FROM (
                    SELECT co1.CommodityName
                    FROM Country c1
                    JOIN CountryLossEvent cl1 ON cl1.m49Code = c1.m49Code
                    JOIN Commodity co1 ON co1.CPCCode = cl1.CPCCode
                    WHERE cl1.YEAR = %d AND c1.CountryName IN ("%s", "%s")
                ) AS AllProducts
            )
            """ , year, mainCountry, country);

            query += """
                    
            SELECT 
                CommonProducts.Count AS CountCommon,
                TotalUniqueProducts.Count AS CountTotalUnique,
                ROUND((CommonProducts.Count * 1.0 / ProductsInMainCountry.Count) * 100, 2) AS AbsoluteScore,
                ROUND((CommonProducts.Count * 1.0 / TotalUniqueProducts.Count) * 100, 2) AS OverlapScore,
                ROUND(100 * (1 - (ABS(SCA.AvgPercentage - ACA.AvgPercentage)/(SCA.AvgPercentage + ACA.AvgPercentage))), 2) AS LossScore,
                ROUND(ACA.AvgPercentage, 2) as LossPercentage,
                ROUND((100 * (1 - (ABS(SCA.AvgPercentage - ACA.AvgPercentage)/(SCA.AvgPercentage + ACA.AvgPercentage))) + 
                (CommonProducts.Count * 1.0 / ProductsInMainCountry.Count) * 100) / 2, 2) AS BothAbsolute,
                ROUND((100 * (1 - (ABS(SCA.AvgPercentage - ACA.AvgPercentage)/(SCA.AvgPercentage + ACA.AvgPercentage))) +
                (CommonProducts.Count * 1.0 / TotalUniqueProducts.Count) * 100) / 2, 2) AS BothOverlap
                
            FROM 
                ProductsInMainCountry,
                ProductsInOtherCountry,
                CommonProducts,
                TotalUniqueProducts,
                SelectedCountryAvg SCA JOIN OtherCountryAverage ACA;

                    """;



            ResultSet results = statement.executeQuery(query);
            
            while (results.next()) {
                double similarityFoodOverlap = results.getDouble("OverlapScore");
                double similarityFoodAbsolute = results.getDouble("AbsoluteScore");
                double similarityLoss = results.getDouble("LossScore");
                double lossPercentage = results.getDouble("LossPercentage");
                double similarityBothOverlap = results.getDouble("BothOverlap");
                double similarityBothAbsolute = results.getDouble("BothAbsolute");
                int CountCommon = results.getInt("CountCommon");
                int UniqueProducts = results.getInt("CountTotalUnique");;

                countryObject.setNumberOfCommonProducts(CountCommon);
                countryObject.setNumberOfUniqueProducts(UniqueProducts);
                countryObject.setAverageLoss(lossPercentage);

                if (similarity.equals("food")) {
                    if (similarityType.equals("overlap")) {
                        countryObject.setOverallScore(similarityFoodOverlap);
                    } else if (similarityType.equals("absolute")) {
                        countryObject.setOverallScore(similarityFoodAbsolute);
                    }
                } else if (similarity.equals("loss")) {
                    countryObject.setOverallScore(similarityLoss);
                } else if (similarity.equals("both")) {
                    if (similarityType.equals("overlap")) {
                        countryObject.setOverallScore(similarityBothOverlap);
                    } else if (similarityType.equals("absolute")) {
                        countryObject.setOverallScore(similarityBothAbsolute);
                    }
                }
            }

            // Close the statement because we are done with it
            statement.close();

        } catch (SQLException e) {
            // If there is an error, lets just pring the error
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
                }
            }

        // Finally we return all of the countries
        return countryObject;
    }

    public static int getMostCommonYear() {
            
            int mostCommonYear = 0;
    
            Connection connection = null;
    
            try {
            connection = DriverManager.getConnection(JDBCConnection.DATABASE);
    
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
    
            // The Query
            String query = "SELECT Year, COUNT(Year) as YearCount FROM CountryLossEvent GROUP BY Year ORDER BY YearCount DESC LIMIT 1;";
    
            // Get Result
            ResultSet results = statement.executeQuery(query);
    
            mostCommonYear = results.getInt("Year");
            
            statement.close();
            } 
            
            catch (SQLException e) {
                System.err.println(e.getMessage());
            } 
            
            finally {
                // Safety code to cleanup
                try {
                    if (connection != null) {
                    connection.close();
                    }
                } 
                
                catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            }
    
            return mostCommonYear;
    }

    public static ArrayList<Region> getRegionsInYear(int year, String selectedRegion, int m49Code) {

        Connection connection = null;
        String query = "";
        ArrayList<Region> Regions = new ArrayList<Region>();

        try {
            
            connection = DriverManager.getConnection(JDBCConnection.DATABASE);

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // The Query
            query += String.format("""

                SELECT DISTINCT r1.regionName AS regionName, r1.m49Code as Code, c.countryName
                FROM Region r1
                JOIN RegionLossEvent rl1 ON rl1.m49Code = r1.m49Code AND r1.RegionName = rl1.regionName
                JOIN Country c on c.m49Code = r1.m49Code
                WHERE YEAR = %d
                AND (r1.regionName != "%s"
                OR r1.m49Code != %d)

                AND EXISTS (
                    SELECT 1
                    FROM Region r2
                    JOIN RegionLossEvent rl2 ON rl2.m49Code = r2.m49Code AND r2.RegionName = rl2.regionName
                    WHERE r2.RegionName = "%s" AND rl2.YEAR = %d AND r2.m49Code = %d
                )
                ORDER BY r1.regionName;
                
                    """, year, selectedRegion, m49Code, selectedRegion, year, m49Code);
            
            // Get Result
            ResultSet results = statement.executeQuery(query);

            // Process all of the results
            while (results.next()) {
                String regionName = results.getString("regionName");
                int regionCode = results.getInt("Code");
                String countryName = results.getString("countryName");
                Region regionNameObject = new Region(regionCode, regionName);
                regionNameObject.setCountryName(countryName);
                Regions.add(regionNameObject);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                }
            }

        return Regions;
    }

    public static Region getOverallSimilarityScoreForRegion(Region regionObject, int year, String mainRegion, int mainM49Code, String similarity, String similarityType) {
        
        String region = regionObject.getName();
        int m49Code = regionObject.getM49Code();

        Connection connection = null;
        String query = "";

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            query += String.format("""

            WITH SelectedRegionAvg AS (
                SELECT AVG(AveragePercentage) AS AvgPercentage 
                FROM (
                    SELECT 
                        r.regionName,
                        AVG(rle.Percentage) AS AveragePercentage 
                    FROM 
                        Region r
                    JOIN 
                        RegionLossEvent rle ON r.m49Code = rle.m49Code AND r.RegionName = rle.RegionName 
                    JOIN 
                        Commodity co ON rle.CPCCode = co.CPCCode 
                    WHERE 
                        YEAR = %d AND r.RegionName = "%s" AND r.m49Code = %d
                    GROUP BY 
                        r.RegionName, co.commodityName
                )
            ),
                    """, year, mainRegion, mainM49Code);


            query += String.format("""
                    
            OtherRegionAverage AS (
                SELECT AVG(AveragePercentage) AS AvgPercentage 
                FROM (
                    SELECT 
                        r.RegionName,
                        AVG(rle.Percentage) AS AveragePercentage 
                    FROM 
                        Region r 
                    JOIN 
                        RegionLossEvent rle ON r.m49Code = rle.m49Code AND r.RegionName = rle.RegionName 
                    JOIN 
                        Commodity co ON rle.CPCCode = co.CPCCode 
                    WHERE 
                        YEAR = %d AND r.RegionName = "%s" AND r.m49Code = %d
                    GROUP BY 
                        r.RegionName, co.commodityName
                )
            ),
                    """, year, region, m49Code);

                
            query += String.format("""

            ProductsInMainRegion AS (
                SELECT COUNT(DISTINCT co1.CommodityName) AS Count, r1.RegionName
                FROM Region r1
                JOIN RegionLossEvent rle1 ON rle1.m49Code = r1.m49Code AND r1.RegionName = rle1.RegionName 
                JOIN Commodity co1 ON co1.CPCCode = rle1.CPCCode
                WHERE rle1.YEAR = %d AND r1.RegionName = "%s" AND r1.m49Code = %d
            ),
                
                """, year, mainRegion, mainM49Code);

            query += String.format("""
                
            ProductsInOtherRegion AS (
                SELECT COUNT(DISTINCT co2.CommodityName) AS Count, r2.RegionName as OtherRegion
                FROM Region r2
                JOIN RegionLossEvent rle2 ON rle2.m49Code = r2.m49Code AND r2.RegionName = rle2.RegionName 
                JOIN Commodity co2 ON co2.CPCCode = rle2.CPCCode
                WHERE rle2.YEAR = %d AND r2.RegionName = "%s" AND r2.m49Code = %d
            ),

                """, year, region, m49Code);

            query += String.format("""

            CommonProducts AS (
                SELECT COUNT(DISTINCT co1.CommodityName) AS Count
                FROM Region r1
                JOIN RegionLossEvent rle1 ON rle1.m49Code = r1.m49Code AND r1.RegionName = rle1.RegionName 
                JOIN Commodity co1 ON co1.CPCCode = rle1.CPCCode
                WHERE rle1.YEAR = %d AND r1.RegionName = "%s" AND r1.m49Code = %d
                AND co1.CommodityName IN (
                    SELECT DISTINCT co2.CommodityName
                    FROM Region r2
                    JOIN RegionLossEvent rle2 ON rle2.m49Code = r2.m49Code
                    JOIN Commodity co2 ON co2.CPCCode = rle2.CPCCode
                    WHERE rle2.YEAR = %d AND r2.RegionName = "%s" AND r2.m49Code = %d
                )
            ),

            """, year, mainRegion, mainM49Code, year, region, m49Code);

            query += String.format("""

            TotalUniqueProducts AS (
                SELECT COUNT(DISTINCT CommodityName) AS Count
                FROM (
                    SELECT co1.CommodityName
                    FROM Region r1
                    JOIN RegionLossEvent rle1 ON rle1.m49Code = r1.m49Code AND r1.RegionName = rle1.RegionName 
                    JOIN Commodity co1 ON co1.CPCCode = rle1.CPCCode
                    WHERE rle1.YEAR = %d AND r1.RegionName IN ("%s", "%s") AND r1.m49Code IN (%d, %d)
                ) AS AllProducts
            )
            """ , year, mainRegion, region, mainM49Code, m49Code);

            query += """
                    
            SELECT 
                ProductsInOtherRegion.Count AS CountInOther,
                CommonProducts.Count AS CountCommon,
                TotalUniqueProducts.Count AS CountTotalUnique,
                ROUND((CommonProducts.Count * 1.0 / ProductsInMainRegion.Count) * 100, 2) AS AbsoluteScore,
                ROUND((CommonProducts.Count * 1.0 / TotalUniqueProducts.Count) * 100, 2) AS OverlapScore,
                ROUND(100 * (1 - (ABS(SCA.AvgPercentage - ACA.AvgPercentage)/(SCA.AvgPercentage + ACA.AvgPercentage))), 2) AS LossScore,
                ROUND(ACA.AvgPercentage, 2) as LossPercentage,
                ROUND((100 * (1 - (ABS(SCA.AvgPercentage - ACA.AvgPercentage)/(SCA.AvgPercentage + ACA.AvgPercentage))) + 
                (CommonProducts.Count * 1.0 / ProductsInMainRegion.Count) * 100) / 2, 2) AS BothAbsolute,
                ROUND((100 * (1 - (ABS(SCA.AvgPercentage - ACA.AvgPercentage)/(SCA.AvgPercentage + ACA.AvgPercentage))) +
                (CommonProducts.Count * 1.0 / TotalUniqueProducts.Count) * 100) / 2, 2) AS BothOverlap
                
            FROM 
                ProductsInMainRegion,
                ProductsInOtherRegion,
                CommonProducts,
                TotalUniqueProducts,
                SelectedRegionAvg SCA JOIN OtherRegionAverage ACA;

                    """;



            ResultSet results = statement.executeQuery(query);
            
            while (results.next()) {
                double similarityFoodOverlap = results.getDouble("OverlapScore");
                double similarityFoodAbsolute = results.getDouble("AbsoluteScore");
                double similarityLoss = results.getDouble("LossScore");
                double lossPercentage = results.getDouble("LossPercentage");
                double similarityBothOverlap = results.getDouble("BothOverlap");
                double similarityBothAbsolute = results.getDouble("BothAbsolute");
                int CountCommon = results.getInt("CountCommon");
                int UniqueProducts = results.getInt("CountTotalUnique");;

                regionObject.setNumberOfCommonProducts(CountCommon);
                regionObject.setNumberOfUniqueProducts(UniqueProducts);
                regionObject.setAverageLoss(lossPercentage);

                if (similarity.equals("food")) {
                    if (similarityType.equals("overlap")) {
                        regionObject.setOverallScore(similarityFoodOverlap);
                    } else if (similarityType.equals("absolute")) {
                        regionObject.setOverallScore(similarityFoodAbsolute);
                    }
                } else if (similarity.equals("loss")) {
                    regionObject.setOverallScore(similarityLoss);
                } else if (similarity.equals("both")) {
                    if (similarityType.equals("overlap")) {
                        regionObject.setOverallScore(similarityBothOverlap);
                    } else if (similarityType.equals("absolute")) {
                        regionObject.setOverallScore(similarityBothAbsolute);
                    }
                }
            }

            // Close the statement because we are done with it
            statement.close();

        } catch (SQLException e) {
            // If there is an error, lets just pring the error
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
                }
            }

        return regionObject;
    }

    public static int getClosestYearForACountry(int year, String countryName) {
            
            int closestYear = 0;
            Connection connection = null;
            String query = "";
    
            try {
                // Connect to JDBC data base
                connection = DriverManager.getConnection(DATABASE);
    
                // Prepare a new SQL Query & Set a timeout
                Statement statement = connection.createStatement();
                statement.setQueryTimeout(30);
    
                query += String.format("""
    
                SELECT 
                    cle.Year AS Year
                FROM 
                    Country c 
                JOIN 
                    CountryLossEvent cle ON c.m49Code = cle.m49Code 
                WHERE 
                    c.CountryName = "%s"
                AND EXISTS

                    ( SELECT * FROM 
                    Country c2 JOIN 
                    CountryLossEvent cle2 ON c2.m49Code = cle2.m49Code 
                    WHERE 
                        c2.CountryName != "%s" AND Year = cle.year )

                GROUP BY 
                    cle.Year
                ORDER BY 
                    ABS(cle.Year - %d)
                LIMIT 1;
                    """, countryName, countryName, year);
    
                // Get Result
                ResultSet results = statement.executeQuery(query);
    
                while (results.next()) {
                    closestYear = results.getInt("Year");
                }
    
                // Close the statement because we are done with it
                statement.close();
            } catch (SQLException e) {
                // If there is an error, lets just print the error
                System.err.println(e.getMessage());
            } finally {
                // Safety code to cleanup
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // connection close failed.
                    System.err.println(e.getMessage());
                }
            }
            
            return closestYear;
    }

    public static int getClosestYearForARegion(int year, String regionName, int m49Code) {
            
            int closestYear = 0;
            Connection connection = null;
            String query = "";
    
            try {
                // Connect to JDBC data base
                connection = DriverManager.getConnection(DATABASE);
    
                // Prepare a new SQL Query & Set a timeout
                Statement statement = connection.createStatement();
                statement.setQueryTimeout(30);
    
                query += String.format("""
    
                SELECT 
                    rle.Year AS Year
                FROM 
                    Region r 
                JOIN 
                    RegionLossEvent rle ON r.m49Code = rle.m49Code AND r.RegionName = rle.RegionName 
                WHERE 
                    r.RegionName = "%s" AND r.m49Code = %d
                AND EXISTS

                    ( SELECT * FROM 
                    Region r2 JOIN 
                    RegionLossEvent rle2 ON r2.m49Code = rle2.m49Code AND r2.RegionName = rle2.RegionName 
                    WHERE 
                        (r2.RegionName != "%s" or r2.m49Code != %d) AND Year = rle.year)

                GROUP BY 
                    rle.Year
                ORDER BY 
                    ABS(rle.Year - %d)
                LIMIT 1;
                    """, regionName, m49Code, regionName, m49Code, year);
    
                // Get Result
                ResultSet results = statement.executeQuery(query);
    
                while (results.next()) {
                    closestYear = results.getInt("Year");
                }
    
                // Close the statement because we are done with it
                statement.close();
            } catch (SQLException e) {
                // If there is an error, lets just print the error
                System.err.println(e.getMessage());
            } finally {
                // Safety code to cleanup
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // connection close failed.
                    System.err.println(e.getMessage());
                }
            }
            
            return closestYear;
    }   

}