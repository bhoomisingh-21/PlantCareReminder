import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.sql.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ViewReminders")
public class ViewReminders extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jdbcURL = "jdbc:oracle:thin:@localhost:1521:XE";
        String dbUser = "system";
        String dbPassword = "Admin123#";

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("oracle.jdbc.OracleDriver");

            try (Connection conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
                 CallableStatement stmt = conn.prepareCall("{CALL GetReminders(?)}")) {
                 
                stmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
                stmt.execute();

                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                    out.print("[");
                    boolean isFirst = true;

                    // Date formatter to remove time part
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    while (rs.next()) {
                        if (!isFirst) {
                            out.print(",");
                        } else {
                            isFirst = false;
                        }

                        // Get and format the date correctly
                        Date reminderDate = rs.getDate("reminder_date");
                        String formattedDate = (reminderDate != null) ? dateFormat.format(reminderDate) : "";

                        out.print("{");
                        out.print("\"id\":" + rs.getInt("id") + ",");
                        out.print("\"plant_name\":\"" + rs.getString("plant_name") + "\",");
                        out.print("\"task_type\":\"" + rs.getString("task_type") + "\",");
                        out.print("\"reminder_date\":\"" + formattedDate + "\",");  // **Formatted Date**
                        out.print("\"reminder_time\":\"" + rs.getString("reminder_time") + "\",");
                        out.print("\"frequency\":\"" + rs.getString("frequency") + "\"");
                        out.print("}");
                    }

                    out.print("]");
                }
            }
        } catch (ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"status\":\"error\", \"message\":\"JDBC Driver not found.\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"status\":\"error\", \"message\":\"Error retrieving reminders: " + e.getMessage() + "\"}");
        }
    }
}
