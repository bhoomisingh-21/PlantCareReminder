import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/AddServlet")
public class AddServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jdbcURL = "jdbc:oracle:thin:@localhost:1521:XE";  
        String dbUser = "system";
        String dbPassword = "Admin123#";

        // Retrieve form parameters
        String plantName = request.getParameter("plant_name");
        String taskType = request.getParameter("task_type");
        String reminderDateStr = request.getParameter("reminder_date");
        String reminderTimeStr = request.getParameter("reminder_time"); // Expecting "09:00", "12:00", "18:00"
        String frequency = request.getParameter("frequency");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // **Validate Input Parameters**
            if (plantName == null || plantName.trim().isEmpty() ||
                taskType == null || taskType.trim().isEmpty() ||
                reminderDateStr == null || reminderDateStr.trim().isEmpty() ||
                reminderTimeStr == null || reminderTimeStr.trim().isEmpty() ||
                frequency == null || frequency.trim().isEmpty()) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"status\":\"error\", \"message\":\"All fields are required.\"}");
                return;
            }

            // **Debugging: Print Received Values**
            System.out.println("Plant Name: " + plantName);
            System.out.println("Task Type: " + taskType);
            System.out.println("Reminder Date: " + reminderDateStr);
            System.out.println("Received Reminder Time: " + reminderTimeStr);
            System.out.println("Frequency: " + frequency);

            // **Convert reminderDateStr to SQL Date**
            Date reminderDate;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date parsedDate = dateFormat.parse(reminderDateStr);
                reminderDate = new Date(parsedDate.getTime());  // Convert to SQL Date
            } catch (ParseException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"status\":\"error\", \"message\":\"Invalid date format. Use YYYY-MM-DD.\"}");
                return;
            }

            // **Convert Short Time Format to Full Text Format**
            if (reminderTimeStr.equals("09:00")) {
                reminderTimeStr = "Morning 9:00 AM";
            } else if (reminderTimeStr.equals("12:00")) {
                reminderTimeStr = "Afternoon 12:00 PM";
            } else if (reminderTimeStr.equals("18:00")) {
                reminderTimeStr = "Evening 6:00 PM";
            }

            // **Debugging: Confirm Transformation**
            System.out.println("Final Reminder Time Stored: " + reminderTimeStr);

            // **Database Connection & Procedure Call**
            try (Connection conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
                 CallableStatement stmt = conn.prepareCall("{CALL AddReminder(?, ?, ?, ?, ?)}")) {

                stmt.setString(1, plantName);
                stmt.setString(2, taskType);
                stmt.setDate(3, reminderDate);  // Stores only date (no time)
                stmt.setString(4, reminderTimeStr);  // Stores full formatted time text
                stmt.setString(5, frequency);

                stmt.execute();
                response.sendRedirect("addplant.html");  
                response.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"status\":\"success\", \"message\":\"Reminder added successfully.\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"status\":\"error\", \"message\":\"Error adding reminder.\"}");
            e.printStackTrace();  // Logs to server console for debugging
        } finally {
            out.close();
        }
    }
}
