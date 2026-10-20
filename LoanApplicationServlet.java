import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/apply")
public class LoanApplicationServlet extends HttpServlet {

    private static final String DB_URL =
        "jdbc:mysql://localhost:3306/bank_loan_db";

    private static final String DB_USER = "root";

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            // 1. Receive form data
           

String name = request.getParameter("name");
String ageText = request.getParameter("age");
String incomeText = request.getParameter("monthly_income");
String amountText = request.getParameter("loan_amount");
String purpose = request.getParameter("loan_purpose");


            
 // 2. Check required fields
if (name == null || name.isBlank()
        || ageText == null || ageText.isBlank()
        || incomeText == null || incomeText.isBlank()
        || amountText == null || amountText.isBlank()
        || purpose == null || purpose.isBlank()) {

    response.setStatus(400);
    out.println("<h2>Please fill all required fields.</h2>");
    out.println("<a href='index.html#apply'>Go Back</a>");
    return;
}


            // 3. Convert numbers

int age;

try {
    age = Integer.parseInt(ageText.trim());
} catch (NumberFormatException ex) {
    response.setStatus(400);
    out.println("<h2>Please enter a valid age.</h2>");
    out.println("<a href='index.html#apply'>Go Back</a>");
    return;
}

if (age < 18 || age > 100) {
    response.setStatus(400);
    out.println("<h2>Age must be between 18 and 100.</h2>");
    out.println("<a href='index.html#apply'>Go Back</a>");
    return;
}

            double income;
            double amount;

            try {
                income = Double.parseDouble(incomeText.trim());
                amount = Double.parseDouble(amountText.trim());
            } catch (NumberFormatException ex) {
                response.setStatus(400);
                out.println("<h2>Please enter valid numeric values.</h2>");
                out.println("<a href='index.html#apply'>Go Back</a>");
                return;
            }

            // 4. Validate values
            if (!Double.isFinite(income) || !Double.isFinite(amount)
                    || income < 0 || amount <= 0) {

                response.setStatus(400);
                out.println("<h2>Please enter valid income and loan amount.</h2>");
                out.println("<a href='index.html#apply'>Go Back</a>");
                return;
            }

            // 5. Read database password
            String password = System.getenv("BANKLOAN_DB_PASSWORD");

            if (password == null || password.isBlank()) {
                response.setStatus(500);
                out.println("<h2>Database password is not configured.</h2>");
                return;
            }

            // 6. Insert application into MySQL
            
String sql = "INSERT INTO loan_applications "
        + "(customer_name, age, monthly_income, loan_amount, "
        + "loan_purpose, status) "
        + "VALUES (?, ?, ?, ?, ?, 'PENDING')";


            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                try (Connection con = DriverManager.getConnection(
                            DB_URL, DB_USER, password);
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    
ps.setString(1, name.trim());
ps.setInt(2, age);
ps.setDouble(3, income);
ps.setDouble(4, amount);
ps.setString(5, purpose);


                    int saved = ps.executeUpdate();

                    if (saved == 1) {
                        out.println("<h1>Loan Application Saved!</h1>");
                        out.println("<p>Your application was saved successfully.</p>");
                        out.println("<p>Status: PENDING</p>");
                        out.println("<a href='index.html'>Back to Home</a>");
                    } else {
                        response.setStatus(500);
                        out.println("<h2>Application was not saved.</h2>");
                    }
                }

            } catch (SQLException | ClassNotFoundException ex) {
                getServletContext().log("Database save failed", ex);
                response.setStatus(500);
                out.println("<h2>Could not save application.</h2>");
                out.println("<p>Please check the database configuration and Tomcat logs.</p>");
                out.println("<a href='index.html'>Back to Home</a>");
            }
        }
    }
}
