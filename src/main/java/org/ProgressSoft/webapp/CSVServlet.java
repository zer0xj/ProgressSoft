/**
 * @author Julien Neidballa
 */

package org.ProgressSoft.webapp;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;

@WebServlet("/CSVServlet")
public class CSVServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ParseCSV p = new ParseCSV(false, false);
		
		p.setInputFile(request.getParameter("filename"));

		response.setStatus(HttpServletResponse.SC_OK);

		response.setHeader("Location", "/index.jsp");
		
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}
}

