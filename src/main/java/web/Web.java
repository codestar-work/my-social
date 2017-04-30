package web;

import java.io.FileOutputStream;
import java.sql.*;
import javax.servlet.http.*;
import org.springframework.ui.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

@Controller
class Web {
	
	boolean item[ ] = new boolean[100];
	
	@RequestMapping("/reserve") @ResponseBody
	synchronized String reserveItem(int id) {
		if (item[id] == false) {
			item[id] = true;
			return "Success";
		} else {
			return "Fail";
		}
	}
	
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
	String showProfile(HttpSession session, Model model) {
		Member m = (Member)session.getAttribute("member");
		if (m == null) {
			return "redirect:/login";
		}
		model.addAttribute("user", m.name);
		model.addAttribute("member", m);
		return "profile";
	}
	@RequestMapping("/logout")
	String showLogout(HttpSession session) {
		session.removeAttribute("member");
		return "logout";
	}
	@RequestMapping("/new")
	String showNew(HttpSession session) {
		Member m = (Member)session.getAttribute("member");
		if (m == null) {
			return "redirect:/login";
		} else {
			return "new";
		}
	}
	@RequestMapping(value="/new", method=RequestMethod.POST)
	String postNew(String topic, String detail, HttpSession session,
			MultipartFile photo) {
		Member m = (Member)session.getAttribute("member");
		if (m == null) {
			return "redirect:/login";
		}
		try {
			String fileName = null;
			if (photo != null) {
				fileName = "./src/main/resource/public/" +
						"photo-" + (int)(Math.random() * 1000000) + ".jpg";
				FileOutputStream fos = new FileOutputStream(fileName);
				byte [ ] data = photo.getBytes();
				for (byte b : data) {
					fos.write(b);
				}
				fos.close();
			}
			String sql = "insert into post(topic, detail, member, time, photo) " 
					+ "values(?,?,?,now(), ?)";
			Connection c = DriverManager.getConnection(dbUrl,dbUser,dbPassword);
			PreparedStatement p = c.prepareStatement(sql);
			p.setString(1, topic);
			p.setString(2, detail);
			p.setLong(3, m.id);
			p.setString(4, fileName);
			p.execute();
			p.close(); c.close();
		} catch (Exception e) { }
		return "redirect:/profile";
	}
	@RequestMapping("/all")
	String showAll(Model model) {
		java.util.ArrayList a = new java.util.ArrayList();
		try {
			Connection c = DriverManager.getConnection(dbUrl,dbUser,dbPassword);
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("select * from post");
			while (r.next()) {
				Post p   = new Post();
				p.id     = r.getLong("id");
				p.topic  = r.getString("topic");
				p.detail = r.getString("detail");
				p.member = r.getLong("member");
				p.time   = r.getString("time");
				a.add(p);
			}
			r.close(); s.close(); c.close();
		} catch (Exception e) { }
		model.addAttribute("post", a);
		return "all";
	}
	@RequestMapping("/detail")
	String showDetail(Model model, long id) {
		Post post = new Post();
		post.id = 0;
		post.topic = "";
		post.detail = "";
		post.member = 0;
		post.time = "";
		try {
			Connection c = DriverManager.getConnection(dbUrl,dbUser,dbPassword);
			PreparedStatement p = c.prepareStatement(
					"select * from post where id=?");
			p.setLong(1, id);
			ResultSet r = p.executeQuery();
			if (r.next()) {
				post.id     = r.getLong("id");
				post.topic  = r.getString("topic");
				post.detail = r.getString("detail");
				post.member = r.getLong("member");
				post.time   = r.getString("time");
			}
			r.close(); p.close(); c.close();
		} catch (Exception e) { }
		model.addAttribute("post", post);
		return "detail";
	}
	
	@RequestMapping("/view/{id}")
	String viewTopic(Model model, @PathVariable long id) {
		return showDetail(model, id);
	}
	
	String dbUrl = "jdbc:mysql://104.199.154.50/web?characterEncoding=UTF-8";
	String dbUser = "social";
	String dbPassword = "soci@l";
}
