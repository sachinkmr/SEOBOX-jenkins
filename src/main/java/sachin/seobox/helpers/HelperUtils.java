package sachin.seobox.helpers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;
import sachin.seobox.crawler.CrawlerConstants;
import sachin.seobox.reporter.ComplexReportFactory;

public class HelperUtils {

	protected static final Logger logger = LoggerFactory.getLogger(HelperUtils.class);

	/**
	 * Method returns the unique string based on time stamp
	 *
	 *
	 * @return unique string
	 */
	public static String generateUniqueString() {
		DateFormat df = new SimpleDateFormat("dd-MMMM-yyyy");
		DateFormat df1 = new SimpleDateFormat("hh-mm-ss-SSaa");
		Calendar calobj = Calendar.getInstance();
		String time = df1.format(calobj.getTime());
		String date = df.format(calobj.getTime());
		return date + "_" + time;
	}

	/**
	 * Method to get current time.
	 *
	 * @return Date date object of current time
	 *
	 **/
	public static Date getTime(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar.getTime();
	}

	public static Response getLinkResponse(String... data) throws ParseException, ClientProtocolException, IOException {
		Request request = Request.Get(data[0]).addHeader("user-agent", CrawlerConstants.PROPERTIES.getProperty("crawler.userAgentString", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:48.0) Gecko/20100101 Firefox/48.0")).addHeader("Accept-Encoding", "gzip, compress, deflate, br, identity, exi, pack200-gzip, bzip2, lzma, peerdist, sdch, xpress, xz").connectTimeout(Integer.parseInt(CrawlerConstants.PROPERTIES.getProperty("crawler.connectionTimeout", "120000"))).socketTimeout(Integer.parseInt(CrawlerConstants.PROPERTIES.getProperty("crawler.connectionTimeout", "120000")));
		if (data.length > 1 && null != data[1] && !data[1].trim().isEmpty()) {
			String login = data[1] + ":" + data[2];
			String base64login = new String(Base64.encodeBase64(login.getBytes()));
			request.addHeader("Authorization", "Basic " + base64login);
		}
		return request.execute();
	}

	public static String getResourceFile(String fileName) {
		File file = null;
		try {
			String str = IOUtils.toString(HelperUtils.class.getClassLoader().getResourceAsStream(fileName));
			file = new File(new File(CrawlerConstants.DATA_LOCATION).getParentFile(), fileName);
			FileUtils.write(file, str, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}

	public static String getSiteAddress(String address) {
		String add = URLCanonicalizer.getCanonicalURL(address);
		WebURL url = new WebURL();
		url.setURL(add);
		String domain = url.getDomain();
		String site = add.substring(0, add.indexOf(domain) + domain.length() + 1);
		return site;
	}

	public static String getResourceFile(String fileName, String pROPERTIES_LOC) {
		File file = null;
		try {
			String str = FileUtils.readFileToString(new File(pROPERTIES_LOC), "UTF-8");
			file = new File(CrawlerConstants.CRAWL_STORAGE_FOLDER, fileName);
			FileUtils.write(file, str, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}

	public static ExtentTest getTestLogger(Method caller) {
		Test anno = caller.getAnnotation(Test.class);
		ExtentTest test = ComplexReportFactory.getInstance().getTest(anno.testName());
		test.setDescription(anno.description());
		test.setStartedTime(getTestCaseTime(System.currentTimeMillis()));
		test.assignCategory(anno.groups());
		System.out.println("Executing: " + anno.testName());
		return test;
	}

	public static Date getTestCaseTime(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar.getTime();
	}

	public static String getUUID() {
		String str = new String("");
		for (char a : UUID.randomUUID().toString().replaceAll("-", "").toCharArray()) {
			if (Character.isDigit(a)) {
				str += Character.toString(Character.toChars(97 + Character.getNumericValue(a))[0]);
			} else {
				str += Character.toString(a);
			}
		}
		return str;
	}

	public static LogStatus getPageSpeedTestStatus(String m, String d) {
		Set<Integer> result = new HashSet<>();
		JSONObject mobile = new JSONObject(m);
		JSONObject desktop = new JSONObject(d);
		result.add(mobile.getJSONObject("SPEED").getInt("score"));
		result.add(mobile.getJSONObject("USABILITY").getInt("score"));
		result.add(desktop.getJSONObject("SPEED").getInt("score"));
		for (int score : result) {
			if (score < CrawlerConstants.PAGE_SPEED_PASS_POINTS) {
				return LogStatus.FAIL;
			}
		}
		return LogStatus.PASS;
	}

	public static Set<String> getTestCasesNames() {
		Set<String> set = new HashSet<>();
		try {
			Set<ClassInfo> classes = ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClasses("sachin.seobox.seo");
			for (ClassInfo info : classes) {
				Method[] methods = Class.forName(info.getName()).getDeclaredMethods();
				for (Method method : methods) {
					if (method.isAnnotationPresent(Test.class)) {
						Test test = method.getAnnotation(Test.class);
						if (test.enabled())
							set.add(method.getName());
					}
				}
			}
		} catch (Exception e) {

		}
		return set;
	}

	public static Response getFluentResponse(String... data) throws ClientProtocolException, IOException {
		String add = URLCanonicalizer.getCanonicalURL(data[0]);
		Request request = Request.Get(add).addHeader("user-agent", CrawlerConstants.PROPERTIES.getProperty("crawler.userAgentString", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:48.0) Gecko/20100101 Firefox/48.0")).connectTimeout(Integer.parseInt(CrawlerConstants.PROPERTIES.getProperty("crawler.connectionTimeout", "120000"))).socketTimeout(Integer.parseInt(CrawlerConstants.PROPERTIES.getProperty("crawler.connectionTimeout", "120000")));
		if (data.length > 1 && null != data[1] && !data[1].trim().isEmpty()) {
			String login = data[1] + ":" + data[2];
			String base64login = new String(Base64.encodeBase64(login.getBytes()));
			request.addHeader("Authorization", "Basic " + base64login);
		}
		return request.execute();
	}

	public static LogStatus getstructuredDataStatus(int errors, int warnings) {
		if (errors > 0) {
			return LogStatus.FAIL;
		} else if (warnings > 0) {
			return LogStatus.WARNING;
		} else {
			return LogStatus.PASS;
		}
	}

	public static String getStructuredDataMicros(JSONArray json) {
		Set<String> set = new HashSet<>();
		for (int i = 0; i < json.length(); i++) {
			set.add(json.getJSONObject(i).getString("type"));
		}
		if (set.isEmpty()) {
			return "";
		} else {
			return set.toString().substring(1, set.toString().length() - 1);
		}

	}

}
