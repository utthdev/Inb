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

import java.io.File;

public class ReadFileNameInput {	
	String file_in="";	
	public ReadFileNameInput(String File1){
		file_in=File1;
	}
	public String[] ReturnFileNameInput( ){		
		File folder = new File(file_in);
        File[] listOfFiles = folder.listFiles();
        String file_name_input[]=new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
        	if (listOfFiles[i].isFile()) {        		
        		file_name_input[i]=listOfFiles[i].getName();
        	}
        }
        return file_name_input;
	}
	public void ClearFile(){
		
		File folder = new File(file_in);
		File[] listOfFiles = folder.listFiles();
        String file_name_input[]=new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
        	if (listOfFiles[i].isFile()) {  		
        		listOfFiles[i].delete();
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
	}
	public int CheckFile(){
		int result=0;
		int count_file=0;
		File folder = new File(file_in);
		File[] listOfFiles = folder.listFiles();
        String file_name_input[]=new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
        	if (listOfFiles[i].isFile()) {
        		count_file++;
        	}
        	else if(listOfFiles[i].isDirectory()){
        		listOfFiles[i].delete();
        		result=1;
        	}
        }
        if(count_file>1){
        	result=1;
        }
		return result;
	}
	 

}

