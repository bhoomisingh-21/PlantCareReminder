import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/GetReminderServlet")
public class GetReminderServlet extends HttpServlet {

    // Database connection constants (replace with your own)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/plant_reminder";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Database connection and query to fetch reminders
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT reminder_message FROM plant_reminders WHERE shown = 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                // Check if reminders are found
                if (rs.next()) {
                    String reminderMessage = rs.getString("reminder_message");

                    // Respond with the reminder message in JSON format
                    response.getWriter().write("{\"status\": \"success\", \"reminder\": \"" + reminderMessage + "\"}");
                } else {
                    // No reminders found
                    response.getWriter().write("{\"status\": \"success\", \"message\": \"No reminders found\"}");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Database error\"}");
        }
    }
}
