<!--
  - Copyright (C) 2017 Ignite Realtime Foundation. All rights reserved.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  - http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
-->
<!--%@ page import="org.apache.commons.httpclient.methods.GetMethod"%-->
<%@ page errorPage="error.jsp"%>
<%@ page import="org.jivesoftware.openfire.XMPPServer"%>
<%@ page import="org.jivesoftware.util.CookieUtils"%>
<%@ page import="org.jivesoftware.util.ParamUtils"%>
<%@ page import="org.jivesoftware.util.StringUtils"%>
<%@ page import="org.jivesoftware.util.JiveGlobals"%>
<%@ page import="java.util.List"%>
<%@ page import="java.io.File"%>
<%@ page import="org.xmpp.packet.JID"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="org.slf4j.Logger"%>
<%@ page import="org.slf4j.LoggerFactory"%>
<%@ page import="java.io.InputStream"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page
	import="de.mopsdom.openfire.plugins.chatbot.ChatbotPlugin"%>
<%@ page
	import="de.mopsdom.openfire.plugins.chatbot.DatabaseUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="webManager" class="org.jivesoftware.util.WebManager" />
<%
    webManager.init(request, response, session, application, out);
%>
<%
    final Logger Log = LoggerFactory.getLogger("chatbot-config.jsp");

			boolean savebot = request.getParameter("savebot") != null;
			boolean savegeneral = request.getParameter("savegeneral") != null;
			boolean saverun = request.getParameter("saverun") != null;

			String update = request.getParameter("update");
			String success = request.getParameter("success");
			String error = null;
			if (saverun) {
				if (ParamUtils.getParameter(request, "runbot") != null) {
					if (ParamUtils.getParameter(request, "runbot").equalsIgnoreCase("true")) {
						ChatbotPlugin.getInstance().init();
					} else {
						//ChatbotPlugin.LASTSTATE.setValue(false);
						JiveGlobals.setProperty(ChatbotPlugin.SETTING_STATE, "false");
						ChatbotPlugin.getInstance().shutdown();
					}
				}

				response.sendRedirect("chatbot-config.jsp?uploadsuccess=true");
				return;
			}
			if (savegeneral) {
				if (ParamUtils.getParameter(request, "usenickchangemessages") != null) {
					if (ParamUtils.getParameter(request, "usenickchangemessages").equalsIgnoreCase("on")) {
						JiveGlobals.setProperty("plugin.chatbot.usenickchangemessages", "true");
					} else {
						JiveGlobals.setProperty("plugin.chatbot.usenickchangemessages", "false");
					}
				} else {
					JiveGlobals.setProperty("plugin.chatbot.usenickchangemessages", "false");
				}

				if (ParamUtils.getParameter(request, "useleavebanmessages") != null) {
					if (ParamUtils.getParameter(request, "useleavebanmessages").equalsIgnoreCase("on")) {
						JiveGlobals.setProperty("plugin.chatbot.useleavebanmessages", "true");
					} else {
						JiveGlobals.setProperty("plugin.chatbot.useleavebanmessages", "false");
					}
				} else {
					JiveGlobals.setProperty("plugin.chatbot.useleavebanmessages", "false");
				}

				if (ParamUtils.getParameter(request, "useleavekickmessages") != null) {
					if (ParamUtils.getParameter(request, "useleavekickmessages").equalsIgnoreCase("on")) {
						JiveGlobals.setProperty("plugin.chatbot.useleavekickmessages", "true");
					} else {
						JiveGlobals.setProperty("plugin.chatbot.useleavekickmessages", "false");
					}
				} else {
					JiveGlobals.setProperty("plugin.chatbot.useleavekickmessages", "false");
				}

				if (ParamUtils.getParameter(request, "useleavemessages") != null) {
					if (ParamUtils.getParameter(request, "useleavemessages").equalsIgnoreCase("on")) {
						JiveGlobals.setProperty("plugin.chatbot.useleavemessages", "true");
					} else {
						JiveGlobals.setProperty("plugin.chatbot.useleavemessages", "false");
					}
				} else {
					JiveGlobals.setProperty("plugin.chatbot.useleavemessages", "false");
				}

				if (ParamUtils.getParameter(request, "usejoinmessagesrest") != null) {
					if (ParamUtils.getParameter(request, "usejoinmessagesrest").equalsIgnoreCase("on")) {
						JiveGlobals.setProperty("plugin.chatbot.usejoinmessagesrest", "true");
					} else {
						JiveGlobals.setProperty("plugin.chatbot.usejoinmessagesrest", "false");
					}
				} else {
					JiveGlobals.setProperty("plugin.chatbot.usejoinmessagesrest", "false");
				}

				if (ParamUtils.getParameter(request, "usejoinmessagesadmin") != null) {
					if (ParamUtils.getParameter(request, "usejoinmessagesadmin").equalsIgnoreCase("on")) {
						JiveGlobals.setProperty("plugin.chatbot.usejoinmessagesadmin", "true");
					} else {
						JiveGlobals.setProperty("plugin.chatbot.usejoinmessagesadmin", "false");
					}
				} else {
					JiveGlobals.setProperty("plugin.chatbot.usejoinmessagesadmin", "false");
				}

				if (ParamUtils.getParameter(request, "usejoinmessagesowners") != null) {
					if (ParamUtils.getParameter(request, "usejoinmessagesowners").equalsIgnoreCase("on")) {
						JiveGlobals.setProperty("plugin.chatbot.usejoinmessagesowners", "true");
					} else {
						JiveGlobals.setProperty("plugin.chatbot.usejoinmessagesowners", "false");
					}
				} else {
					JiveGlobals.setProperty("plugin.chatbot.usejoinmessagesowners", "false");
				}

				if (ParamUtils.getParameter(request, "botrandom") != null) {
					JiveGlobals.setProperty(ChatbotPlugin.SETTING_BOTRANDOM,
							ParamUtils.getParameter(request, "botrandom"));
				} else {
					JiveGlobals.setProperty(ChatbotPlugin.SETTING_BOTRANDOM, "50");
				}
			}
			if (savebot) {

				if (ParamUtils.getParameter(request, "botuser") != null) {
					JiveGlobals.setProperty(ChatbotPlugin.SETTING_BOTUSER, ParamUtils.getParameter(request, "botuser"));
					//ChatbotPlugin.BOTUSER.setValue(ParamUtils.getParameter( request, "botuser" ));
				} else {
					JiveGlobals.deleteProperty(ChatbotPlugin.SETTING_BOTUSER);
				}

				if (ParamUtils.getParameter(request, "botpass") != null) {
					JiveGlobals.setProperty(ChatbotPlugin.SETTING_BOTPASS, ParamUtils.getParameter(request, "botpass"));
					//ChatbotPlugin.BOTPASS.setValue(ParamUtils.getParameter( request, "botpass" ));
				} else {
					JiveGlobals.deleteProperty(ChatbotPlugin.SETTING_BOTPASS);
				}

				if (ParamUtils.getParameter(request, "botnick") != null) {
					JiveGlobals.setProperty(ChatbotPlugin.SETTING_BOTNICK, ParamUtils.getParameter(request, "botnick"));
					//ChatbotPlugin.BOTNICK.setValue(ParamUtils.getParameter( request, "botnick" ));
				} else {
					JiveGlobals.deleteProperty(ChatbotPlugin.SETTING_BOTNICK);
				}

				response.sendRedirect("chatbot-config.jsp?uploadsuccess=true");
				return;
			}

			String botuser = JiveGlobals.getProperty(ChatbotPlugin.SETTING_BOTUSER, "");//ChatbotPlugin.BOTUSER.getValue();
			String botpass = JiveGlobals.getProperty(ChatbotPlugin.SETTING_BOTPASS, "");//ChatbotPlugin.BOTPASS.getValue();
			String botnick = JiveGlobals.getProperty(ChatbotPlugin.SETTING_BOTNICK, "");//ChatbotPlugin.BOTNICK.getValue();
			String botrandom = JiveGlobals.getProperty(ChatbotPlugin.SETTING_BOTRANDOM, "50");

			String usejoinmessagesowners = JiveGlobals.getProperty("plugin.chatbot.usejoinmessagesowners", "true");
			String usejoinmessagesadmin = JiveGlobals.getProperty("plugin.chatbot.usejoinmessagesadmin", "true");
			String usejoinmessagesrest = JiveGlobals.getProperty("plugin.chatbot.usejoinmessagesrest", "true");
			String useleavemessages = JiveGlobals.getProperty("plugin.chatbot.useleavemessages", "true");
			String useleavekickmessages = JiveGlobals.getProperty("plugin.chatbot.useleavekickmessages", "true");
			String useleavebanmessages = JiveGlobals.getProperty("plugin.chatbot.useleavebanmessages", "true");
			String usenickchangemessages = JiveGlobals.getProperty("plugin.chatbot.usenickchangemessages", "true");

			String btncolor = (ChatbotPlugin.getInstance().isRunning() ? "red" : "green");
			String btnvalue = (ChatbotPlugin.getInstance().isRunning() ? "Bot beenden" : "Bot starten");

			String runbot = (ChatbotPlugin.getInstance().isRunning() ? "false" : "true");
