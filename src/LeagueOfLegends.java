import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * PROGRESS LOG
 * 
 * August 4, 2015 
 * - Project Created
 * - getSummonerID and getChampion methods completed
 * 
 * September 6, 2015 
 * - getMatchlist created and completed
 * - changed getChampion to accept int as input
 * - setKey created for public security
 * 
 */

public class LeagueOfLegends 
{
	//Base URL for web query
	public static String baseURL = "https://na.api.pvp.net/api/lol";
	
	//API Key
	public static String key = "";
	
	//Region
	public static String region = "na";
	
	/**
	 * Static method that initializes the API key for the program.
	 * Store API key in a file called api.txt on E drive root.
	 * Initialize value by running at the start of the program.
	 */
	public static void setKey(){
		try
		{
			Scanner sc = new Scanner(new File("E:/api.txt"));
			key = sc.next();
		}
		catch (Exception e)
		{
			System.out.println("Get your own API key!");
		}
	}
	
	/**
	 * Given a summoner name, returns the corresponding summoner id number
	 * @param  
	 * @return summoner id as a string
	 */
	public static String getSummonerID(String sum_name)
	{
		String id = "";
		String URL = baseURL + "/" + region + "/v1.4/summoner/by-name/" + sum_name + "?api_key=" + key;
		try
		{
			Document summoner_call = Jsoup.connect(URL).ignoreContentType(true).get();
			String summoner_json = summoner_call.body().text();
	
			JsonParser jsonParser = new JsonParser();
			JsonObject sum = (JsonObject)jsonParser.parse(summoner_json).getAsJsonObject();
			id = sum.getAsJsonObject().get(sum_name).getAsJsonObject().get("id").getAsString();
		}
		catch(Exception e)
		{
			System.out.println("Something happened - getSummoner()");
			System.out.println(e.getMessage());
		}
		return id;
	}
	
	/**
	 * Given an int containing the champion id, returns the name of the champion
	 * @param id as integer
	 * @return champion name as a string
	 */
	public static String getChampion(int id)
	{
		String champion_name = "";
		try
		{
			Scanner sc = new Scanner(new File("./champion.json"));
			String champions = "";
			while(sc.hasNext())
			{
				champions += sc.next();
			}
			
			JsonParser jsonParser = new JsonParser();
			JsonObject sum = (JsonObject)jsonParser.parse(champions).getAsJsonObject();
			champion_name = sum.getAsJsonObject().get("keys").getAsJsonObject().get(id+"").getAsString();
			sc.close();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		return champion_name;
	}
	
	/**
	 * Given a summoner id, call matchlist to get the role counts of the specified player
	 * @param sum_id
	 * @return summoner role statistics as a hashmap Hash <\Role(String), NumPlayed(Integer)>.
	 * Keys: top, jungle, mid, adc, support, total, error (games not recognized by the API)
	 */
	public static Map<String, Integer> getMatchlist(String sum_id)
	{
		//Hashtable to be returned, and the temp variables to be placed into the hash
		Map<String, Integer> matchlist = new HashMap<String, Integer>();
		int[] num = new int[5]; //0 - Top, 1 - Jungle, 2 - Mid, 3 - ADC, 4 - Support
		int total = 0;
		
		//Parameters for the matchlist retrieval - soloq and 2015 season
		String queue = "rankedQueues=RANKED_SOLO_5x5";
		String season = "seasons=SEASON2015";
		
		//Form the URL for jsoup call
		String URL = baseURL + "/" + region + "/v2.2/matchlist/by-summoner/" + sum_id + "?" + queue + "&" + season + "&" + "api_key=" + key;
		
		//Call the Riot API
		try
		{
			//Retrieve raw data from the Riot API
			Document matchlist_call = Jsoup.connect(URL).ignoreContentType(true).get();
			String matchlist_raw = matchlist_call.body().text();
	
			//Parse the information using JsonParser - to collect total games number
			JsonParser jsonParser = new JsonParser();
			JsonObject matchlist_json = (JsonObject)jsonParser.parse(matchlist_raw).getAsJsonObject();
			
			//Populate the hashmap using a foreach loop
			int i = 0;
			int runningTotal = 0;
			String[] roles = {"TOP", "JUNGLE", "MID", "DUO_CARRY", "DUO_SUPPORT"};
			for(String role : roles)
			{
				String regex = role;
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(matchlist_raw);
				
				while (matcher.find())
				{
					num[i]++;
					runningTotal++;
				}
				matchlist.put(role, num[i]);
				i++;
			}
			total = matchlist_json.getAsJsonObject().get("totalGames").getAsInt();
			int error = Math.abs(total - runningTotal); //calculate games were role was not predicted by API
			matchlist.put("TOTAL",total);
			matchlist.put("ERROR", error);
		}
		//If something goes wrong ..
		catch(Exception e)
		{
			System.out.println("Something happened - getMatchlist()");
			System.out.println(e.getMessage());
		}
		return matchlist;
	}
	
	/**
	 * Given a summoner name, call current game to check if the player is in game.
	 * If so, retrieve all player information.
	 * If not, do nothing.
	 * @param sum_name
	 */
	public static void getCurrentGame(String sum_name)
	{
		try
		{
			Scanner sc = new Scanner(new File("./current_game.txt"));
			String cg = "";
			while(sc.hasNext()){
				cg += sc.next();
			}
			System.out.println(cg);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		/** - WORKS, create rest of code using current_game.txt
		String URL = "https://na.api.pvp.net/observer-mode/rest/consumer/getSpectatorGameInfo/" + region + "1/" + sum_name + "?api_key=" + key;
		try
		{
			Document current_call = Jsoup.connect(URL).ignoreContentType(true).get();
			System.out.println(current_call);
		}
		catch (Exception e)
		{
			System.out.println("Something happened - getCurrentGame()");
			System.out.println(e.getMessage());
		}
		**/
	}
	
	public static void main (String[] args)
	{
		//Scanner sc = new Scanner(System.in);
		//String sum_name;
		//System.out.print("Enter a summoner name: ");
		//sum_name = sc.next();
		setKey();
		String id = getSummonerID("norifurikake");
		System.out.println(id);
		System.out.println(getMatchlist(id));
		System.out.println(getChampion(35));
	}
}

