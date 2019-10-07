package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.tools.SQLTools;
import controllers.tools.SecuredAction;
import play.db.Database;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.sql.SQLException;

public class EventController extends Controller {
    private Database db;
    private SQLTools.ResultSetProcessor nullRp;

    @Inject
    public EventController(Database db) {
        this.db = db;
        nullRp = rs -> {

        };
    }

    // CREATE EVENT (lägga till eventattend)
    public Result createEvent() {
        JsonNode jNode = request().body().asJson();
        String sql = "INSERT INTO Events VALUES (NULL, ?, ?, ?, ?, ?, ?, 1)";

        SQLTools.StatementFiller sf = pstmt -> {
            pstmt.setInt(1, jNode.findPath("location_id").asInt());
            pstmt.setLong(2, jNode.findPath("user_id").asLong());
            pstmt.setString(3, jNode.findPath("date").textValue());
            pstmt.setString(4, jNode.findPath("start_time").textValue());
            pstmt.setString(5, jNode.findPath("end_time").textValue());
            pstmt.setString(6, jNode.findPath("description").textValue());

        };

        String sql2 = "INSERT INTO Event_attendees VALUES ((SELECT MAX(event_id) FROM Events WHERE Events.user_id = ?), ?, ?)";

        SQLTools.StatementFiller sf2 = pstmt -> {
            pstmt.setLong(1, jNode.findPath("user_id").asLong());
            pstmt.setLong(2, jNode.findPath("user_id").asLong());
            pstmt.setString(3, jNode.findPath("attending_children_ids").textValue());
        };

		String sql3 = "INSERT INTO Logs (type) VALUES (6)";

		SQLTools.StatementFiller sf3 = pstmt -> {};

        try {
            SQLTools.doPreparedStatement(db, sql, sf, nullRp);
            SQLTools.doPreparedStatement(db, sql2, sf2, nullRp);
            SQLTools.doPreparedStatement(db, sql3, sf3, nullRp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok("Event created and user attended.");
    }

    // UPDATE EVENT
    public Result updateEvent() {
        JsonNode jNode = request().body().asJson();
        String sql = "UPDATE Events SET location_id = ?, date = ?, start_time = ?, " +
                "end_time = ?, description = ? WHERE event_id = ? AND user_id = ?";

        SQLTools.StatementFiller sf = pstmt -> {
            pstmt.setString(1, jNode.findPath("location_id").textValue());
            pstmt.setString(2, jNode.findPath("date").textValue());
            pstmt.setString(3, jNode.findPath("start_time").textValue());
            pstmt.setString(4, jNode.findPath("end_time").textValue());
            pstmt.setString(5, jNode.findPath("description").textValue());
            pstmt.setString(6, jNode.findPath("event_id").textValue());
            pstmt.setString(7, jNode.findPath("user_id").textValue());
        };

        try {
            SQLTools.doPreparedStatement(db, sql, sf, nullRp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok("Event updated.");

    }

    // CANCEL EVENT
    public Result cancelEvent() {
        JsonNode jNode = request().body().asJson();
        String sql = "UPDATE Events SET status = 0 WHERE event_id = ? AND user_id = ?";

        SQLTools.StatementFiller sf = pstmt -> {
            pstmt.setString(1, jNode.findPath("event_id").textValue());
            pstmt.setString(2, jNode.findPath("user_id").textValue());
        };

        try {
            SQLTools.doPreparedStatement(db, sql, sf, nullRp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok("Event canceled.");
    }

    // DELETE EVENT
    public Result deleteEvent() {
        JsonNode jNode = request().body().asJson();
        String sql = "DELETE FROM Events WHERE event_id = ? AND user_id = ?";

        SQLTools.StatementFiller sf = pstmt -> {
            pstmt.setInt(1, jNode.findPath("event_id").asInt());
            pstmt.setLong(2, jNode.findPath("user_id").asLong());
        };

		String sql2 = "INSERT INTO Logs (type) VALUES (10)";

		SQLTools.StatementFiller sf2 = pstmt ->{};

        try {
            SQLTools.doPreparedStatement(db, sql, sf, nullRp);
            SQLTools.doPreparedStatement(db, sql2, sf2, nullRp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok("Event deleted.");
    }

    // SELECT EVENT
    public Result selectEvent(int eventId) {
        final JsonNode[] result = {null};
        String sql = "SELECT DISTINCT Events.*, Locations.name, Location_types.type_name "
                + "FROM Events, Locations, Location_types WHERE "
                + "Events.event_id = ? AND Events.location_id = Locations.location_id AND "
                + "Locations.location_type = Location_types.type_id";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setInt(1, eventId);
        };

        SQLTools.ResultSetProcessor rp = rs -> {
            result[0] = SQLTools.columnsAndRowsToJSON(rs);
        };

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok(result[0]);
    }

    // SELECT LAST CREATED EVENT
    public Result selectLastCreatedEvent() {
        final JsonNode[] result = {null};
        String sql = "SELECT MAX(event_id) FROM Events";

        SQLTools.StatementFiller sf = stmt -> {};

        SQLTools.ResultSetProcessor rp = rs -> {
            result[0] = SQLTools.columnsAndRowsToJSON(rs);
        };

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok(result[0]);
    }

    // SELECT EVENT BY LOCATION
    public Result selectEventsByLocation(int locationId) {
        final JsonNode[] result = {null};
        String sql = "SELECT DISTINCT Events.*, Locations.name, Location_types.type_name, " +
                "(SELECT COUNT(ea.user_id) FROM Event_attendees ea WHERE ea.event_id = Events.event_id) AS noOfAttendees " +
                "FROM Events, Locations, Location_types " +
                "WHERE Locations.location_id = ? " +
                "AND Events.location_id = Locations.location_id " +
                "AND Locations.location_type = Location_types.type_id " +
                "AND CONCAT(date, ' ', end_time) > NOW() " +
                "ORDER BY Events.date, Events.start_time";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setInt(1, locationId);
        };

        SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError(e.toString());
        }

        return ok(result[0]);
    }

    // SELECT EVENT BY USER
    public Result selectEventsByUser(long userId) {
        final JsonNode[] result = {null};

        String sql = "SELECT DISTINCT l.location_id, l.name_short, l.name, lt.type_name, e.*, (SELECT COUNT(ea.user_id) FROM Event_attendees ea WHERE ea.event_id = e.event_id) AS noOfAttendees, (SELECT GROUP_CONCAT(DISTINCT uc.age ORDER BY uc.age SEPARATOR ', ') FROM User_children uc, Event_attendees ea2 WHERE ea2.event_id = e.event_id AND ea2.attending_children_ids LIKE CONCAT('%,', CONCAT(uc.child_id, ',%')) GROUP BY e.event_id) AS children FROM Events e, Locations l, Location_types lt WHERE EXISTS (SELECT NULL FROM Event_attendees WHERE Event_attendees.event_id = e.event_id AND Event_attendees.user_id = ?) AND e.location_id = l.location_id AND l.location_type = lt.type_id AND CONCAT(date, ' ', end_time) > NOW() ORDER BY e.date, e.start_time";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setLong(1, userId);
        };

        SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError(e.toString());
        }

        return ok(result[0]);
    }

    // CREATE EVENT ATTENDEE (Ska inte denna länkas med Users_children-tabellen?)
    public Result addEventAttendee() {
        JsonNode jNode = request().body().asJson();
        String sql = "INSERT INTO Event_attendees VALUES (?,?,?)";

        SQLTools.StatementFiller sf = pstmt -> {
            pstmt.setInt(1, jNode.findPath("event_id").asInt());
            pstmt.setLong(2, jNode.findPath("user_id").asLong());
            pstmt.setString(3, jNode.findPath("attending_children_ids").textValue());
        };

		String sql2 = "INSERT INTO Logs (type) VALUES (7)";

		SQLTools.StatementFiller sf2 = pstmt ->{};

        try {
            SQLTools.doPreparedStatement(db, sql, sf, nullRp);
            SQLTools.doPreparedStatement(db, sql2, sf2, nullRp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok("User attendee created.");
    }


    // DELETE ATTENDEE
    public Result deleteEventAttendee() {
        JsonNode jNode = request().body().asJson();
        String sql = "DELETE FROM Event_attendees WHERE event_id = ? AND user_id = ?";

        SQLTools.StatementFiller sf = pstmt -> {
            pstmt.setInt(1, jNode.findPath("event_id").asInt());
            pstmt.setLong(2, jNode.findPath("user_id").asLong());
        };

		String sql2 = "INSERT INTO Logs (type) VALUES (8)";

		SQLTools.StatementFiller sf2 = pstmt ->{};

        try {
            SQLTools.doPreparedStatement(db, sql, sf, nullRp);
            SQLTools.doPreparedStatement(db, sql2, sf2, nullRp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok("User attendee deleted.");
    }

    // SELECT EVENT ATTENDEES
    public Result selectEventAttendees(int eventId) {
        final JsonNode[] result = {null};
        String sql = "SELECT e.event_id, e.attending_children_ids, u.* FROM Event_attendees e, Users u " +
                "WHERE e.user_id = u.user_id AND e.event_id = ?";

        SQLTools.StatementFiller sf = stmt -> stmt.setInt(1, eventId);

        SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);

            for (JsonNode currUserNode : result[0]) {
                ObjectNode currUserObj = (ObjectNode) currUserNode;
                int[] childrenIds = commaStringToIntArr(currUserObj.get("attending_children_ids").asText());
                currUserObj.remove("attending_children_ids");

                String childrenAges = getChildrenAgesFromIDs(childrenIds);
                currUserObj.set("children", Json.toJson(childrenAges));

            }

        } catch (SQLException e) {
            return internalServerError(e.toString());
        }

        return ok(result[0]);
    }

    private String getChildrenAgesFromIDs(int[] ids) throws SQLException {
        final String[] result = {""};
        StringBuilder sql = new StringBuilder("SELECT age from User_children WHERE child_id IN (");
        for (int i = 0; i < ids.length - 1; i++) {
            sql.append("?,");
        }
        sql.append("?)");

        SQLTools.StatementFiller sf = stmt -> {
            for (int i = 0; i < ids.length; i++) {
                stmt.setInt(i + 1, ids[i]);
            }
        };

        SQLTools.ResultSetProcessor rp = rs -> {
            while (rs.next()) {
                result[0] += rs.getInt("age") + ",";
            }
        };

        SQLTools.doPreparedStatement(db, sql.toString(), sf, rp);
        return result[0];
    }

    /**
     * Substrings that aren't a number are ignored
     */
    private int[] commaStringToIntArr(String input) {
        String[] splitted = input.split(",");
        int[] numbers = new int[splitted.length];

        int i = 0;
        for (String curr : splitted) {
            try {
                numbers[i] = Integer.parseInt(curr);
                i++;
            } catch (NumberFormatException e) {
            }
        }

        return numbers;
    }

    // SELECT ALL EVENTS CREATED BY USER
    public Result selectEventsCreatedByUser(long userId) {
        final JsonNode[] result = {null};
        String userID = "" + userId;

        String sql = "SELECT l.name_short, e.event_id, e.date, e.start_time, e.end_time, e.description, lt.type_name\n" +
                "FROM Users AS u, Events AS e, Locations AS l, Location_types AS lt \n" +
                "WHERE u.user_id = e.user_id\n" +
                "AND e.location_id = l.location_id AND l.location_type = lt.type_id\n" +
                "AND u.user_id = ?";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setString(1, userID);
        };

        SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError(e.toString());
        }

        return ok(result[0]);
    }

    // SELECT EVENT CHAT
    public Result selectEventChat(int eventId) {
        final JsonNode[] result = {null};

        String sql = "SELECT CONCAT(Users.first_name, ' ', Users.last_name) AS name, message, " +
                "date_format(date_time, 'Skickat %e/%c kl %H.%i') AS date_time, Users.user_id FROM Chats, Users " +
                "WHERE Chats.event_id = ? AND Chats.user_id = Users.user_id ORDER BY Chats.date_time";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setInt(1, eventId);
        };

        SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError(e.toString());
        }

        return ok(result[0]);
    }

    // CREATE EVENT CHAT MESSAGE
    public Result insertEventChat() {

        JsonNode jNode = request().body().asJson();
        String sql = "INSERT INTO Chats (event_id, user_id, message) VALUES (?,?,?)";

        SQLTools.StatementFiller sf = pstmt -> {
            pstmt.setInt(1, jNode.findPath("event_id").asInt());
            pstmt.setLong(2, jNode.findPath("user_id").asLong());
            pstmt.setString(3, jNode.findPath("message").textValue());
        };

		String sql2 = "INSERT INTO Logs (type) VALUES (9)";

		SQLTools.StatementFiller sf2 = pstmt ->{};

        try {
            SQLTools.doPreparedStatement(db, sql, sf, nullRp);
            SQLTools.doPreparedStatement(db, sql2, sf2, nullRp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok("Chat message inserted");

    }

    // ALEX REKESTAD TESTAR SÄKERHET
    @With(SecuredAction.class)
    public Result securityTest() {
        return ok("Method called.");
    }

    // GET ALL EVENTS, LIMITED TO 100 RETURNED EVENTS
    public Result selectAllEvents() {
        final JsonNode[] result = {null};

        String sql = "SELECT l.location_id, l.name_short, l.name, lt.type_name, e.*, (SELECT COUNT(ea.user_id) FROM Event_attendees ea WHERE ea.event_id = e.event_id) AS noOfAttendees, (SELECT GROUP_CONCAT(DISTINCT uc.age ORDER BY uc.age SEPARATOR ', ') FROM User_children uc, Event_attendees ea2 WHERE ea2.event_id = e.event_id AND ea2.attending_children_ids LIKE CONCAT('%', CONCAT(uc.child_id, ',%')) GROUP BY e.event_id) AS children FROM Locations l, Events e, Location_types lt WHERE l.location_id = e.location_id AND l.location_type = lt.type_id AND CONCAT(e.date, ' ', e.end_time) > NOW() ORDER BY e.date, e.start_time LIMIT 100";

        SQLTools.StatementFiller sf = stmt -> {
        };
        SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok(result[0]);
    }

    // method to select everything for the eventActivity-page
    public Result getEverythingForEventActivity(long userId, int eventId) {

        String userSql = "SELECT first_name, last_name FROM Users WHERE user_id = " + userId;
        String userChildrenSql = "SELECT child_id, age FROM `User_children` WHERE parent_id = " + userId + " ORDER BY age";
        String eventSql = "SELECT e.*, l.name_short FROM Events e, Locations l WHERE e.location_id = l.location_id AND e.event_id = "+ eventId;
        String eventAttendeesSql = "SELECT u.user_id, u.first_name, u.last_name, (SELECT GROUP_CONCAT(uc.age ORDER BY uc.age SEPARATOR ',') FROM User_children uc WHERE e.attending_children_ids LIKE CONCAT('%,', CONCAT(uc.child_id, ',%')) GROUP BY e.event_id) AS children FROM Event_attendees e, Users u WHERE e.user_id = u.user_id AND e.event_id = " + eventId;

        final JsonNode[] result = {null, null, null, null, null};

        SQLTools.StatementFiller sf = stmt -> {
        };
        SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);
        SQLTools.ResultSetProcessor rp2 = rs -> result[1] = SQLTools.columnsAndRowsToJSON(rs);
        SQLTools.ResultSetProcessor rp3 = rs -> result[2] = SQLTools.columnsAndRowsToJSON(rs);
        SQLTools.ResultSetProcessor rp4 = rs -> result[3] = SQLTools.columnsAndRowsToJSON(rs);

        try {
            SQLTools.doPreparedStatement(db, userSql, sf, rp);
            SQLTools.doPreparedStatement(db, userChildrenSql, sf, rp2);
            SQLTools.doPreparedStatement(db, eventSql, sf, rp3);
            SQLTools.doPreparedStatement(db, eventAttendeesSql, sf, rp4);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        String returnString = result[0].toString() + "," + result[1].toString() + "," + result[2].toString() + "," + result[3].toString();

        return ok(returnString);
    }

    private String trimArray(String s) {
        return s.substring(1,s.length()-2);
    }
}