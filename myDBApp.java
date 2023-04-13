import java.io.*;
import java.sql.*;
import java.util.*;

public class myDBApp {
    
    // NOTE: You will need to change some variables from START to END.
    public static void main(String[] argv) throws SQLException {
        // START
      Scanner input = new Scanner(System.in);
      System.out.println("PLease Login...\n");
        // Enter your username.
      System.out.println("Enter Username\n");
        String user = input.nextLine();
        // Enter your database password, NOT your university password.
        System.out.println("Enter Your Password\n");
        String password = input.nextLine();
        
        /** IMPORTANT: If you are using NoMachine, you can leave this as it is.
         * 
         *  Otherwise, if you are using your OWN COMPUTER with TUNNELLING:
         *      1) Delete the original database string and 
         *      2) Remove the '//' in front of the second database string.
         */
        String database = "teachdb.cs.rhul.ac.uk";
        //String database = "localhost";
        // END
        
        Connection connection = connectToDatabase(user, password, database);
        if (connection != null) {
            System.out.println("SUCCESS: You made it!"
                    + "\n\t You can now take control of your database!\n");
        } else {
            System.out.println("ERROR: \tFailed to make connection!");
            System.exit(1);
        }
        // Now we're ready to use the DB. You may add your code below this line.
        
        System.out.println("dropping table delayedFlights...");
        dropTable(connection, "delayedFlights");
        System.out.println("dropping table airport...");
        dropTable(connection, "airport");

        System.out.println("creating table delayedFlight...");
        createTable(
                connection,
                "delayedFlights(ID_of_Delayed_Flight int primary key, Month int, DayofMonth int, DayOfWeek int, DepTime int, ScheduledDepTime int, ArrTime int, ScheduledArrTime int, UniqueCarrier varchar(15), FlightNum int, ActualFlightTime int, scheduledFlightTime int, AirTime int, ArrDelay int, DepDelay int, Orig varchar(15), Dest varchar(15), Distance int);");
        int rows1 = insertIntoTableFromFile(connection, "delayedFlights",
                "src/delayedFlights");
        System.out.println(rows1 + " rows inserted into delayedFlights...");
        System.out.println("creating table airport...");
        createTable(
            connection,
            "airport(airportCode  varchar(500) primary key, airportName varchar(500), City varchar(500), State varchar(500));");
    int rows2 = insertIntoTableFromFile(connection, "airport",
            "src/airport");
    System.out.println(rows2 + " rows inserted into airport...");
    
    
    ResultSet rsOut;
    
    //////Query 1///////
    System.out.println("\n################## 1st Query ###############");
    String query1 = " SELECT DISTINCT UniqueCarrier, count(UniqueCarrier)"
        + "FROM delayedFlights"
        + " WHERE ActualFlightTime != scheduledFlightTime OR DepTime != ScheduledDepTime OR ArrTime != DepTime"
        + " GROUP BY UniqueCarrier"
        + " ORDER BY count(UniqueCarrier) DESC"
        + " LIMIT 5";
    rsOut = executeSelect(connection, query1);
    printReasult(rsOut);
    
    //////Query 2//////
    System.out.println("\n################## 2nd Query ###############");
    String query2 = " SELECT DISTINCT City, count(DepDelay)"
        + "FROM delayedFlights, airport "
        + " WHERE DepTime != ScheduledDepTime AND Orig = airportCode "
        + " GROUP BY City"
        + " ORDER BY count(DepDelay) DESC "
        + " LIMIT 5 ";
    rsOut = executeSelect(connection, query2);
    printReasult(rsOut);
    
//////Query 3//////
    System.out.println("\n################## 3rd Query ###############");
    String query3 = "SELECT Dest, SUM(arrDelay) "
        + " FROM delayedFlights "
        + " WHERE arrDelay < (SELECT MAX(arrDelay) FROM delayedFlights) "
        + " GROUP BY Dest "
        + " ORDER BY SUM(arrDelay) DESC LIMIT 5 OFFSET 1 ";
    rsOut = executeSelect(connection, query3);
    printReasult(rsOut);
    
//////Query 4//////
  System.out.println("\n################## 4th Query ###############");
  String query4 = "SELECT State, count(DISTINCT airportCode) "
      + " FROM airport "
      + " GROUP BY State "
      + " HAVING count(DISTINCT airportCode) >= 10";
  rsOut = executeSelect(connection, query4);
  printReasult(rsOut);
  

    }
    // You can write your new methods here.
    
