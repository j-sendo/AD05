/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configuracion;

/**
 *
 * @author José Rosendo
 */
public class Configuracion {
    private Conexion dbConnection;
    private App app;

    public String getUrl(){
        String url="jdbc:postgresql://"+dbConnection.getAddress()+"/"+dbConnection.getName();
        
        return url;
    }
    public Configuracion() {
    }

    public Configuracion(Conexion dbConnection, App app) {
        this.dbConnection = dbConnection;
        this.app = app;
    }

    public Conexion getDbConnection() {
        return dbConnection;
    }

    public void setDbConnection(Conexion dbConnection) {
        this.dbConnection = dbConnection;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @Override
    public String toString() {
        return "Configuracion{" + "dbConnection=" + dbConnection + ", app=" + app + '}';
    }
    
}
