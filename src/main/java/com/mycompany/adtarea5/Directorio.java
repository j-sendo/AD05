/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.adtarea5;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

/**
 *
 * @author José Rosendo
 */
public class Directorio {
    int id;
    String nome;

    public Directorio(String nome) {
        this.nome = nome;
    }

    public Directorio(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final Directorio other = (Directorio) obj;
        if (!Objects.equals(this.getNomeAdaptadoBd(), other.getNomeAdaptadoBd())) {
            return false;
        }
        return true;
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
    public String getNomeAdaptadoSo() {
        return nome.replace('/', File.separatorChar);
    }
    public String getNomeAdaptadoBd() {
        return nome.replace(File.separatorChar,'/');
    }
    public String getNomeSinPunto() {
        return nome.replaceFirst(".", "");
    }
    public String getNomeSinPuntoAdaptadoSo() {
        return nome.replaceFirst(".", "").replace('/', File.separatorChar);
        
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return "Directorio{" + "id=" + id + ", nome=" + nome + '}';
    }
    
    public File getFile(File raiz){
        return new File(raiz,nome);
    }
    public void insertarBd(Connection c) throws SQLException{
        PreparedStatement sentenciaInsercion=c.prepareStatement("INSERT INTO directorio(nome) VALUES (?);");
        sentenciaInsercion.setString(1, this.getNomeAdaptadoBd());
        sentenciaInsercion.execute();
        System.out.println("Engadiuse o directorio "+this.getNomeAdaptadoBd()+" na base de datos.");
        sentenciaInsercion.close();
    }
    public boolean crearDirec(String raiz){
        File f=new File(raiz,this.getNomeSinPuntoAdaptadoSo());
        return f.mkdirs();
    }

}
