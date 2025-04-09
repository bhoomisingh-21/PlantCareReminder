import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/DeleteReminderServlet")
public class DeleteReminderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jdbcURL = "jdbc:oracle:thin:@localhost:1521:XE";  // Replace XE with your PDB name if necessary
        String dbUser = "system";
        String dbPassword = "Admin123#";
        String reminderId = request.getParameter("id");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (reminderId == null || reminderId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"status\":\"error\", \"message\":\"Reminder ID is required.\"}");
            return;
        }

        try {
            // Load Oracle JDBC Driver
            Class.forName("oracle.jdbc.OracleDriver");

            try (Connection conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
                 CallableStatement stmt = conn.prepareCall("{CALL DeleteReminder(?)}")) { 

                stmt.setInt(1, Integer.parseInt(reminderId));
                
                stmt.execute(); // Execute the stored procedure

                // If execution reaches here, deletion was successful
                response.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"status\":\"success\", \"message\":\"Reminder deleted successfully.\"}");

            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"status\":\"error\", \"message\":\"SQL Error: " + e.getMessage().replace("\"", "'") + "\"}");
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"status\":\"error\", \"message\":\"JDBC Driver not found.\"}");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"status\":\"error\", \"message\":\"Invalid Reminder ID format.\"}");
        }
    }
}
