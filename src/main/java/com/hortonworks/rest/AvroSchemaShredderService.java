package com.hortonworks.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.json.InstanceSerialization;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.hortonworks.atlas.avro.types.AvroArray;
import com.hortonworks.atlas.avro.types.AvroField;
import com.hortonworks.atlas.avro.types.AvroMap;
import com.hortonworks.atlas.avro.types.AvroPrimitive;
import com.hortonworks.atlas.avro.types.AvroSchema;
import com.hortonworks.atlas.avro.types.AvroType;

/*
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
*/

@Path("/schemaShredder")
public class AvroSchemaShredderService {
	public static final String DEFAULT_ATLAS_REST_ADDRESS = "http://sandbox.hortonworks.com";
	public static final String DEFAULT_ADMIN_USER = "admin";
	public static final String DEFAULT_ADMIN_PASS = "admin";
	private static AtlasClient atlasClient;
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.WILDCARD})
	@Path("testService")
	public Response testService(String name){
		System.out.print("Success " + name);
		return Response.status(200).entity("success").build();
	}
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.WILDCARD})
	@Path("storeSchema")
	public Response storeSchema(String jsonData) throws Exception {
		String result = "Data post: " + jsonData;
		System.out.println(result);
		//TechnicianDestination techDestination = (TechnicianDestination)convertJSONToPOJO(data);
		
		//CacheManager cacheManager = CacheManager.getInstance();
		//Cache cache = cacheManager.getCache("TechnicianRouteRequest");
		//Element element = new Element(techDestination.getTechnicianId(), techDestination);
		//cache.put(element);
		
	
	String atlasUrl = "http://sandbox.hortonworks.com:21000";
	String[] basicAuth = {DEFAULT_ADMIN_USER, DEFAULT_ADMIN_PASS};
	String[] atlasURL = {atlasUrl};
	
	Properties props = System.getProperties();
    props.setProperty("atlas.conf", "/Users/vvaks/");
	atlasClient = new AtlasClient(atlasURL, basicAuth);
	//atlasClient = new AtlasClient("http://sandbox.hortonworks.com:21000");
	System.out.println("***************** atlas.conf has been set to: " + props.getProperty("atlas.conf"));
	//System.out.println(atlasClient.getEntity("b33d63bb-7d77-4e5a-aa1b-145cfc69112e"));
	
	//byte[] jsonData = Files.readAllBytes(Paths.get("/Users/vvaks/Documents/avroSchemaToAtlas2.json"));
	
	com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
	AvroSchema avroSchema = objectMapper.readValue(jsonData, AvroSchema.class);
	
	//System.out.println(avroSchema.toString());
	
	StringWriter stringAvroSchema = new StringWriter();
	objectMapper.writeValue(stringAvroSchema, avroSchema);
	System.out.println(stringAvroSchema);
	
	System.out.println("********** name: " + avroSchema.getName());
	System.out.println("********** namespace: " + avroSchema.getNamespace());
	System.out.println("********** type: " + avroSchema.getType());
	System.out.println("********** doc: " + avroSchema.getDoc());
	System.out.println("********** fields: " + avroSchema.getFields());
	System.out.println("********** fields size: " + avroSchema.getFields().size());
	
	List<AvroField> avroFields = (List<AvroField>) handleList((ArrayList<?>)avroSchema.getFields(), "AvroField");
	avroSchema.setFields(avroFields);
	reportAtlasAvroObject(avroSchema);
	//getAvroSchemaReference(avroSchema);
	return Response.status(200).entity("success").build();		 
	}
	
	public static AvroType handleHashMap(HashMap nextMap){
		String mapType = nextMap.get("type").toString();
		List fieldList;
		Object next;
	
		System.out.println("**************** Complex Type : " + nextMap.get("type"));
		if(mapType.equalsIgnoreCase("record")){
			System.out.println("**************** name : " + nextMap.get("name"));
			System.out.println("**************** namespace : " + nextMap.get("namespace"));
			System.out.println("**************** aliases : " + nextMap.get("aliases"));
			System.out.println("**************** doc : " + nextMap.get("doc"));
		System.out.println("**************** fields : " + nextMap.get("fields"));
		AvroSchema returnSchema = new AvroSchema();
		returnSchema.setName(nextMap.get("name").toString());
		//returnSchema.setNamespace(nextMap.get("namespace").toString());
		//returnSchema.setDoc(nextMap.get("doc").toString());
		returnSchema.setType(nextMap.get("type").toString());
		List<AvroField> avroFieldList = new ArrayList<AvroField>();
		fieldList = (ArrayList)nextMap.get("fields");
		System.out.println("**************** Call HandleList with : " + nextMap.getClass().toString() + ", AvroField");
		avroFieldList = (List<AvroField>) handleList(fieldList, "AvroField");
		returnSchema.setFields(avroFieldList);
		return returnSchema;
	}else if(mapType.equalsIgnoreCase("array")){
		System.out.println("**************** handleHashMap array items : " + nextMap.get("items"));
		AvroArray returnArray = new AvroArray();
		List<AvroType> avroArrayItems = new ArrayList<AvroType>();
		fieldList = (ArrayList)nextMap.get("items");
		avroArrayItems = (List<AvroType>) handleList(fieldList, "array");
		returnArray.setItems(avroArrayItems);
		return returnArray;
	}else if(mapType.equalsIgnoreCase("arrayItem")){
		System.out.println("**************** handleHashMap arrayItem items : " + nextMap.get("type"));
		if(nextMap.get("type")==null){
			AvroPrimitive returnPrimitive = new AvroPrimitive();
			returnPrimitive.setType(nextMap.toString());
			return returnPrimitive;
		}else if(nextMap.get("type").toString().equalsIgnoreCase("record")){
			System.out.println("**************** name : " + nextMap.get("name"));
			System.out.println("**************** namespace : " + nextMap.get("namespace"));
			System.out.println("**************** doc : " + nextMap.get("doc"));
			System.out.println("**************** fields : " + nextMap.get("fields"));
			AvroSchema returnSchema = new AvroSchema();
			returnSchema.setName(nextMap.get("name").toString());
			//returnSchema.setNamespace(nextMap.get("namespace").toString());
			//returnSchema.setDoc(nextMap.get("doc").toString());
			returnSchema.setType(nextMap.get("type").toString());
			List<AvroField> avroFieldList = new ArrayList<AvroField>();
			fieldList = (ArrayList)nextMap.get("fields");
			avroFieldList = (List<AvroField>) handleList(fieldList, "AvroField");
			returnSchema.setFields(avroFieldList);
			return returnSchema;
		}else if(nextMap.get("type").toString().equalsIgnoreCase("array")){
			System.out.println("**************** handleHashMap array items : " + nextMap.get("items"));
			AvroArray returnArray = new AvroArray();
			List<AvroType> avroArrayItems = new ArrayList<AvroType>();
			fieldList = (ArrayList)nextMap.get("items");
			avroArrayItems = (List<AvroType>) handleList(fieldList, "array");
			returnArray.setItems(avroArrayItems);
			return returnArray;
		}else if(nextMap.get("type").getClass().toString().equalsIgnoreCase("class java.util.ArrayList")){
			AvroArray returnArray = new AvroArray();
			List<AvroType> avroArrayItems = new ArrayList<AvroType>();
			fieldList = (ArrayList)nextMap.get("items");
			avroArrayItems = (List<AvroType>) handleList(fieldList, "array");
			returnArray.setItems(avroArrayItems);
			return returnArray;
		}
		AvroArray returnArray = new AvroArray();
		List<AvroType> avroArrayItems = new ArrayList<AvroType>();
		fieldList = (ArrayList)nextMap.get("items");
		avroArrayItems = (List<AvroType>) handleList(fieldList, "array");
		returnArray.setItems(avroArrayItems);
		return returnArray;
	}else if(mapType.equalsIgnoreCase("map")){
		System.out.println("**************** nested values : " + nextMap.get("values"));
		fieldList = (ArrayList)nextMap.get("values");
		handleList(fieldList, "map");
	}else{
		System.out.println("**************** nested string : " + nextMap.get("type"));
		fieldList = (ArrayList)nextMap.get("type");
		handleList(fieldList, "none");
	}
	return null;
}
public static List<?> handleList(List nextList, String listType){
	Object next;
	Iterator i = nextList.iterator();
	List<AvroType> returnList = new ArrayList<AvroType>();
	
	while(i.hasNext()){
		next = i.next();
		System.out.println("**************** handleList header list type: " + listType.toString());
		System.out.println("**************** handleList header field value: " + next);
		System.out.println("**************** handleList header field class: " + next.getClass());
		if(next.getClass().toString().equalsIgnoreCase("class java.util.LinkedHashMap") && listType.equalsIgnoreCase("fieldType")){
			System.out.println("**************** handleList 1 field value: " + next);
			if(((HashMap)next).get("type").toString().equalsIgnoreCase("array")){	
				AvroArray avroArray = new AvroArray();
				System.out.println("**************** Call HandleHashMap");
				avroArray = (AvroArray) handleHashMap((HashMap)next);
				returnList.add(avroArray);
			}else if(((HashMap)next).get("type").toString().equalsIgnoreCase("record")){
				System.out.println("**************** name : " + ((HashMap)next).get("name"));
				System.out.println("**************** namespace : " + ((HashMap)next).get("namespace"));
				System.out.println("**************** doc : " + ((HashMap)next).get("doc"));
				System.out.println("**************** fields : " + ((HashMap)next).get("fields"));
				AvroSchema returnSchema = new AvroSchema();
				List<AvroField> avroFieldList = new ArrayList<AvroField>();
				returnSchema.setName(((HashMap)next).get("name").toString());
				//returnSchema.setNamespace(((HashMap)next).get("namespace").toString());
				//returnSchema.setDoc(((HashMap)next).get("doc").toString());
				returnSchema.setType(((HashMap)next).get("type").toString());
				avroFieldList = (List<AvroField>) handleList((ArrayList)((HashMap)next).get("fields"), "AvroField");
				returnSchema.setFields(avroFieldList);
				returnList.add(returnSchema);
			}else if(((HashMap)next).get("type")==null && (((HashMap)next).get("type") instanceof ArrayList)){
				
			}
		}else if(next.getClass().toString().equalsIgnoreCase("class java.util.LinkedHashMap") && listType.equalsIgnoreCase("array")){	
			System.out.println("**************** handleList 2 field value: " + next);
			System.out.println("**************** handleList 2 field class: " + next.getClass().toString());
			AvroArray avroArray = new AvroArray();
			System.out.println("**************** Call HandleHashMap");
			AvroType avroArrayItem = (AvroType) handleHashMap((HashMap)next);
			returnList.add(avroArrayItem);
		}else if(next.getClass().toString().equalsIgnoreCase("class java.util.LinkedHashMap") && listType.equalsIgnoreCase("AvroField")){
			System.out.println("**************** handleList 3 field type: " + ((HashMap)next).get("type"));
			AvroField avroField = new AvroField();
			System.out.println("**************** name : " + ((HashMap)next).get("name").toString());
			//System.out.println("**************** aliases : " + ((HashMap)next).get("aliases").toString());
			//System.out.println("**************** doc : " + ((HashMap)next).get("doc").toString());
			System.out.println("**************** type : " + ((HashMap)next).get("type").toString());
			avroField.setName(((HashMap)next).get("name").toString());
			//avroField.setAliases(((AvroField)next).getAliases());
			//avroField.setDoc(((AvroField)next).getDoc());
			System.out.println("**************** Call HandleList");
			avroField.setType((List<Object>) handleList((List)(((HashMap)next).get("type")),"fieldType"));
			returnList.add(avroField);
		}else if(next.getClass().toString().equalsIgnoreCase("class java.util.LinkedHashMap") && listType.equalsIgnoreCase("map")){
		}else if(next.getClass().toString().equalsIgnoreCase("class java.util.ArrayList")){
			System.out.println("**************** handleList 4 field type: " + next.toString());
			System.out.println("**************** handleList 4 field class: " + next.getClass());
		}else if(next.getClass().toString().equalsIgnoreCase("class com.hortonworks.atlas.avro.types.AvroField")){
			System.out.println("**************** handleList 5 field type: " + ((AvroField)next).getType());
			AvroField avroField = new AvroField();
			System.out.println("**************** name : " + ((AvroField)next).getName());
			System.out.println("**************** aliases : " + ((AvroField)next).getAliases());
			System.out.println("**************** doc : " + ((AvroField)next).getDoc());
			System.out.println("**************** type : " + ((AvroField)next).getType());
			avroField.setName(((AvroField)next).getName());
			//avroField.setAliases(((AvroField)next).getAliases());
			//avroField.setName(((AvroField)next).getDoc());
			System.out.println("**************** Call HandleList");
			avroField.setType((List<Object>) handleList((List)((AvroField)next).getType(),"fieldType"));
			returnList.add(avroField);
		}else{
			System.out.println("**************** handleList 6 field value: " + next.toString());
			AvroPrimitive returnPrimitive = new AvroPrimitive();
			returnPrimitive.setType(next.toString());
			System.out.println("**************** Primitive Reached... returning...");
			returnList.add(returnPrimitive);
		}
	}
	
	return returnList;
}
public static void reportAtlasAvroObject(AvroType avroType) throws Exception{
	final Referenceable atlasObject;
	Object current  = avroType; 
	Referenceable referenceableId;
		System.out.println(current);
		if(current instanceof AvroPrimitive){
			atlasObject = new Referenceable("avro_primitive");
			atlasObject.set("type", ((AvroPrimitive)current).getType());
			System.out.println("*** type: " + ((AvroPrimitive)current).getType());
		}else if(current instanceof AvroMap){
			System.out.println("*** type: " +((AvroMap)current).getType());
			System.out.println("*** values: " +((AvroMap)current).getValues().toString());
		}else if(current instanceof AvroArray){
			atlasObject = new Referenceable("avro_collection");
			atlasObject.set("type", ((AvroArray)current).getType());
			System.out.println("*** type: " +((AvroArray)current).getType());
			//System.out.println("*** items: " +((AvroArray)current).getItems().toString());
			reportAtlasAvroCollection((List<?>)((AvroArray)current).getItems());
		}else if(current instanceof AvroField){
			atlasObject = new Referenceable("avro_field");
			atlasObject.set("name", ((AvroSchema)current).getName());
			atlasObject.set("doc", ((AvroSchema)current).getNamespace() + " ");
			System.out.println("*** name: " + ((AvroField)current).getName());
			System.out.println("*** aliases: " + ((AvroField)current).getAliases());
			System.out.println("*** doc: " + ((AvroField)current).getDoc());
			//System.out.println("*** type: " + ((AvroField)current).getType());
			reportAtlasAvroCollection((List<?>)((AvroField)current).getType());
		}else if(current instanceof AvroSchema){
			System.out.println("*** name: " + ((AvroSchema)current).getName());
			System.out.println("*** namespace: " + ((AvroSchema)current).getNamespace());
			System.out.println("*** type: " + ((AvroSchema)current).getType());
			System.out.println("*** doc: " + ((AvroSchema)current).getDoc());
			System.out.println("*** fields: " + ((AvroSchema)current).getFields());
			reportAtlasAvroCollection((List<?>)((AvroSchema)current).getFields());
			
			atlasObject = new Referenceable("avro_schema");
			atlasObject.set("qualifiedName", ((AvroSchema)current).getName());
			atlasObject.set("name", ((AvroSchema)current).getName());
			atlasObject.set("namespace", ((AvroSchema)current).getNamespace() + " ");
			atlasObject.set("type", ((AvroSchema)current).getType());
			atlasObject.set("doc", ((AvroSchema)current).getDoc());
			atlasObject.set("fields", reportAtlasReferenceableCollection((List<?>)((AvroSchema)current).getFields()));
			System.out.println(InstanceSerialization.toJson(atlasObject, true));
			referenceableId = new Referenceable("avro_schema");
			referenceableId = getEntityReference(atlasObject.getTypeName(), ((AvroSchema)current).getName().toString());
			if(referenceableId == null){
				referenceableId = new Referenceable("avro_schema");
				referenceableId = atlasClient.getEntity(atlasClient.createEntity(atlasObject).get(0));
				register(atlasClient, atlasObject);
			}else{
				System.out.println("********* " + ((AvroSchema)current).getName() + "  Already Exists ");
			}
		}
}
public static List<AvroType> reportAtlasAvroCollection(List<?> avroType){
	List<AvroType> returnList = new ArrayList();
	Iterator i = avroType.iterator();
	Object current  = new AvroType(); 
	while(i.hasNext()){
		current = i.next();
		System.out.println(current);
		if(current instanceof AvroPrimitive){
			System.out.println("*** type: " + ((AvroPrimitive)current).getType());
			returnList.add((AvroPrimitive)current);
		}else if(current instanceof AvroMap){
			System.out.println("*** type: " +((AvroMap)current).getType());
			System.out.println("*** values: " +((AvroMap)current).getValues().toString());
			returnList.add((AvroMap)current);
		}else if(current instanceof AvroArray){
			System.out.println("*** type: " +((AvroArray)current).getType());
			//System.out.println("*** items: " +((AvroArray)current).getItems().toString());
			reportAtlasAvroCollection((List<?>)((AvroArray)current).getItems());
			returnList.add((AvroArray)current);
		}else if(current instanceof AvroField){
			System.out.println("*** name: " + ((AvroField)current).getName());
			System.out.println("*** aliases: " + ((AvroField)current).getAliases());
			System.out.println("*** doc: " + ((AvroField)current).getDoc());
			//System.out.println("*** type: " + ((AvroField)current).getType());
			reportAtlasAvroCollection((List<?>)((AvroField)current).getType());
			returnList.add((AvroField)current);
		}else if(current instanceof AvroSchema){
			System.out.println("*** name: " + ((AvroSchema)current).getName());
			System.out.println("*** namespace: " + ((AvroSchema)current).getNamespace());
			System.out.println("*** type: " + ((AvroSchema)current).getType());
			System.out.println("*** doc: " + ((AvroSchema)current).getDoc());
			//System.out.println("*** fields: " + ((AvroSchema)current).getFields());
			reportAtlasAvroCollection((List<?>)((AvroSchema)current).getFields());
			returnList.add((AvroSchema)current);
		}
	}
	return returnList;
}

