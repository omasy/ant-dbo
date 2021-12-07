package system.model;

import java.sql.* ;  // for standard JDBC programs

public class Connection {
	// HERE WE SET CLASS VARIABLES
	private String DB_URL = "jdbc:mysql://localhost/ANTFLUENCER";
	private String USER = "root";
	private String PASS = "717273chukwu";
	
	// HERE WE CONSTRUCT CLASS
	public Connection(){}
	
	// HERE WE CONSTRUCT SET METHOD
	public void url(String uri){
		this.DB_URL = uri;
	}
	
	public void user(String user){
		this.USER = user;
	}
	
	public void pass(String pass){
		this.PASS = pass;
	}
	
	// HERE WE CONSTRUCT THE CONNECT METHOD
	public java.sql.Connection connect(){
		// Here we set variables
		java.sql.Connection conn = null;
		// Open a connection
		try{
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         
		} 
		catch (SQLException e) {
			e.printStackTrace();
		} 
		
		// Here we return
		return conn;
	}
	
	// HERE WE PERFORM ORDINARY STATEMENT
	public ResultSet fetch(java.sql.Connection conn, String query){
		// Here we perform ordinary statement
		// Here we return empty statement for further processing
		ResultSet rs = null;
		try {
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Here we return
		return rs;
	}
	
	// HERE WE PERFORM PREPARED STATEMENT
	public PreparedStatement prepare(java.sql.Connection conn, String query){
		// Here we perform prepared statement
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Here we return
		return stmt;
	}
	
	// HERE WE PERFORM PREPARED STATEMENT
	public CallableStatement prepareCall(java.sql.Connection conn, String query){
		// Here we perform prepared statement
		CallableStatement stmt = null;
		try {
			stmt = conn.prepareCall(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		// Here we return
		return stmt;
	}
	
	// HERE WE CONSTRUCT BIND METHOD
	public int bindUpdate(String[] data, PreparedStatement stmt, int bind, boolean hasID){
		// Here we set variables
		int rows = 0;
		int ret = 0;
		int id = 0;
		int i = 1;
		// Here we start processing
		if(bind > 0 && data.length > 0){
			// Here we make sure data length is upto bind
			if(bind == data.length){
				try {
					for(String d : data){
						// Here we bind
						stmt.setString(i, d);
						// increment
						i++;
					} // end loop
					
					// Here we update rows
					rows = stmt.executeUpdate();
					// Here we ensure record updated
					if (rows > 0) {
						if(hasID == true){
							// Here we get record ID
							ResultSet rs = stmt.getGeneratedKeys();
							if (rs.next()) {
								id = rs.getInt(1);
							}
						}
						
						// Here we check id request
						ret = (hasID == true) ? id : rows;
						
			        }
					else{
						// Here we throw exception
						throw new SQLException("Updating record, no rows affected.");
					}
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		// Here we return
		return ret;
	}
	
	// HERE WE CONSTRUCT BIND METHOD
	public ResultSet bindFetch(String[] data, PreparedStatement stmt, int bind, boolean fetchMany){
		// Here we set variable
		ResultSet rows = null;
		int i = 1;
		// Here we start processing
		if(bind > 0 && data.length > 0){
			// Here we make sure data length is upto bind
			if(bind == data.length){
				try {
					for(String d : data){
						// Here we bind
						stmt.setString(i, d);
						// increment
						i++;
					} // end loop
					
					// Here we limit record amount
					if(fetchMany == false){
						stmt.setMaxRows(1);
					}
					
					// Here we update rows
					rows = stmt.executeQuery();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
			
		// Here we return
		return rows;
	}
	
	// HERE WE CLOSE DATABASE CONNECTIONS
	public void close(java.sql.Connection conn){
		// Here we close connection
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