%>
<!DOCTYPE html>
<html>
<head>
<title><fmt:message key="config.page.title" /></title>
<meta name="pageID" content="chatbot-config" />
<meta http-equiv="Access-Control-Allow-Origin" content="*" />
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<meta http-equiv="Access-Control-Allow-Headers"
	content="Overwrite, Destination, Content-Type, Depth, User-Agent, X-File-Size, X-Requested-With, If-Modified-Since, X-File-Name, Cache-Control" />

<style>
.paginate_button {
	font: 13px Arial !important;
	background-color: #EEEEEE !important;
	padding: 2px 6px 2px 6px !important;
	border-top: 1px solid #CCCCCC !important;
	border-right: 1px solid #333333 !important;
	border-bottom: 1px solid #333333 !important;
	border-left: 1px solid #CCCCCC !important;
}

body {
	font-family: Arial, Helvetica, sans-serif;
}

/* Start by setting display:none to make this hidden.
   Then we position it in relation to the viewport window
   with position:fixed. Width, height, top and left speak
   for themselves. Background we set to 80% white with
   our animation centered, and no-repeating */
.modal {
	display: none;
	position: fixed;
	z-index: 1000;
	top: 0;
	left: 0;
	height: 100%;
	width: 100%;
	background: rgba(255, 255, 255, .8)
		url('http://i.stack.imgur.com/FhHRx.gif') 50% 50% no-repeat;
}

