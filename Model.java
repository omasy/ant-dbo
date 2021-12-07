package system.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class Model {
	// HERE WE SET CLASS VARIABLES
	public LinkedHashMap<Integer, Map<String, String>> wheres;
	private java.sql.PreparedStatement statement;
	public HashMap<String, String> joinParam;
	public HashMap<String, String> orders;
	public HashMap<String, String> limit;
	private java.sql.Connection connection;
	public String join = "LEFT JOIN";
	public String primaryKey = "id";
	public boolean fetchMany = true;
	public boolean hasJoin = false;
	public String[] joincolumns;
	public int wheresCount = 0;
	public int ordersCount = 0;
	public int recordID = 0;
	public String[] columns;
	private Builder builder;
	public String table;
		
	// HERE WE CONSTRUCT CLASS
	public Model(String table){
		
		// Here we call set operator
		this.wheres = new LinkedHashMap<Integer, Map<String, String>>();
		this.joinParam = new HashMap<String, String>();
		this.orders = new HashMap<String, String>();
		this.limit = new HashMap<String, String>();
		this.builder = new Builder(table);
		this.table = table;
	}
	
	public Model(HashMap<String, String> connData){
		// Here we call set operator
		this.wheres = new LinkedHashMap<Integer, Map<String, String>>();
		this.joinParam = new HashMap<String, String>();
		this.orders = new HashMap<String, String>();
		this.limit = new HashMap<String, String>();
		// Here we pass Conn Data map to Builder
		if(!connData.isEmpty()){
			this.builder = new Builder(connData);
			if(connData.containsKey("primaryKey")){
				this.primaryKey = connData.get("primaryKey");
			}
		}
	}
	
	
	// HERE WE CONSTRUCT SELECT METHOD
	public Model select(String[] columns){
		// Here we start processing
		if(!ArrayUtils.isEmpty(columns)){
			this.columns = columns;
		}
		
		// Here we return
		return this;
	}
		
	// HERE WE CONSTRUCT SELECT METHOD
	public Model where(Map<String, String> clause){
		// Here we start processing
		if(!clause.isEmpty()){
			clause.put("condition", "AND");
			this.wheresCount = this.wheresCount + 1;
			this.wheres.put(this.wheresCount, clause);
		}
		
		// Here we return
		return this;
	}
		
	// HERE WE CONSTRUCT SELECT METHOD
	public Model orWhere(Map<String, String> clause){
		// Here we start processing
		if(!clause.isEmpty()){
			clause.put("condition", "OR");
			this.wheresCount = this.wheresCount + 1;
			this.wheres.put(this.wheresCount, clause);
		}
		// Here we return
		return this;
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public Model whereKey(int[] ids){
		// Here we set variables
		Map<String, String> param = new HashMap<String, String>();
		// Here we start processing
		if(!ArrayUtils.isEmpty(ids)){
			for(int id : ids){
				param.put("columns", "id");
				param.put("operators", "=");
				param.put("value", String.valueOf(id));
				this.where(param);
			}
		}
		
		// Here we return
		return this;
	}
		
	// HERE WE CONSTRUCT SELECT METHOD
	public Model orderBy(HashMap<String, String> orderMap){
		// Here we start processing
		if(!orderMap.isEmpty()){
			this.orders = orderMap;
		}
		
		// Here we return
		return this;
	}
			
	// HERE WE CONSTRUCT SELECT METHOD
	public ResultSet find(int id, String[] columns){
		// Here we start processing
		return this.whereKey(new int[]{id}).first(columns);
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public ResultSet findMany(int[] ids, String[] columns){
		// Here we start processing
		return this.whereKey(ids).get(columns);
	}
		
	// HERE WE CONSTRUCT SELECT METHOD
	public ResultSet findOrFail(int id, String[] columns){
		// Here we process and return
		return this.find(id, columns);
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public Model inRandomOrder(String[] columns){
		// Here we start processing
		this.orders.put("col", "RAND()");
		this.orders.put("sort", "");
		// Here we set colums
		if(columns != null){
			this.columns = columns;
		}
		
		// Here we return
		return this;
	}
			
	// HERE WE CONSTRUCT SELECT METHOD
	public Model take(int num){
		// Here we start processing
		this.limit(0, num);
		// Here we return
		return this;
	}
			
	// HERE WE CONSTRUCT SELECT METHOD
	public Model limit(int start, int end){
		// Here we start processing
		String st = (start > 0) ? String.valueOf(start) : "";
		String ed = (end > 0) ? String.valueOf(end) : "";
		// Here we add
		this.limit.put("start", st);
		this.limit.put("end", ed);
		
		// Here we return
		return this;
	}

	// HERE WE CONSTRUCT SELECT METHOD
	public Model with(String table, String key){
		// Here we set variable
		HashMap<String, String> joinMap = new HashMap<String, String>();
		BuildProcess process = new QueryBuilder().process();
		String[] colData = {};
		String columnstr = "";
		// Here we start processing
		if(table != null && key != null){
			// Here we bind table profix to join table
			if(!ArrayUtils.isEmpty(this.joincolumns)){
				colData = process.joinFix(this.joincolumns, table);
				columnstr = process.param(this.joincolumns.length, colData);
			}
			
			joinMap.put("table_1", this.table);
			joinMap.put("table_2", table);
			joinMap.put("primaryKey", this.primaryKey);
			joinMap.put("columns", columnstr);
			joinMap.put("matchKey", key);
			// Here we add to the model object
			this.joinParam = joinMap;
			this.hasJoin = true;
		}
		
		// Here we return
		return this;
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public ResultSet first(String[] columns){
		// Here we start processing
		this.fetchMany = false;
		ResultSet rs = this.get(columns);
		// Here we return
		return rs;
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public ResultSet get(String[] columns){
		// Here we start processing
		ResultSet rs = null;
		try {
			rs = this.builder.select(this, columns, this.fetchMany);
			this.statement = this.builder.stmt;
			this.connection = this.builder.conn;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Here we return
		return rs;
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public ResultSet all(){				
		// Here we return
		return this.get(null);
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public int create(LinkedHashMap<String, String> datas){
		// Here we set variable
		int recordID = 0;
		// Here we start processing
		if(!datas.isEmpty()){
			recordID = this.builder.insert(datas);
		}
		
		// Here we return
		return recordID;
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public int update(LinkedHashMap<String, String> datas, int recordID){
		// Here we set variable
		int rows = 0;
		// Here we start processing
		if(!datas.isEmpty()){
			this.recordID = recordID;
			rows = this.builder.update(this, datas);
		}
		
		// Here we return
		return rows;
	}
	
	// HERE WE CONSTRUCT SELECT METHOD
	public int delete(int id){
		// Here we set variable
		int rows = 0;
		// Here we start processing
		if(id > 0){
			this.recordID = id;
			rows = this.builder.delete(this);
		}
		
		// Here we return
		return rows;
	}
			
	// HERE WE CONSTRUCT SELECT METHOD
	public int exists(int id){
		// Setting variables
		int retval = 0;
		// Here we start processing
		if(id > 0){
			try {
				ResultSet record = this.find(id, null);
				if(record.next()){
					retval = 1;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Here we return
		return retval;
	}
			
	// HERE WE CONSTRUCT SELECT METHOD
	public Model min(String column){
		// Here we start processing
		if(column != null){
			this.columns = new String[]{"MIN("+column+") AS minimum"};
		}
		
		// Here we return
		return this;
	}
		
	// HERE WE CONSTRUCT SELECT METHOD
	public Model max(String column){
		// Here we start processing
		if(column != null){
			this.columns = new String[]{"MAX("+column+") AS maximum"};
		}
											
		// Here we return
		return this;
	}
			
	// HERE WE CONSTRUCT SELECT METHOD
	public Model sum(String column){
		// Here we start processing
		if(column != null){
			this.columns = new String[]{"SUM("+column+") AS sum"};
		}	
		
		// Here we return
		return this;
	}
	
	// HERE WE CONSTRUCT JDBC CONNECTION OBJECT CLOSE
	public void close(ResultSet rs){
		// Here we start processing
		try {
			// Here we close resultset
			if(rs != null){
				rs.close();
			}
			
			// Here we close statement
			if(this.statement != null){
				this.statement.close();
			}
			
			// Here we close connection
			if(this.connection != null){
				this.connection.close();
			}
						
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
