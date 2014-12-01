package com.example.medicationscheduler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.auth.MalformedChallengeException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;


public class InfoDownloaderTask extends AsyncTask<String, Void, MedRecord> {
	private HttpURLConnection mHttpUrl;
	private WeakReference<MainActivity> mParent;
	private Context mContext;
	private boolean hasNetwork;
	public InfoDownloaderTask(MainActivity parent, boolean hasNetwork, Context appContext) {
		super();
		mParent = new WeakReference<MainActivity>(parent);
		mContext = appContext;
		this.hasNetwork = hasNetwork;
	}
	

	@Override
	protected void onPostExecute(MedRecord result) {

		if(result == null){
			Toast.makeText(mContext, "Drug infomation not found. Please enter manually", Toast.LENGTH_LONG).show();
		}else {
			
			if (null != mParent.get()) {
				// mParent.get().addNewPlace(result);
			}
		}
	}
	
	@Override
	protected MedRecord doInBackground(String... params) {
		MedRecord md = null;
//		String url = "http://apps.nlm.nih.gov/medlineplus/services/mpconnect_service.cfm?";
//		url += "mainSearchCriteria.v.cs=2.16.840.1.113883.6.69&"; 
//		url += "mainSearchCriteria.v.c=";
//		url += params[0];
//		url += "&informationRecipient.languageCode.c=en";
		
		String rxnormId = "";
		String url = "http://rxnav.nlm.nih.gov/REST/rxcui?idtype=NDC&id=";
		url += params[0];
		
		
		if(hasNetwork){	
			
			//md = getInfoFromUrl(url);
			rxnormId = getRxNormID(url);
			if(rxnormId != null){
				md = getInterations(rxnormId);
			}
			
		}
		return md;
	}

	public HttpURLConnection getmHttpUrl() {
		return mHttpUrl;
	}

	public void setmHttpUrl(HttpURLConnection mHttpUrl) {
		this.mHttpUrl = mHttpUrl;
	}
	
	
	@SuppressWarnings("unused")
	private MedRecord getInfoFromUrl(String... params){
		String result = null;
		BufferedReader in = null;
		try{
			URL url = new URL(params[0]);
			mHttpUrl= (HttpURLConnection) url.openConnection();
			in = new BufferedReader(new InputStreamReader(mHttpUrl.getInputStream()));
			
			StringBuffer sb = new StringBuffer("");
			String line = "";
			while((line = in.readLine()) != null){
				sb.append(line + "\n");
				
			}
			result = sb.toString();
			
		} catch (MalformedURLException e) {

		} catch (IOException e) {

		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			mHttpUrl.disconnect();
		}
		return medRecordFromXML(result, params[0]);
	}
	
	private String getRxNormID(String params){
		String result = null;
		result = getXml(params);
		return rxNormFromXML(result);
	}

	private MedRecord medRecordFromXML(String xmlString, String NDCCode) {
		DocumentBuilder builder;
		String drugName = "";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		
		try{
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xmlString)));
			NodeList list = document.getDocumentElement().getChildNodes();
			
			for(int idx = 0; idx < list.getLength(); idx++){
				Node curr = list.item(idx);
				NodeList list2 = curr.getChildNodes();
				for(int jdx = 0; jdx < list2.getLength(); jdx++){
					Node curr2 = list2.item(jdx);
					if(curr2.getNodeName().equals("title")){
						drugName = curr2.getTextContent();
						
					}
				}
			}
		}catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(drugName.equals("")){
			Toast.makeText(mContext, "Drug Name not found Please Enter Manually", Toast.LENGTH_SHORT).show();
		}
		MedRecord md = new MedRecord(drugName,NDCCode);
		//md.setInteractables(getInterations(drugName));
		return md;
	}
	
	private String rxNormFromXML(String xmlString){
		DocumentBuilder builder;
		String rxnormID = "";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		
		try{
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xmlString)));
			NodeList list = document.getDocumentElement().getChildNodes();
			
			for(int idx = 0; idx < list.getLength(); idx++){
				Node curr = list.item(idx);
				NodeList list2 = curr.getChildNodes();
				for(int jdx = 0; jdx < list2.getLength(); jdx++){
					Node curr2 = list2.item(jdx);
					if(curr2.getNodeName().equals("rxnormId")){
						rxnormID = curr2.getTextContent();
						
					}
				}
			}
		}catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(rxnormID.equals("")){
			Toast.makeText(mContext, "Drug Name not found Please Enter Manually", Toast.LENGTH_SHORT).show();
		}
			return rxnormID;
		
	}
	

	
	private MedRecord getInterations(String rxnormID) {
		MedRecord md = null ;
		String otherName ="";
		
		TreeSet<Interaction> interAccu = new TreeSet<Interaction>();
		String url = "http://rxnav.nlm.nih.gov/REST/interaction/interaction.xml?rxcui=";
		url += rxnormID;
		String result = getXml(url);
		String drugName = "";
		DocumentBuilder builder;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		
		try{
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(result)));
			NodeList list = document.getDocumentElement().getChildNodes();
			
			for(int idx = 0; idx < list.getLength(); idx++){
				Node curr = list.item(idx);
				NodeList list2 = curr.getChildNodes();
				for(int jdx = 0; jdx < list2.getLength(); jdx++){
					Node curr2 = list2.item(jdx);
					
					if(curr2.getNodeName().equals("interactionType")){
						NodeList list3 = curr2.getChildNodes();
						for(int kdx = 0; kdx < list3.getLength(); kdx++){
							Node curr3 = list3.item(kdx);
							if(curr3.getNodeName().equals("minConceptItem")){
								NodeList list4 = curr3.getChildNodes();
									Node curr4 = list4.item(1);
									drugName = curr4.getTextContent();
									
							}
							
							if(curr3.getNodeName().equals("interactionPair")){
								Interaction inter = null;
								String interactant;
								String idcrp;
								NodeList list4 = curr3.getChildNodes();
								//Node curr4 = list4.item(1);
								Node currDrug = list4.item(0);
									otherName = currDrug.getFirstChild().getFirstChild().getNextSibling().getTextContent();
								Node iteractDrug = list4.item(1);
									interactant = iteractDrug.getFirstChild().getFirstChild().getNextSibling().getTextContent();
								Node description = list4.item(3);
									idcrp = description.getTextContent();
									inter = new Interaction(interactant, idcrp);
									interAccu.add(inter);
								
							}
						}						
					}
				}
			}
			
			md = new MedRecord(drugName,"");
			md.setOtherName(otherName);
			md.setInteractables(interAccu);
			
		}catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return md;
	}

	private String getXml(String params){
		String result = null;
		BufferedReader in = null;
		try{
			URL url = new URL(params);
			mHttpUrl= (HttpURLConnection) url.openConnection();
			in = new BufferedReader(new InputStreamReader(mHttpUrl.getInputStream()));
			
			StringBuffer sb = new StringBuffer("");
			String line = "";
			while((line = in.readLine()) != null){
				sb.append(line + "\n");
				
			}
			result = sb.toString();
			
		} catch (MalformedURLException e) {

		} catch (IOException e) {

		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			mHttpUrl.disconnect();
		}
		return result;
	}
	
}
