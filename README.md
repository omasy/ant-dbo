# ant-dbo
Antware Database Connection Object (Ant DBO) build on top of JDBC and MySQL

SPECIFICATION:
Ant DBO is a repo utility project with classes that extends and implement JDBC; java.sql, meant to manipulate MySQL Database.

Can do:
1. It can connect to MySQL Server
2. Creates and manipulates database records
3. Select from database table(s)
4. Deletes database records from database table
5. Easy to use 

USAGE:
import system.model.*;

Make use you include the ant-dbo.jar in your classpath or reference libraries if your using eclipses as your Editor.

SAMPLE CODES

'''import system.model.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SampleCode {
   public static void main(String[] args){
     Map<String, String> clause = new HashMap<String, String>();
     Map<String, String> clause2 = new HashMap<String, String>();
     HashMap<String, String> clause3 = new HashMap<String, String>();
     clause.put("column", "zip");
     clause.put("operator", "=");
     clause.put("value", "234");
		
     clause2.put("column", "email");
     clause2.put("operator", "=");
     clause2.put("value", "gettrafficworld@yahoo.com");
		
     clause3.put("col", "name");
     clause3.put("sort", "DESC");
		
     Model mod = new Model("users");
     mod.where(clause);
     mod.inRandomOrder(null);
     System.out.println(mod.table);
     ResultSet rs = mod.get(null);
     // Here we display
     try {
	rs.next();
	System.out.println("Email: "+rs.getString("email"));
	mod.close(rs);
     } catch (SQLException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
     }
  }
}'''

DEPENDENCIES:
'''import java.sql.*;'''
