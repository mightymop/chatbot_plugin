package de.mopsdom.openfire.plugins.chatbot;

import java.io.File;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alicebot.ab.Bot;
import org.alicebot.ab.configuration.BotConfiguration;
import org.alicebot.ab.configuration.LanguageConfiguration;
import org.jivesoftware.openfire.ConnectionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.ClusterEventListener;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.container.PluginManagerListener;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.spi.ConnectionListener;
import org.jivesoftware.openfire.spi.ConnectionManagerImpl;
import org.jivesoftware.openfire.spi.ConnectionType;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.jivesoftware.util.SystemProperty;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 */
public class ChatbotPlugin implements Plugin, PropertyEventListener,

        ClusterEventListener, org.jivesoftware.smack.ConnectionListener {
    private static final Logger Log = LoggerFactory.getLogger(ChatbotPlugin.class);
    
    /*public static SystemProperty<String> BOTUSER;

    public static SystemProperty<String> BOTPASS;

    public static SystemProperty<Boolean> LASTSTATE;
    
    public static SystemProperty<String> BOTNICK;*/

    private long lastInit = 0;

    private XMPPTCPConnection con;
    private MultiUserChatManager manager;
    private PluginManager pmgr;

    private static ChatbotPlugin instance;

    private boolean inprogress = false;

    public final static String[] leavemessages_self = { "Ich muss mal weg.", "Tschööö mit ö!", "Au Revoir!", "Und wech...",
            "Es ist nicht alle Tage, ich komm wieder keine Frage.",
            "Man hat nach einem Helden verlangt, bin gleich zurück." };

    public final static String[] joinmessages = { "Da bin ich wieder.", "Und wer hat mich vermisst?",
            "JA JA JA HIER KOMM ICH UND RETTE DICH!!!", "Moin Moin.", "Zack drin... Was hab ich verpasst?",
            "ACHTUNG! Der König ist zurück." };
    
    public final static String[] dankemessages = { "Vielen lieben Danke!", "So jetzt bin ich mal dran. :D",
            "Aufgepasst, nun hab ich das Sagen!", "Jetzt wird hier nach meiner Pfeife getanzt.",
            "Ich werde meiner Rolle versuchen gerecht zu werden.", "Wird ja auch mal Zeit." };

    public final static String[] doofmessages = { "Och Menno! :/", "Was hab ich falsch gemacht?",
            "Na mal sehen wer das besser hinbekommt.", "Es war einen Versuch wert.",
            "Mal gewinnt man, mal verliert man.", "Nächstes mal gehört die Weltherrschaft mir." };
    
    

    public final static String[] ownerbegruessung = { "Sei gegrüßt ehrenwerter %OWNER% !", "@%OWNER%, hallo Chef!",
            "Oha der heilige %OWNER% ist auch da.", "Hey %OWNER%, du siehst heut wieder echt gut aus ;)",
            "HIGH 5 @%OWNER% !", "ACHTUNG! Verneigt euch vor dem Raumbesitzer! Man nennt ihn auch %OWNER% !" };

    public final static String[] adminbegruessung = { "Guten Tag %ADMIN% !", "@%ADMIN%, hallo!",
            "Oha %ADMIN% ist ja auch da.", ".oO(Ob der %ADMIN% mich schon bemerkt hat?) HALLI HALLO!",
            "Hey @%ADMIN% was geht ab?!", "ACHTUNG! Still gestanden %ADMIN% ist da !" };

    public final static String[] restbegruessung = { "Hallo!", ", schön dass du da bist!", "Willkommen im Raum!",
            "Fühl dich wie zu Haus.", "Was geht ab?!", "Die Regeln sind dir sicherlich bekannt!" };

    public final static String[] leavemessages_other = { "Da hat %LEAVE% einfach den Raum verlassen.",
            "Mist, konnte %LEAVE% nicht mal verabschieden.", "Vielleicht sagt %LEAVE% nächstes mal Tschüss?",
            "%LEAVE% war wieder mal schneller...", ".oO(%LEAVE% kommte bestimmt bald wieder?!)", "Bis Bald %LEAVE%!" };

    public final static String[] kickmessage = { "Na wer hat denn da die Regeln missachtet? Genau es war %KICK%!",
            "%KICK% lernt gerade fliegen.", "%KICK% wurde gegangen.", "Weg war er...", "Upps... :D",
            "Immer die selben..." };

    public final static String[] banmessage = { "%BAN% hat die Strafe verdient.", "Das hat %BAN% davon.",
            "HAHA @ %BAN%.", "So schnell kommt %BAN% sicher nicht wieder.", "%BAN% nimmt ne kleine Auszeit.",
            "Zack und weg..." };

    public final static String[] nickchange = { "Aufgepasst, %NICK1% war beim Amt und heißt nun %NICK2%.",
            "Hmm @%NICK2%, irgendwie war der alte besser...",
            "Immer diese Namensänderungen, da sieht doch keiner mehr durch!?",
            "@%NICK% steht der auch schon auf dem Ausweis?", "Oh neuer Künstlername %NICK2%?",
            "Mal was neues was? @%NICK2%" };

    public final static String START_OWNER_GREET_ON_JOIN = "Sie an es gibts sie also doch. Dann mal der Reihe nach, zuerst die Helden...";
    public final static String START_ADMIN_GREET_ON_JOIN = "Ach sie an. Adelsvertreter sind anwesend...";
    public final static String START_MEMBER_GREET_ON_JOIN = "Hallo Rest :D";

    public HashMap<String, Room> rooms = new HashMap<String, Room>();

    private boolean useJoinMessagesOwners = true;
    private boolean useJoinMessagesAdmin = true;
    private boolean useJoinMessagesRest = true;

    private boolean useLeaveMessages = true;
    private boolean useLeaveKickMessages = true;
    private boolean useLeaveBanMessages = true;

    private boolean useNickchangeMessages = true;
    
    private IncomingChatMessageListener recListener;
    
    private ChatManager chatManager = null;

    private String botnick = null;
    private boolean isrunning = false;

    private String selfbarejid;
    
    public String aimlpath;
    
    public static String SETTING_BOTUSER = "plugin.chatbot.botuser";
    public static String SETTING_BOTPASS = "plugin.chatbot.botpass";
    public static String SETTING_STATE = "plugin.chatbot.laststate.running";
    public static String SETTING_BOTNICK = "plugin.chatbot.botnick";
    public static String SETTING_BOTRANDOM = "plugin.chatbot.random";

    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        PropertyEventDispatcher.addListener(this);
        File dirclasses = new File (pluginDirectory,"classes");
        /*File dirbots = new File(dirclasses,"bots");
        File diralice = new File(dirbots,"alice");
        File diraiml = new File(diralice,"aiml");**/
        aimlpath=new File(dirclasses,"de").getAbsolutePath();//diraiml.getAbsolutePath();
        pmgr=manager;
        instance = this;
       /* try
        {
            BOTUSER = SystemProperty.Builder.ofType(String.class)
                    .setKey("plugin.chatbot.botuser")
                    .setEncrypted(true)
                    .setDefaultValue(null)
                    .setDynamic(false)
                    .build();

            BOTPASS = SystemProperty.Builder.ofType(String.class)
                    .setKey("plugin.chatbot.botpass")
                    .setEncrypted(true)
                    .setDefaultValue(null)
                    .setDynamic(false)
                    .build();

            LASTSTATE = SystemProperty.Builder.ofType(Boolean.class)
                    .setKey("plugin.chatbot.laststate.running")
                    .setDefaultValue(false)
                    .setDynamic(false)
                    .build();

            BOTNICK = SystemProperty.Builder.ofType(String.class)                      
                    .setKey("plugin.chatbot.botnick")
                    .setEncrypted(false)
                    .setDefaultValue(null)
                    .setDynamic(false)
                    .build();
        }
        catch (Exception e)
        {
            Log.warn(e.getMessage());
            for (SystemProperty prop : SystemProperty.getProperties())
            {
                if (prop.getKey().equalsIgnoreCase("plugin.chatbot.botuser"))
                {
                    BOTUSER=prop;
                }
                if (prop.getKey().equalsIgnoreCase("plugin.chatbot.botpass"))
                {
                    BOTPASS=prop;
                }
                if (prop.getKey().equalsIgnoreCase("plugin.chatbot.laststate.running"))
                {
                    LASTSTATE=prop;
                }
                if (prop.getKey().equalsIgnoreCase("plugin.chatbot.botnick"))
                {
                    BOTNICK=prop;
                }
            }
        }*/

        if (ensureBotUser()) {
            Log.info("Chatbot user configured");
            //if (LASTSTATE.getValue())
            if (JiveGlobals.getBooleanProperty(SETTING_STATE,false))
            {
                Log.info("Laststate = true, try to start Bot");
                init();
            }
        }
    }

    public static boolean isRunning()
    {
        return ChatbotPlugin.getInstance().isrunning;
    }

    public static ChatbotPlugin getInstance() {
        return instance;
    }

    @Override
    public void destroyPlugin() {
        PropertyEventDispatcher.removeListener(this);
        //SystemProperty.removePropertiesForPlugin("Chatbot");
        //SystemProperty.removePropertiesForPlugin("chatbot");
      
        shutdown();
    }

    public void shutdown() {
        if (con != null) {
            if (lastInit + 10000 > System.currentTimeMillis()) {
                return;
            }

            chatManager.removeIncomingListener(recListener);
            
            Log.info("Fahre Bot herunter!");
            for (String strroom : rooms.keySet()) {
                Room room = rooms.get(strroom);
                Log.info("Verlasse Raum: " + room.jid);
                try {                   
                    String leavemessage = leavemessages_self[getRandomIndex(leavemessages_self.length)];
                    room.muc.sendMessage(leavemessage);
                    room.muc.leave();
                } catch (Exception e) {
                    Log.error("Fehler beim Verlassen des Raums: " + strroom);
                }
            }

            rooms.clear();
            Log.info("Schließe Verbindung zum Server!");
            con.disconnect();
            inprogress = false;
        }
    }

    public void init() {
        if (isAcceptingClientConnections()) {
            if (inprogress || lastInit + 10000 > System.currentTimeMillis()) {
                return;
            }
            refreshSettings();
            lastInit = System.currentTimeMillis();
            inprogress = true;
            botnick = JiveGlobals.getProperty(SETTING_BOTNICK,null);//BOTNICK.getValue();
            selfbarejid = JiveGlobals.getProperty(SETTING_BOTUSER,null)/*BOTUSER.getValue()*/ + "@"
                    + XMPPServer.getInstance().getServerInfo().getXMPPDomain();
            try {
                Log.info("Verbinde zum Server als User: " + selfbarejid);
                con = new XMPPTCPConnection(selfbarejid, JiveGlobals.getProperty(SETTING_BOTPASS,null)/*BOTPASS.getValue()*/);

                con.setUseStreamManagement(true);
                con.setUseStreamManagementResumption(true);
                con.addConnectionListener(this);
                con.connect();
            } catch (Exception e) {
                try {
                    Log.error("Fehler beim Verbinden des Bots zum Server! Versuche Localhost! " + e.getMessage());
                    XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder()
                            .setUsernameAndPassword(JiveGlobals.getProperty(SETTING_BOTUSER,null)/*BOTUSER.getValue()*/,
                                    JiveGlobals.getProperty(SETTING_BOTPASS,null)/*BOTPASS.getValue()*/).setResource(String.valueOf(System.currentTimeMillis()))
                            .setPort( JiveGlobals.getIntProperty(" xmpp.socket.plain.port", 5222)).setConnectTimeout(5000).setXmppDomain(XMPPServer.getInstance().getServerInfo().getXMPPDomain())
                            .setHost("localhost").setHostAddress(InetAddress.getLocalHost()) .setSecurityMode(SecurityMode.disabled).setSendPresence(true);
                 
                    XMPPTCPConnectionConfiguration config = builder.build();

                    con = new XMPPTCPConnection(config);

                    con.setUseStreamManagement(true);
                    con.setUseStreamManagementResumption(true);
                    con.addConnectionListener(this);
                    con.connect();
                } catch (Exception ex) {
                    Log.error("Fehler beim Verbinden des Bots zum Server! " + ex.getMessage());
                    con = null;
                    inprogress = false;
                    return;
                }
            }

        } else {
            inprogress = false;
            Log.error("Der Server akzeptiert keine Verbindungen!");
        }
    }

    private void init1to1()
    {
        Bot bot = null;
        final org.alicebot.ab.Chat chatSession;
        
        LanguageConfiguration lang = LanguageConfiguration.builder()
                .defaultResponse(JiveGlobals.getProperty("plugin.chatbot.aiml.default",Room.BOT_AIML_DEFAULT_ANSWER))
                .errorResponse(JiveGlobals.getProperty("plugin.chatbot.aiml.error",Room.BOT_AIML_ERROR_ANSWER))
                .scheduleError(JiveGlobals.getProperty("plugin.chatbot.aiml.scheduleerror",Room.BOT_AIML_ERROR_ANSWER))
                .systemFailed(JiveGlobals.getProperty("plugin.chatbot.aiml.systemfailed",Room.BOT_AIML_ERROR_ANSWER))
                .templateFailed(JiveGlobals.getProperty("plugin.chatbot.aiml.templatefailed",Room.BOT_AIML_ERROR_ANSWER))
                .tooMuchLooping(JiveGlobals.getProperty("plugin.chatbot.aiml.toomuchlooping",Room.BOT_AIML_ERROR_ANSWER))
                .tooMuchRecursion(JiveGlobals.getProperty("plugin.chatbot.aiml.toomuchrecursion",Room.BOT_AIML_ERROR_ANSWER))
                .build();

        Log.info("Using Aimlpath: "+JiveGlobals.getProperty("plugin.chatbot.res_path",aimlpath));

        BotConfiguration bconfig = BotConfiguration.builder()
                .defaultLanguage("de")
                .name("alice")
                .path(JiveGlobals.getProperty("plugin.chatbot.res_path",aimlpath))
                .enableExternalMaps(true)
                .enableExternalSets(true)
                .enableNetworkConnection(true)
                .enableSystemTag(true)
                .graphShortCuts(true)
                .language(lang)
                .build();

        bot = new Bot(bconfig);
        chatSession = new org.alicebot.ab.Chat(bot);
        bot.getBrain().nodeStats();

        chatManager = ChatManager.getInstanceFor(con);
        
        recListener = new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid fromjid, Message message, Chat chat) {
                String from = message.getFrom().toString();

                if (fromjid.toString().equalsIgnoreCase(selfbarejid)||from.equalsIgnoreCase(selfbarejid)) {
                    Log.debug("IGNORIERE (EIGENE) NACHRICHT!");
                    return;
                }

                String to = message.getTo().toString();
                String body = message.getBody();
                String q = body!=null?body.replaceAll("[ ]{2,}", " "):null;
                String aimlq = q;
                String sendbody = "";
                String search = q!=null?q.replaceAll("[^0-9a-zA-ZäöüßÄÖÜ\\s]", ""):null;

                if (q==null||q.length()==0)
                    return;

                if (search!=null&&search.toLowerCase().contains("heute ist ein guter tag zum sterben"))
                {
                    Log.warn("KILL SWITCH FÜR BOT ERKANNT");
                    try {
                            chat.send("Immer muss ich dran glauben... :/");
                    } catch (Exception e) {
                        Log.error("Der Bot konnte keine Antwort and " + fromjid + " schicken. " + e.getMessage());
                    }
                    shutdown();
                    return;
                }

                

                if (search!=null&&search.trim().length()>2)
                {
                    String result = DatabaseUtils.getQA(search);
                    if (result != null) {
                        try
                        {
                            JSONArray arr = new JSONArray(result);
        
                            if (arr.length() > 0) {
                                int idx = getRandomIndex(arr.length());
                                sendbody = arr.getJSONObject(idx).getString("a");
                                sendbody = filter(sendbody,"",
                                        search.split("\\s").length==1?search:null);
                            }
                        }
                        catch (Exception e)
                        {
                            
                        }
                    }
                }
                
                if (sendbody==null||sendbody.trim().length()==0)
                    sendbody = chatSession.multisentenceRespond(aimlq);

                if (sendbody.trim().length()==0||sendbody.contains(Room.BOT_AIML_DEFAULT_ANSWER)||sendbody.contains(Room.BOT_AIML_ERROR_ANSWER)) {
                   
                    try {
                       
                        if (search.contains("\"")) {
                            search = "";
                            Pattern p = Pattern.compile("\"([^\"]*)\"");
                            Matcher m = p.matcher(q);
                            while (m.find()) {
                                if (m.group(1).length() > search.length()) {
                                    search = m.group(1);
                                }
                            }
                            search = "\"" + search + "\"";
                        } else if (q.split("\\s").length > 1) {
                            search = "";
                            for (int n = 0; n < q.split("\\s").length; n++) {
                                if (q.split("\\s")[n].length() > search.length()) {
                                    search = q.split("\\s")[n];
                                }
                            }
                        }
    
                        if (search!=null&&search.trim().length()>2)
                        {
                            String url = "https://de.wikipedia.org/w/api.php?action=opensearch&limit=2&format=json&search="
                                    + URLEncoder.encode(search, "UTF-8");
        
                            Log.debug("Suche online: " + url);
                            OkHttpClient okHttp = NetUtils.getHttpClient(url);
        
                            Request request = new Request.Builder().url(url).get().build();
                            Call call = okHttp.newCall(request);
                            Response response = call.execute();
                            if (response.isSuccessful()) {
                                String resstr = new String(response.body().bytes());
                                Log.debug("RESULT 200: " + resstr);
                                JSONArray resarr = new JSONArray(resstr);
                                String searchword = (String) resarr.getString(0);
                                String word1 = ((JSONArray) resarr.get(1)).length() == 2
                                        ? ((JSONArray) resarr.get(1)).getString(0)
                                        : null;
                                String word2 = ((JSONArray) resarr.get(1)).length() == 2
                                        ? ((JSONArray) resarr.get(1)).getString(1)
                                        : null;
        
                                if (word1 != null && word2 != null) {
                                    String site1 = ((JSONArray) resarr.get(1)).length() == 2
                                            ? ((JSONArray) resarr.get(3)).getString(0)
                                            : null;
                                    String site2 = ((JSONArray) resarr.get(1)).length() == 2
                                            ? ((JSONArray) resarr.get(3)).getString(1)
                                            : null;
        
                                    sendbody += "Habe folgendes zu " + searchword + " gefunden. " + word1 + " oder " + word2
                                            + "? Dann schau mal hier: " + site1 + " oder " + site2 + " !";
                                } else if (((JSONArray) resarr.get(1)).length() == 1) {
                                    String site1 = ((JSONArray) resarr.get(3)).getString(0);
        
                                    sendbody += "Habe folgendes zu " + searchword + " gefunden. schau mal hier: " + site1
                                            + " !";
                                } else {
                                    sendbody += " Online habe ich nix gefunden. Sorry!";
                                }
                            } else {
                                
                                sendbody += " Sorry, hab versucht online zu suchen, aber mein Ansprechpartner hat ein Fehler zurückgegeben!";
        
                                Log.debug("Nix gefunden online.");
                            }
                        }
                    } catch (Exception e) {
                        Log.error("Konnte online nicht nach Antworten suchen...! " + e.getMessage());
                        sendbody += " Sorry, konnte online nicht nachgucken! Gibt da ein Problem mit der Verbindung in die Außenwelt.";
                    }
                   
                }

                try {
                    if (sendbody!=null&&sendbody.trim().length()>0)
                    {
                        chat.send(sendbody);
                        Log.debug("from: " + from + " to: " + to + " q: " + q + " a: " + sendbody);
                    }
                } catch (Exception e) {
                    Log.error("Der Bot konnte keine Antwort in den Raum " + fromjid + " schicken. " + e.getMessage());
                }
                
            }
        };

        chatManager.addIncomingListener(recListener);
    }

    private void joinRooms() {
        JSONArray jsonrooms = new JSONArray(getRooms());
        // ChatManager chatManager = ChatManager.getInstanceFor(con);
        manager = org.jivesoftware.smackx.muc.MultiUserChatManager.getInstanceFor(con);

        for (int n = 0; n < jsonrooms.length(); n++) {
            try {
                String strjidmuc = jsonrooms.getJSONObject(n).getString("roomjid");

                if ( jsonrooms.getJSONObject(n).getBoolean("aktiv"))
                {
                    org.xmpp.packet.JID jid = new org.xmpp.packet.JID(strjidmuc);
                    MultiUserChat multiUserChat = manager.getMultiUserChat(JidCreate.entityBareFrom(strjidmuc));
                    Room r = joinRoom(multiUserChat, jid, jsonrooms.getJSONObject(n).getBoolean("qaonline"),
                            jsonrooms.getJSONObject(n).getBoolean("qadb"),
                            jsonrooms.getJSONObject(n).getBoolean("qaaiml"),
                            jsonrooms.getJSONObject(n).getInt("qarandom"));
                    if (r != null) {
                        Log.info("Add MessageListener to " + strjidmuc);
                        multiUserChat.addMessageListener(r);
                    } else {
                        Log.warn("Fehler beim Betreten eines Raumes durch den Bot: " + strjidmuc);
                    }
                }

            } catch (Exception e1) {
                Log.error("Fehler beim Betreten eines Raumes durch den Bot: " + e1.getMessage());
                continue;
            }
        }
    }

    public int getRandomIndex(int length) {
        int randomNum = ThreadLocalRandom.current().nextInt(0, length);
        return randomNum;
    }

    public Room joinRoom(MultiUserChat multiUserChat, org.xmpp.packet.JID mucjid, boolean qaonline, boolean qadb, boolean qaaiml, int qarandom)
            throws Exception {
        Log.debug("Erstelle Ressourcepart");
        Resourcepart ressource = Resourcepart.fromOrNull(botnick);

        Log.debug("Betrete den Raum: " + mucjid.toString());
        multiUserChat.join(ressource);

        Room mucroom = new Room(this);

        multiUserChat.addParticipantStatusListener(mucroom);
        multiUserChat.addUserStatusListener(mucroom);
        multiUserChat.addParticipantListener(mucroom);
        multiUserChat.addSubjectUpdatedListener(mucroom);

        try {
            String joinmessage = joinmessages[getRandomIndex(joinmessages.length)];
            multiUserChat.sendMessage(joinmessage);
        } catch (Exception e) {
            Log.error("Konnte eigene JOIN Nachricht nicht an den Raum " + mucjid.toString() + " senden.");
        }

        mucroom.jid = mucjid;
        mucroom.muc = multiUserChat;
        try {
            mucroom.subject = multiUserChat.getSubject();
        } catch (Exception e) {
            Log.error("Thema konnte vom Raum " + mucjid.toString() + " nicht geladen werden.");
        }

        mucroom.qaonline = qaonline;
        mucroom.qadb = qadb;
        mucroom.qaaiml = qaaiml;
        mucroom.qarandom = qarandom;
        rooms.put(mucjid.toString(), mucroom);

        ArrayList<String> owners = new ArrayList<String>();
        ArrayList<String> admins = new ArrayList<String>();

        for (org.xmpp.packet.JID owner : XMPPServer.getInstance().getAdmins())
        {
            owners.add(owner.toBareJID());
        }

        String servicestr = mucjid.getDomain().split("[.]")[0];

        MultiUserChatService mucservice = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(servicestr);

        mucroom.serverroom=mucservice.getChatRoom(mucjid.getNode()); 

        if (mucservice!=null)
        {
            for (org.xmpp.packet.JID owner : mucservice.getSysadmins())
            {
                if (!owners.contains(owner.toBareJID()))
                    owners.add(owner.toBareJID());
            }
        }

        if (mucroom.serverroom!=null)
        {
            for (org.xmpp.packet.JID owner : mucroom.serverroom.getOwners())
            {
                if (!owners.contains(owner.toBareJID()))
                {
                    owners.add(owner.toBareJID());
                }
            }
        }

        ArrayList<String> allreadysent = new ArrayList<String>();
        try
        {
            boolean sendintro=false;
            if (useJoinMessagesOwners)
            {
                for (MUCRole role : mucroom.serverroom.getOccupants())
                {
                    for (String strjid : owners)
                    {
                        if (role.getUserAddress().toBareJID().equalsIgnoreCase(strjid)&&!strjid.equalsIgnoreCase(selfbarejid)&&!allreadysent.contains(role.getUserAddress().toBareJID()))
                        {
                            if (!sendintro)
                            {
                                sendintro=true;
                                try
                                {
                                    multiUserChat.sendMessage(START_OWNER_GREET_ON_JOIN);
                                }catch (Exception e)
                                {
                                    Log.error("Konnte Intro-Ownerbegrüßung nicht senden! "+e.getMessage());
                                }
                            }

                            String[] messages =  JiveGlobals.getProperty("plugin.chatbot.joinmessagesowners", String.join("\n", ownerbegruessung)).split("\n"); 
                            String ownermsg = messages[getRandomIndex(messages.length)].replace("%OWNER%", role.getNickname());
                            allreadysent.add(role.getUserAddress().toBareJID());
                            try
                            {
                                multiUserChat.sendMessage(ownermsg);
                            }catch (Exception e)
                            {
                                Log.error("Konnte Ownerbegrüßung nicht senden! "+e.getMessage());
                            }
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.error("JOIN - OWNER - MESSAGE: " + e.getMessage());
            Log.error("Konnte eine Owner-Begrüßung nicht an den Raum " + mucjid.toString() + " senden.");
        }
        
        try
        {
            if (useJoinMessagesAdmin)
            {
                boolean sendintro=false;
                for (MUCRole role : mucroom.serverroom.getOccupants())
                {
                    for (String strjid : admins)
                    {
                        if (role.getUserAddress().toBareJID().equalsIgnoreCase(strjid)&&!strjid.equalsIgnoreCase(selfbarejid)&&!strjid.equalsIgnoreCase(selfbarejid)&&!allreadysent.contains(role.getUserAddress().toBareJID()))
                        {
                            if (!sendintro)
                            {
                                sendintro=true;
                                try
                                {
                                    multiUserChat.sendMessage(START_ADMIN_GREET_ON_JOIN);
                                }catch (Exception e)
                                {
                                    Log.error("Konnte Intro-Adminbegrüßung nicht senden! "+e.getMessage());
                                }
                            }
    
                            String[] messages =  JiveGlobals.getProperty("plugin.chatbot.joinmessagesowners", String.join("\n", adminbegruessung)).split("\n"); 
                            String adminmsg = messages[getRandomIndex(messages.length)].replace("%OWNER%", role.getNickname());
                            allreadysent.add(role.getUserAddress().toBareJID());
                            try
                            {
                                multiUserChat.sendMessage(adminmsg);
                            }catch (Exception e)
                            {
                                Log.error("Konnte Adminbegrüßung nicht senden! "+e.getMessage());
                            }
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.error("JOIN - ADMIN - MESSAGE: " + e.getMessage());
            Log.error("Konnte eine Admin-Begrüßung nicht an den Raum " + mucjid.toString() + " senden.");
        }
        
        try {
            if (useJoinMessagesRest&&mucroom.serverroom.getMembers().size()>0) {
                try
                {
                    multiUserChat.sendMessage(START_MEMBER_GREET_ON_JOIN);
                }catch (Exception e)
                {
                    Log.error("Konnte Memberbegrüßung nicht senden! "+e.getMessage());
                }
               
            }
        } catch (Exception e) {
            Log.error("JOIN - OTHER - MESSAGE: " + e.getMessage());
            Log.error("Konnte die Begrüßung an den Rest im Raum " + mucjid.toString() + " nicht senden.");
        }

        for (MUCRole role : mucroom.serverroom.getParticipants())
        {       
            UserJID jids = new UserJID();
            jids.partjid=role.getRoleAddress().toString();
            jids.realjid=role.getUserAddress().toString();
            jids.nick=role.getNickname();
            mucroom.nicks.put(role.getRoleAddress().toString(), jids);
        }
        return mucroom;
    }

    /**
     * Checks if the server is accepting client connections on the default c2s port.
     *
     * @return true if the server is accepting connections, otherwise false.
     */
    private static boolean isAcceptingClientConnections() {
        final ConnectionManager cm = XMPPServer.getInstance().getConnectionManager();
        if (cm != null) {
            final ConnectionManagerImpl cmi = ((ConnectionManagerImpl) cm);
            final ConnectionListener cl = cmi.getListener(ConnectionType.SOCKET_C2S, false);
            return cl != null && cl.getSocketAcceptor() != null;
        }
        return false;
    }

    private boolean ensureBotUser() {

        String botuser = JiveGlobals.getProperty(SETTING_BOTUSER,null)/*BOTUSER!=null?BOTUSER.getValue():null;*/;
        String botpass = JiveGlobals.getProperty(SETTING_BOTPASS,null)/*BOTPASS!=null?BOTPASS.getValue():null*/;

        if (botuser == null || botuser.trim().length() == 0 || botpass == null || botpass.trim().length() == 0) {
            Log.error("Es wurde kein User für den Bot konfiguriert oder es wurden nicht alle Daten angegeben!");
            return false;
        }

        final UserManager userManager = XMPPServer.getInstance().getUserManager();
        if (!userManager.isRegisteredUser(botuser)) {
            Log.error("Der für den Bot konfigurierte Benutzer existiert nicht!");
            return false;
        }

        return true;
    }

    public static boolean checkWordOrder(String str1, String str2) {
        Set<String> set1 = new TreeSet<String>(Arrays.asList(str1.split("\\s")));
        Set<String> set2 = new TreeSet<String>(Arrays.asList(str2.split("\\s")));
        return set1.equals(set2);
    }

    public static String getRooms() {

        org.jivesoftware.openfire.muc.MultiUserChatManager mucmgr = XMPPServer.getInstance().getMultiUserChatManager();
        JSONArray rooms = DatabaseUtils.getRooms();

        for (MultiUserChatService service : mucmgr.getMultiUserChatServices()) {

            for (MUCRoom room : service.getChatRooms()) {
                if (room.isPersistent())
                {
                    boolean found = false;

                    for (int n = 0; n < rooms.length(); n++) {
                        if (rooms.getJSONObject(n).getString("roomjid").equalsIgnoreCase(room.getJID().toBareJID())) {
                            found = true;
                            rooms.getJSONObject(n).put("name", room.getNaturalLanguageName());
                            rooms.getJSONObject(n).put("aktiv", true);
                            break;
                        }
                    }
                    if (!found) {
                       
                        JSONObject jsonroom = new JSONObject();
                        jsonroom.put("roomjid", room.getJID().toBareJID());
                        jsonroom.put("name", room.getNaturalLanguageName());
                        jsonroom.put("qaonline", false);
                        jsonroom.put("qadb", false);
                        jsonroom.put("qaaiml", false);
                        jsonroom.put("qarandom", -1);
                        jsonroom.put("aktiv", false);
                        rooms.put(jsonroom);
                    }
                }
            }
        }

        return rooms.toString();
    }

    public static void refresh() {
        ChatbotPlugin.getInstance().shutdown();
        ChatbotPlugin.getInstance().init();
    }

    public void refreshSettings() {
        useJoinMessagesOwners = JiveGlobals.getBooleanProperty("plugin.chatbot.usejoinmessagesowners", true);
        useJoinMessagesAdmin = JiveGlobals.getBooleanProperty("plugin.chatbot.usejoinmessagesadmin", true);
        useJoinMessagesRest = JiveGlobals.getBooleanProperty("plugin.chatbot.usejoinmessagesrest", true);
        useLeaveMessages = JiveGlobals.getBooleanProperty("plugin.chatbot.useleavemessages", true);
        useLeaveKickMessages = JiveGlobals.getBooleanProperty("plugin.chatbot.useleavekickmessages", true);
        useLeaveBanMessages = JiveGlobals.getBooleanProperty("plugin.chatbot.useleavebanmessages", true);
        useNickchangeMessages = JiveGlobals.getBooleanProperty("plugin.chatbot.usenickchangemessages", true);
    }

    @Override
    public void propertySet(String property, Map params) {
        if (property.startsWith("plugin.chatbot")&&!property.equals("plugin.chatbot.laststate.running")) {

            if (isrunning)
            {
                if (lastInit + 10000 < System.currentTimeMillis()) {
                    lastInit = System.currentTimeMillis();
                    Log.info("Einstellungsänderungen erkannt, reinitialisiere Bot.");
                    refreshSettings();
                    refresh();
                }
            }
            else
            {
                refreshSettings();
            }
        }
    }

    @Override
    public void propertyDeleted(String property, Map params) {

    }

    @Override
    public void xmlPropertySet(String property, Map params) {

    }

    @Override
    public void xmlPropertyDeleted(String property, Map params) {

    }

    @Override
    public void joinedCluster() {
        // TODO Auto-generated method stub

    }

    @Override
    public void joinedCluster(byte[] nodeID) {
    }

    @Override
    public void leftCluster() {
        shutdown();
    }

    @Override
    public void leftCluster(byte[] nodeID) {
    }

    @Override
    public void markedAsSeniorClusterMember() {
        init();
    }

    @Override
    public void connected(XMPPConnection connection) {
        // TODO Auto-generated method stub
        Log.info("Der Bot ist mit dem Server verbunden.");
        try {
            con.login();
        } catch (Exception e) {
            Log.error("Der Bot konnte sich nicht einloggen! " + e.getMessage());
        }
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        // TODO Auto-generated method stub
        Log.info("Der Bot wurde erfolgreich angemeldet!");
        //LASTSTATE.setValue(true);
        JiveGlobals.setProperty(SETTING_STATE,"true");
        isrunning=true;
        joinRooms();
        init1to1();
    }

    @Override
    public void connectionClosed() {
        Log.info("Die Verbindung vom Bot zum Server wurde geschlossen!");
        isrunning=false;
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.error("Der Bot hat unerwartet die Verbindung zum Server verloren! " + e.getMessage());
        try {
            isrunning=false;
            shutdown();
            init();
        } catch (Exception e1) {
            Log.error("Fehler beim Reconnect! " + e1.getMessage());
            inprogress = false;
        }
    }

    public boolean isUseJoinMessagesOwners() {
        return useJoinMessagesOwners;
    }

    public boolean isUseJoinMessagesAdmin() {
        return useJoinMessagesAdmin;
    }

    public boolean isUseJoinMessagesRest() {
        return useJoinMessagesRest;
    }

    public boolean isUseLeaveMessages() {
        return useLeaveMessages;
    }

    public boolean isUseLeaveMessagesKick() {
        return useLeaveKickMessages;
    }

    public boolean isUseLeaveMessagesBan() {
        return useLeaveBanMessages;
    }

    public boolean isUseNickchangeMessages() {
        return useNickchangeMessages;
    }

    public String getBotNick() {
        return botnick;
    }
    
    public String getBotUser() {
        return JiveGlobals.getProperty(SETTING_BOTUSER,"Bot");
    }
    
    public String filter(String sendbody, String sender, String singlesearchword) {
        sendbody = sendbody.replaceAll(
                "[^0-9a-zA-ZäöüÄÖÜß \"\\{\\}\\:\\*\\!\\?\\-\\+\\§\\$\\%\\&\\/\\\\\\(\\)\\[\\]\\~\\#\\'\\.\\,\\;\\<\\>\\|\\_]",
                "");
        if (sendbody.contains("%SINGLESEARCHWORD%"))
        {
            sendbody = sendbody.replace("%SINGLESEARCHWORD%", singlesearchword!=null?singlesearchword:"...");
        }
        if (sendbody.contains("%TIME%")) {
            SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");
            sendbody = sendbody.replace("%TIME%", formater.format(new Date()));
        }
        if (sendbody.contains("%DATE%")) {
            SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy");
            sendbody = sendbody.replace("%DATE%", formater.format(new Date()));
        }
        if (sendbody.contains("%DATETIME%")) {
            SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
            sendbody = sendbody.replace("%DATETIME%", formater.format(new Date()));
        }
        if (sendbody.contains("%SENDER%")) {
            sendbody = sendbody.replace("%SENDER%", sender);
        }

        return sendbody;
    }
}
