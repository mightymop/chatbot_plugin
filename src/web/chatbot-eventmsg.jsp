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
			final Logger Log = LoggerFactory.getLogger("chatbot-eventmsg.jsp");

			boolean savebot = request.getParameter("savebot") != null;
			boolean savegeneral = request.getParameter("savegeneral") != null;

			String update = request.getParameter("update");			
			String success = request.getParameter("success");
			String error = null;
			
			if (request.getParameter("savenickchangemessages")!= null ) 
		    {			  
			    if ( ParamUtils.getParameter( request, "nickchangemessages" ) != null )
	            {			      
			        JiveGlobals.setProperty( "plugin.chatbot.nickchangemessages", ParamUtils.getParameter( request, "nickchangemessages" ));
	            }
	            else
	            {
	            	JiveGlobals.deleteProperty("plugin.chatbot.nickchangemessages");
	            }
			    
			    response.sendRedirect("chatbot-eventmsg.jsp?uploadsuccess=true");
	            return;
		    }
			
			if (request.getParameter("saveleavebanmessages")!= null ) 
		    {			  
			    if ( ParamUtils.getParameter( request, "leavebanmessages" ) != null )
	            {			      
			        JiveGlobals.setProperty( "plugin.chatbot.leavebanmessages", ParamUtils.getParameter( request, "leavebanmessages" ));
	            }
	            else
	            {
	            	JiveGlobals.deleteProperty("plugin.chatbot.leavebanmessages");
	            }
			    
			    response.sendRedirect("chatbot-eventmsg.jsp?uploadsuccess=true");
	            return;
		    }	
			
			if (request.getParameter("saveleavekickmessages")!= null ) 
		    {			  
			    if ( ParamUtils.getParameter( request, "leavekickmessages" ) != null )
	            {			      
			        JiveGlobals.setProperty( "plugin.chatbot.leavekickmessages", ParamUtils.getParameter( request, "leavekickmessages" ));
	            }
	            else
	            {
	            	JiveGlobals.deleteProperty("plugin.chatbot.leavekickmessages");
	            }
			    
			    response.sendRedirect("chatbot-eventmsg.jsp?uploadsuccess=true");
	            return;
		    }	
			
			if (request.getParameter("saveleavemessages")!= null ) 
		    {			  
			    if ( ParamUtils.getParameter( request, "leavemessages" ) != null )
	            {			      
			        JiveGlobals.setProperty( "plugin.chatbot.leavemessages", ParamUtils.getParameter( request, "leavemessages" ));
	            }
	            else
	            {
	            	JiveGlobals.deleteProperty("plugin.chatbot.leavemessages");
	            }
			    
			    response.sendRedirect("chatbot-eventmsg.jsp?uploadsuccess=true");
	            return;
		    }	
			
			if (request.getParameter("savejoinmessagesrest")!= null ) 
		    {			  
			    if ( ParamUtils.getParameter( request, "joinmessagesrest" ) != null )
	            {			      
			        JiveGlobals.setProperty( "plugin.chatbot.joinmessagesrest", ParamUtils.getParameter( request, "joinmessagesrest" ));
	            }
	            else
	            {
	            	JiveGlobals.deleteProperty("plugin.chatbot.joinmessagesrest");
	            }
			    
			    response.sendRedirect("chatbot-eventmsg.jsp?uploadsuccess=true");
	            return;
		    }	
			
			if (request.getParameter("savejoinmessagesadmin")!= null ) 
		    {			  
			    if ( ParamUtils.getParameter( request, "joinmessagesadmin" ) != null )
	            {			      
			        JiveGlobals.setProperty( "plugin.chatbot.joinmessagesadmin", ParamUtils.getParameter( request, "joinmessagesadmin" ));
	            }
	            else
	            {
	            	JiveGlobals.deleteProperty("plugin.chatbot.joinmessagesadmin");
	            }
			    
			    response.sendRedirect("chatbot-eventmsg.jsp?uploadsuccess=true");
	            return;
		    }	
			
			if (request.getParameter("savejoinmessagesowners")!= null ) 
		    {			  
			    if ( ParamUtils.getParameter( request, "joinmessagesowners" ) != null )
	            {			      
			        JiveGlobals.setProperty( "plugin.chatbot.joinmessagesowners", ParamUtils.getParameter( request, "joinmessagesowners" ));
	            }
	            else
	            {
	            	JiveGlobals.deleteProperty("plugin.chatbot.joinmessagesowners");
	            }
			    
			    response.sendRedirect("chatbot-eventmsg.jsp?uploadsuccess=true");
	            return;
		    }	
			
		
			String joinmessagesowners = JiveGlobals.getProperty("plugin.chatbot.joinmessagesowners",String.join("\n", ChatbotPlugin.ownerbegruessung));
	        String joinmessagesadmin =JiveGlobals.getProperty("plugin.chatbot.joinmessagesadmin",String.join("\n", ChatbotPlugin.adminbegruessung));
	        String joinmessagesrest = JiveGlobals.getProperty("plugin.chatbot.joinmessagesrest",String.join("\n", ChatbotPlugin.restbegruessung));
			String leavemessages = JiveGlobals.getProperty("plugin.chatbot.leavemessages",String.join("\n", ChatbotPlugin.leavemessages_other));
            String leavekickmessages = JiveGlobals.getProperty("plugin.chatbot.leavekickmessages",String.join("\n", ChatbotPlugin.kickmessage));
            String leavebanmessages = JiveGlobals.getProperty("plugin.chatbot.leavebanmessages",String.join("\n", ChatbotPlugin.banmessage));
			String nickchangemessages = JiveGlobals.getProperty("plugin.chatbot.nickchangemessages",String.join("\n", ChatbotPlugin.nickchange));
