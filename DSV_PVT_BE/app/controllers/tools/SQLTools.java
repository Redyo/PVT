package controllers.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.db.Database;
import play.libs.Json;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 2017-04-28.
 */
public class SQLTools {

	/**
	 * @param db    database connection pool
	 * @param query prepared query statment to be filled in by StatementFiller sf
	 * @param sf    fills in the PreparedStatement
	 * @param rp    processes the ResultSet
	 * @throws SQLException if the query, StatementFiller or ResultProcessor was faulty or from a database connection error
	 */
	public static void doPreparedStatement(Database db, String query, StatementFiller sf, ResultSetProcessor rp) throws SQLException {
		Connection conn = db.getConnection();
		PreparedStatement pstmt = null;
		try {
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(query);
			sf.fillStatement(pstmt);
			pstmt.execute();
			rp.processResultSet(pstmt.getResultSet());
			conn.commit();
		} finally {
			// closing connection
			try {
				if (pstmt != null)
					pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public interface StatementFiller {
		void fillStatement(PreparedStatement pstmt) throws SQLException;
	}

	public interface ResultSetProcessor {
		void processResultSet(ResultSet rs) throws SQLException;
	}

	public static JsonNode columnsAndRowsToJSON(ResultSet rs) throws SQLException {
		List<ObjectNode> rows = new ArrayList<>();

		ResultSetMetaData metaData = rs.getMetaData();
		String[] columns = new String[metaData.getColumnCount()];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = metaData.getColumnName(i + 1);
		}

		while (rs.next()) {
			rows.add(generateJsonNodeFromRow(rs, columns));
		}

		return Json.toJson(rows);
	}

	private static ObjectNode generateJsonNodeFromRow(ResultSet rs, String[] cols) throws SQLException {
		ObjectNode node = Json.newObject();

		for (int i = 0; i < cols.length - 1; i++) {
			node.put(cols[i], rs.getString(cols[i]));
		}
		String lastCol = cols[cols.length - 1];
		node.put(lastCol, rs.getString(lastCol));

		return node;
	}
}
