/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.adtarea5;

import configuracion.Configuracion;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author José Rosendo
 */
public class ConexionBd {
    private Properties propiedades;
    String url;
    Connection conexion;

    public Connection getConexion() {
        return conexion;
    }

    public static final String[] SENTENCIAS_CREACION={
        "create table if not exists directorio (id SERIAL primary key,nome varchar unique not null);",
        "create table if not exists arquivo (id SERIAL primary key,nome varchar not null,id_directorio INTEGER not null references directorio(id)  on delete cascade on update CASCADE,"
	+"binario bytea not null,constraint arquivounico UNIQUE(nome,id_directorio));",
        "create or replace function notificar_arquivo_novo()"+
	"returns trigger as $$"+
	"begin "+
		"perform pg_notify('novoarquivo',new.id::text);"+
		"return new;"+
	"end;"+
	"$$ language plpgsql;",
        "drop trigger if exists notif_archivo_nuevo on arquivo;\n" +
        "create trigger notif_archivo_nuevo\n" +
        "	after insert on arquivo for each row\n" +
        "execute procedure notificar_arquivo_novo();"
    };
    
    public ConexionBd(Configuracion conf, boolean inicial)  {
        try {
            propiedades=new Properties();
            url=conf.getUrl();
            propiedades.put("user", conf.getDbConnection().getUser());
            propiedades.put("password", conf.getDbConnection().getPassword());
            conexion=DriverManager.getConnection(url, propiedades);
           
            if (inicial){
                Statement sentencia=conexion.createStatement();
                for (String s: SENTENCIAS_CREACION){
                    sentencia.execute(s);
                }

                sentencia.close();
            }
        } catch (SQLException ex) {
                System.err.println("Non se puido conectar coa BD, revise os datos do ficheiro de configuración .json, debe exister a bd e ter comunicación co servidor.");
                //ex.printStackTrace();
                System.exit(0);     
        }
    }
    public void cerrarConexion() throws SQLException{
        conexion.close();
    }
    
    //
    public void insertarDirectorio(Directorio dir){
        try {
            dir.insertarBd(conexion);
        } catch (SQLException ex) {
            if (ex.getMessage().contains("llave duplicada")) System.out.println("Xa existe o directorio "+dir.getNomeAdaptadoSo()+", non se engadirá a bd.");
            //Logger.getLogger(ConexionBd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Directorio getDirectorioBd(Directorio dir){
        Directorio dirTmp=null;
        try {
            PreparedStatement sentenciaConsulta=conexion.prepareStatement("SELECT * FROM directorio WHERE nome=?");
            sentenciaConsulta.setString(1, dir.getNomeAdaptadoBd());
            ResultSet resultado=sentenciaConsulta.executeQuery();
            if (resultado.next()){
                dirTmp=new Directorio(resultado.getInt("id"),resultado.getString("nome"));
                
            }
           
        } catch (SQLException ex) {
            Logger.getLogger(ConexionBd.class.getName()).log(Level.SEVERE, null, ex);
        }
         return dirTmp;
    }
    public ArrayList<Directorio> getDirectorios(){
        ArrayList<Directorio> directorios=new ArrayList<Directorio>();
        int id;
        String nome;
        try {
            PreparedStatement sentenciaConsulta=conexion.prepareStatement("SELECT * FROM directorio ORDER BY nome");
            ResultSet resultados=sentenciaConsulta.executeQuery();
            while(resultados.next()){
                id=resultados.getInt("id");
                nome=resultados.getString("nome");
                directorios.add(new Directorio(id,nome));
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConexionBd.class.getName()).log(Level.SEVERE, null, ex);
        }
        return directorios;
    }
    
    //
    public void insertarArquivo(Arquivo ar){
        try {
            ar.insertarBd(conexion);
        } catch (SQLException ex) {
           
            if (ex.getMessage().contains("llave duplicada")) System.out.println("Xa existe o arquivo "+ar.getDirectorio().getNomeAdaptadoSo()+File.separatorChar+ar.nome+", non se engadirá a bd.");
            //Logger.getLogger(ConexionBd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean existeArquivoBd(Arquivo arquivo){
        try {
            PreparedStatement consultaExisteArquivo=conexion.prepareStatement("SELECT * FROM Arquivo  WHERE nome=? AND id_directorio=?");
            consultaExisteArquivo.setString(1, arquivo.nome);
            consultaExisteArquivo.setInt(2, this.getDirectorioBd(arquivo.getDirectorio()).getId());
            return consultaExisteArquivo.executeQuery().next();
        } catch (SQLException ex) {
            Logger.getLogger(ConexionBd.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public ArrayList<Arquivo> getArquivos(Directorio d){
        ArrayList<Arquivo> arquivos=new ArrayList<Arquivo>();
        int id,idDirec=d.getId();
        String nome;
        
        try {
            PreparedStatement sentenciaConsulta=conexion.prepareStatement("SELECT * FROM arquivo WHERE id_directorio=? ORDER BY nome");
            sentenciaConsulta.setInt(1, idDirec);
            ResultSet resultados=sentenciaConsulta.executeQuery();
            while(resultados.next()){
                
                id=resultados.getInt("id");
                nome=resultados.getString("nome");
                Arquivo tmp=new Arquivo(id,nome,d);
                tmp.setBinario(resultados.getBytes("binario"));
                arquivos.add(tmp);
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConexionBd.class.getName()).log(Level.SEVERE, null, ex);
        }
        return arquivos;
    }
    public Arquivo getArquivoById(int id){
        Arquivo arquivo=null;
        
        try {
            PreparedStatement sentencia=conexion.prepareStatement("SELECT * FROM arquivo WHERE arquivo.id=?");
            sentencia.setInt(1, id);
            ResultSet rs=sentencia.executeQuery();
            while(rs.next()){
                arquivo=new Arquivo(id,rs.getString("nome"),this.getDirById(rs.getInt("id_directorio")),rs.getBytes("binario"));
            }
            
                    } catch (SQLException ex) {
            Logger.getLogger(ConexionBd.class.getName()).log(Level.SEVERE, null, ex);
        }
        return arquivo;
    }
    //
    public Directorio getDirByNome(String nome){
        nome=nome.replace(File.separatorChar, '/');
        Directorio dirTmp=null;
        int id;
        String nomeDir;
        try {
            PreparedStatement sentenciaConsulta=conexion.prepareStatement("SELECT * FROM directorio WHERE nome=?");
            sentenciaConsulta.setString(1, nome);
            ResultSet resultado=sentenciaConsulta.executeQuery();
            if (resultado.next()){
                id=resultado.getInt("id");
                nomeDir=resultado.getString("nome");
                dirTmp=new Directorio(id,nomeDir);
            }
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(ConexionBd.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dirTmp;
    }
    public Directorio getDirById(int id){
        Directorio dirTmp=null;
        
        String nomeDir;
        try {
            PreparedStatement sentenciaConsulta=conexion.prepareStatement("SELECT * FROM directorio WHERE id=?");
            sentenciaConsulta.setInt(1, id);
            ResultSet resultado=sentenciaConsulta.executeQuery();
            if (resultado.next()){
                id=resultado.getInt("id");
                nomeDir=resultado.getString("nome");
                dirTmp=new Directorio(id,nomeDir);
            }
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(ConexionBd.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dirTmp;
 
    }
    

    
}
