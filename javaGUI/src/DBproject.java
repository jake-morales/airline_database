/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	static DBproject esql;
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if(esql != null) {
				System.out.print("Disconnecting from database...");
				try{
					if (this._connection != null){
						this._connection.close ();
					}//end if
				}catch (SQLException e){
					 // ignored.
				}//end try
				System.out.println("Done\n\nBye !");
			}//end if				
		}catch(Exception e){
			// ignored.
		}
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;

		GUI dbUI = new GUI();
		dbUI.setVisible(true);
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");

		}catch(Exception e){
			System.err.println (e.getMessage ());
		}
	}

	public static String AddPlane(String[] input){//1
		String query = "INSERT INTO plane(id, make, model, age, seats) VALUES (" + input[0] + ", "
																					+ "'" + input[1] + "', "
																					+ "'" + input[2] + "', "
																					+ "'" + input[3] + "', "
																					+ input[4] + ");";
		try{
			esql.executeUpdate(query);
			return new String("Success...!");
		}catch(SQLException se){
			return "Failed.." + se.getMessage();
		}catch(Exception e){
			return "Failed.." + e.getMessage();
		}
	}

	public static String AddPilot(String[] input) {//2
		String query = "INSERT INTO pilot(id, fullname, nationality) VALUES (" + input[0] + ", "
																					+ "'" + input[1] + "', "
																					+ "'" + input[2] + "');";
		try{
			esql.executeUpdate(query);
			return "Success...!";
		}catch(SQLException se){
			return "Failed.." + se.getMessage();
		}catch(Exception e){
			return "Failed.." + e.getMessage();
		}
	}

	public static String AddFlight(String[] input) {//3
		// Given a pilot, plane and flight, adds a flight in the DB -- details of fligth also?
		String query = "INSERT INTO FlightInfo(fiid, flight_id, pilot_id, plane_id) VALUES (" + input[0] + ", "
																					+ input[1] + ", "
																					+ input[2] + ", "
																					+ input[3] + ");";
		try{
			esql.executeUpdate(query);
			return "Success...!";
		}catch(SQLException se){
			return "Failed.." + se.getMessage();
		}catch(Exception e){
			return "Failed.." + e.getMessage();
		}
	}

	public static String AddTechnician(String[] input) {//4
		
		String query = "INSERT INTO technician(id, full_name) VALUES (" + input[0] + ", " + "'" + input[1] + "');";
		try{
			esql.executeUpdate(query);
			return "Success...!";
		}catch(SQLException se){
			return "Failed.." + se.getMessage();
		}catch(Exception e){
			return "Failed.." + e.getMessage();
		}
	}

	public static String BookFlight(String[] input) {//5
		String query = "INSERT INTO reservation(rnum, cid, fid) VALUES ( 10, " + input[0] + ", " + "'" + input[1] + "');";
		try{
			esql.executeUpdate(query);
			return "Success...!";
		}catch(SQLException se){
			return "Failed.." + se.getMessage();
		}catch(Exception e){
			return "Failed.." + e.getMessage();
		}
	}

	public static List<List<String>> ListNumberOfAvailableSeats() {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		String query = "SELECT P.seats - F.num_sold FROM Flight F, FlightInfo FI, Plane P WHERE (F.fnum = FI.flight_id) AND (FI.plane_id = P.id) AND fnum = 10;";
		List<List<String>> result  = new ArrayList<List<String>>();
		try{
			result = esql.executeQueryAndReturnResult(query);
			return result;
		}catch(SQLException se){
			System.out.println("Failed.");
			return result;
		}catch(Exception e){
			System.out.println("Failed.");
			return result;
		}
	}

	public static List<List<String>> ListsTotalNumberOfRepairsPerPlane() {//7
		List<List<String>> result  = new ArrayList<List<String>>();
		// Count number of repairs per planes and list them in descending order
		String query = "SELECT count(*) as \"# Repairs\", plane_id as \"Plane ID#\" FROM repairs group by \"Plane ID#\" ORDER BY \"# Repairs\" DESC";
		try{
			result = esql.executeQueryAndReturnResult(query);
			return result;
		}catch(SQLException se){
			System.out.println("Failed.");
			return result;
		}catch(Exception e){
			System.out.println("Failed.");
			return result;
		}
	}

	public static List<List<String>> ListTotalNumberOfRepairsPerYear() {//8
		List<List<String>> result  = new ArrayList<List<String>>();
		// Count repairs per year and list them in ascending order
		String query = "SELECT count(*) as \"# Repairs\", extract(year from repair_date) as \"Year\" FROM repairs GROUP BY \"Year\" ORDER BY \"# Repairs\" ASC;";
		try{
			result = esql.executeQueryAndReturnResult(query);
			return result;
		}catch(SQLException se){
			System.out.println("Failed.");
			return result;
		}catch(Exception e){
			System.out.println("Failed.");
			return result;
		}
	}
	
	public static List<List<String>> FindPassengersCountWithStatus(String input){//9
		List<List<String>> result  = new ArrayList<List<String>>();

		String query = "SELECT count(*) as \"# Customers with Status\" FROM reservation R WHERE R.fid = 10 AND R.status = \'" + input.toUpperCase() + "\';";
		
		try{
			result = esql.executeQueryAndReturnResult(query);
			return result;
		}catch(SQLException se){
			System.out.println("Failed.");
			return result;
		}catch(Exception e){
			System.out.println("Failed.");
			return result;
		}
	}
}