    public static void printReasult(ResultSet rs) throws SQLException {
      try {
        while (rs.next()) {
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i ++) {
              if (i > 1) System.out.print(", ");
              System.out.print(rs.getString(i));
            }
            System.out.println("");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    rs.close();
    }
    
    // code from lab worksheet 7
    public static ResultSet executeSelect(Connection connection, String query) {
      Statement st = null;
      try {
          st = connection.createStatement();
      } catch (SQLException e) {
          e.printStackTrace();
          return null;
      }

      ResultSet rs = null;
      try {
          rs = st.executeQuery(query);
      } catch (SQLException e) {
          e.printStackTrace();
          return null;
      }

      return rs;
  }
  
 // code from lab worksheet 7
  public static void dropTable(Connection connection, String table) {
      Statement st = null;
      try {
          st = connection.createStatement();
          st.execute("DROP TABLE IF EXISTS " + table);
          st.close();
      } catch (SQLException e) {
          e.printStackTrace();
      }
  }

//code from lab worksheet 7
  public static void createTable(Connection connection,
          String tableDescription) {
      Statement operation = null;
      try {
          operation = connection.createStatement();
          operation.execute("CREATE TABLE " + tableDescription);
          operation.close();
      } catch (SQLException e) {
          e.printStackTrace();
      }
  }

//code from lab worksheet 7
  public static int insertIntoTableFromFile(Connection connection,
          String table, String file) {

      BufferedReader br = null;
      int numRows = 0;
      try {
          Statement operation = connection.createStatement();
          String sCurrentLine, brokenLine[], composedLine = "";
          br = new BufferedReader(new FileReader(file));

          while ((sCurrentLine = br.readLine()) != null) {
              // Insert each line to the DB
              brokenLine = sCurrentLine.split(",");
              composedLine = "INSERT INTO " + table + " VALUES (";
              int i;
              for (i = 0; i < brokenLine.length - 1; i++) {
                  composedLine += "'" + brokenLine[i] + "',";
              }
              composedLine += "'" + brokenLine[i] + "')";
              operation.addBatch(composedLine);
              numRows++;
          }
            operation.executeBatch();
            
          
      } catch (IOException e) {
          e.printStackTrace();
      } catch (SQLException e) {
          e.printStackTrace();
      } finally {
          try {
              if (br != null)
                  br.close();
          } catch (IOException ex) {
              ex.printStackTrace();
          }
      }
      return numRows;
  }
  
  


    
    // ADVANCED: This method is for advanced users only. You should not need to change this!
    public static Connection connectToDatabase(String user, String password, String database) {
        System.out.println("------ Testing PostgreSQL JDBC Connection ------");
        Connection connection = null;
        try {
            String protocol = "jdbc:postgresql://";
            String dbName = "/CS2855/";
            String fullURL = protocol + database + dbName + user;
            connection = DriverManager.getConnection(fullURL, user, password);
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("authentication failed")) {
                System.out.println("ERROR: \tDatabase password is incorrect. Have you changed the password string above?");
                System.out.println("\n\tMake sure you are NOT using your university password.\n"
                        + "\tYou need to use the password that was emailed to you!");
            } else {
                System.out.println("Connection failed! Check output console.");
                e.printStackTrace();
            }
        }
        return connection;
    }
}