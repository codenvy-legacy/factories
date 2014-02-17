package com.codenvy.factories;

import java.lang.StringBuilder;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

import java.util.Map;
import java.util.HashMap;

public class GenerateFactoryFiles 
{

    private static String rest_url_file = null;
    private static String factory_file = null;
    private static String html_file = null;

    public static void main( String[] args ) {

        // Usage: GenerateFactoryFiles name
        // This app will append .endpoint to the file to be read.
        // Will create name.factory.
        // Will create name.html.

        // Load string from argument
        // Call REST API from String
        // Get result and place into JSON object
        // Parse JSON object
        String rest_url = args[0];
        
        rest_url_file = rest_url + ".endpoint";
        factory_file = rest_url + ".factory";
        html_file = rest_url + ".html";

        // 1) Get root node.
        // 2) Find all of its children in loaded commands.
        // 3) Add the child to the parent
        // 4) Repeat for each child iteratively.
        callFactoryRESTFromFile(rest_url_file);

     }

     
    public static String callFactoryRESTFromFile(String input_file) {

        /*
         * STEP 1: Search for the file specified by --in
         *         If valid file, then call REST API with the string
         */
        String rest_url = null;

        if (input_file == null) {
            System.out.println("No input file");
            System.exit(0);
        } else {
            rest_url = readFile(input_file) ;

            /*
             * STEP 2: Call REST API with contents
             */
            String output = callRESTAPIAndRetrieveResponse(rest_url);

            /*
             * STEP 3: Endpoint called will return JSON object
             */
            try {
                JSONObject output_data = null;
                JSONParser parser = new JSONParser();
                output_data = (JSONObject) parser.parse(output);

                Iterator input_iterator = ((JSONArray)output_data.get("links")).iterator();
                while (input_iterator.hasNext()) {
                    JSONObject entry = (JSONObject)input_iterator.next();
                    if (entry.get("rel").equals("create-project")) {
                        writeFile(factory_file, (String) entry.get("href"));
                    }

                    if (entry.get("rel").equals("snippet/html")) {
                        String html = callRESTAPIAndRetrieveResponse((String)entry.get("href"));
                        writeFile(html_file, html);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    public static String readFile(String input_file) {

        if (input_file != null) {

            FileReader working_file = null;
            BufferedReader reader = null;
            boolean is_readable = false;

            try {
                working_file = new FileReader(input_file);
                reader = new BufferedReader(working_file);
                return reader.readLine();
             } catch (Exception ex) {
                System.out.println("########################################");
                System.out.println("### The file specified is not valid. ###");
                System.out.println("########################################");
                System.exit(0);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {}
                }
            }
        }

        return null;
    }

    public static void writeFile(String file_name, String file_contents) {

        if (file_name != null) {
            
            FileWriter writer = null;
            boolean is_writeable = false;
            
            File write_file = new File(file_name);

            try {
                boolean does_exist = write_file.exists();

                if (!does_exist) {
                    // Cannot put these two statements in an &.
                    // The mkdirs() function will return false if directory already exists.
                    does_exist = write_file.createNewFile(); 
                }
            
                if (does_exist) {
                    is_writeable = write_file.canWrite();
                }            
            
                if (is_writeable) {

                    writer = new FileWriter(write_file);
                    writer.write(file_contents);
                    writer.flush();
                }

            } catch (java.io.IOException e) {
                    System.out.println("##########################################################################");
                    System.out.println("### The file write operation failed.                                   ###");
                    System.out.println("### Issues could be non-existant directory or poorly formed file name. ###");
                    System.out.println("##########################################################################");
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {}
                }
            }
        } 
    }

        
    public static String callRESTAPIAndRetrieveResponse(String rest_resource) {
        HttpURLConnection conn = null;
        BufferedReader br = null;
        InputStream errorStream = null;
        InputStreamReader in = null;

        try {

            // Set up the connection.
            conn = (HttpURLConnection) new URL(rest_resource).openConnection();
  
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json"); 
            conn.setRequestProperty("charset", "utf-8");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);

            int responseCode = conn.getResponseCode();

            if (responseCode / 100 != 2) {
                System.out.println("#######################################################");
                System.out.println("### Unexpected REST API response code received: " + responseCode + " ###");
                System.out.println("#######################################################");
                System.out.println("-----------------------------------------------------------------");

                errorStream = conn.getErrorStream();
                String message = errorStream != null ? readAndCloseQuietly(errorStream) : "";
                System.out.println("\n" + "Response Error: " + message);
                System.out.println("\n");
            } else {
                br = new BufferedReader(new InputStreamReader((InputStream) conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                return sb.toString();
           }

        } catch (UnknownHostException | javax.net.ssl.SSLHandshakeException | java.net.SocketTimeoutException e ) {

            System.out.println("####################################################################");
            System.out.println("### Network issues.  We cannot reach the remote Codenvy host.    ###");
            System.out.println("### Issues can be SSL handshake, Socket Timeout, or uknown host. ###");
            System.out.println("####################################################################");
            System.exit(0);

        } catch (IOException e ) {
            System.out.println(e);
            System.exit(0);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

            if (errorStream != null) {
                try {
                    errorStream.close();
                } catch (IOException e) {}
            }
        }
        return null;
    }


    public static String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
    
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
    
        while ((r = inputStream.read(buf)) != -1) {
            bout.write(buf, 0, r);
        }
    
        return bout.toString();
    }

    public static String readAndCloseQuietly(InputStream inputStream) throws IOException {
        try {
            return readStream(inputStream);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
