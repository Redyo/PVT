//package controllers;
//
//import java.sql.SQLException;
//import java.util.ArrayList;
//import javax.inject.Inject;
//import controllers.tools.SQLTools;
//import play.db.Database;
//import play.mvc.Controller;
//
//
//public abstract class AppController extends Controller {
//	private Database db;
//	private String exceptionMessage;
//	private ArrayList<String> returnData;
//	
//	@Inject
//	public AppController(Database db) {
//		this.db = db;
//		exceptionMessage = "";
//		returnData = new ArrayList<String>();
//	}
//	
//	protected Database getDb() {
//		return db;
//	}
//	
//	protected void setMessage(String str) {
//		exceptionMessage = str;
//	}
//	
//	protected String getMessage() {
//		return "Error: " + exceptionMessage;
//	}
//	
//	protected void addReturnData(String data) {
//		returnData.add(data);
//	}
//
//	protected String getReturnData() {
//		String str = "[";
//
//		if (!returnData.isEmpty()) {
//			for (int i = 0; i < returnData.size(); i++) {
//				str += returnData.get(i);
//				if (i != (returnData.size() - 1))
//					str += ", \n";
//			}
//			returnData.clear();
//			return str + "] ";
//		} else {
//			return "No records found.";
//		}
//
//	}
//	
//	protected boolean executeQuery(String sql, SQLTools.StatementFiller sf, SQLTools.ResultSetProcessor rp) {
//		if (rp == null) {
//			rp = rs -> {
//			};
//		}
//
//		if (sf == null) {
//			sf = pstmt -> {
//			};
//		}
//
//		try {
//			SQLTools.doPreparedStatement(db, sql, sf, rp);
//		} catch (SQLException e) {
//			setMessage(e.toString());
//			return false;
//		}
//
//		return true;
//	}
//}
