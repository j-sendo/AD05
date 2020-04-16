/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.adtarea5;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import configuracion.Configuracion;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author José Rosendo
 */
public class Main {
    static Configuracion leerConfJson(File fichConfJson){
        Configuracion configuracion=null;
        Gson GSON=new GsonBuilder().setPrettyPrinting().create();
        BufferedReader bR;
        try {
            bR = new BufferedReader(new FileReader(fichConfJson));
            configuracion=GSON.fromJson(bR, Configuracion.class);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return configuracion; 
    }
    public static void main(String[] args){
        File fichConfJson=new File("minidrive.json");
        Configuracion configuracionBd=leerConfJson(fichConfJson);
        
        //Conexión BD
        ConexionBd conexionBd=new ConexionBd(configuracionBd,true);
        
        //Raiz
        File raiz=new File(configuracionBd.getApp().getDirectory());
        if (!raiz.exists()){
            System.err.println("O directorio "+raiz+" non existe. Corrixe o directorio no .json.");
            System.exit(0);
        }
        Directorio dirRaiz=new Directorio(".");
        conexionBd.insertarDirectorio(dirRaiz);
        
        System.out.println("Sincronización inicial da base de datos co contido da carpeta.");
        //Recorrer directorio e insertar en BD
        recorrerRuta(null,raiz,conexionBd);
         
        //Recorrer BD e crear no sistema de arquivos
        sincronizarDirectorio(raiz,conexionBd);
        
        //Recorrer directorio e insertar en BD cada 15 segundos.
        new Thread(new Runnable(){
            @Override
            public void run() {
                ConexionBd conexionBd2=new ConexionBd(configuracionBd,false);
                while(true){
                    try {
                        Thread.sleep(15000);
                        System.out.println("\nSincronización da base de datos co contido da carpeta raiz.");
                        Main.recorrerRuta(null,raiz,conexionBd2);
                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }            
        }).start();
        
        //Consultar BD por se existen novos arquivos cada 20 segundos.
        NovoArquivoListener nAL=new NovoArquivoListener(new ConexionBd(configuracionBd,false),raiz);
        nAL.start();
  
    }
    public static void sincronizarDirectorio(File raiz, ConexionBd conexion){
        System.out.println("\nIníciase sincronización do directorio co contido da base de datos.");
        ArrayList<Directorio> directorios=conexion.getDirectorios();
        for (Directorio d: directorios){
            d.crearDirec(raiz.getAbsolutePath());
            ArrayList<Arquivo> arquivos=conexion.getArquivos(d);
            for (Arquivo a:arquivos){
                a.crearArquivo(raiz.getAbsolutePath());
            }
        }  
    }
    
    
    public static void recorrerRuta(File[] contido, File raiz, ConexionBd conexion){
        if (contido==null) contido=raiz.listFiles();
        for (File f:contido){
            if (f.isDirectory()){  
                String rutaDirectorio="."+f.getPath().replace(raiz.getPath(),"");
                Directorio dirTmp=new Directorio(rutaDirectorio);
                
                conexion.insertarDirectorio(dirTmp);
                recorrerRuta(f.listFiles(),raiz,conexion);
            }
            else{
                FileInputStream fis=null;
                try {
                    String rutaDirectorio="."+f.getParent().replace(raiz.getPath(),"");
                    Directorio dirTmp=conexion.getDirByNome(rutaDirectorio);
                    
                    Arquivo arquivoTmp=new Arquivo(f.getName(),dirTmp);
                    
                    if (!conexion.existeArquivoBd(arquivoTmp)){
                        fis = new FileInputStream(f);
                        ByteArrayOutputStream baos=new ByteArrayOutputStream();
                        int leido=fis.read();
                    
                        while (leido!=-1){
                            baos.write(leido);
                            leido=fis.read();
  
                        }
                        byte[] arquivoBin=baos.toByteArray();
                    
                        fis.close();
                        arquivoTmp.setBinario(arquivoBin);
                        conexion.insertarArquivo(arquivoTmp);
                    } else System.out.println("Xa existe o arquivo "+arquivoTmp.getDirectorio().getNomeAdaptadoSo()+File.separatorChar+arquivoTmp.nome+", non se engadirá a bd.");
                    
                    
                    
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } finally {

                }
                
            }
            
        }
    }
}
