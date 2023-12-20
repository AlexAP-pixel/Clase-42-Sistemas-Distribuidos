/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.mycompany.app; 

import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException, InterruptedException {
        int currentServerPort = 3000; // Puerto por defecto
        if (args.length == 1) { // Si se pasa un argumento, se toma como puerto
            currentServerPort = Integer.parseInt(args[0]); // Se convierte a entero
        }
        App application = new App(); // Se crea una instancia de la clase 

        WebServer webServer = new WebServer(currentServerPort); // Se crea una instancia de la clase WebServer
        webServer.startServer(); // Se inicia el servidor

        System.out.println("Servidor escuchando en el puerto: " + currentServerPort); // Se imprime el puerto en el que se está escuchando
    }
}