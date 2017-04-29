package web;

import java.sql.*;
import javax.servlet.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
class Web {
	Web() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) { }
	}
	@RequestMapping("/")
	String showHome() {
		return "index";
	}
	@RequestMapping("*")
	String showError() {
		return "error";
	}
	@RequestMapping("/register")
	String showRegister() {
		return "register";
	}
	@RequestMapping(value="/register", method=RequestMethod.POST)
	String registerUser(String name, String email, String password) {
		try {
			Connection c = DriverManager.getConnection(dbUrl,dbUser,dbPassword);
			PreparedStatement p = c.prepareStatement(
					"insert into member(name, email, password)" 
					+ " values(?,?, sha2(?, 512))");
			p.setString(1, name);
			p.setString(2, email);
			p.setString(3, password);
			p.execute();
			p.close();
			c.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return "redirect:/login";
	}
	@RequestMapping("/login")
	String showLogin() {
		return "login";
	}
	@RequestMapping(value="/login", method=RequestMethod.POST)
	String checkLogin(HttpSession session, String email, String password) {
		try {
			Connection c = DriverManager.getConnection(dbUrl,dbUser,dbPassword);
			PreparedStatement p = c.prepareStatement(
				"select * from member where email=? and password=sha2(?,512)");
			p.setString(1, email);
			p.setString(2, password);
			ResultSet r = p.executeQuery();
			if (r.next()) {
				Member m = new Member();
				m.id = r.getLong("id");
				m.name = r.getString("name");
				m.email = r.getString("email");
				session.setAttribute("member", m);
				System.out.println(" Log In Passed");
			} else {
				System.out.println("Log In Failed");
			}
			r.close(); p.close(); c.close();
		} catch (Exception e) { }
		return "redirect:/profile";
	}
	@RequestMapping("/profile")
	String showProfile(HttpSession session) {
		Member m = (Member)session.getAttribute("member");
		if (m == null) {
			return "redirect:/login";
		}
		return "profile";
	}
	@RequestMapping("/logout")
	String showLogout(HttpSession session) {
		session.removeAttribute("member");
		return "logout";
	}
	
	String dbUrl = "jdbc:mysql://104.199.154.50/web";
	String dbUser = "social";
	String dbPassword = "soci@l";
}
