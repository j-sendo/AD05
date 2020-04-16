/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.adtarea5;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

/**
 *
 * @author José Rosendo
 */
public class NovoArquivoListener extends Thread{
    private ConexionBd conexionBd;
    private PGConnection pgconexion;
    private PGNotification notificacions[];
    File raiz;
    public NovoArquivoListener(ConexionBd conexionBd, File raiz) {
        try {
            this.conexionBd=conexionBd;
            this.raiz=raiz;
            pgconexion=conexionBd.getConexion().unwrap(PGConnection.class);
            Statement sentencia=conexionBd.getConexion().createStatement();
            sentencia.execute("LISTEN novoarquivo");
            sentencia.close();
        } catch (SQLException ex) {
            Logger.getLogger(NovoArquivoListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            
            while(true){   
                Thread.sleep(20000);
                System.out.println("Iniciase revisión de novos arquivos na base de datos e creación na carpeta local.");
                notificacions=pgconexion.getNotifications();
                if (notificacions!=null){               
                    for (int i=0;i<notificacions.length;i++){
                        
                        int id=Integer.parseInt(notificacions[i].getParameter());
                        Arquivo arquivoNovo=conexionBd.getArquivoById(id);
                        //
                        arquivoNovo.getDirectorio().crearDirec(raiz.getAbsolutePath());
                        if (arquivoNovo.crearArquivo(raiz.getAbsolutePath())) System.out.println("Descargouse o arquivo: "+arquivoNovo.getNome()+" na carpeta: "+arquivoNovo.getDirectorio().getNome());;

                    }
                }
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(NovoArquivoListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(NovoArquivoListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
