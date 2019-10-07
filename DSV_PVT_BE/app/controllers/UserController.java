package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.tools.SQLTools;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;

/**
 * Created by felix on 2017-04-27.
 */
public class UserController extends Controller {
	private Database db;

	@Inject
	public UserController(Database db) {
		this.db = db;
	}

	// LIST ALL USERS
	public Result listUsrs() {
		final JsonNode[] result = {null};

		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Users", stmt -> {
			}, rp);
		} catch (SQLException e) {
			return internalServerError("couldn't list users" + e);
		}

		return ok(result[0]);
	}

	// CREATE USER
	public Result createUser() {
		JsonNode jNode = request().body().asJson();

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, jNode.findPath("user_id").asLong());
			pstmt.setString(2, jNode.findPath("first_name").textValue());
			pstmt.setString(3, jNode.findPath("last_name").textValue());
		};

		SQLTools.StatementFiller sf2 = pstmt ->{};

		SQLTools.ResultSetProcessor rp = rs -> {};

		try {
			SQLTools.doPreparedStatement(db, "INSERT INTO Users (user_id, first_name, last_name) VALUES (?,?,?)",
					sf, rp);
			SQLTools.doPreparedStatement(db,"INSERT INTO Logs (type) VALUES (1)",sf2,rp);
		} catch (SQLException e) {
			return internalServerError("couldn't make user: " + e);
		}

		return ok("made user!");

	}

	// DELETE ACCOUNT
	public Result deleteAccount(){
		JsonNode jNode = request().body().asJson();

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, jNode.findPath("user_id").asLong());
		};

		SQLTools.StatementFiller sf2 = pstmt -> {};

		SQLTools.ResultSetProcessor rp = rs -> {};

		try{
			SQLTools.doPreparedStatement(db, "DELETE FROM Users WHERE user_id = ?", sf, rp);
			SQLTools.doPreparedStatement(db, "INSERT INTO Logs (type) VALUES (11)", sf2, rp);
		} catch(SQLException e){
			return internalServerError("couldn't delete user" + e);
		}

		return ok("deleted user");
	}

	// CREATE CHILD
	public Result createChild(){
		JsonNode jNode = request().body().asJson();
		System.out.println(jNode);

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, jNode.findPath("parent_id").asLong());
			pstmt.setInt(2, jNode.findPath("age").asInt());
		};

		SQLTools.ResultSetProcessor rp = rs -> {};

		try{
			SQLTools.doPreparedStatement(db, "INSERT INTO User_children (parent_id, age) VALUES (?,?)", sf, rp);
		} catch (SQLException e){
			return internalServerError("couldn't make child: " + e);
		}

		return ok("made child");
	}

	// CREATE FAVORITE LOCATION
	public Result createFavoriteLocation(){
		JsonNode jNode = request().body().asJson();

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, jNode.findPath("user_id").asLong());
			pstmt.setInt(2, jNode.findPath("location_id").asInt());
		};

		SQLTools.StatementFiller sf2 = pstmt -> {};

		SQLTools.ResultSetProcessor rp = rs -> {};

		try{
			SQLTools.doPreparedStatement(db, "INSERT INTO User_locations VALUES (?,?)", sf, rp);
			SQLTools.doPreparedStatement(db,"INSERT INTO Logs (type) VALUES (2)", sf2, rp);
		} catch (SQLException e){
			return internalServerError("couldn't load favorite location" + e);
		}

		return ok("made favorite location");
	}

	// DELETE FAVORITE LOCATION
	public Result deleteFavoriteLocation(){
		JsonNode jNode = request().body().asJson();

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, jNode.findPath("user_id").asLong());
			pstmt.setLong(2, jNode.findPath("location_id").asLong());
		};

		SQLTools.StatementFiller sf2 = pstmt -> {};

		SQLTools.ResultSetProcessor rp = rs -> {};

		try{
			SQLTools.doPreparedStatement(db, "DELETE FROM User_locations WHERE user_id = ? AND location_id = ?", sf, rp);
			SQLTools.doPreparedStatement(db, "INSERT INTO Logs (type) VALUES (3)", sf2, rp);
		} catch(SQLException e){
			return internalServerError("couldn't delete like" + e);
		}

		return ok("deleted like");
	}

	// CREATE LIKE
	public Result createLike(){
		JsonNode jNode = request().body().asJson();

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, jNode.findPath("liker_id").asLong());
			pstmt.setLong(2, jNode.findPath("liked_id").asLong());
		};

		SQLTools.StatementFiller sf2 = pstmt -> {};

		SQLTools.ResultSetProcessor rp = rs -> {};

		try{
			SQLTools.doPreparedStatement(db, "INSERT INTO User_likes VALUES (?,?)", sf, rp);
			SQLTools.doPreparedStatement(db, "INSERT INTO Logs (type) VALUES (4)", sf2, rp);
		} catch (SQLException e){
			return internalServerError("couldn't load like" + e);
		}

		return ok("made like");
	}

	// DELETE LIKE
	public Result deleteLike(){
		JsonNode jNode = request().body().asJson();

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, jNode.findPath("liker_id").asLong());
			pstmt.setLong(2, jNode.findPath("liked_id").asLong());
		};

		SQLTools.StatementFiller sf2 = pstmt -> {};

		SQLTools.ResultSetProcessor rp = rs -> {};

		try{
			SQLTools.doPreparedStatement(db, "DELETE FROM User_likes WHERE liker_id = ? AND liked_id = ?", sf, rp);
			SQLTools.doPreparedStatement(db, "INSERT INTO Logs (type) VALUES (5)", sf2, rp);
		} catch(SQLException e){
			return internalServerError("couldn't delete like" + e);
		}

		return ok("deleted like");
	}

	// GET SELECTED USER
	public Result getUser(long userID) {
		final JsonNode[] result = {null};
		String fbIDStr = "" + userID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, fbIDStr);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT * FROM Users WHERE user_id=?", sf, rp);
		} catch (SQLException e) {
			return internalServerError("couldn't load user " + e);
		}

		return ok(result[0]);
	}

	// GET USERS CHILDREN
	public Result getUserChildren(long userID){
		final JsonNode[] result = {null};
		String userID2 = "" + userID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, userID2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try{
			SQLTools.doPreparedStatement(db, "SELECT child_id, age FROM `User_children` WHERE parent_id = ? ORDER BY age", sf, rp);
		} catch (SQLException e){
			return internalServerError("couldn't load children");
		}

		return ok(result[0]);
	}

	// GET CHILD AGE
	public Result getChildAge(int childID){
		final JsonNode[] result = {null};
		String childID2 = "" + childID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, childID2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try{
			SQLTools.doPreparedStatement(db, "SELECT age FROM `User_children` WHERE child_id = ?", sf, rp);
		} catch (SQLException e){
			return internalServerError("couldn't load age");
		}

		return ok(result[0]);
	}

	// DELETE CHILD
	public Result deleteChild(){
		JsonNode jNode = request().body().asJson();
		System.out.println("test deleteChild");
		System.out.println(jNode);

		SQLTools.StatementFiller sf = pstmt -> {
			pstmt.setLong(1, jNode.findPath("child_id").asLong());
			pstmt.setLong(2, jNode.findPath("parent_id").asLong());
		};

		SQLTools.ResultSetProcessor rp = rs -> {};

		try{
			SQLTools.doPreparedStatement(db, "DELETE FROM User_children WHERE child_id = ? AND parent_id = ?", sf, rp);
		} catch(SQLException e){
			return internalServerError("couldn't delete child" + e);
		}

		return ok("deleted child");
	}

	// GET USERS AMOUNT OF LIKES
	public Result getAmountOfLikes(long fbID) {
		final JsonNode[] result = {null};
		String id2 = "" + fbID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, id2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT COUNT(*) AS likes FROM User_likes WHERE liked_id=?", sf,
					rp);
		} catch (SQLException e) {
			return internalServerError("couldn't load likes: " + e);
		}

		return ok(result[0]);
	}

	// GET LIKED USERS
	public Result getLikedUsers(long userID){
		final JsonNode[] result = {null};
		String userID2 = ""+userID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, userID2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT liked_id FROM `User_likes` WHERE liker_id = ?", sf,
					rp);
		} catch (SQLException e) {
			return internalServerError("couldn't load likes: " + e);
		}

		return ok(result[0]);

	}

	// GET MY LIKERS ID AND NAME
	public Result getLikers(long userID){
		final JsonNode[] result = {null};
		String userID2 = ""+userID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, userID2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try {
			SQLTools.doPreparedStatement(db, "SELECT Users.user_id, Users.first_name, Users.last_name \n" +
							"FROM Users, User_likes \n" +
							"WHERE Users.user_id = User_likes.liker_id \n" +
							"AND User_likes.liked_id = ?", sf,
					rp);
		} catch (SQLException e) {
			return internalServerError("couldn't load likers: " + e);
		}

		return ok(result[0]);

	}

	// GET A USERS LOCATIONS (PROFILE VIEW)
	public Result getUserLocations(long userID){
		final JsonNode[] result = {null};
		String userID2 = "" + userID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, userID2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try{
			SQLTools.doPreparedStatement(db, "SELECT l.location_id, l.name, l.location_type FROM Locations AS l, User_locations AS ul WHERE l.location_id = ul.location_id AND ul.user_id = ?", sf, rp);
		} catch (SQLException e){
			return internalServerError("coudn't load users locations");
		}

		return ok(result[0]);
	}

	// GET A USERS LOCATIONS EVENTS (HOME VIEW)
	public Result getUserLocationsEvents(long userID){
		final JsonNode[] result = {null};
		String userID2 = "" + userID;

		SQLTools.StatementFiller sf = stmt -> stmt.setString(1, userID2);
		SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

		try{
			SQLTools.doPreparedStatement(db, "SELECT l.location_id, l.name, lt.type_name, e.* FROM Locations AS l, User_locations AS ul, Events AS e, Location_types AS lt WHERE l.location_id = ul.location_id AND l.location_id =e.location_id AND l.location_type = lt.type_id AND ul.user_id = ? AND CONCAT(e.date, ' ', e.end_time) > NOW()", sf, rp);
		} catch (SQLException e){
			return internalServerError("Error: " + e.toString());
		}

		return ok(result[0]);
	}

    // GET A USERS LOCATIONS EVENTS (HOME VIEW)
    public Result getUserLocationsEventsTest(long userID){
        final JsonNode[] result = {null};

        String sql = "SELECT l.location_id, l.name_short, l.name, lt.type_name, e.*, (SELECT COUNT(ea.user_id) FROM Event_attendees ea WHERE ea.event_id = e.event_id) AS noOfAttendees, (SELECT GROUP_CONCAT(DISTINCT uc.age ORDER BY uc.age SEPARATOR ', ') FROM User_children uc, Event_attendees ea2 WHERE ea2.event_id = e.event_id AND ea2.attending_children_ids LIKE CONCAT('%,', CONCAT(uc.child_id, ',%')) GROUP BY e.event_id) AS children FROM Locations l, User_locations ul, Events e, Location_types lt WHERE l.location_id = ul.location_id AND ul.user_id = ? AND l.location_id = e.location_id AND l.location_type = lt.type_id AND CONCAT(e.date, ' ', e.end_time) > NOW() ORDER BY e.date, e.start_time";

        SQLTools.StatementFiller sf = stmt -> stmt.setLong(1, userID);
        SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

        try{
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e){
            return internalServerError("Error: " + e.toString());
        }

        return ok(result[0]);
    }

}
