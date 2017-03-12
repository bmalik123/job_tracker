<<<<<<< HEAD
package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

import function.RetrieveData;
import function.StringFilter;
import model.User;

/**
 * Servlet implementation class AcctManagement
 */
@WebServlet("/Settings")
public class Settings extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getSession().getAttribute("user") == null){
			response.sendRedirect("Home");
		}
		else{
			if(request.getParameter("id") != null )
			{
				if(Integer.parseInt(request.getSession().getAttribute("position").toString()) != 0){
					request.setAttribute("errorMessage", "Invalid request!");
					request.getRequestDispatcher("/WEB-INF/Home.jsp").forward(request, response);
				}
				else{
					RetrieveData rd = null;
					if (Boolean.valueOf(request.getServletContext().getAttribute("onServer").toString())){
						rd = new RetrieveData((DataSource)request.getServletContext().getAttribute("dbSource"));
					}
					else{
						String dbURL = request.getServletContext().getAttribute("dbURL").toString();
						String dbUser = request.getServletContext().getAttribute("dbUser").toString();
						String dbPass = request.getServletContext().getAttribute("dbPass").toString();
						rd = new RetrieveData(dbURL, dbUser, dbPass);
					}
					request.setAttribute("adminModify", true);
					request.setAttribute("editUser", rd.getUser(Integer.parseInt(request.getParameter("id"))));
					request.setAttribute("positionList", Arrays.asList("USER", "TECHNICIAN", "SUPERVISING TECHNICIAN", "SYSTEM ADMINISTRATOR"));
					request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
				}
			}
			else{
				request.setAttribute("adminModify", false);
				request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringFilter sf = new StringFilter();
		String adminModify = sf.filterNull(request.getParameter("adminModify"));
		String email = sf.filterNull(request.getParameter("email"));
		String pnumber = sf.filterNull(request.getParameter("phoneNumber"));
		RetrieveData rd = null;
		if (Boolean.valueOf(request.getServletContext().getAttribute("onServer").toString())){
			rd = new RetrieveData((DataSource)request.getServletContext().getAttribute("dbSource"));
		}
		else{
			String dbURL = request.getServletContext().getAttribute("dbURL").toString();
			String dbUser = request.getServletContext().getAttribute("dbUser").toString();
			String dbPass = request.getServletContext().getAttribute("dbPass").toString();
			rd = new RetrieveData(dbURL, dbUser, dbPass);
		}
		
		if( email.isEmpty() || pnumber.isEmpty() || ( pnumber.length() < 14 ) )
		{
			if(adminModify.equals("true"))
			{
				request.setAttribute("adminModify", true);
				request.setAttribute("email", email);
				request.setAttribute("phoneNumber", pnumber);
				request.setAttribute("errorMessage", "Some fields are missing!");

				request.setAttribute("editUser", rd.getUser(Integer.parseInt(request.getParameter("userId"))));
				request.setAttribute("positionList", Arrays.asList("USER", "TECHNICIAN", "SUPERVISING TECHNICIAN", "SYSTEM ADMINISTRATOR"));
				request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
			}
			else{
				request.setAttribute("adminModify", false);
				request.setAttribute("email", email);
				request.setAttribute("phoneNumber", pnumber);
				request.setAttribute("errorMessage", "Some fields are missing!");
				request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
			}
		}
		else{

			Connection c = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			
			if(adminModify.equals("true")){ // Admin modifying a user's account information
				User editUser = rd.getUser(Integer.parseInt(request.getParameter("userId")));
				if(editUser.getEmail().equals(email) 
						&& editUser.getPhoneNumber().equals(pnumber)
						&& editUser.getStatusString().equals(request.getParameter("position")))
				{
					request.setAttribute("userList", rd.getAllUsers());
					request.setAttribute("positionList", Arrays.asList("USER", "TECHNICIAN", "SUPERVISING TECHNICIAN", "SYSTEM ADMINISTRATOR"));
					request.setAttribute("successMessage", "No changes were made because there were no differences!");
					request.getRequestDispatcher("/WEB-INF/AcctManagement.jsp").forward(request, response);
				}
				else{
					try
					{
						String newPosition = request.getParameter("position");
						int position;
						
						if(newPosition.equals("SYSTEM ADMINISTRATOR")){
							position = 0;
						}
						else if(newPosition.equals("SUPERVISING TECHNICIAN")){
							position = 1;
						}
						else if(newPosition.equals("TECHNICIAN")){
							position = 2;
						}
						else{
							position = 3;
						}
						
						if(Boolean.valueOf(request.getServletContext().getAttribute("onServer").toString()))
						{
							c = ((DataSource)request.getServletContext().getAttribute("dbSource")).getConnection();
						}
						else{
							String dbURL = request.getServletContext().getAttribute("dbURL").toString();
							String dbUser = request.getServletContext().getAttribute("dbUser").toString();
							String dbPass = request.getServletContext().getAttribute("dbPass").toString();

							c = DriverManager.getConnection(dbURL, dbUser, dbPass);
						}
						String updateQuery = "update users set email = ?, phone = ?, position = ? where username = ?";
						pstmt = c.prepareStatement( updateQuery );
						pstmt.setString(1, email);
						pstmt.setString(2, pnumber);
						pstmt.setInt(3, position);
						pstmt.setString(4, editUser.getUsername());
						pstmt.executeUpdate();
						
						pstmt.close();
						c.close();
						
					} catch (SQLException e) {
						e.printStackTrace();
					}finally
					{
						DbUtils.closeQuietly(pstmt);
						DbUtils.closeQuietly(rs);
						DbUtils.closeQuietly(c);
					}
		
					request.setAttribute("userList", rd.getAllUsers());
					request.setAttribute("unitList", rd.getAllUnits());

					request.setAttribute("positionList", Arrays.asList("USER", "TECHNICIAN", "SUPERVISING TECHNICIAN", "SYSTEM ADMINISTRATOR"));
					request.setAttribute("successMessage", "Successfully modified " + editUser.getFirstName() + " " + editUser.getLastName() + " account information!");
					request.getRequestDispatcher("/WEB-INF/AcctManagement.jsp").forward(request, response);
				}
			}
			else{	// User changing their own account information
				if(request.getSession().getAttribute("email").equals(email) 
						&& request.getSession().getAttribute("phoneNumber").equals(pnumber)){
					
					request.setAttribute("successMessage", "No changes were made because there were no differences!");
					request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
				}
				else{
					try{
						if(Boolean.valueOf(request.getServletContext().getAttribute("onServer").toString()))
						{
							c = ((DataSource)request.getServletContext().getAttribute("dbSource")).getConnection();
						}
						else{
							String url = "jdbc:mysql://cs3.calstatela.edu/cs4961stu01";
							String db_user = "cs4961stu01";
							String db_pass = ".XCGG1Bc";

							c = DriverManager.getConnection(url, db_user, db_pass);
						}
						String updateQuery = "update users set email = ?, phone = ? where username = ?";
						pstmt = c.prepareStatement(updateQuery);
						pstmt.setString(1, email);
						pstmt.setString(2, pnumber);
						pstmt.setString(3, request.getSession().getAttribute("user").toString());
						pstmt.executeUpdate();
						
						pstmt.close();
						c.close();
						
					} catch (SQLException e) {
						e.printStackTrace();
					}finally
					{
						DbUtils.closeQuietly(pstmt);
						DbUtils.closeQuietly(rs);
						DbUtils.closeQuietly(c);
					}
					
					request.getSession().setAttribute("email", email);
					request.getSession().setAttribute("phoneNumber", pnumber);
					request.setAttribute("successMessage", "Successfully modified account information!");
					request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
					
				}
			}
		}

	}
}
=======
package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import function.RetrieveData;
import function.StringFilter;
import model.User;