%>
<!DOCTYPE html>
<html>
<head>
<title><fmt:message key="config.page.title" /></title>
<meta name="pageID" content="chatbot-eventmsg" />
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

<script>jQuery.noConflict();</script>

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
            Fehler beim Ausführen der Aktion!
        </admin:infobox>
	</c:if>
	<c:if test="${param.actionsuccess eq 'true'}">
		<admin:infobox type="success" style="color:green;">
            Aktion erfolgreich gestartet.
        </admin:infobox>
	</c:if>
	<c:if test="${param.actionsuccess eq 'false'}">
		<admin:infobox type="error" style="color:red;">
            Aktion konnte nicht ausgeführt werden.
        </admin:infobox>
	</c:if>
	<% if (error != null) { %>

	<div class="jive-error">
		<table cellpadding="0" cellspacing="0" border="0">
			<tbody>
				<tr>
					<td class="jive-icon"><img src="images/error-16x16.gif"
						width="16" height="16" border="0" alt=""></td>
					<td class="jive-icon-label">
						<% if ( "csrf".equalsIgnoreCase( error )  ) { %> <fmt:message
							key="global.csrf.failed" /> <% } else { %> <fmt:message
							key="admin.error" />: <c:out value="error"></c:out> <% } %>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
	<br>

	<%  } %>


	<%  if (success != null) { %>

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

	<%  } %>
	<div class="jive-contentBoxHeader">
		<fmt:message key="config.page.headerevengmsg" />
	</div>
	<div class="jive-contentBox" id='mygeneraldiv'>
		<form action="chatbot-eventmsg.jsp" method="post">
			<input type="hidden" name="savejoinmessagesowners" id="savejoinmessagesowners" value="true" />
			<table cellpadding="0" cellspacing="0" style="width: 100%; margin: 0; padding: 0;">
				<thead><tr><th>Besitzerbegrüßungen</th></tr></thead>
				<tbody>
					<tr><td><textarea style="width: 100%;" name="joinmessagesowners" id="joinmessagesowners" ><%=joinmessagesowners%></textarea></td></tr>
				</tbody>
			</table>
			<input style="width: 100%; color: #D76C0D; font-weight: bold;" type="submit" id="submitgeneral" value="<fmt:message key="global.save_settings" />">
		</form>
		<br/><br/>
		<form action="chatbot-eventmsg.jsp" method="post">
			<input type="hidden" name="savejoinmessagesadmin" id="savejoinmessagesadmin" value="true" />
			<table cellpadding="0" cellspacing="0" style="width: 100%; margin: 0; padding: 0;">
				<thead><tr><th>Adminbegrüßungen</th></tr></thead>
				<tbody>
					<tr><td><textarea style="width: 100%;" name="joinmessagesadmin" id="joinmessagesadmin" ><%=joinmessagesadmin%></textarea></td></tr>
				</tbody>
			</table>
			<input style="width: 100%; color: #D76C0D; font-weight: bold;" type="submit" id="submitgeneral" value="<fmt:message key="global.save_settings" />">
		</form>
		<br/><br/>
		<form action="chatbot-eventmsg.jsp" method="post">
			<input type="hidden" name="savejoinmessagesrest" id="savejoinmessagesrest" value="true" />
			<table cellpadding="0" cellspacing="0" style="width: 100%; margin: 0; padding: 0;">
				<thead><tr><th>rest. Userbegrüßungen</th></tr></thead>
				<tbody>
					<tr><td><textarea style="width: 100%;" name="joinmessagesrest" id="joinmessagesrest" ><%=joinmessagesrest%></textarea></td></tr>
				</tbody>
			</table>
			<input style="width: 100%; color: #D76C0D; font-weight: bold;" type="submit" id="submitgeneral" value="<fmt:message key="global.save_settings" />">
		</form>
		<br/><br/>
		<form action="chatbot-eventmsg.jsp" method="post">
			<input type="hidden" name="saveleavemessages" id="saveleavemessages" value="true" />
			<table cellpadding="0" cellspacing="0" style="width: 100%; margin: 0; padding: 0;">
				<thead><tr><th>Verabschiedungsnachrichten</th></tr></thead>
				<tbody>
					<tr><td><textarea style="width: 100%;" name="leavemessages" id="leavemessages" ><%=leavemessages%></textarea></td></tr>
				</tbody>
			</table>
			<input style="width: 100%; color: #D76C0D; font-weight: bold;" type="submit" id="submitgeneral" value="<fmt:message key="global.save_settings" />">
		</form>	
		<br/><br/>
		<form action="chatbot-eventmsg.jsp" method="post">
			<input type="hidden" name="saveleavekickmessages" id="saveleavekickmessages" value="true" />
			<table cellpadding="0" cellspacing="0" style="width: 100%; margin: 0; padding: 0;">
				<thead><tr><th>Kick-Nachrichten</th></tr></thead>
				<tbody>
					<tr><td><textarea style="width: 100%;" name="leavekickmessages" id="leavekickmessages" ><%=leavekickmessages%></textarea></td></tr>
				</tbody>
			</table>
			<input style="width: 100%; color: #D76C0D; font-weight: bold;" type="submit" id="submitgeneral" value="<fmt:message key="global.save_settings" />">
		</form>	
		<br/><br/>
		<form action="chatbot-eventmsg.jsp" method="post">
			<input type="hidden" name="saveleavebanmessages" id="saveleavebanmessages" value="true" />
			<table cellpadding="0" cellspacing="0" style="width: 100%; margin: 0; padding: 0;">
				<thead><tr><th>Ban-Nachrichten</th></tr></thead>
				<tbody>
					<tr><td><textarea style="width: 100%;" name="leavebanmessages" id="leavebanmessages" ><%=leavebanmessages%></textarea></td></tr>
				</tbody>
			</table>
			<input style="width: 100%; color: #D76C0D; font-weight: bold;" type="submit" id="submitgeneral" value="<fmt:message key="global.save_settings" />">
		</form>	
		<br/><br/>
		<form action="chatbot-eventmsg.jsp" method="post">
			<input type="hidden" name="savenickchangemessages" id="savenickchangemessages" value="true" />
			<table cellpadding="0" cellspacing="0" style="width: 100%; margin: 0; padding: 0;">
				<thead><tr><th>Nicknamenwechselnachrichten</th></tr></thead>
				<tbody>
					<tr><td><textarea style="width: 100%;" name="nickchangemessages" id="nickchangemessages" ><%=nickchangemessages%></textarea></td></tr>
				</tbody>
			</table>
			<input style="width: 100%; color: #D76C0D; font-weight: bold;" type="submit" id="submitgeneral" value="<fmt:message key="global.save_settings" />">
		</form>	
		<br/><br/>
	</div>
	
</body>
</html>
