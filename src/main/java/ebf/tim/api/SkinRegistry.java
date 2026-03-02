package ebf.tim.api;

import train.common.Traincraft;
import train.common.api.AbstractTrains;
import train.common.library.TraincraftRegistry;

import java.util.*;


public class SkinRegistry {

    public static HashMap<String, HashMap<String,TransportSkin>> liveryMap = new HashMap<String, HashMap<String,TransportSkin>>();

    public static Map<String,TransportSkin> get(AbstractTrains t){
        if(liveryMap.containsKey(t.getName())) {
            return liveryMap.get(t.getName());
        } else {
            return new HashMap<String,TransportSkin>();
        }
    }
    public static Map<String,TransportSkin> get(String t){
        if(liveryMap.containsKey(t)) {
            return liveryMap.get(t);
        } else {
            return new HashMap<String,TransportSkin>();
        }
    }

    public static void addSkin(String trainName, TransportSkin str, String color){
        if(!liveryMap.containsKey(trainName)) {
            HashMap<String,TransportSkin> m = new HashMap<String, TransportSkin>();
            m.put(color,str);
            liveryMap.put(trainName, m);
        } else {
            liveryMap.get(trainName).put(color,str);
        }
    }

    public static void addSkin(String trainName, String str, String color){
        if(!liveryMap.containsKey(trainName)) {
            HashMap<String,TransportSkin> m = new HashMap<String, TransportSkin>();
            m.put(color,new TransportSkin(str));
            liveryMap.put(trainName, m);
        } else {
            liveryMap.get(trainName).put(color,new TransportSkin(str));
        }
    }

    @Deprecated
    public static void addSkin(String trainName, String str){
        if(!liveryMap.containsKey(trainName)) {
            HashMap<String,TransportSkin> m = new HashMap<String, TransportSkin>();
            m.put(str,new TransportSkin(str));
            liveryMap.put(trainName, m);
        } else {
            liveryMap.get(trainName).put(str,new TransportSkin(str));
        }
    }

    public static void addSkin(String trainName, String modid,String addr, String[] bogieSkins,String name, String description){
        if(!liveryMap.containsKey(trainName)) {
            HashMap<String,TransportSkin> m = new HashMap<String, TransportSkin>();
            m.put(name,new TransportSkin(modid,addr));
            liveryMap.put(trainName, m);
        } else {
            liveryMap.get(trainName).put(name,new TransportSkin(modid,addr));
        }

        liveryMap.get(trainName).get(name).bogieSkins=Arrays.asList(bogieSkins);
    }

    public static void addSkin(String trainName, String modid,String addr, String bogieSkin,String name, String description){
        if(!liveryMap.containsKey(trainName)) {
            HashMap<String,TransportSkin> m = new HashMap<String, TransportSkin>();
            m.put(name,new TransportSkin(modid,addr));
            liveryMap.put(trainName, m);
        } else {
            liveryMap.get(trainName).put(name,new TransportSkin(modid,addr));
        }

        liveryMap.get(trainName).get(name).bogieSkins= Collections.singletonList(bogieSkin);
    }

}
