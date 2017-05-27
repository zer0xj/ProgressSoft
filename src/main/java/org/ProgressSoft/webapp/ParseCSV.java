/**
 * @author Julien Neidballa
 */

package org.ProgressSoft.webapp;

import java.io.FileReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class ParseCSV implements Runnable {
	
	protected static boolean calcTiming = false;
	
	public static CSVServlet webinterface;
	
	protected static Handler outputHandler;

	/**
	 * Loading Status
	 * 
	 * -1 = not currently loading anything
	 * 0 = file has been loaded previously
	 * 1 = loading finished successfully
	 * 2 = previous load failed
	 * 3 = currently loading file
	 */
	protected static int loadStatus = -1;
	
	private static final Logger LOGGER = Logger.getLogger(ParseCSV.class.getName());
	
	protected static MySQLInterface mysql;

	protected static String inputFile = "";
		
	private static Thread t;
	
	protected static boolean verbose = false;
	
	/**
	 * ParseCSV is a class to handle the parsing and importing of CSV files
	 * into the database.
	 * <p>
	 * This is the default constructor, it sets verbosity to its lowest level.
	 */
	
	public ParseCSV() {
		calcTiming = false;
		verbose = false;
		mysql = new MySQLInterface(verbose);
		webinterface = new CSVServlet();
		try {
			outputHandler = new ConsoleHandler();
			outputHandler.setLevel(Level.OFF);
			LOGGER.addHandler(outputHandler);
			LOGGER.setLevel(Level.ALL);
		}
		catch (SecurityException s) {
			LOGGER.log(Level.WARNING, "ParseCSV(): " + s.getMessage());
		}
		t = new Thread(this);
		t.start();
	}
		
	/**
	 * ParseCSV is a class to handle the parsing and importing of CSV files
	 * into the database.
	 * 
	 * @param c enables displaying time information for each file load
	 * @param v enables verbose output
	 */
	public ParseCSV(boolean c, boolean v)
	{
		super();
		calcTiming = c;
		verbose = v;
		if (v) {
			outputHandler.setLevel(Level.ALL);
			mysql.setVerbose(true);
		}
	}
	
	/**
	 * Initiates a file load, for use by the web front-end
	 * 
	 * @return message to display on the web front-end
	 */
	public String getLoad() {
		if (inputFile.equals("")) return "You must select an input file to load first.";
		if (loadStatus < 3) loadStatus = -1;
		return "Initiating load of: \"" + inputFile + "\"";
	}
	
	/**
	 * Checks the CSV loader's current status.
	 * 
	 * @return message to display on the web front-end
	 */
	public String getStatus() {
		
		if (loadStatus <= -1) {
			if (!inputFile.equals("")) return getLoad();
			else return "Nothing has been loaded yet.";
		}
		else if (loadStatus >= 3) return "Currently loading \"" + inputFile + "\"...";
		else {
			String output = "";
			if (loadStatus == 0) output = "File \"" + inputFile + "\" has already been loaded before.";
			else if (loadStatus == 1) output = "Successfully loaded \"" + inputFile + "\".";
			else output = "There was an error loading \"" + inputFile + "\".";
			return output;
		}
	}

	/**
	 * Loads the file denoted by the inputFile variable, unless another load
	 * is already in progress.
	 */
	public synchronized void LoadFile() {
		if ((loadStatus < 3) && !inputFile.equals("")) {
			try {
				long startTime = System.nanoTime();
				CSVReader reader = new CSVReader(new FileReader(inputFile), ',', '"', 0);
				boolean checkDb = mysql.CheckDB();
				if (checkDb && mysql.FileAppend(inputFile)) {
					int count = 0;
					long currentTime = 0;
					if (calcTiming) currentTime = System.nanoTime();
					int fileId = mysql.FileCheck(inputFile);
					loadStatus = 3;
					String[] row = reader.readNext();
					while (row != null) {
						if ((row.length < 5) || ((!mysql.DealInsert("VALID_DEALS", row, fileId)) &&
							(!mysql.DealInsert("INVALID_DEALS", row, fileId)))) {
								LOGGER.log(Level.WARNING, "LoadFile(): Invalid row data provided (" + Arrays.toString(row) + ")");
						}
						if (calcTiming) {
							if (count >= 10000) {
								long timeDiff = currentTime;
								currentTime = System.nanoTime();
								timeDiff = (currentTime - timeDiff) / 1000000;
								LOGGER.log(Level.INFO, "Processed 10000 rows of data in " + Long.toString(timeDiff) + "ms.");
								count = 0;
								checkDb = mysql.CheckDB();
							}
							count += 1;
						}
						row = reader.readNext();
			 		}
					mysql.UpdateDealAggregate("VALID_DEALS");
					currentTime = (long) ((System.nanoTime() - startTime) / 1000000000.0);
					if (calcTiming) LOGGER.log(Level.INFO, "Successfully processed all of the data in " + Long.toString(currentTime) + "s.");
					reader.close();
					loadStatus = 1;
				} else if (loadStatus < 0) loadStatus = 0;
			}
			catch (SQLException s) {
		 		LOGGER.log(Level.SEVERE, "LoadFile(): " + s.getMessage());
		 		s.printStackTrace();
		 	}
		 	catch (Exception e) {
		 		LOGGER.log(Level.WARNING, "LoadFile(): " + e.getMessage());
		 		e.printStackTrace();
		 		loadStatus = 2;
		 	}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (true) {
			try {
				if ((loadStatus < 0) && !inputFile.equals("")) LoadFile();
				Thread.sleep(1000);
			}
			catch (InterruptedException i) {
		 		LOGGER.log(Level.WARNING, "run(): " + i.getMessage());
			}
		}
	}
	
	/**
	 * Sets the inputFile variable, and if a previous file name had been set it
	 * resets the current file loading status.
	 * 
	 * @param infile the local path corresponding to the file you wish to load
	 */
	public void setInputFile(String infile) {
		if (!inputFile.equals(infile)) loadStatus = -1;
		inputFile = infile;
	}
}

