/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Tue Dec 31 15:59:32 EST 2024 */
package teal;

import java.util.Date;

/**
 * This class provides information gathered from the build environment.
 * 
 * @author JReleaseInfo AntTask
 */
public class Version {


   /** buildDate (set during build process to 1735678772224L). */
   private static Date buildDate = new Date(1735678772224L);

   /**
    * Get buildDate (set during build process to Tue Dec 31 15:59:32 EST 2024).
    * @return Date buildDate
    */
   public static final Date getBuildDate() { return buildDate; }


   /** project (set during build process to "TEALsim"). */
   private static String project = new String("TEALsim");

   /**
    * Get project (set during build process to "TEALsim").
    * @return String project
    */
   public static final String getProject() { return project; }


   /** year (set during build process to "${year}"). */
   private static String year = new String("${year}");

   /**
    * Get year (set during build process to "${year}").
    * @return String year
    */
   public static final String getYear() { return year; }


   /** version (set during build process to "v0.4"). */
   private static String version = new String("v0.4");

   /**
    * Get version (set during build process to "v0.4").
    * @return String version
    */
   public static final String getVersion() { return version; }


   /** buildTimestamp (set during build process to "12/31/2024 03:59 PM"). */
   private static String buildTimestamp = new String("12/31/2024 03:59 PM");

   /**
    * Get buildTimestamp (set during build process to "12/31/2024 03:59 PM").
    * @return String buildTimestamp
    */
   public static final String getBuildTimestamp() { return buildTimestamp; }

}
