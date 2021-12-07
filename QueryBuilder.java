package system.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class QueryBuilder {
	// HERE WE SET PUBLIC VARIABLE
	private String Table;
	
	// HERE WE CONSTRUCT CLASS
	public QueryBuilder(String table){
		this.Table = table;
	} // Constructor
	
	public QueryBuilder(){} // Constructor
	
	// CONSTRUCTING BUILD INSERT METHOD
	public Map<String, String> buildInsert(String[] fields){
		// Here we set local variables
		Map<String, String> queryMap = new HashMap<String, String>();
		Map<String, String[]> qa = new HashMap<String, String[]>();
		BuildProcess process = this.process();
		String fieldParam = "";
		String valueParam = "";
		String sql = "";
		int bind = 0;
		// Here we start processing
		qa = process.queryBuild(fields, "s[,]", " ");
		fieldParam = qa.get("q")[0];
		bind = Integer.parseInt(qa.get("bc")[0]);
		valueParam = process.param(bind, new String[]{"?"});
		// Here we build the SQL Query
		sql = "INSERT INTO "+this.Table+" ("+fieldParam+") VALUES ("+valueParam+")";
		
		// Here add the map up
		queryMap.put("sql", sql);
		queryMap.put("bind", qa.get("bc")[0]);
		
		// Here we return
		return queryMap;
	}
	
	// CONSTRUCTING BUILD UPDATE METHOD
	public Map<String, String> buildUpdate(String[]fields, boolean hasID){
		// Here we set local variables
		Map<String, String> queryMap = new HashMap<String, String>();
		Map<String, String[]> qa = new HashMap<String, String[]>();
		BuildProcess process = this.process();
		String fieldParam = "";
		String sql = "";
		String sql1 = "";
		String sql2 = "";
		// Here we start processing
		qa = process.queryBuild(fields, "c[,&=]", " ");
		fieldParam = qa.get("q")[0];
		// Here we build the SQL Query
		sql1 = "UPDATE "+this.Table+" SET "+fieldParam+" WHERE id = ? ";
		sql2 = "UPDATE "+this.Table+" SET "+fieldParam;
		sql = (hasID == true) ? sql1 : sql2;
				
		// Here add the map up
		queryMap.put("sql", sql);
		queryMap.put("bind", qa.get("bc")[0]);
		
		// Here we return
		return queryMap;
	}
	
	// CONSTRUCTING COMPLEX BUILD SELECT METHOD
	// HERE WE PUT DESCRIPTION FOR THE SELECT CLAUSE
	// This clause will be a linkedHashMap containing where, order and limit keys
	// Which each keys contains a map of lists, first is where key clause
	// The clause is a hashmap with 4 key names: fields, lclause, rclause, fetch
	// fields is the db fields, lclause is AND OR Conditions, rclause is {=,<,>,Like} Logical
	// operators, fetch is the db fields you want to fetch from the select and its optional
	// Second is order which has leftpos and rightpos, leftpos is column and rightpos is value
	// Third is limit which has start and end ie 1, 10 separated by comma
	public Map<String, String> buildSelect(LinkedHashMap<String, Map<String, String[]>> clause, HashMap<String, String> joinMap, boolean hasJoin){
		// Here we set local variables
		Map<String, String[]> where = new HashMap<String, String[]>();
		Map<String, String[]> order = new HashMap<String, String[]>();
		Map<String, String[]> limit = new HashMap<String, String[]>();
		Map<String, String> queryMap = new HashMap<String, String>();
		Map<String, String[]> qa = new HashMap<String, String[]>();
		BuildProcess process = this.process();
		boolean hasclause = false;
		boolean hasselect = false;
		boolean haswhere = false;
		boolean hasorder = false;
		boolean haslimit = false;
		boolean hasfetch = false;
		String jstatement = "";
		String fieldparam = "";
		String delimiter = "";
		String[] lclause = {};
		String[] rclause = {};
		String[] fields = {};
		String rightcol = "";
		String leftcol = "";
		String columns = "";
		String plclause = "";
		String prclause = "";
		String orderby = "";
		String limitby = "";
		String whereby = "";
		String[] rightpos = {};
		String[] leftpos = {};
		String[] fetch = {};
		String onright = "";
		String onleft = "";
		String select = "";
		String pfetch = "";
		String table1 = "";
		String table2 = "";
		String[] start = {};
		String bind = "0";
		String[] end = {};
		String sql = "";
		
		// Here we start processing
		if(clause != null && clause.size() > 0){
			// Lets get parts of the clause
			where = clause.get("where");
			order = clause.get("order");
			limit = clause.get("limit");
			// Here we get part of where
			if(where.size() > 0){
				if(where.get("fields") != null){
					if(where.get("fields").length > 0){
						fields = where.get("fields");
						lclause = where.get("lclause");
						rclause = where.get("rclause");
						hasclause = true;
					}
				}
				
				if(where.get("fetch") != null){
					if(where.get("fetch").length > 0){
						fetch = where.get("fetch");
						hasselect = true;
					}
				}
				// Here we set where condition
				haswhere = true;
			}
			
			// Here we get part of order
			if(order.size() > 0){
				leftpos = order.get("col");
				rightpos = order.get("sort");
				hasorder = true;
			}
			
			// Here we get part of limit
			if(limit.size() > 0){
				start = limit.get("start");
				end = limit.get("end");
				haslimit = true;
			}
			
			// Here we process individual parts
			// Here we process fields and clauses for table join
			if(hasJoin == true){
				if(!joinMap.isEmpty()){
					// Here we change fields
					// fields = process.joinFix(fields, prefix)
					table1 = joinMap.get("table_1");
					table2 = joinMap.get("table_2");
					onleft = table1+"."+joinMap.get("primaryKey");
					onright = table2+"."+joinMap.get("matchKey");
					// Here we check columns process
					if(joinMap.get("columns").length() > 0 || hasfetch){
						String[] prefData = {};
						String leftp = "";
						if(hasfetch){
							prefData = process.joinFix(fetch, table1);
							leftp = process.param(prefData.length, prefData);
						}
						
						leftcol = (hasfetch) ? leftp : table1+".*";
						rightcol = (joinMap.get("columns").length() > 0) ? joinMap.get("columns") : table2+".*";
					}
					else{
						// Here columns wasnt set
						leftcol = table1+".*";
						rightcol = table2+".*";
					}
					
					// Here we set join statement
					columns = leftcol+", "+rightcol;
					jstatement = " LEFT JOIN "+table2+" ON "+onleft+" = "+onright;
				}
			}
			
			// Here we process fields and clause
			if(haswhere){
				// Here we check clause parts
				// Here we process each
				if(hasclause){
					plclause = (lclause.length > 0) ? process.queryBuild(lclause, "s[.]", "").get("q")[0] : "";
					prclause = (rclause.length > 0) ? process.queryBuild(rclause, "s[.]", "").get("q")[0] : "";
				}
				
				if(hasselect){
					hasfetch = (fetch.length > 0) ? true : false;
				}
				// here we choose fetch
				pfetch = (hasfetch) ? process.select(fetch, process) : "*";
				
				// Here we build sub query of select
				if(plclause.length() > 0 && prclause.length() > 0){
					// Here we set param
					delimiter = "c[{"+plclause+"}&{"+prclause+"}]";
					qa = process.queryBuild(fields, delimiter, " ");
					fieldparam = qa.get("q")[0];
					bind = qa.get("bc")[0];
				}
			}
			
			// Here we process other clauses
			select = (hasJoin == true) ? columns : pfetch;
			whereby = (haswhere) ? process.wheresql(fieldparam) : "";
			orderby = (hasorder) ? process.ordersql(leftpos[0], rightpos[0]) : "";
			limitby = (haslimit) ? process.limitsql(start[0], end[0]) : "";
			
			// Here we build our query for select
			// Here we make sure to check if query has table join order set
			sql = "SELECT "+select+" FROM "+this.Table+jstatement+whereby+orderby+limitby;
			
			// Here add the map up
			queryMap.put("sql", sql.trim());
			queryMap.put("bind", bind);
			System.out.println(sql);
		}
		
		// Here we return
		return queryMap;
	}
	
	// CONSTRUCTING BUILD DELETE METHOD
	public Map<String, String> buildDelete(){
		// Here we set local variables
		Map<String, String> queryMap = new HashMap<String, String>();
		String sql = "";
		// Here we build the SQL Query
		sql = "DELETE FROM "+this.Table+" WHERE id = ? ";
		
		// Here add the map up
		queryMap.put("sql", sql);
		queryMap.put("bind", "1");
				
		// Here we return
		return queryMap;
	}
	
	// CONSTRUCTING THE PROCESS METHOD
	public BuildProcess process(){
		// Here we return
		return new BuildProcess();
	}
	
	
	// End of Class
}

