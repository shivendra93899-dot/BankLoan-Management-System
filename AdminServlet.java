import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private static final String DB =
        "jdbc:mysql://localhost:3306/bank_loan_db";
    private static final String USER = "root";

    private boolean loggedIn(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null && Boolean.TRUE.equals(s.getAttribute("admin"));
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;");
    }

    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {
        res.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = res.getWriter()) {
            if (!loggedIn(req)) {
                out.println("<html><head><title>Admin Login</title></head><body>");
                out.println("<h2>Bank Loan Admin Login</h2>");
                out.println("<form method='post' action='admin'>");
                out.println("<input type='hidden' name='action' value='login'>");
                out.println("Username: <input name='username' required><br><br>");
                out.println("Password: <input type='password' name='password' required><br><br>");
                out.println("<button type='submit'>Login</button></form></body></html>");
                return;
            }

            String dbPassword = System.getenv("BANKLOAN_DB_PASSWORD");
            if (dbPassword == null || dbPassword.isBlank()) {
                res.setStatus(500);
                out.println("Database password is not configured.");
                return;
            }

            
out.println("<!DOCTYPE html><html><head><title>Bank Loan Admin</title>");
out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
out.println("<style>");
out.println("body{margin:0;font-family:Arial,sans-serif;background:#f1f5f9;color:#1e293b;padding:25px;}");
out.println(".header{background:#123b68;color:white;padding:22px 25px;border-radius:12px;margin-bottom:24px;}");
out.println(".header h1{margin:0 0 8px;font-size:26px;}");
out.println(".header a{color:white;float:right;text-decoration:none;background:#24558a;padding:9px 14px;border-radius:6px;}");
out.println(".panel{background:white;padding:22px;border-radius:12px;box-shadow:0 3px 12px #00000010;overflow-x:auto;}");
out.println("table{width:100%;border-collapse:collapse;min-width:750px;}");
out.println("th{background:#e8eef7;color:#123b68;text-align:left;padding:13px;}");
out.println("td{padding:13px;border-bottom:1px solid #e2e8f0;}");
out.println("tr:hover td{background:#f8fafc;}");
out.println("button{border:0;border-radius:6px;padding:9px 12px;cursor:pointer;font-weight:bold;margin:2px;}");
out.println(".approve{background:#dcfce7;color:#166534;}");
out.println(".reject{background:#fee2e2;color:#991b1b;}");
out.println("@media(max-width:600px){body{padding:10px}.header h1{font-size:21px}.panel{padding:10px}}");
out.println("</style></head><body>");
out.println("<div class='header'><a href='admin?logout=1'>Logout</a><h1>Bank Loan Management</h1><p>Admin Dashboard | Manage Loan Applications</p></div>");
out.println("<div class='panel'><h2>Loan Applications</h2>");
out.println("<table><tr>");
out.println("<th>ID</th><th>Name</th><th>Age</th><th>Income</th>");
out.println("<th>Loan</th><th>Purpose</th><th>Status</th><th>Action</th></tr>");

            
            
            
           try {
    Class.forName("com.mysql.cj.jdbc.Driver");
} catch (ClassNotFoundException e) {
    out.println("<p>MySQL JDBC driver not found.</p>");
    e.printStackTrace();
    return;
}

try (Connection c = DriverManager.getConnection(DB, USER, dbPassword);
                 PreparedStatement p = c.prepareStatement(
                   "SELECT application_id,customer_name,age,monthly_income," +
                   "loan_amount,loan_purpose,status FROM loan_applications " +
                   "ORDER BY application_id DESC");
                 ResultSet r = p.executeQuery()) {

                while (r.next()) {
        String income = r.getBigDecimal("monthly_income") == null
        ? "-" : r.getBigDecimal("monthly_income").toString();

String purpose = r.getString("loan_purpose") == null
        || r.getString("loan_purpose").isBlank()
        ? "-" : r.getString("loan_purpose");
                    int id = r.getInt("application_id");
                    String status = r.getString("status");
                    out.println("<tr><td>" + id + "</td><td>" +
                        esc(r.getString("customer_name")) + "</td><td>" +
                        r.getInt("age") + "</td><td>" +
                        income + "</td><td>" +
r.getBigDecimal("loan_amount") + "</td><td>" +
                         esc(purpose) + "</td><td>" +
    esc(status) + "</td><td>");
                    if ("PENDING".equals(status)) {
                        out.println("<form method='post' action='admin'>");
                        out.println("<input type='hidden' name='id' value='" + id + "'>");
                        out.println("<button name='action' value='approve'>Approve</button>");
                        out.println("<button name='action' value='reject'>Reject</button>");
                        out.println("</form>");
                    } else {
                        out.println("-");
                    }
                    out.println("</td></tr>");
                }
            } catch (SQLException e) {
                getServletContext().log("Admin dashboard database error", e);
                res.setStatus(500);
                out.println("</table><p>Could not load applications. Check server logs.</p>");
                return;
            }
            out.println("</table></body></html>");
        }
    }

    protected void doPost(HttpServletRequest req,
                          HttpServletResponse res) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        if ("login".equals(action)) {
            String expected = System.getenv("BANKLOAN_ADMIN_PASSWORD");
            String username = req.getParameter("username");
            String password = req.getParameter("password");

            if (expected != null && !expected.isBlank()
                    && "admin".equals(username)
                    && expected.equals(password)) {
                req.getSession(true).setAttribute("admin", true);
                res.sendRedirect("admin");
            } else {
                res.setStatus(401);
                res.setContentType("text/html;charset=UTF-8");
                res.getWriter().println(
                    "<h3>Login failed. Check credentials and server configuration.</h3>" +
                    "<a href='admin'>Try again</a>");
            }
            return;
        }

        if (!loggedIn(req)) {
            res.sendError(401);
            return;
        }

        String status;
        if ("approve".equals(action)) status = "APPROVED";
        else if ("reject".equals(action)) status = "REJECTED";
        else {
            res.sendError(400, "Invalid action");
            return;
        }

        try {
            int id = Integer.parseInt(req.getParameter("id"));
            String dbPassword = System.getenv("BANKLOAN_DB_PASSWORD");
            if (dbPassword == null || dbPassword.isBlank()) {
                res.sendError(500, "Database password not configured");
                return;
            }

            
int updatedRows;

try (Connection c = DriverManager.getConnection(DB, USER, dbPassword);
     PreparedStatement p = c.prepareStatement(
         "UPDATE loan_applications SET status=? " +
         "WHERE application_id=? AND status='PENDING'")) {

    p.setString(1, status);
    p.setInt(2, id);
    updatedRows = p.executeUpdate();
}

if (updatedRows == 0) {
    res.sendError(409,
        "Application not updated. Check its ID or current status.");
    return;
}

res.sendRedirect("admin");

        } catch (NumberFormatException e) {
            res.sendError(400, "Invalid application ID");
        } catch (SQLException e) {
            getServletContext().log("Loan status update failed", e);
            res.sendError(500, "Could not update status");
        }
    }

    protected void service(HttpServletRequest req,
                           HttpServletResponse res)
            throws IOException, jakarta.servlet.ServletException {
        if ("1".equals(req.getParameter("logout"))) {
            HttpSession s = req.getSession(false);
            if (s != null) s.invalidate();
            res.sendRedirect("admin");
            return;
        }
        super.service(req, res);
    }
}