package de.mopsdom.openfire.plugins.chatbot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.DbConnectionManager.DatabaseType;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.util.JiveGlobals;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

public class DatabaseUtils {
    private static final Logger Log = LoggerFactory.getLogger(DatabaseUtils.class);

    private static final String GET_ROOMS = "SELECT roomjid, qa_online,qa_db,qaaiml,qarandom FROM ofChatbotRooms";

    private static final String DELETE_ROOM = "DELETE FROM ofChatbotRooms where roomjid = ?";

    private static final String SET_ROOM_SQLSERVER = "BEGIN TRAN "
            + " IF EXISTS (SELECT roomjid FROM ofChatbotRooms WHERE roomjid = ?) " + " BEGIN "
            + "   UPDATE ofChatbotRooms SET qa_online = ?, qa_db = ?, qaaiml=?, qarandom=? WHERE roomjid = ?  END  ELSE "
            + " BEGIN  INSERT into ofChatbotRooms (roomjid, qa_online, qa_db,qaaiml,qarandom) VALUES (?, ?, ?, ?, ?)  END "
            + " COMMIT TRAN";

    private static final String SET_ROOM_MYSQL = "INSERT INTO ofChatbotRooms (roomjid, qa_online, qa_db, qaaiml, qarandom) "
            + " VALUES (? , ? , ?, ?, ?)  ON DUPLICATE KEY UPDATE   qa_online = ?,  qa_db = ?, qaaiml = ?, qarandom= ?";

    private static final String GET_QAS = "SELECT id, q, a FROM ofChatbotQA";

    private static final String GET_QA = "SELECT id, q, a FROM ofChatbotQA WHERE %WORDS%";

    private static final String SET_QA_SQLSERVER = "BEGIN TRAN "
            + " IF EXISTS (SELECT id FROM ofChatbotQA WHERE id = ? ) " + " BEGIN "
            + "   UPDATE ofChatbotQA SET q = ? , a = ? WHERE id = ? " + " END " + " ELSE " + " BEGIN "
            + "     INSERT into ofChatbotQA (id, q, a) VALUES (? , ? , ?) " + " END " + " COMMIT TRAN";

    private static final String SET_QA_MYSQL = "INSERT INTO ofChatbotQA (id, q,a) VALUES (?,?,?) "
            + " ON DUPLICATE KEY UPDATE q = ?, a = ?";

    private static final String DELETE_QA = "DELETE FROM ofChatbotQA where  id = ?";

    private static final int MIN_Q_WORD_LENGTH = 3;

    public static void setRooms(String json) {
        setRooms(new JSONArray(json));
    }

    public static void setRooms(JSONArray json) {
        for (int n = 0; n < json.length(); n++) {
            JSONObject room = json.getJSONObject(n);
            if (room.has("aktiv") && room.getBoolean("aktiv")) {
                setPropRoom(room.getString("roomjid"), room.getBoolean("qaonline"), room.getBoolean("qadb"), room.getBoolean("qaaiml"), room.getInt("qarandom"));
            } else {
                deleteRoom(room.getString("roomjid"));
            }
        }
    }

