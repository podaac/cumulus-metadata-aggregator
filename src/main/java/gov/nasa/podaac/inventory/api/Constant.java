//Copyright 2008, by the California Institute of Technology.
//ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
package gov.nasa.podaac.inventory.api;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * This class contains constants used by Inventory Program Set.
 *
 * @author clwong
 *
 * @version
 */
public class Constant {

	public enum AccessType {
		//PUBLIC {PREVIEW, OPEN, RETRIED, REDUCED}
		//RESTRICTED {SHARED, CONTROLLED}
		OPEN("OPEN"), PREVIEW("PREVIEW"),SIMULATED("SIMULATED"), RETIRED("RETIRED"), REDUCED("REDUCED"), PUBLIC("PUBLIC"), PRIVATE("PRIVATE"), RESTRICTED("RESTRICTED"), SHARED("SHARED"),CONTROLLED("CONTROLLED"), DORMANT("DORMANT"); 	
		private AccessType(String type) {}
	};

	public enum VersionPolicy{
		ALL("ALL"), LATEST("LATEST");
		private String Policy;
		private VersionPolicy(String type){
			this.Policy = type;
		}
		public String toString(){
			return this.Policy;
		}
		public static VersionPolicy VersionPolicy(String val){
			if(val.equals("ALL"))
				return ALL;
			else if(val.equals("LATEST"))
				return LATEST;
			else
				return null;
		}
	}

	public enum GranuleStatus {
		ONLINE("ONLINE"), OFFLINE("OFFLINE");
		private String Status;
		private GranuleStatus(String type) {

			this.Status = type;
		}

		public String toString()
		{
			return this.Status;
		}

		public static GranuleStatus ValueOf(String val)
		{
			if(val.equals("ONLINE"))
				return ONLINE;
			else
				return OFFLINE;
			//return null;
		}

		public String getID()
		{
			return this.Status;
		}
	};

	public enum GranuleArchiveType {
		DATA("DATA"), IMAGE("IMAGE"), GRIB("GRIB"), CHECKSUM("CHECKSUM"), METADATA("METADATA");
		private GranuleArchiveType(String type) {}
	}

	public enum GranuleArchiveStatus {
		ONLINE("ONLINE"), CORRUPTED("CORRUPTED"), DELETED("DELETED"), MISSING("MISSING"), IN_PROCESS("IN-PROCESS"), ANOMALY("ANOMALY"); 	
		private final String type;
		private GranuleArchiveStatus(String type) {
			this.type = type;
		}
		public String toString(){
			return this.type;
		}
	};

	public enum AppendBasePathType {
		YEAR_MONTH_DAY("YEAR-MONTH-DAY"),YEAR_DOY("YEAR-DOY"), YEAR("YEAR"), YEAR_MONTH("YEAR-MONTH"), YEAR_WEEK("YEAR-WEEK"), BATCH("BATCH"), CYCLE("CYCLE"), NONE("NONE"); 	
		private final String type;
		private AppendBasePathType(String type) {
			this.type = type;
		}
		public String toString(){
			return this.type;
		}
	};