//HERE WE CONSTRUCT THE PROCCESS CLASS
// Delimiter is the query separator like ',' or 'OR | AND | < | > | Like'
// there will be simple and complex delimiter, ie simple: ', | OR | AND | > | < | Like
// Complex delimiters: , AND = (,&=). Simple comes as: s[,] complex: c[,&=] or c[AND&=]
// multi-condition can be part of complex delimiter ie. c[{AND.OR}&=] OR
// c[{AND.OR}&{>.<.Like.=}] - The sets in each condition must match with the record passed
// Here we set main method to test our codes
class BuildProcess{
	// CONSTRUCTOR SET
	BuildProcess(){}
	
	// CONSTRUCTING DATA PROCESSOR
	public String[][] dataProcessor(LinkedHashMap<String, String> record){
		String[][] data = {};
		int size = 0;
		
		// Here we start processing
		if(!record.isEmpty()){
			size = record.size();
			data = new String[2][size];
			int i = 0;
			// Here we loop
			for(String key : record.keySet()){
				data[0][i] = key.toString();
				data[1][i] = record.get(key);
							
				// Increment i
				i++;
			}
					
		}
		
		// Here we return
		return data;
	}
	
	// HERE WE CONSTRUCT QUERY BUILD
	public Map<String, String[]> queryBuild(String[] fields, String delimiter, String spacex){
		Map<String, String[]> builtQuery = new HashMap<String, String[]>();
		boolean isLClause = false;
		boolean isRClause = false;
		String[] deliparts = {};
		boolean isSimple = true;
		String delitype = "";
		String delidata = "";
		String[] dcomplex = {};
		String[] cLArray = {};
		String[] cRArray = {};
		String clauseL = "";
		String clauseR = "";
		String dcomp1 = "";
		String dcomp2 = "";
		// Here we start processing
		if(fields.length > 0 && delimiter != null){
			// Here we check delimiter type
			if(delimiter.indexOf("s[") >= 0 || delimiter.indexOf("c[") >= 0){
				// Here we process delimeter
				deliparts = delimiter.split("\\[");
				delitype = deliparts[0];
				delidata = deliparts[1].replace("]", "");
				dcomp1 = delidata;
				// Lets process delimiter complex if it exists
				if(delitype.equals("c")){
					dcomplex = delidata.split("&");
					dcomp1 = dcomplex[0];
					dcomp2 = dcomplex[1];
					isSimple = false;
				}
				
				// Here we process multi clause condition
				if(dcomp1.indexOf("{") >= 0 && dcomp1.indexOf("}") >= 0){
					clauseL = dcomp1.replace("{", "").replace("}", "");
					cLArray = (clauseL.indexOf(".") >= 0) ? clauseL.split("\\.") : new String[]{clauseL};
					isLClause = true;
				}
				
				if(dcomp2.indexOf("{") >= 0 && dcomp2.indexOf("}") >= 0){
					clauseR = dcomp2.replace("{", "").replace("}", "");
					cRArray = (clauseR.indexOf(".") >= 0) ? clauseR.split("\\.") : new String[]{clauseR};
					isRClause = true;
				}
				
				// Here we check type of processing
				if(isRClause == true || isLClause == true){
					if(isRClause == true && isLClause == false){
						// One has multiple
						builtQuery = this.bind(fields, new String[]{dcomp1}, cRArray, isSimple, spacex);
						
					}
					else{
						// both has multiple
						builtQuery = this.bind(fields, cLArray, cRArray, isSimple, spacex);
					}
					
				}
				else{
					// Here we call bind to build query
					builtQuery = this.bind(fields, new String[]{dcomp1}, new String[]{dcomp2}, isSimple, spacex);
				}
				
			}
			
		}
			
		// Here we return
		return builtQuery;
	}
	
