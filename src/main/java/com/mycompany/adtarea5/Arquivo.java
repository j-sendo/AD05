/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.adtarea5;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author José Rosendo
 */
public class Arquivo {
    int id;
    String nome;
    Directorio directorio;
    byte[] binario;

    public Arquivo() {
    }

    public Arquivo(int id, String nome, Directorio directorio) {
        this.id = id;
        this.nome = nome;
        this.directorio = directorio;
    }

    public Arquivo(String nome, Directorio directorio) {
        this.nome = nome;
        this.directorio = directorio;
        
    }
    public Arquivo(String nome, Directorio directorio, byte[] binario) {
        this.nome = nome;
        this.directorio = directorio;
        this.binario = binario;
    }

    public Arquivo(int id, String nome, Directorio directorio, byte[] binario) {
        this.id = id;
        this.nome = nome;
        this.directorio = directorio;
        this.binario = binario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Directorio getDirectorio() {
        return directorio;
    }

    public void setDirectorio(Directorio directorio) {
        this.directorio = directorio;
    }

    public byte[] getBinario() {
        return binario;
    }

    public void setBinario(byte[] binario) {
        this.binario = binario;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Arquivo other = (Arquivo) obj;
        if (!Objects.equals(this.nome, other.nome)) {
            return false;
        }
        if (!Objects.equals(this.directorio, other.directorio)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Arquivo{" + "id=" + id + ", nome=" + nome + ", directorio=" + directorio + ", binario=" + binario + '}';
    }
    public void insertarBd(Connection c) throws SQLException{
        ByteArrayInputStream bais=new ByteArrayInputStream(binario);
        
        PreparedStatement sentenciaInsercion=c.prepareStatement("INSERT INTO arquivo(nome,id_directorio,binario) VALUES (?,?,?);");
        sentenciaInsercion.setString(1, nome);
        sentenciaInsercion.setInt(2, directorio.getId());
        sentenciaInsercion.setBinaryStream(3, bais);
                
        sentenciaInsercion.execute();
        System.out.println("Engadiuse o arquivo "+this.getDirectorio().getNome()+this.nome+" na base de datos.");
        sentenciaInsercion.close();
    }
    //Crear arquivo no directorio
    public boolean crearArquivo(String raiz){
        FileOutputStream fOS=null;
        File f=new File(raiz+directorio.getNomeSinPuntoAdaptadoSo(),nome);
        try { 
            if (!f.exists()){
            fOS = new FileOutputStream(f);
            fOS.write(binario);
            fOS.close();
            return true;
            }
            else return false;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Arquivo.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(Arquivo.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } 
        
    }
    
}
