/*******************************************************************************
 * Copyright 2018  Uttaradit Hospital
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.utt.app;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.lowagie.text.DocumentException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.GrayColor;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.joda.time.DateTime;

public class Inb {
	String UrlCust11 = "";
 	
	String version="1.9.3";
	String nameBefore="", nameOnBarcode="";
	int status_insert=0;
	PrintWriter toServer=null;
	FileInputStream fis = null;
    BufferedInputStream bis = null;
    OutputStream os = null;
    InetAddress ip=null;
    String ip_account="";
    
    public Inb(){
    	try {
			ip=InetAddress.getLocalHost();
			ip_account=ip.getHostAddress().trim();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    	checkFolder("C:\\utthscan\\");
    	checkFolder("C:\\utthscanTmp\\");
    	File folder = new File("C:\\utthscan\\");
		File[] listOfFiles = folder.listFiles();
        //System.out.println(listOfFiles.length);
        String file_name_input[]=new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {     	 
        	if (listOfFiles[i].isFile()) {
        		file_name_input[i]=listOfFiles[i].getName().trim();     		 
        		if(file_name_input[i].substring(file_name_input[i].length()-3).equals("tif") || file_name_input[i].substring(file_name_input[i].length()-3).equals("TIF")){
        			//System.out.println("F "+file_name_input[i]);    			
        			nameOnBarcode=file_name_input[i];
        			//System.out.println("nameOnBarcode..."+nameOnBarcode);
        			String tmpImgFile = "C:\\utthscan\\"+nameOnBarcode;
        	        Map<DecodeHintType,Object> tmpHintsMap = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        	        tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        	        tmpHintsMap.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
        	        tmpHintsMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
        	        
        	        File tmpFile = new File(tmpImgFile);     	  
        	        try {
        				String tmpRetString = BarCodeUtil.decode(tmpFile, tmpHintsMap);
        				nameBefore =tmpRetString.trim();
        				renameFilescan("C:\\utthscan\\"+nameOnBarcode,"C:\\utthscanTmp\\"+nameBefore+".tif");					
        				ScanOPD();
    					//System.out.println("nameOnBarcode..."+nameOnBarcode+"----"+nameBefore);
			 
        			} catch (Exception e) {
        				e.printStackTrace();
        			}  	              			
        		}
        		else{
        			listOfFiles[i].delete();
        		}
        	}
        	else if(listOfFiles[i].isDirectory()){
        		String[]entries = listOfFiles[i].list();
        		for(String s: entries){			
        		    File currentFile = new File(listOfFiles[i].getPath(),s);
        		    currentFile.delete();
        		}
        		listOfFiles[i].delete();
        		 
        	}
        }
 
  		ReadFileNameInput rfni2=new ReadFileNameInput("C:\\utthscanTmp\\");
  		rfni2.ClearFile();
  		nameBefore="";
  		nameOnBarcode="";
		 
    }
    public static void main(String[] args){
    	Inb inb = new Inb();
    }
    public void ScanOPD() {
    	Socket sock = null;
    	if(nameBefore.length()==21 ){
    		String newfilename="C:\\utthscanTmp\\"+nameBefore+".tif";
    		String filename=nameBefore;
			//String visitdate=Setup.ConvertScantoDBDate(nameBefore.substring(0,8));
			String visitdate_db=Setup.ConvertScantoDBDate(nameBefore.substring(0,8));
			String clinic=nameBefore.substring(8,12);
			String page=nameBefore.substring(12,14);
			String hn= nameBefore.substring(14);
			//System.out.println(nameBefore);
			
			//make pdf
			makePDF(newfilename,"C:\\utthscan\\"+nameBefore+".pdf");
			//send pdf
			sendFileHDFS(nameBefore);
			
			Connection con;
			String sql_in = "insert into txlog (visitdate,hn,txdatetime,txtype,servroom,txip,dupdate,status,version) values ('"+visitdate_db+"','"+hn+"','"+GetDateTimeNow()+"','2','"+clinic+"','"+ip_account+"','"+GetDateTimeNow()+"','1','"+version+"') ";
			try {
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection(UrlCust11);
				//System.out.println(id+"******************"+Setup.DateInDBMSSQL(oUserInfo.GetPtVisitdate())+"----"+oUserInfo.GetPtHN()+"---****--"+oUserInfo.GetPtCliniccode()+"-----"+Setup.GetDateTimeNow());
				PreparedStatement stmt = con.prepareStatement(sql_in);
				int rs_save =stmt.executeUpdate();
				stmt.close();
				con.close();
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			status_insert=0;
    	}
    	else if(nameBefore.length()==25 ){
    		String newfilename="C:\\utthscanTmp\\"+nameBefore+".tif";
    		String filename=nameBefore;
			//String visitdate=Setup.ConvertScantoDBDate(nameBefore.substring(0,8));
			String visitdate_db=Setup.ConvertScantoDBDate(nameBefore.substring(0,8));
			String clinic=nameBefore.substring(8,12);
			String page=nameBefore.substring(12,14);
			String hn= nameBefore.substring(18);
			//System.out.println(nameBefore);
			
			//make pdf
			makePDF(newfilename,"C:\\utthscan\\"+nameBefore+".pdf");
			//send pdf
			sendFileHDFS(nameBefore);
			
			Connection con;
			String sql_in = "insert into txlog (visitdate,hn,txdatetime,txtype,servroom,txip,dupdate,status,version) values ('"+visitdate_db+"','"+hn+"','"+GetDateTimeNow()+"','2','"+clinic+"','"+ip_account+"','"+GetDateTimeNow()+"','1','"+version+"') ";
			try {
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection(UrlCust11);
				//System.out.println(id+"******************"+Setup.DateInDBMSSQL(oUserInfo.GetPtVisitdate())+"----"+oUserInfo.GetPtHN()+"---****--"+oUserInfo.GetPtCliniccode()+"-----"+Setup.GetDateTimeNow());
				PreparedStatement stmt = con.prepareStatement(sql_in);
				int rs_save =stmt.executeUpdate();
				stmt.close();
				con.close();
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			status_insert=0;
    	}
    	else if(nameBefore.length()==30 ){
    		String newfilename="C:\\utthscanTmp\\"+nameBefore+".tif";
    		String filename=nameBefore;
			String visitdate=Setup.ConvertScantoDBDate(nameBefore.substring(0,8));
			//String visitdate_db=Setup.ConvertScantoDBDate(nameBefore.substring(0,8));
			String formcode=nameBefore.substring(8,12);
			String page=nameBefore.substring(12,14);
			String an= nameBefore.substring(newfilename.length()-14,newfilename.length()-7);
			String hn= nameBefore.substring(newfilename.length()-7);
			//System.out.println(nameBefore);
			
			//make pdf
			makePDF(newfilename,"C:\\utthscan\\"+nameBefore+".pdf");
			//send pdf
			//sendFileHDFSIPD(nameBefore);
			
			Connection con;
			String sql_in = "insert into txlog (visitdate,hn,an,txdatetime,txtype,servroom,txip,dupdate,status,flag,version) values ('"+visitdate+"','"+hn+"','"+an+"','"+GetDateTimeNow()+"','2','"+formcode+"','"+ip_account+"','"+GetDateTimeNow()+"','1','0','"+version+"') ";
			try {
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection(UrlCust11);
				//System.out.println(id+"******************"+Setup.DateInDBMSSQL(oUserInfo.GetPtVisitdate())+"----"+oUserInfo.GetPtHN()+"---****--"+oUserInfo.GetPtCliniccode()+"-----"+Setup.GetDateTimeNow());
				PreparedStatement stmt = con.prepareStatement(sql_in);
				int rs_save =stmt.executeUpdate();
				stmt.close();
				con.close();
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			status_insert=0;
    	}
    	nameBefore="";
    }
    public void makePDF(String tiff_in,String pdf_out){
    	String pdf=pdf_out;
		String tiff=tiff_in;
		RandomAccessFileOrArray ra;
		try {
			ra = new RandomAccessFileOrArray(tiff);
			int n = TiffImage.getNumberOfPages(ra);
	        //Image img;
			float x, y;
	        for (int p = 1; p <= n; p++) {
	        	 com.lowagie.text.Image image = TiffImage.getTiffImage(ra, 1);
	        	//image.setAbsolutePosition(0f, 0f);
	        	 com.lowagie.text.Rectangle pageSize = new  com.lowagie.text.Rectangle(image.getWidth(), image.getHeight()+80);

	        	Document document = new Document(pageSize);
	        	//Document document = new Document(PageSize.A4);
	        	PdfWriter writer = PdfWriter.getInstance(document,  new FileOutputStream(pdf));
	        	
	        	writer.setStrictImageSequence(true);
	        	document.open();
	        	document.add(image);
	        	Phrase p1 = new Phrase(tiff.trim(),FontFactory.getFont(FontFactory.COURIER, 24, com.lowagie.text.Font.BOLD,new GrayColor(0.5f)));
	        	PdfContentByte canvas = writer.getDirectContent();
	        	x = (pageSize.getLeft() + pageSize.getRight()) / 4;
	        	y = (pageSize.getTop() + pageSize.getBottom()) / 10;
	        	canvas.saveState();
	        	//document.newPage();
	        	PdfGState state = new PdfGState();
	            state.setFillOpacity(0.2f);
	            canvas.setGState(state);
	            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, p1, x, y, 0);
	            canvas.restoreState();
	        	/*
	        	PdfPTable table =new PdfPTable(1);
	    		table.setTotalWidth(500);
	    		table.setLockedWidth(true);
	    		table.getDefaultCell().setFixedHeight(30);
	    		table.getDefaultCell().setBorder(Rectangle.BOTTOM);
	    		table.addCell(("   "+d+"-"+m+"-"+y).trim());
	    		table.writeSelectedRows(0, -1, 10, table.getTotalHeight() + 30, canvas);
	        	*/
	        	//document.newPage();

	        	document.close();
	        	
	            ra.close();
	        }

		} catch (IOException | DocumentException e) {
			e.printStackTrace();
		}
    }
    public void renameFilescan(String fileinput ,String newfilename){
		File infile =new File(fileinput);
		File outfile =new File(newfilename);
		if (!outfile.exists()) {
			 if(infile.renameTo(new File(newfilename))){
				 
			 }
		}else{
			outfile.delete();
			if(infile.renameTo(new File(newfilename))){
				
			}
		}
	}
    public void sendFileHDFS(String fn){
    	String path="";

		Configuration conf = new Configuration();
		conf.set("fs.defaultFS", "");
		System.setProperty("HADOOP_USER_NAME", "");
		System.setProperty("hadoop.home.dir", "");
		
		FileSystem fs;
		try {
			fs = FileSystem.get(URI.create(""), conf);
			//System.out.println(fs.toString());
			Path homeDir=fs.getHomeDirectory();
			//System.out.println("Home folder -" +homeDir);
			Path workingDir=fs.getWorkingDirectory();
			String hn=fn.trim().substring(fn.length()-7);
			String folderhn=hn.substring(0, 2).trim();
		      Path newFolderPath= new Path(path+"/"+folderhn+"/"+hn);
		      if(!fs.exists(newFolderPath)) {
		         // Create new Directory
		         fs.mkdirs(newFolderPath);
		         //System.out.println("Path "+path+" created.");
		      }
			Path localFilePath = new Path("c://utthscan//"+fn+".pdf");
			Path hdfsFilePath=new Path(newFolderPath+"/"+fn+"HN.pdf");

			fs.moveFromLocalFile(localFilePath, hdfsFilePath);
			//System.out.println("sent "+fn);

			fs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    public void checkFolder(String folder){
		File file = new File(folder);
		if (!file.exists()) {
			if (file.mkdir()) {
				
			}else {
				
			}
		}
	}
    public  String GetDateTimeNow(){
		DateTime now = new DateTime();
		String datetime_in=now.toLocalDateTime().toString();
		return datetime_in;
	}
    
}