    public static void setPropRoom(String room, boolean qaonline, boolean qadb,boolean qaaiml, int qarandom) {
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DbConnectionManager.getConnection();

            if (DbConnectionManager.getDatabaseType() == DatabaseType.sqlserver) {
                pstmt = con.prepareStatement(SET_ROOM_SQLSERVER);
                pstmt.setString(1, room);
                pstmt.setInt(2, qaonline ? 1 : 0);
                pstmt.setInt(3, qadb ? 1 : 0);
                pstmt.setInt(4, qaaiml ? 1 : 0);
                pstmt.setInt(5, qarandom);
                pstmt.setString(6, room);
                pstmt.setString(7, room);
                pstmt.setInt(8, qaonline ? 1 : 0);
                pstmt.setInt(9, qadb ? 1 : 0);
                pstmt.setInt(10, qaaiml ? 1 : 0);
                pstmt.setInt(11, qarandom );
                
            } else {
                pstmt = con.prepareStatement(SET_ROOM_MYSQL);
                pstmt.setString(1, room);
                pstmt.setInt(2, qaonline ? 1 : 0);
                pstmt.setInt(3, qadb ? 1 : 0);
                pstmt.setInt(4, qaaiml ? 1 : 0);
                pstmt.setInt(5, qarandom );
                pstmt.setInt(6, qaonline ? 1 : 0);
                pstmt.setInt(7, qadb ? 1 : 0);
                pstmt.setInt(8, qaaiml ? 1 : 0);
                pstmt.setInt(9, qarandom );
            }

            pstmt.executeUpdate();

        } catch (SQLException sqle) {
            Log.error("setPropRoom(): " + sqle.getMessage());
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    public static void setQAa(String json) {
        setQAa(new JSONArray(json));
    }

    public static void setQAa(JSONArray json) {
        Connection con = null;
        PreparedStatement pstmt = null;

        Log.debug("Speichere QAs");
        try {
            con = DbConnectionManager.getConnection();

            for (int n = 0; n < json.length(); n++) {
                JSONObject qa = json.getJSONObject(n);

                if (!qa.has("remove") || !qa.getBoolean("remove")) {

                    if (DbConnectionManager.getDatabaseType() == DatabaseType.sqlserver) {

                        pstmt = con.prepareStatement(SET_QA_SQLSERVER);
                        pstmt.setString(1, qa.getString("id"));
                        pstmt.setString(2, qa.getString("q"));
                        pstmt.setString(3, qa.getString("a"));
                        pstmt.setString(4, qa.getString("id"));
                        pstmt.setString(5, qa.getString("id"));
                        pstmt.setString(6, qa.getString("q"));
                        pstmt.setString(7, qa.getString("a"));
                    } else {
                        pstmt = con.prepareStatement(SET_QA_MYSQL);
                        pstmt.setString(1, qa.getString("id"));
                        pstmt.setString(2, qa.getString("q"));
                        pstmt.setString(3, qa.getString("a"));
                        pstmt.setString(4, qa.getString("q"));
                        pstmt.setString(5, qa.getString("a"));
                    }
                } else {

                    pstmt = con.prepareStatement(DELETE_QA);
                    pstmt.setString(1, qa.getString("id"));
                }
                pstmt.executeUpdate();
            }

        } catch (SQLException sqle) {
            Log.error("setQAa(): " + sqle.getMessage());
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    public static String getQA(String q) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        JSONArray qas = new JSONArray();

        try {
            con = DbConnectionManager.getConnection();

            String query = GET_QA;
            String wordstr = "";
            final String[] words = q.split("\\s");
            for (int m = 0; m < words.length; m++) {

                if (words[m].trim().length() > MIN_Q_WORD_LENGTH) {
                    wordstr += " LOWER(q) like '%" + words[m].trim() + "%'";

                    if (m + 1 < words.length) {
                        wordstr += " OR ";
                    }
                }
            }

            if (wordstr.trim().endsWith("OR")) {
                wordstr = wordstr.substring(0, wordstr.lastIndexOf("OR")).trim();
            }

            if (wordstr.trim().length() > 0) {
                query = query.replace("%WORDS%", wordstr);
                Log.debug(query);
                pstmt = con.prepareStatement(query);

                rs = pstmt.executeQuery();

                ArrayList<JSONObject> results = new ArrayList<>();
                // ERGEBNISSE VON DB MERKEN
                while (rs.next()) {

                    JSONObject qa = new JSONObject();
                    String id = rs.getString(1);
                    String q2 = rs.getString(2);
                    String a = rs.getString(3);
                    qa.put("id", id);
                    qa.put("q", q2);
                    qa.put("a", a);

                    results.add(qa);
                }

                Log.debug("Anzahl gefundener Ergebnisse: " + String.valueOf(results.size()));
                // SUCHE STARTEN UND RANKING ERSTELLEN
                Collections.sort(results, new Comparator<JSONObject>() {

                    @Override
                    public int compare(JSONObject o1, JSONObject o2) {
                        int count1 = 0;
                        int count2 = 0;
                        String[] words1 = o1.getString("q").split("\\s");
                        String[] words2 = o2.getString("q").split("\\s");

                        for (int n = 0; n < words.length; n++) {
                            for (int m = 0; m < words1.length; m++) {
                                if (words[n].equalsIgnoreCase(words1[m])) {
                                    count1++;
                                    break;
                                }
                            }

                            for (int m = 0; m < words2.length; m++) {
                                if (words[n].equalsIgnoreCase(words2[m])) {
                                    count2++;
                                    break;
                                }
                            }
                        }

                        o1.put("count", count1);
                        o2.put("count", count2);

                        if (count1 > count2)
                            return -1;
                        else if (count1 < count2)
                            return +1;
                        else
                            return 0;
                    }
                });

                int count=0;
                boolean same=true;

                for (int n = 0; n < 4 && n < results.size(); n++) {
                    Log.debug("RESULTS: "+results.get(n).getString("q")+" | "+results.get(n).getString("a"));
                    if (n==0)
                    {
                        count=results.get(n).has("count")?results.get(n).getInt("count"):results.get(n).getString("q").split("\\s").length;
                    }
                    else
                    {
                        if (count!=(results.get(n).has("count")?results.get(n).getInt("count"):results.get(n).getString("q").split("\\s").length))
                        {
                            same=false;
                            break;
                        }
                    }
                }

                if (same)
                {
                    for (int n = 0; n < 4 && n < results.size(); n++) {
                        qas.put(results.get(n));
                        Log.debug("QA: "+results.get(n).getString("q")+" | "+results.get(n).getString("a"));
                    }
                }
                else
                {
                    if (results.size()>0)
                    {
                        qas.put(results.get(0));
                        Log.debug("QA: "+results.get(0).getString("q")+" | "+results.get(0).getString("a"));
                    }
                }
                Log.debug("NUTZE BESTE ERGEBNISSE: " + qas.toString());
            }

        } catch (SQLException sqle) {
            Log.error("getQA(): " + sqle.getMessage());
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return qas.toString();
    }

    public static String getQAs() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        JSONArray qas = new JSONArray();

        try {
            con = DbConnectionManager.getConnection();

            pstmt = con.prepareStatement(GET_QAS);

            rs = pstmt.executeQuery();

            while (rs.next()) {

                JSONObject qa = new JSONObject();
                String id = rs.getString(1);
                String q = rs.getString(2);
                String a = rs.getString(3);
                qa.put("id", id);
                qa.put("q", q);
                qa.put("a", a);

                qas.put(qa);

            }
        } catch (SQLException sqle) {
            Log.error("getQAs(): " + sqle.getMessage());
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return qas.toString();
    }

    public static JSONArray getRooms() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        JSONArray rooms = new JSONArray();

        try {
            con = DbConnectionManager.getConnection();

            pstmt = con.prepareStatement(GET_ROOMS);

            rs = pstmt.executeQuery();

            while (rs.next()) {

                JSONObject room = new JSONObject();
                String roomjid = rs.getString(1);
                int qaonline = rs.getInt(2);
                int qadb = rs.getInt(3);
                int qaaiml = rs.getInt(4);
                int qarandom = rs.getInt(5);
                room.put("roomjid", roomjid);
                room.put("qaonline", qaonline == 1 ? true : false);
                room.put("qadb", qadb == 1 ? true : false);
                room.put("qaaiml", qaaiml == 1 ? true : false);
                room.put("qarandom", qarandom);
                room.put("aktiv", true);

                rooms.put(room);

            }
        } catch (SQLException sqle) {
            Log.error("getRooms(): " + sqle.getMessage());
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return rooms;
    }

    public static void deleteRoom(String roomjid) {
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DbConnectionManager.getConnection();

            pstmt = con.prepareStatement(DELETE_ROOM);
            pstmt.setString(1, roomjid);

            pstmt.executeUpdate();

        } catch (SQLException sqle) {
            Log.error("deleteRoom(): " + sqle.getMessage());
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

}
