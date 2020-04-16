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
public class App {
    private String directory;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public App() {
    }

    public App(String directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return "App{" + "directory=" + directory + '}';
    }
}
