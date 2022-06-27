package com.meinc.jdbc;

import java.sql.SQLException;

public class SQLError {
    public static boolean isTransactionLost(SQLException e) {
        String state = e.getSQLState();
        return state.equals("40001")
                || state.equals("HY000")
                || state.startsWith("08")
                || state.startsWith("5") 
                || state.startsWith("6") 
                || state.startsWith("7") 
                || state.startsWith("8") 
                || state.startsWith("9");
    }
}