	// HERE WE CONSTRUCT THE INSERT PARAM BUILDER
	public String param(int bind, String[] attribute){
		// Here we set local variable
		String param = "";
		String attr = "";
 		// Here we start processing
		for(int i = 0; i < bind; i++){
			attr = (attribute.length > 1) ? attribute[i] : attribute[0];
			if(param.length() > 0){
				param += ", "+attr+" ";
			}
			else{
				param += " "+attr+" ";
			}
		}
		
		// Here we return
		return param;
	}
	
	// HERE WE CONSTRUCT THE BINDER METHOD
	private Map<String, String[]> bind(String[] data, String[] lClause, String[] rClause, boolean isSimple, String spacex){
		// Here we set concatenation strings
		Map<String, String[]> builtQuery = new HashMap<String, String[]>();
		String queryfield = "";
		String query = "";
		int bind = 0;
		int k1 = 0;
		int k2 = 0;
		int i = 0;
		int j = 0;
		// Here we start processing
		// One has multiple
		for(String d : data){
			k1 = (lClause.length > 1) ? i : j;
			k2 = (rClause.length > 1) ? i : j;
			queryfield = (isSimple == true) ? d : d+" "+rClause[k2]+" ? ";
			if(query.length() > 0){
				query += lClause[k1]+spacex+queryfield;
				// increment bind count
				bind = bind + 1;
			}
			else{
				query += queryfield;
				// increment bind count
				bind = bind + 1;
			}
			
			// here we increment i
			i++;
		}
		
		// Here we create hash map to contain our data
		builtQuery.put("q", new String[]{query});
		builtQuery.put("bc", new String[]{Integer.toString(bind)});
		
		// Here we return
		return builtQuery;
	}
	