public static List<?> reportAtlasReferenceableCollection(List<?> avroType) throws Exception{
	List<Object> returnList = new ArrayList();
	Iterator i = avroType.iterator();
	Object current  = new AvroType();
	Referenceable referenceable;
	Referenceable referenceableId;
	while(i.hasNext()){
		current = i.next();
		System.out.println(current);
		if(current instanceof AvroPrimitive){
			System.out.println("*** type: " + ((AvroPrimitive)current).getType());
			referenceable = new Referenceable("avro_primitive");
			referenceable.set("qualifiedName", referenceable.getTypeName() + "." + ((AvroPrimitive)current).getType());
			referenceable.set("name", referenceable.getTypeName() + "." + ((AvroPrimitive)current).getType());
			referenceable.set("type", ((AvroPrimitive)current).getType());
			referenceableId = new Referenceable("avro_primitive");
			referenceableId = getEntityReference(referenceable.getTypeName(), (String)referenceable.get("name"));
			if(referenceableId == null){
				referenceableId = new Referenceable("avro_primitive");
				referenceableId = atlasClient.getEntity(atlasClient.createEntity(referenceable).get(0));
				returnList.add(referenceableId.getId());
			}else{
				returnList.add(referenceableId.getId());
			}
		}else if(current instanceof AvroMap){
			System.out.println("*** type: " + ((AvroMap)current).getType());
			System.out.println("*** values: " + ((AvroMap)current).getValues().toString());
			referenceable = new Referenceable("avro_map");
			referenceable.set("qualifiedName", " ");
			referenceable.set("name", " ");
			referenceable.set("type", ((AvroMap)current).getType());
			referenceable.set("values", ((AvroMap)current).getValues().toString());
			returnList.add(referenceable);
		}else if(current instanceof AvroArray){
			System.out.println("*** type: " + ((AvroArray)current).getType());
			//System.out.println("*** items: " +((AvroArray)current).getItems().toString());
			referenceable = new Referenceable("avro_array");
			referenceable.set("qualifiedName", referenceable.getTypeName() + "." + ((AvroArray)current).getType());
			referenceable.set("name", referenceable.getTypeName() + "." + ((AvroArray)current).getType());
			referenceable.set("type", ((AvroArray)current).getType());
			referenceable.set("items", reportAtlasReferenceableCollection((List<?>)((AvroArray)current).getItems()));
			System.out.println(InstanceSerialization.toJson(referenceable, true));
			referenceableId = new Referenceable("avro_array");
			referenceableId = getEntityReference(referenceable.getTypeName(), (String)referenceable.get("name"));
			if(referenceableId == null){
				referenceableId = new Referenceable("avro_array");
				referenceableId = atlasClient.getEntity(atlasClient.createEntity(referenceable).get(0));
				returnList.add(referenceableId);
			}else{
				returnList.add(referenceableId);
			}
		}else if(current instanceof AvroField){
			System.out.println("*** name: " + ((AvroField)current).getName());
			System.out.println("*** aliases: " + ((AvroField)current).getAliases());
			System.out.println("*** doc: " + ((AvroField)current).getDoc());
			referenceable = new Referenceable("avro_field");
			referenceable.set("qualifiedName", referenceable.getTypeName() + "." + ((AvroField)current).getName());
			referenceable.set("name", referenceable.getTypeName() + "." + ((AvroField)current).getName());
			//referenceable.set("aliases", ((AvroField)current).getAliases());
			referenceable.set("doc", ((AvroField)current).getDoc());
			referenceable.set("type", reportAtlasReferenceableCollection((List<?>)((AvroField)current).getType()));
			returnList.add(referenceable);
		}else if(current instanceof AvroSchema){
			System.out.println("*** name: " + ((AvroSchema)current).getName());
			System.out.println("*** namespace: " + ((AvroSchema)current).getNamespace());
			System.out.println("*** type: " + ((AvroSchema)current).getType());
			System.out.println("*** doc: " + ((AvroSchema)current).getDoc());
			referenceable = new Referenceable("avro_schema");
			referenceable.set("qualifiedName", ((AvroSchema)current).getName());
			referenceable.set("name", ((AvroSchema)current).getName());
			referenceable.set("namespace", ((AvroSchema)current).getNamespace() + " ");
			referenceable.set("type", ((AvroSchema)current).getType());
			referenceable.set("doc", ((AvroSchema)current).getDoc());
			referenceable.set("fields", reportAtlasReferenceableCollection((List<?>)((AvroSchema)current).getFields()));
			System.out.println(InstanceSerialization.toJson(referenceable, true));
			referenceableId = new Referenceable("avro_schema");
			referenceableId = getEntityReference(referenceable.getTypeName(), (String)referenceable.get("name"));
			if(referenceableId == null){
				referenceableId = new Referenceable("avro_schema");
				referenceableId = atlasClient.getEntity(atlasClient.createEntity(referenceable).get(0));
				returnList.add(referenceableId.getId());
			}else{
				returnList.add(referenceableId.getId());
			}
		}
	}
	return returnList;
}
public static void cacheSchemas(){
	
}
public static Referenceable register(final AtlasClient atlasClient, final Referenceable referenceable) throws AtlasServiceException, JSONException {
    if (referenceable == null) {
        return null;
    }

    final String typeName = referenceable.getTypeName();
    System.out.println("creating instance of type " + typeName);

    final String entityJSON = InstanceSerialization.toJson(referenceable, true);
    System.out.println("Submitting new entity " + referenceable.getTypeName() + ":" + entityJSON);

    //final JSONArray guid = atlasClient.createEntity(entityJSON); //client vesion 0.6
    //final JSONObject guid = atlasClient.createEntity(entityJSON);
    List<String> guid = (List<String>) atlasClient.createEntity(entityJSON);
    
    System.out.println("created instance for type " + typeName + ", guid: " + guid); //client version 0.6
    //System.out.println("created instance for type " + typeName + ", guid: " + guid.getString("GUID"));
    
    //return new Referenceable(guid.getString(0), referenceable.getTypeName(), null); //client version 0.6
    //return new Referenceable(guid.getString("GUID"), referenceable.getTypeName(), null);
    return new Referenceable(guid.get(0), referenceable.getTypeName(), null); //client version 0.7
    //return null;
}