	public static String appendArchiveSubDir(String basePathAppendType, Date startTime, Integer cycle ) throws Exception {
		//		String basePathAppendType = 
		//		this.granule.getDataset().getDatasetPolicy().getBasePathAppendType();

		String versionString = "";
		//	if(dsVersioned.equals("Y")){
		//		versionString = metadata.getVersion() + File.separator;
		//	}

		if (basePathAppendType.equals(AppendBasePathType.YEAR_DOY.toString())) {
			TimeZone gmt = TimeZone.getTimeZone("GMT");
			Calendar tempCal = Calendar.getInstance();
			tempCal.setTime(startTime);
			GregorianCalendar cal = new GregorianCalendar(gmt);
			cal.setTime(startTime);
			String zero="";
			if(cal.get(Calendar.DAY_OF_YEAR) < 100)
				zero="0";
			if(cal.get(Calendar.DAY_OF_YEAR) < 10)
				zero="00";
			return ""+versionString  +cal.get(Calendar.YEAR) + "/"  +zero+ cal.get(Calendar.DAY_OF_YEAR);


		}
		else if (basePathAppendType.equals(AppendBasePathType.YEAR_MONTH_DAY.toString())) {
			//Date startTime = this.granule.getStartTime();
			TimeZone gmt = TimeZone.getTimeZone("GMT");
			Calendar tempCal = Calendar.getInstance();
			tempCal.setTime(startTime);
			GregorianCalendar cal = new GregorianCalendar(gmt);
			cal.setTime(startTime);

			String dayZero="";
			String monthZero="";
			if(cal.get(Calendar.DAY_OF_MONTH) < 10)
				dayZero="0";
			if(cal.get(Calendar.MONTH)+1 < 10){
				monthZero="0"; 
			}

			return ""+versionString  +cal.get(Calendar.YEAR) + "/"  +monthZero.toString()+ (cal.get(Calendar.MONTH) +1) + "/"  +dayZero.toString()+ cal.get(Calendar.DAY_OF_MONTH);

		} 
		else if (basePathAppendType.equals(AppendBasePathType.YEAR.toString())) {
			//Date startTime = this.granule.getStartTime();
			TimeZone gmt = TimeZone.getTimeZone("GMT");
			Calendar tempCal = Calendar.getInstance();
			tempCal.setTime(startTime);
			GregorianCalendar cal = new GregorianCalendar(gmt);
			cal.setTime(startTime);
			//System.out.println("cal_value:" + cal.get(Calendar.YEAR));
			//System.out.println("date_value:" + new SimpleDateFormat("yyyy").format(startTime).toString());
			return ""+versionString  +cal.get(Calendar.YEAR);
			//return new SimpleDateFormat("yyyy").format(startTime).toString();

		} else if (basePathAppendType.equals(AppendBasePathType.BATCH.toString())) {
			// need batch number from sip header so this is filled at InventoryImpl
			return ""+versionString ;
		} else if (basePathAppendType.equals(AppendBasePathType.YEAR_WEEK.toString())) {
			//week is value 1-52
			//Date startTime = this.granule.getStartTime();
			TimeZone gmt = TimeZone.getTimeZone("GMT");
			Calendar tempCal = Calendar.getInstance();
			tempCal.setTime(startTime);
			GregorianCalendar cal = new GregorianCalendar(gmt);
			cal.setTime(startTime);	
			String zero = "0";
			if(cal.get(Calendar.WEEK_OF_YEAR) > 10)
			{
				zero="";
			}

			return ""+versionString  +cal.get(Calendar.YEAR) + "/"+zero+  + cal.get(Calendar.WEEK_OF_YEAR);
		} else if (basePathAppendType.equals(AppendBasePathType.YEAR_MONTH.toString())) {
			//week is value 1-52
			//Date startTime = this.granule.getStartTime();
			TimeZone gmt = TimeZone.getTimeZone("GMT");
			Calendar tempCal = Calendar.getInstance();
			tempCal.setTime(startTime);
			GregorianCalendar cal = new GregorianCalendar(gmt);
			cal.setTime(startTime);	
			String zero = "0";
			if(cal.get(Calendar.MONTH)+1 >= 10)
			{
				zero="";
			}

			return ""+versionString  +cal.get(Calendar.YEAR) + "/" + zero + (cal.get(Calendar.MONTH)+1);
		} else if (basePathAppendType.equals(AppendBasePathType.CYCLE.toString())) {
			try {
				//Integer cycle = metadata.getCycle();
				if (cycle==null) throw new Exception("Cycle null!");
				return versionString + "c"+String.format("%03d",cycle);		
			} catch (NullPointerException npe) {
				throw new Exception("Cycle NPE!");
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}		
		} else if (basePathAppendType.equals(AppendBasePathType.NONE.toString())) {
			return "";
		}
		else{
			return null;
		}
	}



	public enum LocationPolicyType {
		ARCHIVE_PUBLIC("ARCHIVE-PUBLIC"), ARCHIVE_PRIVATE("ARCHIVE-PRIVATE"), ARCHIVE_RESTRICTED("ARCHIVE-RESTRICTED"),
		ARCHIVE("ARCHIVE"), LOCAL_FTP("LOCAL-FTP"), LOCAL_OPENDAP("LOCAL-OPENDAP"), LOCAL("LOCAL"), 
		REMOTE_FTP("REMOTE-FTP"),REMOTE_HTTP("REMOTE-HTTP"), REMOTE_OPENDAP("REMOTE-OPENDAP"), REMOTE("REMOTE"), ARCHIVE_PREVIEW("ARCHIVE-PREVIEW"), 
		ARCHIVE_OPEN("ARCHIVE-OPEN"), ARCHIVE_SIMULATED("ARCHIVE-SIMULATED"), ARCHIVE_RETIRED("ARCHIVE-RETIRED"), ARCHIVE_REDUCED("ARCHIVE-REDUCED"), 
		ARCHIVE_SHARED("ARCHIVE-SHARED"), ARCHIVE_CONTROLLED("ARCHIVE-CONTROLLED"), LOCAL_HTTP("LOCAL-HTTP"), OPENDAP("OPENDAP");
		private final String type;
		private LocationPolicyType(String type) {
			this.type = type;
		}
		public String toString(){
			return this.type;
		}
	}

}
