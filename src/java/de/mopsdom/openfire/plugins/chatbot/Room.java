package de.mopsdom.openfire.plugins.chatbot;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.configuration.BotConfiguration;
import org.alicebot.ab.configuration.LanguageConfiguration;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.util.JiveGlobals;
import org.json.JSONArray;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Resourcepart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Room implements ParticipantStatusListener, UserStatusListener, PresenceListener, MessageListener,
        SubjectUpdatedListener {

    private static final Logger Log = LoggerFactory.getLogger(Room.class);
    public MultiUserChat muc;
    public MUCRoom serverroom;
    public org.xmpp.packet.JID jid;
    public boolean qaonline;
    public boolean qadb;
    public boolean qaaiml;
    public int qarandom;

    private Bot bot = null;
    private Chat chatSession = null;

    public String subject;
    public ChatbotPlugin ref;
    public HashMap<String,UserJID> nicks = new HashMap<String,UserJID>();
    
    public static final String BOT_AIML_DEFAULT_ANSWER = "Hmm...";
    public static final String BOT_AIML_ERROR_ANSWER = "Oh ich glaub dazu kann ich nichts sagen.";


    public Room(ChatbotPlugin ref) {
        this.ref = ref;
        LanguageConfiguration lang = LanguageConfiguration.builder()
                .defaultResponse(JiveGlobals.getProperty("plugin.chatbot.aiml.default",BOT_AIML_DEFAULT_ANSWER))
                .errorResponse(JiveGlobals.getProperty("plugin.chatbot.aiml.error",BOT_AIML_ERROR_ANSWER))
                .scheduleError(JiveGlobals.getProperty("plugin.chatbot.aiml.scheduleerror",Room.BOT_AIML_ERROR_ANSWER))
                .systemFailed(JiveGlobals.getProperty("plugin.chatbot.aiml.systemfailed",Room.BOT_AIML_ERROR_ANSWER))
                .templateFailed(JiveGlobals.getProperty("plugin.chatbot.aiml.templatefailed",Room.BOT_AIML_ERROR_ANSWER))
                .tooMuchLooping(JiveGlobals.getProperty("plugin.chatbot.aiml.toomuchlooping",Room.BOT_AIML_ERROR_ANSWER))
                .tooMuchRecursion(JiveGlobals.getProperty("plugin.chatbot.aiml.toomuchrecursion",Room.BOT_AIML_ERROR_ANSWER))
                .build();

        String aimlpath = JiveGlobals.getProperty("plugin.chatbot.res_path",ref.aimlpath);
        Log.info("Using Aimlpath: "+aimlpath);

        BotConfiguration bconfig = BotConfiguration.builder()
                .defaultLanguage("de")
                .name("alice")
                .path(aimlpath)
                .enableExternalMaps(true)
                .enableExternalSets(true)
                .enableNetworkConnection(true)
                .enableSystemTag(true)
                .graphShortCuts(true)
                .language(lang)
                .build();

        bot = new Bot(bconfig);
        chatSession = new Chat(bot);
        bot.getBrain().nodeStats();
    }

    @Override
    public void subjectUpdated(String subject, EntityFullJid from) {
        Log.info("Das Thema im Raum " + jid + " wurde von " + from.asBareJid().toString() + " geändert. Neu: "
                + subject);
        try {
            muc.sendMessage("Hey das Thema wurde geändert!");
        } catch (Exception e) {
            Log.error("Der Bot konnte im Raum " + jid + " keine Nachricht senden.");
        }
    }

    @Override
    public void processPresence(Presence presence) {
        // TODO Auto-generated method stub

    }

    @Override
    public void kicked(Jid actor, String reason) {
        Log.warn("Der Bot wurde von " + actor.asBareJid() + " aus dem Raum " + jid + " gekickt. Grund: " + reason);
    }

    @Override
    public void voiceGranted() {
        Log.info("Der Bot hat im Raum " + jid + " das Stimmrecht erhalten.");
        String dankemessage = ref.dankemessages[ref.getRandomIndex(ref.dankemessages.length)];
        try {
            muc.sendMessage(dankemessage);
        } catch (Exception e) {
            Log.error("Der Bot konnte im Raum " + jid + " keine Nachricht senden.");
        }
    }

    @Override
    public void voiceRevoked() {
        Log.info("Dem Bot wurde im Raum " + jid + " das Stimmrecht entzogen.");
    }

    @Override
    public void banned(Jid actor, String reason) {
        Log.warn("Der Bot wurde von " + actor.asBareJid() + " aus dem Raum " + jid + " verbannt. Grund: " + reason);
    }

    @Override
    public void membershipGranted() {
        Log.info("Der Bot hat im Raum " + jid + " den Mitgliedsstatus erhalten.");
    }

    @Override
    public void membershipRevoked() {
        Log.info("Der Bot hat im Raum " + jid + " den Mitgliedsstatus verloren.");
    }

    @Override
    public void moderatorGranted() {
        Log.info("Der Bot hat im Raum " + jid + " zum Moderator befördert.");
        String dankemessage = ref.dankemessages[ref.getRandomIndex(ref.dankemessages.length)];
        try {
            muc.sendMessage(dankemessage);
        } catch (Exception e) {
            Log.error("Der Bot konnte im Raum " + jid + " keine Nachricht senden.");
        }
    }

    @Override
    public void moderatorRevoked() {
        Log.info("Dem Bot wurden im Raum " + jid + " Moderatorrechte abgenommen.");
        String doofmessage = ref.doofmessages[ref.getRandomIndex(ref.doofmessages.length)];
        try {
            muc.sendMessage(doofmessage);
        } catch (Exception e) {
            Log.error("Der Bot konnte im Raum " + jid + " keine Nachricht senden.");
        }
    }

    @Override
    public void ownershipGranted() {
        Log.info("Der Bot wurde im Raum " + jid + " zum Raumbesitzer geheiligt.");
        String dankemessage = ref.dankemessages[ref.getRandomIndex(ref.dankemessages.length)];
        try {
            muc.sendMessage(dankemessage);
        } catch (Exception e) {
            Log.error("Der Bot konnte im Raum " + jid + " keine Nachricht senden.");
        }
    }

    @Override
    public void ownershipRevoked() {
        Log.info("Dem Bot wurden im Raum " + jid + " die heiligen Raumbesitzerrechte geklaut!");
        String doofmessage = ref.doofmessages[ref.getRandomIndex(ref.doofmessages.length)];
        try {
            muc.sendMessage(doofmessage);
        } catch (Exception e) {
            Log.error("Der Bot konnte im Raum " + jid + " keine Nachricht senden.");
        }
    }

    @Override
    public void adminGranted() {
        Log.info("Der Bot wurde im Raum " + jid + " zum Raumadministrator befördert.");
        String dankemessage = ref.dankemessages[ref.getRandomIndex(ref.dankemessages.length)];
        try {
            muc.sendMessage(dankemessage);
        } catch (Exception e) {
            Log.error("Der Bot konnte im Raum " + jid + " keine Nachricht senden.");
        }
    }

    @Override
    public void adminRevoked() {
        Log.info("Der Bot wurde im Raum " + jid + " degradiert und hat keine Raumadministratorrechte mehr.");
        String doofmessage = ref.doofmessages[ref.getRandomIndex(ref.doofmessages.length)];
        try {
            muc.sendMessage(doofmessage);
        } catch (Exception e) {
            Log.error("Der Bot konnte im Raum " + jid + " keine Nachricht senden.");
        }
    }

    @Override
    public void roomDestroyed(MultiUserChat alternateMUC, String reason) {
        Log.warn("Der Raum " + jid + " wurde geschlossen! " + (reason != null ? ("(Grund: " + reason + ")") : ""));
        if (alternateMUC != null) {
            muc = alternateMUC;
            ref.rooms.remove(jid);
            jid = new org.xmpp.packet.JID(alternateMUC.getRoom().asBareJid().toString());
            ref.rooms.put(jid.toString(), this);

            try {
                ref.joinRoom(alternateMUC, jid, qaonline, qadb, qaaiml,qarandom);
            } catch (Exception e) {
                Log.error("Der Bot konnte den Alternativraum: " + jid + " nicht betreten! " + e.getMessage());
                ref.rooms.remove(jid);
            }
        } else {
            ref.rooms.remove(jid);
        }
    }
    
    private JID getJIDFromParticipant(EntityFullJid participant)
    {
        JID result=null;
        for (MUCRole role : serverroom.getParticipants())
        {
            if (role.getRoleAddress().toString().equalsIgnoreCase(participant.toString()))
            {
                result=role.getUserAddress();
            }
        }
        if (result==null)
        {            
            if (muc.getOccupant(participant).getJid()!=null)
            {
                result = new JID(muc.getOccupant(participant).getJid().toString());
            }
        }
        return result;
    }

    private String getNickFromParticipant(EntityFullJid participant)
    {
        String result=null;
        for (MUCRole role : serverroom.getParticipants())
        {
            if (role.getRoleAddress().toString().equalsIgnoreCase(participant.toString()))
            {
                result=role.getNickname();
            }
        }
        if (result==null)
        {
            result = participant.toString().substring(participant.toString().lastIndexOf("/")+1);
        }
        return result;
    }

    @Override
    public void joined(EntityFullJid participant) {

        String nick = getNickFromParticipant(participant);
        JID userjid = getJIDFromParticipant(participant);
       
        Log.debug("JOIN - NICK: "+(nick!=null?nick:"NULL")+" JID: "+(userjid!=null?userjid.toString():"NULL")+" PART: "+participant.toString());

        boolean found=false;
        if (ref.isUseJoinMessagesOwners()) {
            ArrayList<String> owners = new ArrayList<String>();

            for (org.xmpp.packet.JID owner : XMPPServer.getInstance().getAdmins())
            {
                owners.add(owner.toBareJID());
            }

            String servicestr = jid.getDomain().split("[.]")[0];

            MultiUserChatService mucservice = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(servicestr);

            if (mucservice!=null)
            {
                for (org.xmpp.packet.JID owner : mucservice.getSysadmins())
                {
                    if (!owners.contains(owner.toBareJID()))
                        owners.add(owner.toBareJID());
                }
            }

            for (org.xmpp.packet.JID owner : serverroom.getOwners())
            {
                if (!owners.contains(owner.toBareJID()))
                {
                    owners.add(owner.toBareJID());
                }
            }
            
            for (String ownerjid : owners)
            {
                if (userjid!=null&&ownerjid.equalsIgnoreCase(userjid.toBareJID()))
                {
                    found=true;
                    try {
                        String[] messages =  JiveGlobals.getProperty("plugin.chatbot.joinmessagesowners", String.join("\n", ref.ownerbegruessung)).split("\n");
                        String msg = messages[ref.getRandomIndex(messages.length)];
                        msg=msg.replace("%OWNER%", nick);
                        muc.sendMessage(msg);
                    } catch (Exception e) {
                        Log.error("JOIN - OWNERMESSAGE: " + e.getMessage());
                        Log.error("Konnte die Begrüßung an den Owner " + userjid.toString()
                                + " nicht an den Raum " + jid + " senden.");
                    }
                }
            }
        }

        if (ref.isUseJoinMessagesAdmin()&&!found) {
            for (JID adminjid : serverroom.getAdmins())
            {
                if (adminjid.toBareJID().equalsIgnoreCase(userjid.toBareJID()))
                {
                    found=true;
                    try {
                        String[] messages =  JiveGlobals.getProperty("plugin.chatbot.joinmessagesadmin", String.join("\n", ref.adminbegruessung)).split("\n");
                        String msg = messages[ref.getRandomIndex(messages.length)];
                        msg=msg.replace("%ADMIN%", nick);
                        muc.sendMessage(msg);
                    } catch (Exception e) {
                        Log.error("JOIN - ADMINMESSAGE: " + e.getMessage());
                        Log.error("Konnte die Begrüßung an den Admin " + userjid.toString()
                                + " nicht an den Raum " + jid + " senden.");
                    }
                }
            }
        }

        if (ref.isUseJoinMessagesOwners()&&!found) {
            try {
                String[] messages =  JiveGlobals.getProperty("plugin.chatbot.joinmessagesrest", String.join("\n", ref.restbegruessung)).split("\n");
                String restmsg = "@" + nick + " " + messages[ref.getRandomIndex(messages.length)];
                muc.sendMessage(restmsg);
            } catch (Exception e) {
                Log.error("JOIN - MESSAGE: " + e.getMessage());
                Log.error("Konnte die Begrüßung an den User " + userjid.toString()
                        + " nicht an den Raum " + jid + " senden.");
            }
        }
        UserJID jids = new UserJID();
        jids.partjid=participant.toString();
        jids.realjid=userjid.toString();
        jids.nick=nick;
        nicks.put(participant.toString(), jids);
    }

    @Override
    public void left(EntityFullJid participant) {
        if (ref.isUseLeaveMessages()) {
            try {
                String[] messages =  JiveGlobals.getProperty("plugin.chatbot.leavemessages", String.join("\n", ref.leavemessages_other)).split("\n");
                UserJID jids = nicks.get(participant.toString());
                Log.debug("LEAVE - NICK: "+jids.nick+" JID: "+jids.realjid.toString()+" PART: "+jids.partjid.toString());

                String msg =messages[ref.getRandomIndex(messages.length)].replace("%LEAVE%",jids.nick);
                muc.sendMessage(msg);
                nicks.remove(participant.toString());
            } catch (Exception e) {
                Log.error("LEAVE - MESSAGE: " + e.getMessage());
                Log.error("Konnte die Leavenachricht von " + participant.asBareJid().toString() + " nicht an den Raum "
                        + jid + " senden.");
            }
        }
    }

    @Override
    public void kicked(EntityFullJid participant, Jid actor, String reason) {
        if (ref.isUseLeaveMessagesKick()) {
            try {
                String[] messages =  JiveGlobals.getProperty("plugin.chatbot.leavekickmessages", String.join("\n", ref.kickmessage)).split("\n");
                String nick = getNickFromParticipant(participant);

                String msg = messages[ref.getRandomIndex(messages.length)].replace("%KICK%",nick);
                muc.sendMessage(msg);
            } catch (Exception e) {
                Log.error("KICK - MESSAGE: " + e.getMessage());
                Log.error("Konnte die Kicknachricht von " + participant.asBareJid().toString() + " nicht an den Raum "
                        + jid + " senden.");
            }
        }
    }

    @Override
    public void voiceGranted(EntityFullJid participant) {

    }

    @Override
    public void voiceRevoked(EntityFullJid participant) {

    }

    @Override
    public void banned(EntityFullJid participant, Jid actor, String reason) {
        if (ref.isUseLeaveMessagesBan()) {
            try {
                String[] messages =  JiveGlobals.getProperty("plugin.chatbot.leavebanmessages", String.join("\n", ref.banmessage)).split("\n");
                String nick = getNickFromParticipant(participant);
             
                String msg = messages[ref.getRandomIndex(messages.length)].replace("%BAN%",nick);
                muc.sendMessage(msg);
            } catch (Exception e) {
                Log.error("BAN - MESSAGE: " + e.getMessage());
                Log.error("Konnte die Bannachricht von " + participant.asBareJid().toString() + " nicht an den Raum "
                        + jid + " senden.");
            }
        }
    }

    @Override
    public void membershipGranted(EntityFullJid participant) {
        // TODO Auto-generated method stub

    }

    @Override
    public void membershipRevoked(EntityFullJid participant) {
        // TODO Auto-generated method stub

    }

    @Override
    public void moderatorGranted(EntityFullJid participant) {
        // TODO Auto-generated method stub

    }

    @Override
    public void moderatorRevoked(EntityFullJid participant) {
        // TODO Auto-generated method stub

    }

    @Override
    public void ownershipGranted(EntityFullJid participant) {
        // TODO Auto-generated method stub

    }

    @Override
    public void ownershipRevoked(EntityFullJid participant) {
        // TODO Auto-generated method stub

    }

    @Override
    public void adminGranted(EntityFullJid participant) {
        // TODO Auto-generated method stub

    }

    @Override
    public void adminRevoked(EntityFullJid participant) {
        // TODO Auto-generated method stub

    }

    @Override
    public void nicknameChanged(EntityFullJid participant, Resourcepart newNickname) {
        if (ref.isUseNickchangeMessages()) {
            try {
                String[] messages =  JiveGlobals.getProperty("plugin.chatbot.nickchangemessages", String.join("\n", ref.nickchange)).split("\n");
                UserJID jids = nicks.get(participant.toString());

                String msg = messages[ref.getRandomIndex(messages.length)]
                        .replace("%NICK1%", jids.nick)
                        .replace("%NICK2%", newNickname.toString());

                jids.nick=newNickname.toString();
                nicks.put(participant.toString(), jids);

                muc.sendMessage(msg);
            } catch (Exception e) {
                Log.error("BAN - MESSAGE: " + e.getMessage());
                Log.error("Konnte die Bannachricht von " + participant.toString() + " nicht an den Raum "
                        + jid + " senden.");
            }
        }
    }

    @Override
    public void processMessage(Message message) {
        String from = message.getFrom().toString();

        if (from.equalsIgnoreCase(jid + "/" + ref.getBotNick())||(!qadb&&!qaonline&&!qaaiml)) {
            Log.debug("IGNORIERE (EIGENE) NACHRICHT!");
            return;
        }

        String to = message.getTo().toString();
        String body = message.getBody();
        String q = body!=null?body.replaceAll("[ ]{2,}", " "):null;
        String aimlq = q;
        String sendbody = "";
        String test = q!=null?q.replaceAll("[^0-9a-zA-ZäöüßÄÖÜ\\s]", ""):null;

        if (q==null||q.length()==0)
            return;

        if (test!=null&&test.toLowerCase().contains("heute ist ein guter tag zum sterben")&&test.toLowerCase().contains(ref.getBotNick().toLowerCase()))
        {
            Log.warn("KILL SWITCH FÜR BOT ERKANNT");
            try {
                    muc.sendMessage("Immer muss ich dran glauben... :/");
            } catch (Exception e) {
                Log.error("Der Bot konnte keine Antwort in den Raum " + jid + " schicken. " + e.getMessage());
            }
            ref.shutdown();
            return;
        }
        
        Random r = new Random();
        int low = 1;
        int high = 100;
        int iresult = r.nextInt(high-low) + low;
        int schwelle = qarandom;
        if (schwelle==-1)
        {
            schwelle = JiveGlobals.getIntProperty(ChatbotPlugin.SETTING_BOTRANDOM, 50);
        }

        if (qaaiml)
        {
            if (test.toLowerCase().contains(ref.getBotNick().toLowerCase())||test.toLowerCase().contains(ref.getBotUser().toLowerCase())||iresult<=schwelle)
            {
                sendbody = chatSession.multisentenceRespond(aimlq);
            }
        }

        if (qadb) {
            if (test.toLowerCase().contains(ref.getBotNick().toLowerCase())||test.toLowerCase().contains(ref.getBotUser().toLowerCase())||iresult<=schwelle)
            {
                String search = test.replace(ref.getBotNick(), "").toLowerCase();
                if (search!=null&&search.trim().length()>2)
                {
                    String result = DatabaseUtils.getQA(search);
                    if (result != null) {
                        try
                        {
                            JSONArray arr = new JSONArray(result);
        
                            if (arr.length() > 0) {
                                int idx = ref.getRandomIndex(arr.length());
                                sendbody = arr.getJSONObject(idx).getString("a");
                                sendbody = ref.filter(sendbody,
                                        muc.getOccupant(message.getFrom().asEntityFullJidIfPossible()).getNick().toString(),
                                        search.split("\\s").length==1?search:null);
                                if (test.toLowerCase().contains(ref.getBotNick().toLowerCase())||test.toLowerCase().contains(ref.getBotUser().toLowerCase()))
                                {
                                    sendbody="@"+muc.getOccupant(message.getFrom().asEntityFullJidIfPossible()).getNick().toString()+" "+sendbody;
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            
                        }
                    }
                }
            }
        }

        if (qaonline&&(sendbody.trim().length()==0||sendbody.contains(JiveGlobals.getProperty("plugin.chatbot.aiml.default",BOT_AIML_DEFAULT_ANSWER))||sendbody.contains(JiveGlobals.getProperty("plugin.chatbot.aiml.default",BOT_AIML_ERROR_ANSWER)))) {
            if (test.toLowerCase().contains(ref.getBotNick().toLowerCase())||test.toLowerCase().contains(ref.getBotUser().toLowerCase())||iresult<=schwelle) {
                try {
                    String search = test.replace(ref.getBotNick(), "");

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
        }

        try {
            if (sendbody!=null&&sendbody.trim().length()>0)
            {
                muc.sendMessage(sendbody);
                Log.debug("from: " + from + " to: " + to + " q: " + q + " a: " + sendbody);
            }
        } catch (Exception e) {
            Log.error("Der Bot konnte keine Antwort in den Raum " + jid + " schicken. " + e.getMessage());
        }

    }

    

}