	// CONSTRUCTING FETCH PROCESSOR
	public String select(String[] fetch, BuildProcess process){
		// Here we start processing
		String select = (fetch.length > 1) ? process.queryBuild(fetch, "s[,]", " ").get("q")[0] : fetch[0];
		// Here we return
		return select;
	}
	
	// CONSTRUCTING WHERE PROCESS
	public String wheresql(String param){
		// Here we start processing
		String retval = "";
		if(param != null){
			retval = (param.length() > 1) ? " WHERE "+param : "";
		}
		
		// Here we return
		return retval;
	}
		
	// CONSTRUCTING WHERE PROCESS
	public String ordersql(String col, String value){
		// Here we start processing
		String retval = "";
		if(col != null && value != null){
			String cl = (col.length() > 0) ? col+" " : "";
			String vl = (value.length() > 0) ? value : "";
			retval = (col.length() > 0 || value.length() > 0) ? " ORDER BY "+cl+vl : "";
		}
		
		// Here we return
		return retval;
	}
			
	// CONSTRUCTING WHERE PROCESS
	public String limitsql(String start, String end){
		// Here we start processing
		String retval = "";
		if(start != null && end != null){
			String st = (start.length() > 0) ? start+", " : "";
			String ed = (end.length() > 0 ) ? end : "";
			retval = (start.length() > 0 || end.length() > 0) ? " LIMIT "+st+ed : "";
		}
		
		// Here we return
		return retval;
	}
	
	// CONSTRUCTING JOIN PROCESS
	public String[] joinFix(String[] datas, String prefix){
		// Here we set variable
		String[] data = {};
		int i = 0;
		// Here we start processing
		if(!ArrayUtils.isEmpty(datas) && prefix != null){
			data = new String[data.length];
			for(String d : datas){
				data[i] = prefix+"."+d;
				// Here we increment i
				i++;
			}
		}
		// Here we return
		return data;
	}
	
	// End of private class
}
