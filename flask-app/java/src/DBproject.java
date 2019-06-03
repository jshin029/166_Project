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
				System.out.println("10. Add Repair");
				System.out.println("11. < EXIT");

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
					case 10: AddRepair(esql); break;
					case 11: keepon = false; break;
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

	public static void AddPlane(DBproject esql) {//1
		try{
					 String query = "INSERT INTO Plane (id, make, model, age, seats) Values (nextval('plane_id_seq'), '";

					 System.out.print("\tEnter make: ");
					 String input = in.readLine();
					 query += input;
					 query += "', '";

					 System.out.print("\tEnter model: ");
					 String input1 = in.readLine();
					 query += input1;
					 query += "', '";

					 System.out.print("\tEnter age: ");
					 String input2 = in.readLine();
					 query += input2;
					 query += "', '";

					 System.out.print("\tEnter seats: ");
					 String input3 = in.readLine();
					 query += input3;
					 query += "');";

					 int rowCount = esql.executeQuery(query);
					 System.out.println ("total row(s): " + rowCount);
				}catch(Exception e){
					 System.err.println (e.getMessage());
				}
	}

	public static void AddPilot(DBproject esql) {//2
		try{
					 String query = "INSERT INTO Pilot (id, fullname, nationality) Values (nextval('pilot_id_seq'), '";

					 System.out.print("\tEnter full name: ");
					 String input = in.readLine();
					 query += input;
					 query += "', '";

					 System.out.print("\tEnter nationality: ");
					 String input1 = in.readLine();
					 query += input1;
					 query += "');";

					 int rowCount = esql.executeQuery(query);
					 System.out.println ("total row(s): " + rowCount);
				}catch(Exception e){
					 System.err.println (e.getMessage());
				}
	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
		try{
					 String query = "INSERT INTO Flight (fnum, cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport) Values (nextval('flight_id_seq'), '";

					 System.out.print("\tEnter cost: ");
					 String input = in.readLine();
					 query += input;
					 query += "', '";

					 System.out.print("\tEnter number sold: ");
					 String input1 = in.readLine();
					 query += input1;
					 query += "', '";

					 System.out.print("\tEnter number stops: ");
					 String input2 = in.readLine();
					 query += input2;
					 query += "', '";

					 System.out.print("\tEnter actual departure date: ");
					 String input3 = in.readLine();
					 query += input3;
					 query += "', '";

					 System.out.print("\tEnter actual arrival date: ");
					 String input4 = in.readLine();
					 query += input4;
					 query += "', '";

					 System.out.print("\tEnter actual arrival airport: ");
					 String input5 = in.readLine();
					 query += input5;
					 query += "', '";

					 System.out.print("\tEnter departure airport: ");
					 String input6 = in.readLine();
					 query += input6;
					 query += "') RETURNING *;";

					 System.out.print("\tEnter pilot id: ");
					 String input7 = in.readLine();

					 String max_pilot = "SELECT max(P.id) FROM Pilot P;";
					 List<List<String>> max_pilot_list = esql.executeQueryAndReturnResult(max_pilot);
					 String string_m_pilot = max_pilot_list.get(0).get(0);
					 int number_max_pilot = Integer.parseInt(string_m_pilot);
					 //converting query from list of list of strings to Integer

					 int user_pilot_id = Integer.parseInt(input7);
					 //convert pilot id from user converted from string to int

					 while (user_pilot_id > number_max_pilot) {
						 System.out.print("\tInvalid pilot id please enter a valid id: ");
						 input7 = in.readLine();
						 user_pilot_id = Integer.parseInt(input7);
					 }

					 System.out.print("\tEnter plane id: ");
					 String input8 = in.readLine();

					 String max_plane = "SELECT max(P.id) FROM Plane P;";
					 List<List<String>> max_plane_list = esql.executeQueryAndReturnResult(max_plane);
					 String string_m_plane = max_plane_list.get(0).get(0);
					 int number_max_plane = Integer.parseInt(string_m_plane);
					 //converting query from list of list of strings to Integer

					 int user_plane_id = Integer.parseInt(input8);
					 //convert plane id from user converted from string to int

					 while (user_plane_id > number_max_plane) {
						 System.out.print("\tInvalid plane id please enter a valid id: ");
						 input8 = in.readLine();
						 user_plane_id = Integer.parseInt(input8);
					 }

					 String query3 = "INSERT INTO FlightInfo (fiid, flight_id, pilot_id, plane_id) Values (nextval('flightinfo_id_seq'), (SELECT max(F.fnum) FROM Flight F), '";
					 query3 += input7;
					 query3 += "', '";
					 query3 += input8;
					 query3 += "');";


					 int rowCount =  esql.executeQueryAndPrintResult(query);
					 System.out.println ("total row(s): " + rowCount);

					 int rowCount1 = esql.executeQuery(query3);
					 System.out.println ("total row(s): " + rowCount1);



				}catch(Exception e){
					 System.err.println (e.getMessage());
				}
	}

	public static void AddTechnician(DBproject esql) {//4
		try{
					 String query = "INSERT INTO Technician (id, full_name) Values (nextval('tech_id_seq'), '";

					 System.out.print("\tEnter full name: ");
					 String input = in.readLine();
					 query += input;
					 query += "');";

					 esql.executeQueryAndPrintResult(query);
					 int rowCount = esql.executeQuery(query);
					 System.out.println ("total row(s): " + rowCount);
				}catch(Exception e){
					 System.err.println (e.getMessage());
				}
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		try{
					 String query = "INSERT INTO Reservation (rnum, cid, fid, status) SELECT nextval('res_id_seq'), ";
					 System.out.print("\tEnter customer id: ");
					 String input = in.readLine();
					 query += input;
					 query += ", ";

					 System.out.print("\tEnter flight id: ");
					 String input2 = in.readLine();
					 query += input2;
					 query += ", ";

					 String query1 = "CASE WHEN (SELECT P.seats FROM Plane P WHERE P.id = (SELECT F.plane_id FROM FlightInfo F WHERE F.flight_id = ";
					 String query10 = ")) > (SELECT num_sold FROM Flight F WHERE F.fnum = ";
					 String query11 = ") THEN 'C' ELSE 'W' END;";
					 query += query1;
					 query += input2;
					 query += query10;
					 query += input2;
					 query += query11;

					 esql.executeQueryAndPrintResult(query);

					 String query2 = "UPDATE Flight SET num_sold = num_sold+1  WHERE fnum = ";
					 query2 += input2;
					 query2 += ";";
					 esql.executeQueryAndPrintResult(query2);

				}catch(Exception e){
					 System.err.println (e.getMessage());
				}
	}

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		try{
					 String query1 = "SELECT (SELECT P.seats FROM Plane P WHERE P.id = (SELECT F.plane_id FROM FlightInfo F WHERE F.flight_id = ";
					 String query2 = "(SELECT num_sold FROM Flight F WHERE F.fnum =";

					 System.out.print("\tEnter flight number: ");
					 String input1 = in.readLine();
					 query1 += input1;
					 query1 += ")) - ";
					 query2 += input1;
					 query2 += ") AS Difference;";
					 query1 += query2;

					 esql.executeQueryAndPrintResult(query1);
					 int rowCount = esql.executeQuery(query1);
					 System.out.println ("total row(s): " + rowCount);

				}catch(Exception e){
					 System.err.println (e.getMessage());
				}
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order