private Referenceable createSchema(AvroSchema event) {
    final String flowFileUuid = event.toString();
    
    // TODO populate processor properties and determine real parent group, assuming root group for now
    final Referenceable processor = new Referenceable("event");
    //processor.set(AtlasClient.REFERENCEABLE_ATTRIBUTE_NAME, flowFileUuid);
    processor.set("name", flowFileUuid);
    processor.set("event_key", "accountNumber");
    processor.set("description", flowFileUuid);
    return processor;
}
private static Referenceable getAvroSchemaReference(AvroSchema event) throws Exception {
		final String typeName = "avro_schema";
		final String id = "Application";
		 
		String dslQuery = String.format("%s where %s = \"%s\"", typeName, "name", id);
		//System.out.println("********************* Atlas Version is: " + atlasVersion);
		//Referenceable eventReferenceable = null;
		
		//if(atlasVersion >= 0.7)
			return getEntityReferenceFromDSL6(atlasClient, typeName, dslQuery);
		//else
		//return null;
}
private static Referenceable getEntityReference(String typeName, String id) throws Exception {
	
	String dslQuery = String.format("%s where %s = \"%s\"", typeName, "name", id);
	//System.out.println("********************* Atlas Version is: " + atlasVersion);
	//Referenceable eventReferenceable = null;
	
	//if(atlasVersion >= 0.7)
		return getEntityReferenceFromDSL6(atlasClient, typeName, dslQuery);
	//else
	//return null;
}
private static Referenceable getEntityReferenceFromDSL6(final AtlasClient atlasClient, final String typeName, final String dslQuery)
          	throws Exception {
	   System.out.println("****************************** Query String: " + dslQuery);
	   
	   JSONArray results = atlasClient.searchByDSL(dslQuery);
       //JSONArray results = atlasClient.searchByDSL(dslQuery, 0, 0);
       //JSONArray results = searchDSL(atlasUrl + "/api/atlas/discovery/search/dsl?query=", dslQuery);
       System.out.println("****************************** Query Results Count: " + results.length());
       if (results.length() == 0) {
           return null;
       }else{
    	   	String guid;
        	String state = "";
        	int i=0;
        	JSONObject row;
        	
        	for(i=0; i<results.length(); i++){
        	   row = results.getJSONObject(i);
        	   if (row.has("$id$")) {
        		   guid = row.getJSONObject("$id$").getString("id");
        		   state = row.getJSONObject("$id$").getString("state");
        	   } else {
        		   guid = row.getJSONObject("_col_0").getString("id");
        		   state = row.getJSONObject("_col_0").getString("state");
        	   }
        	   
        	   if(state.equalsIgnoreCase("ACTIVE")){
        		   System.out.println("****************************** Resulting JSON Object: " + row.toString());
        		   return new Referenceable(guid, typeName, null);
        	   }else{
        		   System.out.println("****************************** Current Result JSON Object is marked as DELETED: " + row.toString());
        		   System.out.println("****************************** Skipping.....");
        	   }
           }
           return null;	
      }
	}
	
}
