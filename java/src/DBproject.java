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
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
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
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddPlane(DBproject esql) throws SQLException, IOException{//1
		String[] prompt = new String[]{"Enter plane id: ", "make: ", "model: ", "age: ", "seats: "};
		String[] input = new String[prompt.length];
		for (int i = 0; i < prompt.length; i++){
			System.out.print(prompt[i]);
			input[i] = in.readLine();
		}

		String query = "INSERT INTO plane(id, make, model, age, seats) VALUES (" + input[0] + ", "
																					+ "'" + input[1] + "', "
																					+ "'" + input[2] + "', "
																					+ "'" + input[3] + "', "
																					+ input[4] + ");";
		esql.executeUpdate(query);
		System.out.println("Success...!");
	}

	public static void AddPilot(DBproject esql) throws SQLException, IOException{//2
		String[] prompt = new String[]{"Enter pilot id: ", "fullname: ", "nationality: "};
		String[] input = new String[prompt.length];
		for (int i = 0; i < prompt.length; i++){
			System.out.print(prompt[i]);
			input[i] = in.readLine();
		}

		String query = "INSERT INTO pilot(id, fullname, nationality) VALUES (" + input[0] + ", "
																					+ "'" + input[1] + "', "
																					+ "'" + input[2] + "');";
		esql.executeUpdate(query);
		System.out.println("Success...!");
	}

	public static void BookFlight(DBproject esql) throws SQLException, IOException{//5
		List<List<String>> result  = new ArrayList<List<String>>(); //container for returned info
		List<List<String>> customerCheck = new ArrayList<List<String>>();
		List<List<String>> reservationNum = esql.executeQueryAndReturnResult("select count(*) from Reservation;");
		int custCheck = 0;
		int flyCheck = 0;
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		
		//Get cust id from user	
		
		System.out.print("Enter a customer ID: ");
		String custID = in.readLine();
		customerCheck = esql.executeQueryAndReturnResult("select count(*) from Customer C where C.id = " + custID + ";");
		custCheck = Integer.parseInt(customerCheck.get(0).get(0));
		
		//check to see if valid
		while(custCheck == 0){
			System.out.print("Sorry, that customer cannot be found. Please enter a valid customer ID: ");
			custID = in.readLine();
			customerCheck = esql.executeQueryAndReturnResult("select count(*) from Customer C where C.id = " + custID + ";");
			custCheck = Integer.parseInt(customerCheck.get(0).get(0));
		}

		//get flight number 
		System.out.print("Enter a flight number: ");
		String fnum = in.readLine();
		List<List<String>> flightCheck = esql.executeQueryAndReturnResult("select count(*) from Flight F where F.fnum = " + fnum + ";");
		flyCheck = Integer.parseInt(flightCheck.get(0).get(0));
		
		//make sure flight number exists
		while(flyCheck == 0){
			System.out.print("Sorry, that flight cannot be found. Please enter a valid flight number: ");
			fnum = in.readLine();
			flightCheck = esql.executeQueryAndReturnResult("select count(*) from Flight F where F.fnum = " + fnum + ";");
			
			flyCheck = Integer.parseInt(flightCheck.get(0).get(0));
			//System.out.println(flyCheck);
		}
		String waitlist = "Unfortunately this flight is sold out, would you like to be added to the wait list? (y/n): ";
		
		//check to see if valid entries
		String seatQuery = "SELECT P.seats - F.num_sold FROM Flight F, FlightInfo FI, Plane P WHERE (F.fnum = FI.flight_id) AND (FI.plane_id = P.id) AND fnum = " + fnum + ";";
		
		//return # 0f seats on given flight
		result = esql.executeQueryAndReturnResult(seatQuery);
		
		int seatsLeft = Integer.parseInt(result.get(0).get(0));
		//System.out.println("Remaining seats: " + seatsLeft);
		
		//if numseats = 0, add to waitlist
		if (seatsLeft == 0){
			System.out.print(waitlist);
		}
		while(seatsLeft == 0){
			String response = in.readLine();
			String lower = response.toLowerCase();
			if ( lower.equals("y")){
				System.out.println("Adding you to the Waitlist!" );
				//int reserveNum = Integer.parseInt(reservationNum.get(0).get(0));
				System.out.println(reservationNum.get(0).get(0));
				String addWaitlist = "INSERT INTO Reservation(rnum, cid, fid, status) VALUES (" + reservationNum.get(0).get(0) + ", " + custID + ", " + fnum + ","+"'W');";
				esql.executeUpdate(addWaitlist);
				break;
			}
			else if (lower.equals("n")){
				System.out.println("Thank you for your interest in our airline!" );
				break;
			}
			else{
				System.out.print("Sorry, response not recognized. Please enter either y or n: " );
			}
		}
		//else if numseats > 0, add to reservation
		if (seatsLeft > 0){
			System.out.println("Thank you for choosing our airline, we are booking your flight as we speak!");
			String addReservation = "INSERT INTO Reservation(rnum, cid, fid, status) VALUES (" + reservationNum.get(0).get(0) + ", " + custID + ", " + fnum + ","+"'R');";
			esql.executeUpdate(addReservation);

			List<List<String>> numSold = esql.executeQueryAndReturnResult("select num_sold from Flight F where F.fnum = " + fnum + ";");
			int numSoldInt = Integer.parseInt(numSold.get(0).get(0));
			numSoldInt++;
			System.out.println(numSoldInt);
			
			esql.executeUpdate("UPDATE flight SET num_sold = " + numSoldInt + " WHERE fnum = " + fnum + ";");
			
			System.out.println("Success! Your travel arrangements are confirmed");
		}
	}

	public static void ListNumberOfAvailableSeats(DBproject esql) throws SQLException, IOException{//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		System.out.println("Enter a flight number: ");
		String fnum = in.readLine();
		//System.out.print("Enter a departure date");
		//String date = in.readLine();

		String query = "SELECT P.seats - F.num_sold FROM Flight F, FlightInfo FI, Plane P WHERE (F.fnum = FI.flight_id) AND (FI.plane_id = P.id) AND fnum = " + fnum + ";";
		System.out.print("Number of remaining seats: ");
		esql.executeQueryAndPrintResult(query);
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) throws SQLException {//7
		// Count number of repairs per planes and list them in descending order
		String query = "SELECT count(*) as \"# Repairs\", plane_id as \"Plane ID#\" FROM repairs group by \"Plane ID#\" ORDER BY \"# Repairs\" DESC";
		esql.executeQueryAndPrintResult(query);
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) throws SQLException{//8
		// Count repairs per year and list them in ascending order
		String query = "SELECT count(*) as \"# Repairs\", extract(year from repair_date) as \"Year\" FROM repairs GROUP BY \"Year\" ORDER BY \"# Repairs\" ASC;";
		esql.executeQueryAndPrintResult(query);
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) throws SQLException, IOException{//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
		System.out.print("Enter a flight number: ");
		String fnum = in.readLine();
		System.out.print("Enter a status(R, W, C): ");
		String status = in.readLine();
		//error check later
		String query = "SELECT count(*) as \"# Customers with Status\" FROM reservation R WHERE R.fid = " + fnum + " AND R.status = \'" + status.toUpperCase() + "\';";
		esql.executeQueryAndPrintResult(query);
	}
}