/* When the body has the loading class, we turn
   the scrollbar off with overflow:hidden */
body.loading .modal {
	overflow: hidden;
}

/* Anytime the body has the loading class, our
   modal element will be visible */
body.loading .modal {
	display: block;
}

.red {
	color: red;
}

td.details-control {
	background: url('html/details_open.png') no-repeat center center;
	cursor: pointer;
}

tr.shown td.details-control {
	background: url('html/details_close.png') no-repeat center center;
}
</style>

<script type="text/javascript" src="html/jquery-3.5.1.js"></script>
<script type="text/javascript" src="html/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="html/md5.min.js"></script>

<link rel="stylesheet" type="text/css"
	href="html/jquery.dataTables.min.css">

<script>
	jQuery.noConflict();
</script>

</head>

</head>
<body>
	<c:if test="${param.uploadsuccess eq 'true'}">
		<admin:infobox type="success" style="color:green;">
            Aktion erfolgreich!
        </admin:infobox>
	</c:if>
	<c:if test="${param.uploadsuccess eq 'false'}">
		<admin:infobox type="error" style="color:red;">
            Fehler beim Ausf&uuml;hren der Aktion!
        </admin:infobox>
	</c:if>
	<c:if test="${param.actionsuccess eq 'true'}">
		<admin:infobox type="success" style="color:green;">
            Aktion erfolgreich gestartet.
        </admin:infobox>
	</c:if>
	<c:if test="${param.actionsuccess eq 'false'}">
		<admin:infobox type="error" style="color:red;">
            Aktion konnte nicht ausgef&uuml;hrt werden.
        </admin:infobox>
	</c:if>
	<%
	    if (error != null) {
	%>

	<div class="jive-error">
		<table cellpadding="0" cellspacing="0" border="0">
			<tbody>
				<tr>
					<td class="jive-icon"><img src="images/error-16x16.gif"
						width="16" height="16" border="0" alt=""></td>
					<td class="jive-icon-label">
						<%
						    if ("csrf".equalsIgnoreCase(error)) {
						%> <fmt:message
							key="global.csrf.failed" /> <%
     } else {
 %> <fmt:message
							key="admin.error" />: <c:out value="error"></c:out> <%
     }
 %>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
	<br>

	<%
	    }
	%>


	<%
	    if (success != null) {
	%>

	<div class="jive-success">
		<table cellpadding="0" cellspacing="0" border="0">
			<tbody>
				<tr>
					<td class="jive-icon"><img src="images/success-16x16.gif"
						width="16" height="16" border="0" alt=""></td>
					<td class="jive-icon-label"><fmt:message
							key="properties.save.success" /></td>
				</tr>
			</tbody>
		</table>
	</div>
	<br>

	<%
	    }
	%>
	<div class="jive-contentBoxHeader">
		<fmt:message key="config.page.headerstatus" />
	</div>
	<div class="jive-contentBox" id='mystatusdiv'>
		<form action="chatbot-config.jsp">
			<input type="hidden" name="saverun" id="saverun" value="true" /> <input
				type="hidden" name="runbot" id="runbot" value="<%=runbot%>" /> <input
				style="width: 100%; color: <%=btncolor%>; font-weight: bold;"
				type="submit" id="submitrun" value="<%=btnvalue%>">
		</form>
	</div>

	<div class="jive-contentBoxHeader">
		<fmt:message key="config.page.headergeneral" />
	</div>
	<div class="jive-contentBox" id='mygeneraldiv'>
		<form action="chatbot-config.jsp">
			<input type="hidden" name="savegeneral" id="savegeneral" value="true" />
			<table cellpadding="0" cellspacing="0"
				style="width: 100%; margin: 0; padding: 0;">
				<tbody>
					<tr>
						<td>Besitzerbegr&uuml;&szlig;ung</td>
						<td><input style="width: 100%;" type="checkbox"
							name="usejoinmessagesowners" id="usejoinmessagesowners"
							<%=usejoinmessagesowners.equals("true") ? "checked" : ""%> /></td>
					</tr>
					<tr>
						<td>Adminbegr&uuml;&szlig;ung</td>
						<td><input style="width: 100%;" type="checkbox"
							name="usejoinmessagesadmin" id="usejoinmessagesadmin"
							<%=usejoinmessagesadmin.equals("true") ? "checked" : ""%> /></td>
					</tr>
					<tr>
						<td>rest. User begr&uuml;&szlig;en</td>
						<td><input style="width: 100%;" type="checkbox"
							name="usejoinmessagesrest" id="usejoinmessagesrest"
							<%=usejoinmessagesrest.equals("true") ? "checked" : ""%> /></td>
					</tr>
					<tr>
						<td>Verabschiedungsnachricht</td>
						<td><input style="width: 100%;" type="checkbox"
							name="useleavemessages" id="useleavemessages"
							<%=useleavemessages.equals("true") ? "checked" : ""%> /></td>
					</tr>
					<tr>
						<td>Kick-Nachricht</td>
						<td><input style="width: 100%;" type="checkbox"
							name="useleavekickmessages" id="useleavekickmessages"
							<%=useleavekickmessages.equals("true") ? "checked" : ""%> /></td>
					</tr>
					<tr>
						<td>Ban-Nachricht</td>
						<td><input style="width: 100%;" type="checkbox"
							name="useleavebanmessages" id="useleavebanmessages"
							<%=useleavebanmessages.equals("true") ? "checked" : ""%> /></td>
					</tr>
					<tr>
						<td>Nicknamenwechselnachricht</td>
						<td><input style="width: 100%;" type="checkbox"
							name="usenickchangemessages" id="usenickchangemessages"
							<%=usenickchangemessages.equals("true") ? "checked" : ""%> /></td>
					</tr>
					<tr>
						<td>Antworth&auml;ufigkeit (AIML)</td>
						<td align="center"><input maxlength="3" size="10" min="1"
							max="100" type="number" name="botrandom" id="botrandom"
							value="<%=botrandom%>" />%</td>
					</tr>
				</tbody>
			</table>
			<input style="width: 100%; color: #D76C0D; font-weight: bold;"
				type="submit" id="submitgeneral"
				value="<fmt:message key="global.save_settings" />">
		</form>
	</div>

	<div class="jive-contentBoxHeader">
		<fmt:message key="config.page.headerbotuser" />
	</div>
	<div class="jive-contentBox" id='myroomdiv'>
		<form action="chatbot-config.jsp?savebot" method="post">
			<table cellpadding="0" cellspacing="0"
				style="width: 100%; margin: 0; padding: 0;">
				<thead>
					<tr>
						<th>Benutzer</th>
						<th>Passwort</th>
						<th>Nickname</th>
						<th></th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td><input style="width: 100%;" type="text" name="botuser"
							id="botuser" value="<%=botuser%>"></td>
						<td><input style="width: 100%;" type="password"
							name="botpass" id="botpass" value="<%=botpass%>"></td>
						<td><input style="width: 100%;" type="text" name="botnick"
							id="botnick" value="<%=botnick%>"></td>
						<td><input
							style="width: 100%; color: #D76C0D; font-weight: bold;"
							type="submit" id="submitrooms"
							value="<fmt:message key="global.save_settings" />"></td>
					</tr>
				</tbody>
			</table>
		</form>
	</div>


</body>
</html>