@WebServlet("/Settings")
public class Settings extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getSession().getAttribute("user") == null){
			response.sendRedirect("Login");
		}
		else{
			if(Integer.parseInt(request.getSession().getAttribute("position").toString()) != 0){
				request.setAttribute("adminModify", false);
				request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
			}
			else{
				RetrieveData rd = new RetrieveData((DataSource)request.getServletContext().getAttribute("dbSource")); 
				
				if(request.getParameter("id") == null){
					/*
					 * This means that the system administrator is editing his own settings
					 * using the "My Settings" option on the navbar.
					 */
					request.setAttribute("adminModify", false);
				}
				else{
					/*
					 * This means that the system administrator is editing account
					 * information using the "Account Management" option.
					 */
					request.setAttribute("adminModify", true);
					request.setAttribute("editUser", rd.getUser(Integer.parseInt(request.getParameter("id"))));
				}
				request.setAttribute("positionList", Arrays.asList("USER", "TECHNICIAN", "SUPERVISING TECHNICIAN", "SYSTEM ADMINISTRATOR"));
				request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		StringFilter sf = new StringFilter();
		
		String adminModify = sf.filterNull(request.getParameter("adminModify"));
		String email = sf.filterNull(request.getParameter("email"));
		String pnumber = sf.filterNull(request.getParameter("phoneNumber"));
		String department = sf.filterNull(request.getParameter("department"));
		
		RetrieveData rd = new RetrieveData((DataSource)request.getServletContext().getAttribute("dbSource"));
		
		if( email.isEmpty() || pnumber.isEmpty() || ( pnumber.length() < 14 ) )
		{
			if(adminModify.equals("true"))
			{
				request.setAttribute("adminModify", true);
				request.setAttribute("email", email);
				request.setAttribute("phoneNumber", pnumber);
				request.setAttribute("errorMessage", "Some fields are missing!");

				request.setAttribute("editUser", rd.getUser(Integer.parseInt(request.getParameter("userId"))));
				request.setAttribute("positionList", Arrays.asList("USER", "TECHNICIAN", "SUPERVISING TECHNICIAN", "SYSTEM ADMINISTRATOR"));
				request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
			}
			else{
				request.setAttribute("adminModify", false);
				request.setAttribute("email", email);
				request.setAttribute("phoneNumber", pnumber);
				request.setAttribute("errorMessage", "Some fields are missing!");
				request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
			}
		}
		else{
			
			Logger settingLog = LoggerFactory.getLogger(Settings.class);
			
			if(adminModify.equals("true")){ // Admin modifying a user's account information
				User editUser = rd.getUser(Integer.parseInt(request.getParameter("userId")));
				String editUserDepartment = "";
				if(editUser.getDepartment()!= null){
					editUserDepartment = editUser.getDepartment();
				}
				
				if(editUser.getEmail().equals(email) 
						&& editUser.getPhoneNumber().equals(pnumber)
						&& editUser.getStatusString().equals(request.getParameter("position"))
						&& editUserDepartment.equals(department))
				{
					request.setAttribute("userList", rd.getAllUsers());
					request.setAttribute("positionList", Arrays.asList("USER", "TECHNICIAN", "SUPERVISING TECHNICIAN", "SYSTEM ADMINISTRATOR"));
					request.setAttribute("successMessage", "No changes were made because there were no differences!");
					request.getRequestDispatcher("/WEB-INF/AcctManagement.jsp").forward(request, response);
				}
				else{
					String newPosition = request.getParameter("position");
					int position = 3;
					
					switch(newPosition){
						case "SYSTEM ADMINISTRATOR":
							position = 0;
							break;
							
						case "SUPERVISING TECHNICIAN":
							position = 1;
							break;
							
						case "TECHNICIAN":
							position = 2;
							break;
							
						case "USER":
						default:
							position = 3;
							break;
					}
						
					try(Connection c = ((DataSource)request.getServletContext().getAttribute("dbSource")).getConnection()){
						String updateQuery = "update users set email = ?, phone = ?, position = ?, department = ? where username = ?";
						
						try(PreparedStatement pstmt = c.prepareStatement( updateQuery )){
							pstmt.setString(1, email);
							pstmt.setString(2, pnumber);
							pstmt.setInt(3, position);
							pstmt.setString(4, department);
							pstmt.setString(5, editUser.getUsername());
							pstmt.executeUpdate();	
						}
					} catch (SQLException e) {
						settingLog.error("SQL Error @ Settings (admin mode).", e);
					} catch (Exception e){
						settingLog.error("Non-SQL Error @ Settings (admin mode).", e);
					}
					
					settingLog.info("System Admin. " + request.getSession().getAttribute("user").toString() + " has edited user " 
							+ editUser.getUsername() + "'s profile settings.");
		
					request.setAttribute("userList", rd.getAllUsers());
					request.setAttribute("unitList", rd.getAllUnits());

					request.setAttribute("positionList", Arrays.asList("USER", "TECHNICIAN", "SUPERVISING TECHNICIAN", "SYSTEM ADMINISTRATOR"));
					request.setAttribute("successMessage", "Successfully modified " + editUser.getFirstName() + " " + editUser.getLastName() + " account information!");
					request.getRequestDispatcher("/WEB-INF/AcctManagement.jsp").forward(request, response);
				}
			}
			else{	// User changing their own account information
				String userDepartment = "";
				if(request.getSession().getAttribute("department") != null){
					userDepartment = request.getSession().getAttribute("department").toString();
				}
				
				if(request.getSession().getAttribute("email").equals(email) 
						&& request.getSession().getAttribute("phoneNumber").equals(pnumber)
						&& userDepartment.equals(department)){
					
					request.setAttribute("successMessage", "No changes were made because there were no differences!");
					request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
				}
				else{
					try(Connection c = ((DataSource)request.getServletContext().getAttribute("dbSource")).getConnection()){
						String updateQuery = "update users set email = ?, phone = ?, department = ? where username = ?";
						
						try(PreparedStatement pstmt = c.prepareStatement(updateQuery)){
							pstmt.setString(1, email);
							pstmt.setString(2, pnumber);
							pstmt.setString(3, department);
							pstmt.setString(4, request.getSession().getAttribute("user").toString());
							pstmt.executeUpdate();
						}
					} catch (SQLException e) {
						settingLog.error("SQL Error @ Settings (user mode).", e);
					} catch (Exception e){
						settingLog.error("Non-SQL Error @ Settings (user mode).", e);
					}
					
					settingLog.info("User " + request.getSession().getAttribute("user").toString() + " has edited their profile settings.");
					
					request.getSession().setAttribute("email", email);
					request.getSession().setAttribute("phoneNumber", pnumber);
					request.getSession().setAttribute("department", department);
					request.setAttribute("successMessage", "Successfully modified account information!");
					request.getRequestDispatcher("/WEB-INF/Settings.jsp").forward(request, response);
				}
			}
		}

	}
}
>>>>>>> 61b1120018bf6e4d8a225d35f085b754f2c8338c
