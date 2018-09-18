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

import org.joda.time.DateTime;

public class Setup {
	public static String GetDateNow(){
		DateTime now = new DateTime();
		String date_in=now.toLocalDateTime().toString();
		return date_in;
		}
	
	public static String ConvertScantoDBDate(String _visitdate){
		String date =_visitdate.substring(6);
		String month =_visitdate.substring(4,6);
		int  year = Integer.parseInt(_visitdate.substring(0,4))-543;
			String visitDate = year+"-"+month+"-"+date;
		return visitDate;
	}

}
