package system.model;

// import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class Builder {
	// HERE WE SET CLASS VARIABLES
	public java.sql.PreparedStatement stmt;
	public java.sql.Connection conn;
	public Connection connection;
	public QueryBuilder builder;
	public BuildProcess process;
	
	// HERE WE CONSTRUCT CLASS
	public Builder(String table){
		// Here we set class variable values
		this.connection = new Connection();
		this.builder = new QueryBuilder(table);
		this.process = this.builder.process();
	}
	
	// HERE WE CONSTRUCT CLASS
	// THIS CONSTRUCTOR TAKES IN A HASHMAP THAT SETS CONNECTION DATAS
	public Builder(HashMap<String, String> conMap){
		// Here we set class variable values
		// Here we check if map is not empty
		if(!conMap.isEmpty()){
			this.builder = new QueryBuilder(conMap.get("table"));
			this.process = this.builder.process();
			// Here we set connection datas
			this.connection = new Connection();
			// Here we set the connection strings
			this.connection.url(conMap.get("url"));
			this.connection.user(conMap.get("username"));
			this.connection.pass(conMap.get("password"));
		}
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public ResultSet select(Model mod, String[] columns, boolean fetchMany) throws SQLException{
		// Here we set variables
		LinkedHashMap<String, Map<String, String[]>> clause = new LinkedHashMap<String, Map<String, String[]>>();
		Map<String, String[]> compose = new HashMap<String, String[]>();
		Map<String, String[]> where = new HashMap<String, String[]>();
		Map<String, String[]> order = new HashMap<String, String[]>();
		Map<String, String[]> limit = new HashMap<String, String[]>();
		Map<String, String> sqlMap = new HashMap<String, String>();
		DataClause process = new DataClause();
		// java.sql.Connection conn = null;
		// PreparedStatement stmt = null;
		ResultSet record = null;
		String[] values = {};
		String[] cols = {};
		String sql = "";
		int bind = 0;
		
		// Here we start processing
		cols = (columns != null) ? columns : mod.columns;
		compose = process.compose(mod.wheres);
		values = compose.get("values");
		// Here we set clause
		where.put("fields", compose.get("columns"));
		where.put("lclause", compose.get("conditions"));
		where.put("rclause", compose.get("operators"));
		where.put("fetch", cols);
		
		// Here we process other clauses
		if(mod.orders.size() > 0){
			order.put("col", new String[]{mod.orders.get("col")});
			order.put("sort", new String[]{mod.orders.get("sort")});
		}
		
		if(mod.limit.size() > 0){
			limit.put("start", new String[]{mod.limit.get("start")});
			limit.put("end", new String[]{mod.limit.get("end")});
		}
		
		// Here we add up clause for query
		clause.put("where", where);
		clause.put("order", order);
		clause.put("limit", limit);
		
		sqlMap = this.builder.buildSelect(clause, mod.joinParam, mod.hasJoin);
		bind = Integer.parseInt(sqlMap.get("bind"));
		sql = sqlMap.get("sql");
		this.conn = this.connection.connect();
		// Here we check type to process
		if(bind > 0){
			this.stmt = this.connection.prepare(this.conn, sql);
			record = this.connection.bindFetch(values, this.stmt, bind, fetchMany);
		}
		else{
			record = this.connection.fetch(this.conn,  sql);
		}
		
		// Here we return
		return record;
	}
		
	// HERE WE CONSTRUCT SELECT METHOD
	public int insert(LinkedHashMap<String, String> datas){
		// Here we set variables
		Map<String, String> sqlMap = new HashMap<String, String>();
		// java.sql.Connection conn = null;
		// PreparedStatement stmt = null;
		String[][] data = {};
		String[] fields = {};
		String[] values = {};
		int recordID = 0;
		
		// Here we start processing
		data = this.process.dataProcessor(datas);
		if(!ArrayUtils.isEmpty(data)){
			fields = data[0];
			values = data[1];
			sqlMap = this.builder.buildInsert(fields);
			this.conn = this.connection.connect();
			this.stmt = this.connection.prepare(this.conn, sqlMap.get("sql"));
			// Here lets bind
			recordID = this.connection.bindUpdate(values, this.stmt, Integer.parseInt(sqlMap.get("bind")), true);
		}
		
		// Here we return
		return recordID;
	}

	// HERE WE CONSTRUCT SELECT METHOD
	public int update(Model mod, LinkedHashMap<String, String> datas){
		// Here we set variables
		Map<String, String> sqlMap = new HashMap<String, String>();
		// java.sql.Connection conn = null;
		// PreparedStatement stmt = null;
		String[][] data = {};
		String[] fields = {};
		String[] values = {};
		boolean hasID = false;
		int row = 0;
				
		// Here we start processing
		data = this.process.dataProcessor(datas);
		if(!ArrayUtils.isEmpty(data)){
			fields = data[0];
			values = data[1];
			// Here we set up the recod ID and add to value
			if(mod.recordID > 0){
				List<String> lvalues = Arrays.asList(values);
				lvalues.add(String.valueOf(mod.recordID));
				values = lvalues.toArray(new String[lvalues.size()]);
				hasID = true;
			}
			
			// Here we build query and execute
			sqlMap = this.builder.buildUpdate(fields, hasID);
			this.conn = this.connection.connect();
			this.stmt = this.connection.prepare(this.conn, sqlMap.get("sql"));
			
			// Here lets bind
			row = this.connection.bindUpdate(values, this.stmt, Integer.parseInt(sqlMap.get("bind")), false);
		}
		
		// Here we return
		return row;
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public int delete(Model mod){
		// Here we set variables
		Map<String, String> sqlMap = new HashMap<String, String>();
		// java.sql.Connection conn = null;
		// PreparedStatement stmt = null;
		String[] values = {};
		int row = 0;
		
		// Here we start processing
		if(mod.recordID > 0){
			sqlMap = this.builder.buildDelete();
			this.conn = this.connection.connect();
			this.stmt = this.connection.prepare(this.conn, sqlMap.get("sql"));
			values = new String[]{String.valueOf(mod.recordID)};
			// Here lets bind
			row = this.connection.bindUpdate(values, this.stmt, Integer.parseInt(sqlMap.get("bind")), false);
		}
		
		// Here we return
		return row;
	}
	

	// End of class
}



// HERE WE CONSTRUCT CLASS FOR BUILDING DATA CLAUSE
class DataClause{
	// HERE WE CONSTRUCT
	public DataClause(){}
	
	// HERE WE CONSTRUCT DATA COMPOSE METHOD
	public Map<String, String[]> compose(LinkedHashMap<Integer, Map<String, String>> clauseMap){
		// Here we set variables
		Map<String, String[]> clause = new HashMap<String, String[]>();
		String[] conditions = {};
		String[] operators = {};
		String[] fields = {};
		String[] values = {};
		int size = 0;
		int i = 0;
		
		// Here we start processing
		if(!clauseMap.isEmpty()){
			// Here we set array sizes
			size = clauseMap.size();
			conditions = new String[size];
			operators = new String[size];
			fields = new String[size];
			values = new String[size];
			// Here we loop
			for(Map.Entry<Integer, Map<String, String>> entry : clauseMap.entrySet()){
				Map<String, String> clMap = entry.getValue();
				fields[i] = clMap.get("column");
				operators[i] = clMap.get("operator");
				values[i] = clMap.get("value");
				conditions[i] = clMap.get("condition");
				// Here we increment
				i++;
			}
			
			// Here we create the parsed record map
			clause.put("columns", fields);
			clause.put("operators", operators);
			clause.put("values", values);
			clause.put("conditions", conditions);
		}
		
		// Here we return
		return clause;
	}
	
	
	// End of class
}
