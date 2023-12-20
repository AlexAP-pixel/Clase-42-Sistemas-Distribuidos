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

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.io.InputStream;  
import java.util.StringTokenizer;

import com.fasterxml.jackson.databind.DeserializationFeature;   
import com.fasterxml.jackson.databind.ObjectMapper;             
import com.fasterxml.jackson.databind.PropertyNamingStrategy;   

public class WebServer {
   
    private static final String STATUS_ENDPOINT = "/status";
    private static final String HOME_PAGE_ENDPOINT = "/";
    private static final String HOME_PAGE_UI_ASSETS_BASE_DIR = "/ui_assets/";
    private static final String ENDPOINT_PROCESS = "/procesar_datos";

    private final int port; 
    private HttpServer server; 
    private final ObjectMapper objectMapper;

    public WebServer(int port) {
        this.port = port;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT); 
        HttpContext taskContext = server.createContext(ENDPOINT_PROCESS);
        HttpContext homePageContext = server.createContext(HOME_PAGE_ENDPOINT);
        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);
        homePageContext.setHandler(this::handleRequestForAsset);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleRequestForAsset(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        byte[] response;

        String asset = exchange.getRequestURI().getPath(); 
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");


        if (asset.equals(HOME_PAGE_ENDPOINT)) { 
            response = readUiAsset(HOME_PAGE_UI_ASSETS_BASE_DIR + "index.html");
        } else if (asset.equals("/favicon.ico")) {
            response = readUiAsset(HOME_PAGE_UI_ASSETS_BASE_DIR + "favicon.ico");
        }
        else {
            response = readUiAsset(asset); 
        }
        addContentType(asset, exchange);
        sendResponse(response, exchange);

        System.out.println("handleRequestForAsset called with method: " + exchange.getRequestMethod()); // Se imprime el método de la petición
        System.out.println("Content-Type of incoming request for asset: " + contentType); // Se imprime el contentType
        System.out.println("Asset: " + asset); // Se imprime el asset


    }

    private byte[] readUiAsset(String asset) throws IOException {
        InputStream assetStream = getClass().getResourceAsStream(asset); // Se obtiene el recurso como un stream de bytes 

        if (assetStream == null) {
            return new byte[]{};
        }
        return assetStream.readAllBytes(); 
    }

    private static void addContentType(String asset, HttpExchange exchange) {

        String contentType = "text/html";  
        if (asset.endsWith("js")) {
            contentType = "text/javascript";
        } else if (asset.endsWith("css")) {
            contentType = "text/css";
        } else if (asset.endsWith("ico")) { // Agregamos para manejar .ico
            contentType = "image/x-icon";
        } 
        exchange.getResponseHeaders().add("Content-Type", contentType);
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) { 
            exchange.close();
            return;
        }

        try {
            FrontendSearchRequest frontendSearchRequest = objectMapper.readValue(exchange.getRequestBody().readAllBytes(), FrontendSearchRequest.class);  // Se obtiene el cuerpo de la petición y se convierte a bytes
            // readValue() convierte el JSON en un objeto de la clase FrontendSearchRequest 
            String frase = frontendSearchRequest.getSearchQuery(); // Se obtiene la frase
            StringTokenizer st = new StringTokenizer(frase);
            FrontendSearchResponse frontendSearchResponse = new FrontendSearchResponse(frase, st.countTokens()); // Se crea una instancia de la clase FrontendSearchResponse con la frase y la cantidad de palabras
            byte[] responseBytes = objectMapper.writeValueAsBytes(frontendSearchResponse); // Se convierte a bytes
            sendResponse(responseBytes, exchange);
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            System.out.println("Content-Type of incoming request: " + contentType); // Se imprime el contentType


        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        System.out.println("handleTaskRequest called with method: " + exchange.getRequestMethod()); // Se imprime el método de la petición
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = "El servidor está vivo\n";
        sendResponse(responseMessage.getBytes(), exchange);
        System.out.println("handleStatusCheckRequest called with method: " + exchange.getRequestMethod()); // Se imprime el método de la petición
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        System.out.println("sendResponse size: " + responseBytes.length); // Se imprime el largo de la respuesta

        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }
}


