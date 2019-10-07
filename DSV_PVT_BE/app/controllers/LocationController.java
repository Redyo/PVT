package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.tools.CoordinateConverter;
import controllers.tools.SQLTools;
import play.db.Database;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;

public class LocationController extends Controller {
    private Database db;
    private String returnData;
    private String convertResults;

    @Inject
    public LocationController(Database db) {
        this.db = db;
    }

    public Result listLocations() {
        final JsonNode[] result = {null};

        SQLTools.ResultSetProcessor rp = rs -> result[0] = SQLTools.columnsAndRowsToJSON(rs);

        try {
            SQLTools.doPreparedStatement(db, "SELECT * FROM Locations", stmt -> {
            }, rp);
        } catch (SQLException e) {
            return internalServerError("couldn't list locations");
        }

        return ok(result[0]);
    }

    public String getReturnData() {
        String returnThis = returnData;
        returnData = "";

        return returnThis;
    }

    public void setReturnData(String returnData) {
        this.returnData = returnData;
    }

    public Result getLocation(int locationId) {
        final JsonNode[] result = {null};
        String sql = "SELECT * FROM Locations WHERE location_id = ?";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setInt(1, locationId);
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

    public Result searchLocations(String search) {
        final JsonNode[] result = {null};
        //search += "%";
        final String search2 = search;

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setString(1, search2);
        };

        SQLTools.ResultSetProcessor rp = rs -> {
            result[0] = SQLTools.columnsAndRowsToJSON(rs);
        };

        try {
            SQLTools.doPreparedStatement(db, "SELECT * FROM Locations WHERE name REGEXP ?", sf, rp);
        } catch (SQLException e) {
            return internalServerError("couldn't load search");
        }


        return ok(result[0]);
    }

    // LOCATIONS NEAR YOU
    public Result getLocationsNearYou(double lat, double lng) {
        final JsonNode[] result = {null};
        final double latitude = lat;
        final double longitude = lng;

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setDouble(1, latitude);
            stmt.setDouble(2, latitude);
            stmt.setDouble(3, longitude);
        };

        SQLTools.ResultSetProcessor rp = rs -> {
            result[0] = SQLTools.columnsAndRowsToJSON(rs);
        };

        try {
            SQLTools.doPreparedStatement(db, "SELECT description, location_id, name_short, location_type, round(6371 * 2 * ASIN(SQRT( POWER(SIN((? - abs(lat)) * pi()/180 / 2), 2) + COS(? * pi()/180 ) * COS(abs(lat) * pi()/180) * POWER(SIN((? - lng) * pi()/180 / 2), 2) )),1) AS distance FROM Locations HAVING distance < 3 ORDER BY distance LIMIT 20", sf, rp);
        } catch (SQLException e) {
            return internalServerError("couldn't load locations" + e);
        }


        return ok(result[0]);
    }

    // CHECK IF LOCATION IS USER FAVOURITE
    public Result checkIfFavourite(long userId, int locationId) {

        String sql = "SELECT IF(EXISTS(SELECT * FROM User_locations WHERE user_id = ? AND location_id = ?),'TRUE','FALSE') AS result";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setLong(1, userId);
            stmt.setInt(2, locationId);

        };

        SQLTools.ResultSetProcessor rp = rs -> {
            while (rs.next())
                setReturnData(rs.getString("result"));
        };

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        return ok(getReturnData());
    }

    // CHECK IF LOCATION IS USER FAVOURITE 2
    public Result checkIfFavourite2(long userId, int locationId) {
        final JsonNode[] result = {null};
        final long userID = userId;
        final int locationID = locationId;

        String sql = "SELECT CASE WHEN EXISTS(SELECT 1 FROM User_locations WHERE user_id = ? AND location_id = ?) THEN 'true' ELSE 'false' END AS result";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setLong(1, userID);
            stmt.setInt(2, locationID);

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

    // CHECK IF LOCATION IS USER FAVOURITE 2
    public Result checkIfFavourite3(long userId, int locationId) {
        final JsonNode[] result = {null};

        String sql = "SELECT * FROM User_locations WHERE user_id = ? AND location_id = ?";

        SQLTools.StatementFiller sf = stmt -> {
            stmt.setLong(1, userId);
            stmt.setInt(2, locationId);
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

    public Result convertCoordinate() {
        String sql = "SELECT * FROM Locations";
        ArrayList coordinates = new ArrayList<LocationCoordinate>();

        CoordinateConverter cordi = new CoordinateConverter();

        SQLTools.StatementFiller sf = stmt -> {
        };

        SQLTools.ResultSetProcessor rp = rs -> {
            while (rs.next()) {
                Integer id = rs.getInt("location_id");
                int posX = rs.getInt("position_x");
                int posY = rs.getInt("position_y");

                double[] coords = cordi.grid_to_geodetic(posX, posY);

                coordinates.add(new LocationCoordinate(id, coords[0], coords[1]));
            }
        };

        try {
            SQLTools.doPreparedStatement(db, sql, sf, rp);
        } catch (SQLException e) {
            return internalServerError("Error: " + e.toString());
        }

        String returnThis = "";

        for (int i = 0; i < coordinates.size(); i++) {
            LocationCoordinate lc = (LocationCoordinate) coordinates.get(i);

            executeUpdate(lc.getId(), lc.getxPos(), lc.getyPos());

            returnThis += coordinates.get(i).toString() + "\n";

        }

        return ok(returnThis);
    }

    private void addToString(String s) {
        returnData += s;
    }

    public class LocationCoordinate {
        private int id;
        private double xPos;

        public int getId() {
            return id;
        }

        public double getxPos() {
            return xPos;
        }

        public double getyPos() {
            return yPos;
        }

        private double yPos;

        LocationCoordinate(int id, double xPos, double yPos) {
            this.id = id;
            this.xPos = xPos;
            this.yPos = yPos;
        }

        public String toString() {
            return "id: " + id + ", X: " + xPos + ", Y: " + yPos;
        }

    }

    private void executeUpdate(int id, double x, double y) {

        SQLTools.StatementFiller sf = pstmt -> {
            pstmt.setDouble(1, x);
            pstmt.setDouble(2, y);
            pstmt.setInt(3, id);
        };

        SQLTools.ResultSetProcessor rp = rs -> {
        };

        try {
            SQLTools.doPreparedStatement(db,
                    "UPDATE Locations SET lat = ?, lng = ? WHERE location_id = ?",
                    sf, rp);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



