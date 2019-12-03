package com.ap.ex1a;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestAlgs 
{
    public static void main(String[] args) {
        String parentDirectory;

        // check existance of parentDirectory as command line argument
        if (args.length != 1) {
            System.err.println("You must specify the algos' parent directory");
            return;
        } else {
            parentDirectory = args[0];
        }
        
        // Create the registry of algos' keys
        KeyRegistry keyRegistry = new KeyRegistry();
        
        // Initialize a ClassLoader with the path given in input
        ClassLoader cl;
        File file = new File(parentDirectory);
        try {
            URL url = file.toURI().toURL();
            URL[] urls = new URL[]{url};
            cl = new URLClassLoader(urls);
        }
        catch( MalformedURLException e) {return;}

        
        try(Stream<String> stream = Files.lines(Paths.get(parentDirectory + "/crypto/keys.list"))) 
        {            
            // For each class, add it to the registry
            stream.forEach(l -> {
                String[] splittedLine = l.split(" ", 2);
                try {
                    Class c = cl.loadClass(splittedLine[0]);
                    String key = splittedLine[1];
                    keyRegistry.add(c, key);
                } catch (ClassNotFoundException e) {}
            });
        }
        catch (IOException e) {return;}
        
        try 
        {
            String secretWords[] = Files.lines(Paths.get(parentDirectory + "/crypto/secret.list"))
                    .toArray(String[]::new);
            Files.list(Paths.get(parentDirectory + "/crypto/algos"))
                    .filter(Files::isRegularFile)
                    .forEach(f -> 
                    {
                        try 
                        {
                            String filename = f.getFileName().toString().replace(".class", "");
                            filename = "crypto.algos." + filename;
                            Class c = cl.loadClass(filename);
                            checkAlgorithm(c, keyRegistry, secretWords);
                        } catch (ClassNotFoundException e) {System.err.println(e.toString());}   
                    });
            
        } catch (IOException e) {}
    }
    
    private static void checkAlgorithm(Class c, KeyRegistry registry, String[] words) {
        String enc = null;
        String dec = null;
        Constructor constructor;
        
        // Print class name
        System.out.println(c.getSimpleName());
        try 
        { 
            // Check for public constructor with String parameter
            Class[] constructorArgsTypes = new Class[]{ String.class };
            constructor = c.getConstructor(constructorArgsTypes); 
            
            Method[] methods = c.getMethods();
            for(Method m: methods) {
                String name = m.getName();
                
                // Check for a method with String parameter
                if(Arrays.equals(m.getParameterTypes(), new Class[]{String.class}))
                {
                    if(name.startsWith("enc"))
                        enc = name.split("enc",2)[1];

                    if(name.startsWith("dec"))
                        dec = name.split("dec",2)[1];
                    
                }
            }
        }
        catch(NoSuchMethodException e) {System.err.println("Enc/Dec methods not found"); return;}
        
        // test the algorithm if it's a valid one
        if(dec != null && enc != null && enc.equals(dec))
        {
            try 
            {
                // instantiate a new object 
                Object[] constructorArgs = new Object[]{registry.get(c)};
                Object o = constructor.newInstance(constructorArgs);
                
                // get encryption method
                Class[] types = new Class[]{String.class};
                Method encMethod = c.getMethod("enc"+enc, types);
                
                // get decription method
                Method decMethod = c.getMethod("dec"+dec, types);
                
                for(String w: words) 
                {   
                    // call encryption method
                    Object[] encArgs = new Object[]{w};
                    String e = (String) encMethod.invoke(o, encArgs);
                    
                    // call decription method
                    Object[] decArgs = new Object[]{e};
                    String d = (String) decMethod.invoke(o, decArgs);
                    
                    if(!d.equals(w) && !w.equals(d.replaceAll("#", "")))
                        System.err.println("KO: " + w  + " -> " + e + " -> " + d);
                }
            }
            catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) 
            {
                System.err.println("Error in encripting/decripting");
            }
        }
        else
            System.err.println("Enc/Dec methods not found");
        
    }            
}
