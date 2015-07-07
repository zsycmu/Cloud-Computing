import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class q1 extends HttpServlet {
	
	private static final long serialVersionUID = 1L;	

	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
	    PrintWriter printer = response.getWriter();
	    printer.println("Supernova,9556-3772-4289" + "\n" + dateFormat.format(date));
	}
}