try{
			 String query = "SELECT P.id, P.model, count(R.rid) FROM Plane P, Repairs R WHERE P.id = R.plane_id GROUP BY P.id ORDER BY count DESC;";
			 esql.executeQueryAndPrintResult(query);
		}catch(Exception e){
			 System.err.println (e.getMessage());
		}
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		//Count repairs per year and list them in ascending order
		try{
					 String query = "SELECT EXTRACT (year FROM R.repair_date) as \"Year\", count(R.rid) FROM Repairs R GROUP BY \"Year\" ORDER BY count ASC;";
					 esql.executeQueryAndPrintResult(query);
				}catch(Exception e){
					 System.err.println (e.getMessage());
				}
}


	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
		try{
					 String query = "SELECT COUNT(*) FROM Reservation R WHERE R.fid = ";

					 System.out.print("\tEnter flight number: ");
					 String input = in.readLine();
					 query += input;
					 query += "AND R.status = '";

					 System.out.print("\tEnter passenger status: ");
					 String input1 = in.readLine();
					 query += input1;
					 query += "';";

					 esql.executeQueryAndPrintResult(query);
				}catch(Exception e){
					 System.err.println (e.getMessage());
				}

}

public static void AddRepair(DBproject esql) {//11
	try{
				 String query = "INSERT INTO Repairs (rid, repair_date, repair_code, pilot_id, plane_id, technician_id) Values (nextval('repair_id_seq'), '";

				 System.out.print("\tEnter repair date: ");
				 String input = in.readLine();
				 query += input;
				 query += "', '";

				 System.out.print("\tEnter repair code: ");
				 String input1 = in.readLine();
				 query += input1;
				 query += "', '";

				 System.out.print("\tEnter pilot id: ");
				 String input2 = in.readLine();


				 String max_pilot = "SELECT max(P.id) FROM Pilot P;";
				 List<List<String>> max_pilot_list = esql.executeQueryAndReturnResult(max_pilot);
				 String string_m_pilot = max_pilot_list.get(0).get(0);
				 int number_max_pilot = Integer.parseInt(string_m_pilot);
				 //converting query from list of list of strings to Integer

				 int user_pilot_id = Integer.parseInt(input2);
				 //convert pilot id from user converted from string to int

				 while (user_pilot_id > number_max_pilot) {
					 System.out.print("\tInvalid pilot id please enter a valid id: ");
					 input2 = in.readLine();
					 user_pilot_id = Integer.parseInt(input2);
				 }
				 query += input2;
				 query += "', '";

				 System.out.print("\tEnter plane id: ");
				 String input3 = in.readLine();


				 String max_plane = "SELECT max(P.id) FROM Plane P;";
				 List<List<String>> max_plane_list = esql.executeQueryAndReturnResult(max_plane);
				 String string_m_plane = max_plane_list.get(0).get(0);
				 int number_max_plane = Integer.parseInt(string_m_plane);
				 //converting query from list of list of strings to Integer

				 int user_plane_id = Integer.parseInt(input3);
				 //convert plane id from user converted from string to int

				 while (user_plane_id > number_max_plane) {
					 System.out.print("\tInvalid plane id please enter a valid id: ");
					 input3 = in.readLine();
					 user_plane_id = Integer.parseInt(input3);
				 }

				 query += input3;
				 query += "', '";

				 System.out.print("\tEnter technician id: ");
				 String input4 = in.readLine();
				 query += input4;
				 query += "') RETURNING *;";

				 int rowCount = esql.executeQueryAndPrintResult(query);
				 System.out.println ("total row(s): " + rowCount);
			}catch(Exception e){
				 System.err.println (e.getMessage());
			}
}

}
