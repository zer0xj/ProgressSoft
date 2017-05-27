/**
 * @author Julien Neidballa
 */

package org.ProgressSoft.webapp;

import java.sql.*;
import java.util.Arrays;

public class MySQLInterface {
	
	protected boolean connected = false;
	protected boolean verbose = false;
	
	private Connection dbConnection;
	private Statement statement;
	
	private String dbUser = "ProgressSoft";
	private String dbPassword = "ProgressSoft";

	private String destURL = "jdbc:mysql://localhost/ProgressSoft";
	private String driver = "org.gjt.mm.mysql.Driver";
	
	public MySQLInterface(boolean v) {
		verbose = v;
		Connect();
	}
	
	/**
	 * Checks that the current database connection is still valid.
	 * 
	 * @param timeout number of seconds to wait before timing out
	 * @return true if the database connection is valid, otherwise false
	 */
	public boolean CheckDB(int timeout) {
		try {
			return dbConnection.isValid(timeout);
		}
		catch (SQLException s) {
	 		if (verbose) System.err.println("CheckDB(): " + s.getMessage());
		}
		catch (Exception e) {
	 		if (verbose) System.err.println("CheckDB(): " +e.getMessage());
		}
		return false;
	}
	
	public boolean CheckDB() {
		return CheckDB(5);
	}
	
	/**
	 * Closes the database connection.
	 */
	public void Close() {
		try {
			dbConnection.close();
			statement.close();
		}
		catch (SQLException s) {
	 		if (verbose) System.err.println("Close(): " + s.getMessage());
		}
		catch (Exception e) {
	 		if (verbose) System.err.println("Close(): " + e.getMessage());
		}
	}
	
	/**
	 * Connects to the database.
	 */
	private void Connect() {
		try {
			Class.forName(driver);
			dbConnection = DriverManager.getConnection(destURL, dbUser, dbPassword);
			statement = dbConnection.createStatement();
		}
		catch (SQLException s) {
	 		if (verbose) System.err.println("Connect(): " + s.getMessage());
		}
		catch (Exception e) {
	 		if (verbose) System.err.println("Connect(): " + e.getMessage());
		}
	}

	/**
	 * Inserts a row containing deal data into the specified table with
	 * reference to the file it came from.
	 * 
	 * @param table  the name of the SQL table to insert data into.
	 * @param row    an array containing a row's data
	 * @param fileId the FILE_ID corresponding to the data's source file
	 * @return true on success, false on failure
	 */
	public boolean DealInsert(String table, String[] row, int fileId) {
		String query = "INSERT INTO ProgressSoft." + table + "(ID, DEAL_ID, FROM_CURRENCY, TO_CURRENCY, DEAL_TIMESTAMP, AMOUNT, FILE_ID) values(DEFAULT, '" + row[0] + "', '" + row[1] + "', '" + row[2] + "', '" + row[3] + "', " + row[4] + ", " + Integer.toString(fileId) +  ")";
		try {
			statement.executeUpdate(query);
			return true;
		}
		catch (SQLException s) {
	 		if (verbose) {
	 			System.err.println("DealInsert(" + table + ", " + Arrays.toString(row) + ", " + Integer.toString(fileId) + "): query = \"" + query + "\"\n\t" + s.getMessage());
	 		}
			return false;
		}
		catch (Exception e) {
			if (verbose) {
				System.err.println("DealInsert(" + table + ", " + Arrays.toString(row) + ", " + Integer.toString(fileId) + "): query = \"" + query + "\"\n\t" + e.getMessage());
	 		}
			return false;
		}
	}
		
	/**
	 * Inserts an entry into the LOADED_FILES table to indicate a file has been
	 * loaded.
	 * 
	 * @param filename the name of the file to add to the LOADED_FILES table
	 * @return true on success, false on failure
	 */
	public boolean FileAppend(String filename) {
		if (!filename.equals("")) {
			String query = "INSERT INTO ProgressSoft.LOADED_FILES(FILE_ID, TIMESTAMP, FILE_NAME) values(DEFAULT, DEFAULT, '" + filename + "')";
			try {
				statement.executeUpdate(query);
				return true;
			}
			catch (SQLException s) {
		 		if (verbose) System.err.println("FileAppend(" + filename + "): " + s.getMessage());
				return false;
			}
			catch (Exception e) {
		 		if (verbose) System.err.println("FileAppend(" + filename + "): " + e.getMessage());
				return false;
			}
		}
		return false;
	}

	/**
	 * Checks a given file against those already loaded into the database.
	 * 
	 * @param  filename the local path to the file in question	
	 * @return fileId   the FILE_ID corresponding to an already-loaded file,
	 *                  -1 if the file has not already been loaded  
	 */
	public int FileCheck(String filename) {
		int fileId = -1;
		String query = "SELECT FILE_ID FROM ProgressSoft.LOADED_FILES where FILE_NAME = '" + filename + "' LIMIT 1";
		ResultSet results;
		try {
			results = statement.executeQuery(query);
			if (results.next()) fileId = results.getInt("FILE_ID");
			else fileId = 0;
		}
		catch (SQLException s) {
	 		if (verbose) System.err.println("FileCheck(" + filename + "): " + s.getMessage());
	 		return fileId;
		}
		catch (Exception e) {
	 		if (verbose) System.err.println("FileCheck(" + filename + "): " + e.getMessage());
	 		return fileId;
		}
		return fileId;
	}
	
	public void setVerbose(boolean v) {
		verbose = v;
	}
	
	/**
	 * Updates the DEALS_PER_CURRENCY table with the loaded values in the given
	 * table.
	 * 
	 * @param table the name of the SQL table aggregate data from  
	 * @throws SQLException @{link SQLException}
	 */
	public void UpdateDealAggregate(String table) throws SQLException {
		String inClause = "";
		String insertQuery = "INSERT INTO ProgressSoft.DEALS_PER_CURRENCY (SELECT DISTINCT FROM_CURRENCY AS CURRENCY_ID, COUNT(*) AS COUNT_OF_DEALS, SUM(COALESCE(AMOUNT, 0.0)) AS SUM_OF_DEALS FROM ProgressSoft." + table;
		String listQuery = "SELECT DISTINCT CURRENCY_ID FROM ProgressSoft.DEALS_PER_CURRENCY";
		String updateQuery = "UPDATE ProgressSoft.DEALS_PER_CURRENCY d INNER JOIN (SELECT DISTINCT FROM_CURRENCY, COUNT(*) AS C, SUM(COALESCE(AMOUNT, 0.0)) AS S FROM ProgressSoft." + table + " GROUP BY FROM_CURRENCY) v ON v.FROM_CURRENCY = d.CURRENCY_ID SET d.COUNT_OF_DEALS = v.C, d.SUM_OF_DEALS = v.S";
		ResultSet results = statement.executeQuery(listQuery);
		while (results.next()) {
			if (inClause.length() > 0) inClause += ", ";
			inClause += "'" + results.getString("CURRENCY_ID") + "'";
		}
		if (!inClause.equals("")) {
			insertQuery += " WHERE FROM_CURRENCY NOT IN (" + inClause + ")";
			updateQuery += " WHERE v.FROM_CURRENCY IN (" + inClause + ")";
		}
		insertQuery += " GROUP BY FROM_CURRENCY)";
		statement.executeUpdate(insertQuery);
		statement.executeUpdate(updateQuery);
		System.out.println("Successfully updated the DEALS_PER_CURRENCY table.");
	}
}
