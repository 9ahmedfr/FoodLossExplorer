package app;

import java.util.ArrayList;

import helper.Commodity;
import helper.Student;
import helper.persona;
import helper.personaAttribute;
import helper.SimilarFoodResult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCConnection2 {
  public static final String DATABASE = "jdbc:sqlite:database/foodloss.db";

  public JDBCConnection2() {
    System.out.println("Created second JDBC Connection Object");
  }


  /* Methods for PageMission
  <-- collaspe and expand here */

  public static ArrayList<Student> getTeamMembers() {
    ArrayList<Student> teamMembers = new ArrayList<Student>();

    Connection connection = null;

    try {
      connection = DriverManager.getConnection(JDBCConnection2.DATABASE);

      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);

      String query = "SELECT * FROM student";

      ResultSet results = statement.executeQuery(query);

      while (results.next()) {
        Student s = new Student();
        s.ID = results.getString("studentID");
        s.name = results.getString("name");
        s.link = results.getString("link");

        teamMembers.add(s);
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

    return teamMembers;
  }

  public static ArrayList<persona> getPersonas() {
    ArrayList<persona> personas = new ArrayList<persona>();

    Connection connection = null;

    try {
      connection = DriverManager.getConnection(JDBCConnection2.DATABASE);

      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);

      String query = "SELECT * FROM persona";

      ResultSet results = statement.executeQuery(query);

      while (results.next()) {
        persona p = new persona();
        p.name = results.getString("name");
        p.img = results.getString("imageFilePath");

        personas.add(p);
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

    return personas;
  }

  public static ArrayList<personaAttribute> getAttributes(String name) {
    ArrayList<personaAttribute> attributes = new ArrayList<personaAttribute>();

    Connection connection = null;

    try {
      connection = DriverManager.getConnection(JDBCConnection2.DATABASE);

      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);

      String query = "SELECT * FROM personaattribute WHERE personaName = '" + name + "'";

      ResultSet results = statement.executeQuery(query);

      while (results.next()) {
        personaAttribute a = new personaAttribute();
        a.id = results.getString("ID");
        a.type = results.getString("attributeType");
        a.description = results.getString("description");

        attributes.add(a);
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

    return attributes;
  }

  /* Methods for PageST2B
  <-- collaspe and expand here */

  static ArrayList<Commodity> foodGroups = new ArrayList<Commodity>();

  public static String[] convertNamesToCodes(String[] selectedGroups) {
    String[] cpcCodes = new String[selectedGroups.length];

    for (int i = 0; i < selectedGroups.length; i++) {
      for (Commodity group : foodGroups) {
        if (group.name.equals(selectedGroups[i])) {
          cpcCodes[i] = group.cpcCode;
        }
      }
    }

    return cpcCodes;
  }

  public static String convertOneName(String groupName) {
    String cpcCode = "";

    for (Commodity group : foodGroups) {
      if (group.name.equals(groupName)) {
        cpcCode = group.cpcCode;
      }
    }

    return cpcCode;
  }


  public static ArrayList<String> getAllGroups() {
    ArrayList<String> groupNames = new ArrayList<String>();

    Connection connection = null;

    try {
      connection = DriverManager.getConnection(JDBCConnection2.DATABASE);

      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);

      String query = """
      SELECT *
      FROM Commodity
      WHERE cpcCode IN (SELECT SUBSTR(cpcCode, 1, 3) FROM CountryLossEvent) AND LENGTH(cpcCode) = 3
      ORDER BY commodityName ASC;
      """;

      ResultSet results = statement.executeQuery(query);

      while (results.next()) {
        String commodityName = results.getString("commodityName");

        Commodity commodityGroup = new Commodity();
        commodityGroup.cpcCode = results.getString("cpcCode");
        commodityGroup.name = results.getString("commodityName");
        foodGroups.add(commodityGroup);

        groupNames.add(commodityName);
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

    return groupNames;
  }

  public static ArrayList<String> getSortedGroups(String[] selectedGroups, int startyear, int endyear, String sort, boolean activity, boolean supply, boolean cause) {

    ArrayList<String> commodityNames = new ArrayList<>();

    Connection connection = null;
    String query = "";

    String[] cpcCodes = convertNamesToCodes(selectedGroups);

    try {
      connection = DriverManager.getConnection(JDBCConnection2.DATABASE);
      Statement statement = connection.createStatement();

      query = "DROP TABLE IF EXISTS SortedResults;";
      statement.executeUpdate(query);

      query = """
        CREATE TABLE SortedResults (
            CommodityName VARCHAR(255),
            startValue DECIMAL(10, 2),
            endValue DECIMAL(10, 2),
            lossChange DECIMAL(10, 2)
        );
        """;

      statement.executeUpdate(query);

      for (String code : cpcCodes) {
        query = String.format("""
          INSERT INTO SortedResults (CommodityName, startValue, endValue, lossChange)
          SELECT
              commodityTable.CommodityName,
              startTable.startValue,
              endTable.endValue,
              endTable.endValue - startTable.startValue AS lossChange
          FROM
              (
                  SELECT DISTINCT CommodityName, cpcCode
                  FROM Commodity
                  WHERE cpcCode = '%s'
              ) AS commodityTable
          CROSS JOIN
              (
                  SELECT AVG(percentage) AS startValue
                  FROM CountryLossEvent
                  WHERE year = (
                      SELECT year
                      FROM CountryLossEvent
                      WHERE cpcCode LIKE '%s%%' AND year >= %d AND year <= %d
                      ORDER BY year
                      LIMIT 1
                  )
                  AND cpcCode LIKE '%s%%'
              ) AS startTable
          CROSS JOIN
              (
                  SELECT AVG(percentage) AS endValue
                  FROM CountryLossEvent
                  WHERE year = (
                      SELECT year
                      FROM CountryLossEvent
                      WHERE cpcCode LIKE '%s%%' AND year >= %d AND year <= %d
                      ORDER BY year DESC
                      LIMIT 1
                  )
                  AND cpcCode LIKE '%s%%'
              ) AS endTable
          ;
          """, code, code, startyear, endyear, code, code, startyear, endyear, code
        );

        statement.executeUpdate(query);
      }

        query = String.format("""
          SELECT *
          FROM SortedResults
          ORDER BY lossChange %s
          ;
          """, sort);

      ResultSet results = statement.executeQuery(query);

      while (results.next()) {
          // Lookup the columns we need
          String CommodityName = results.getString("CommodityName");
          commodityNames.add(CommodityName);
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

    return commodityNames;
}

  public static ArrayList<Integer> getYearRangeForAGroup(String groupName, int Startyear, int Endyear, boolean yearsInBetween) {

    ArrayList<Integer> years = new ArrayList<Integer>();

    Connection connection = null;

    String cpcCode = convertOneName(groupName);

    try {
        connection = DriverManager.getConnection(JDBCConnection2.DATABASE);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);

        String query = "";

        if (!yearsInBetween) { query += "SELECT year FROM (";}

        query += String.format(" SELECT DISTINCT year FROM CountryLossEvent WHERE cpcCode LIKE '%s%%' AND year >= %d and year <= %d Order By year ", cpcCode, Startyear, Endyear);

        if (!yearsInBetween) { query += String.format("""
            LIMIT 1) UNION SELECT year FROM
            (SELECT DISTINCT year FROM CountryLossEvent
            WHERE cpcCode LIKE '%s%%' AND year >= %d and year <= %d Order By year DESC LIMIT 1) ORDER BY year;""", cpcCode, Startyear, Endyear);}

        ResultSet results = statement.executeQuery(query);

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

  public static double getGroupLossForAYear(String groupName, int year, boolean activity, boolean supply, boolean cause) {
    double value = 0;
    Connection connection = null;

    String cpcCode = convertOneName(groupName);

    try {
        connection = DriverManager.getConnection(JDBCConnection2.DATABASE);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);

        String query = String.format("""
                        SELECT ROUND(AVG(AveragePercentage), 2) AS LossValue FROM (

                            SELECT
                                AVG(cle.Percentage) AS AveragePercentage
                            FROM
                                Commodity c
                            JOIN
                                CountryLossEvent cle ON c.cpcCode = cle.cpcCode
                            JOIN
                                Commodity co ON cle.cpcCode = co.cpcCode
                            WHERE
                                c.cpcCode LIKE '%s%%' AND cle.Year = '%d'
                            GROUP BY
                                co.CommodityName
                        """, cpcCode, year);

        if (activity) query += ", cle.Activity";
        if (supply) query += ", cle.supplyStage";
        if (cause) query += ", cle.Cause";

        query +=    ");";

        ResultSet results = statement.executeQuery(query);

        while (results.next()) {
            value = results.getDouble("LossValue");
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

    return value;
  }

  public static ArrayList<FoodLossEvent> getCommodityLossForAYear(String foodGroup, int year, boolean isCheckedActivity, boolean isCheckedSupply, boolean isCheckedCause) {
    ArrayList<FoodLossEvent> FoodLossEvents = new ArrayList<FoodLossEvent>();

    Connection connection = null;

    String cpcCode = convertOneName(foodGroup);

    try {
        connection = DriverManager.getConnection(JDBCConnection2.DATABASE);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);

        String query = String.format("""
                        SELECT
                            COALESCE(NULLIF(co.CommodityName, ''), 'N/A') AS CommodityName,
                            COALESCE(NULLIF(cle.Activity, ''), 'N/A') AS Activity,
                            COALESCE(NULLIF(cle.supplyStage, ''), 'N/A') AS Stage,
                            COALESCE(NULLIF(cle.Cause, ''), 'N/A') AS Cause,
                            ROUND(AVG(cle.Percentage), 2) AS Percentage
                        FROM
                            Commodity c
                        JOIN
                            CountryLossEvent cle ON c.cpcCode = cle.cpcCode
                        JOIN
                            Commodity co ON cle.CPCCode = co.CPCCode
                        WHERE
                            c.cpcCode LIKE '%s%%' AND cle.Year = %d
                        GROUP BY
                            COALESCE(NULLIF(co.CommodityName, ''), 'N/A')
                        """, cpcCode, year);

                if (isCheckedActivity) query += ", COALESCE(NULLIF(cle.Activity, ''), 'N/A')";
                if (isCheckedSupply) query += ", COALESCE(NULLIF(cle.supplyStage, ''), 'N/A')";
                if (isCheckedCause) query += ", COALESCE(NULLIF(cle.Cause, ''), 'N/A')";

                query += "ORDER BY COALESCE(NULLIF(co.CommodityName, ''), 'N/A') ";

                if (isCheckedActivity) query += ", COALESCE(NULLIF(cle.Activity, ''), 'N/A')";
                if (isCheckedSupply) query += ", COALESCE(NULLIF(cle.supplyStage, ''), 'N/A')";
                if (isCheckedCause) query += ", COALESCE(NULLIF(cle.Cause, ''), 'N/A')";


        ResultSet results = statement.executeQuery(query);

        while (results.next()) {
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

    return FoodLossEvents;
  }

  /* Methods for PageST3B
  <-- collaspe and expand here */

  static ArrayList<Commodity> commodities = new ArrayList<Commodity>();

  public static String convertOneName2(String groupName) {
    String cpcCode = "";

    for (Commodity food : commodities) {
      if (food.name.equals(groupName)) {
        cpcCode = food.cpcCode;
      }
    }

    return cpcCode;
  }


  public static ArrayList<String> getAllCommodities() {
    ArrayList<String> foodNames = new ArrayList<String>();

    Connection connection = null;

    try {
      connection = DriverManager.getConnection(JDBCConnection2.DATABASE);

      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);

      String query = """
      SELECT *
      FROM Commodity
      WHERE cpcCode IN (SELECT cpcCode FROM CountryLossEvent) AND LENGTH(cpcCode) >= 5
      ORDER BY commodityName ASC;
      """;

      ResultSet results = statement.executeQuery(query);

      while (results.next()) {
        String commodityName = results.getString("commodityName");
        foodNames.add(commodityName);

        Commodity commodityGroup = new Commodity();
        commodityGroup.cpcCode = results.getString("cpcCode");
        commodityGroup.name = results.getString("commodityName");
        commodities.add(commodityGroup);
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

    return foodNames;
  }

  public static String getACommoditysGroup(String commodityName) {
    String groupName = "";

    Connection connection = null;

    String cpcCode = convertOneName2(commodityName);

    try {
      connection = DriverManager.getConnection(JDBCConnection2.DATABASE);

      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);

      String query = String.format("""
      SELECT *
      FROM Commodity
      WHERE cpcCode = SUBSTR('%s', 1, 3);
      """, cpcCode);

      ResultSet results = statement.executeQuery(query);

      while (results.next()) {
        groupName = results.getString("commodityName");
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

    return groupName;
  }

  public static ArrayList<SimilarFoodResult> getClosestGroups(String commodityName, String similarity, int numResults, String sort) {
    ArrayList<SimilarFoodResult> foodResults = new ArrayList<SimilarFoodResult>();

    Connection connection = null;

    String cpcCode = convertOneName2(commodityName);

    String option = "";
    if (similarity.equals("highest")) {
      option = "DESC";
    } else {
      option = "ASC";
    }

    try {
      connection = DriverManager.getConnection(JDBCConnection2.DATABASE);

      Statement statement = connection.createStatement();
      statement.setQueryTimeout(30);

      String query = String.format("""
        WITH selectedGroup AS (
        SELECT cpcCode
        FROM Commodity
        WHERE cpcCode = SUBSTR('%s', 1, 3)
        ),
        firstRow AS (
            SELECT AVG(percentage) AS firstAvgPercentage
            FROM CountryLossEvent
            WHERE cpcCode LIKE ((SELECT cpcCode FROM selectedGroup) || '%%')
            GROUP BY cpcCode
            ORDER BY AVG(percentage) %s
            LIMIT 1
        ),
        avgPercentage AS (
            SELECT cpcCode, AVG(percentage) AS comparableValue
            FROM CountryLossEvent
            WHERE cpcCode LIKE ((SELECT cpcCode FROM selectedGroup) || '%%')
            GROUP BY cpcCode
            ORDER BY AVG(percentage) %s
            LIMIT 1
        ),
        closestAverages AS (
            SELECT cpcCode, commodityName, AVG(percentage) AS avgPercentage
            FROM CountryLossEvent
            NATURAL JOIN Commodity
            GROUP BY cpcCode
            ORDER BY ABS(AVG(percentage) - (SELECT comparableValue FROM avgPercentage))
            LIMIT %d
        ),
        getGroups AS (
            SELECT commodityName
            FROM Commodity
            WHERE cpcCode = SUBSTR((SELECT cpcCode FROM closestAverages), 1, 3)
        )
        SELECT
            ca.cpcCode AS closestCommodity,
            comm.commodityName AS groupName,
            ca.avgPercentage AS closestAvgValue,
            100 - (ABS(ca.avgPercentage - fr.firstAvgPercentage) / fr.firstAvgPercentage) * 100 AS similarityScore
        FROM closestAverages ca
        JOIN firstRow fr ON 1=1
        LEFT JOIN Commodity comm ON SUBSTR(ca.cpcCode, 1, 3) = SUBSTR(comm.cpcCode, 1, 3)
        GROUP BY closestCommodity
        ORDER BY similarityScore %s
        ;
        """, cpcCode, option, option, numResults + 1, sort
      );

      ResultSet results = statement.executeQuery(query);

      while (results.next()) {
        String groupName = results.getString("groupName");
        double lossValue = results.getDouble("closestAvgValue");
        double similarityScore = results.getDouble("similarityScore");

        SimilarFoodResult foodResult = new SimilarFoodResult(groupName, lossValue, similarityScore);
        foodResults.add(foodResult);

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

    return foodResults;
  }

}
