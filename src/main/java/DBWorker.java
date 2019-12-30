import java.sql.*;
import java.util.ArrayList;

public class DBWorker {
    Connection connection;
    Statement statement;
    ResultSet resultSet;
    private String DBurl;
    private String DBName;
    private String DBpassword;

    public DBWorker(){
         this.DBurl = "jdbc:mysql://localhost/telegramBot?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
         this.DBName = "root";
         this.DBpassword = "050493";
         createConnection();
    }

    public void createConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(DBurl, DBName, DBpassword);
        } catch (SQLException e) {
            closeConnection();
            e.printStackTrace();
        }
    }


    private void createStatement(){

        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException e1) {
                closeConnection();
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public int insertRecord(ArrayList columnNames, ArrayList columnValues, String tableName){
        createStatement();
        String colNames="";
        String colValues="";
        String insert;
        int createdRowId = 0;
        if(columnNames.size() != columnValues.size()){
            System.out.println("Метод insertRecord. Передали разное количество данных для операции Insert");
        }else{
            for(int i=0; i<columnNames.size(); i++){
                colNames = colNames + columnNames.get(i) + ",";
                colValues = colValues + columnValues.get(i) + ",";
            }
        }

        insert = "INSERT INTO " + tableName + " (" + colNames.substring(0, colNames.length()-1) + ") VALUES (" + colValues.substring(0, colValues.length()-1) + ");";

        PreparedStatement ps;
        try {
            ps = connection.prepareStatement(insert,
                    Statement.RETURN_GENERATED_KEYS);
            ps.execute();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                createdRowId = rs.getInt(1);
                System.out.println(createdRowId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return createdRowId;
    }

    public Integer isHaveSelectedRecord(String selectString){
        createStatement();
        int row_id=0;
        try {
            resultSet = statement.executeQuery(selectString);
            if(resultSet.next()){
                row_id = resultSet.getInt("ROW_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            //isRecords = false;
        }finally {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return row_id;
    }

    public ResultSet selectRecord(String selectString){

        createStatement();
        try {
            resultSet = statement.executeQuery(selectString);
        } catch (SQLException e) {
            e.printStackTrace();
            closeConnection();
        }
        return resultSet;
    }

    public void updateRecord(String updateString){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(updateString);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void closeConnection(){
        try {
            connection.close();
            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}

