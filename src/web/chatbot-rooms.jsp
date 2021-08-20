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
			final Logger Log = LoggerFactory.getLogger("chatbot-rooms.jsp");
			
			boolean saveroom = request.getParameter("saveroom") != null;
			
			String update = request.getParameter("update");			
			String success = request.getParameter("success");
			String error = null;
		
			if (saveroom) 
		    {
		        try (InputStream is = request.getInputStream()) 
		        {
		            if (is.available()>0)
		            {
		                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			            StringBuilder value = new StringBuilder();
			            char[] buffer = new char[1024];
			            for (int length = 0; (length = reader.read(buffer)) > 0;) {
			                value.append(buffer, 0, length);
			            }
			            String daten = value.toString();
			            if (daten.trim().length()>0)
			            {
				            int posfirst = daten.indexOf("[");
				            int poslast = daten.lastIndexOf("]");
				            if (posfirst!=-1&&poslast!=-1)
				            {
					            daten=daten.substring(posfirst,poslast+1);
					            //daten=daten.replaceAll("[^0-9a-zA-ZäöüÄÖÜß \\\\\"\\'\\@\\{\\}\\:\\*\\!\\?\\-\\+\\$\\%\\&\\/\\\\\\(\\)\\[\\]\\~\\#\\.\\,\\;\\<\\>\\|\\_]","")
					            DatabaseUtils.setRooms(daten);
					            ChatbotPlugin.refresh();
				            }
			            }
		            }
		            response.sendRedirect("chatbot-rooms.jsp?uploadsuccess=true");
		            return;
		        } catch (Exception e) 
		        {
		            Log.error(e.getMessage());
		            response.sendRedirect("chatbot-rooms.jsp?uploadsuccess=false");
		            return;
		        }
		    }
			
			String rooms = ChatbotPlugin.getRooms();
			
%>
<!DOCTYPE html>
<html>
<head>
<title><fmt:message key="config.page.title" /></title>
<meta name="pageID" content="chatbot-rooms" />
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
<script>

	var strjsonrooms = '<%=rooms.replaceAll("[\u0000-\u001f]","")%>';
	strjsonrooms = strjsonrooms.replace('[^0-9a-zA-ZäöüÄÖÜß \@\"\{\}:\*\-\+\\$\%\/\\\(\)\[\]\#\'\.\,\;\<\>\|\_]', '');
	var jsonrooms = JSON.parse(strjsonrooms); 
	var roomlisttable = null;

	function deleteroom(id) {
		var checked = jQuery("#" + id).prop("checked");
		var id = id.split("_")[0];
		
		jQuery("#" + id+"_qaonline").prop("disabled",!checked);
		jQuery("#" + id+"_qadb").prop("disabled",!checked);
		jQuery("#" + id+"_qaaiml").prop("disabled",!checked);
		jQuery("#" + id+"_qarandom").prop("disabled",!checked);
		jQuery("#" + id+"_qarandom").val("-1");
		
		for (var i = 0; i < jsonrooms.length; i++) {
			if (md5(jsonrooms[i].roomjid) === id) {
				jsonrooms[i].aktiv=checked;
				jQuery("#datarooms").val(JSON.stringify(jsonrooms));
				
				break;
			}
		}
	}
	
	
	function changeqaonline(id) {
		var checked = jQuery("#" + id).prop("checked");
		var id = id.split("_")[0];

		for (var i = 0; i < jsonrooms.length; i++) {
			if (md5(jsonrooms[i].roomjid) === id) {
				jsonrooms[i].qaonline=checked;
				jQuery("#datarooms").val(JSON.stringify(jsonrooms));
				
				break;
			}
		}
	}
	
	function changeqadb(id) {
		var checked = jQuery("#" + id).prop("checked");
		var id = id.split("_")[0];

		for (var i = 0; i < jsonrooms.length; i++) {
			if (md5(jsonrooms[i].roomjid) === id) {
				jsonrooms[i].qadb=checked;
				jQuery("#datarooms").val(JSON.stringify(jsonrooms));
				
				break;
			}
		}
	}
	
	function changeqaaiml(id) {
		var checked = jQuery("#" + id).prop("checked");
		var id = id.split("_")[0];

		for (var i = 0; i < jsonrooms.length; i++) {
			if (md5(jsonrooms[i].roomjid) === id) {
				jsonrooms[i].qaaiml=checked;
				jQuery("#datarooms").val(JSON.stringify(jsonrooms));
				
				break;
			}
		}
	}
	
	function changeqarandom(id) {
		var val = jQuery("#" + id).val();
		var id = id.split("_")[0];

		for (var i = 0; i < jsonrooms.length; i++) {
			if (md5(jsonrooms[i].roomjid) === id) {
				jsonrooms[i].qarandom=val;
				jQuery("#datarooms").val(JSON.stringify(jsonrooms));
				
				break;
			}
		}
	}

	function initTable() {

		roomlisttable = jQuery('#roomlist').DataTable({
			"language" : {
				"url" : "html/German.json"
			},
			dom : 'Bfrtip',
			"order" : [ [ 1, 'asc' ] ],
			"columnDefs": [ 
			{
			"orderable": true
			},
			{
			"orderable": true
			},
			{
			"orderable": false
			}, 
			{
			"orderable": false
			},
			{
			"orderable": false
			},
			{
			"orderable": false
			},
			{
			"orderable": false
			} ]
		});
	}

	function loadTables() {

		var bbody = jQuery("body");
		if (!bbody.hasClass("loading"))
			bbody.addClass("loading");
		var rooms = document.getElementById('rooms');
		var roomlist = jsonrooms;
		if (roomlist == null || roomlist.length == 0) {
			bbody.removeClass("loading");
			alert("Keine R&auml;ume gefunden.");
		}
		
		var strrooms = "<table id=\"roomlist\" class=\"display\" style=\"width:100%\"><thead><th>Raumname</th><th>JID</th>"+
				  "<th>Q/A Online</th><th>Q/A Datenbank</th><th>Q/A AIML</th><th>Antworth&auml;ufigkeit (AIML)</th><th>Betreten</th></thead><tbody>";
		for (var i = 0; i < roomlist.length; i++) {
			
			var jid = roomlist[i].roomjid;
			
			strrooms += "<tr>"+
			"<td align='center'>" + roomlist[i].name + "</td>"+
			"<td align='center'>" + jid + "</td>"+
			"<td align='center'><input type='checkbox' id='"+md5(roomlist[i].roomjid)+"_qaonline' value='"+roomlist[i].qaonline+"' "+(roomlist[i].qaonline?"checked":"")+" "+(roomlist[i].aktiv?"":"disabled")+" onchange='changeqaonline(id)'/></td>"+
			"<td align='center'><input type='checkbox' id='"+md5(roomlist[i].roomjid)+"_qadb' value='"+roomlist[i].qadb+"' "+(roomlist[i].qadb?"checked":"")+" "+(roomlist[i].aktiv?"":"disabled")+" onchange='changeqadb(id)'/></td>"+
			"<td align='center'><input type='checkbox' id='"+md5(roomlist[i].roomjid)+"_qaaiml' value='"+roomlist[i].qaaiml+"' "+(roomlist[i].qaaiml?"checked":"")+" "+(roomlist[i].aktiv?"":"disabled")+" onchange='changeqaaiml(id)'/></td>"+
			"<td align='center'><input type='number' maxlength='3' size='10' min='0' max='100' id='"+md5(roomlist[i].roomjid)+"_qarandom' value='"+roomlist[i].qarandom+"' "+(roomlist[i].aktiv?"":"disabled")+" onkeyup='changeqarandom(id)'/></td>"+
			"<td align='center'><input type='checkbox' id='"+md5(roomlist[i].roomjid)+"_delete' value='false' onchange='deleteroom(id)' "+(roomlist[i].aktiv?"checked":"")+"/></td>"+
			"</tr>";
		}
		strrooms += "</tbody></table>"
		rooms.innerHTML = strrooms;
				
		initTable();
		bbody.removeClass("loading");
	}
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
		<fmt:message key="config.page.headerrooms" />
	</div>
	<div class="jive-contentBox" id='myroomdiv'>
		<form action="chatbot-rooms.jsp?saveroom"
			enctype="multipart/form-data" method="post">

			<input type="hidden" name="datarooms" id="datarooms" value="" /> <input
				type="submit" id="submitrooms"
				style="color: #D76C0D; font-weight: bold;"
				value="<fmt:message key="global.save_settings" />">
			<div id="rooms"></div>
		</form>
	</div>

	<br />
	

	<script>
		jQuery(document).ready(function() {

			loadTables();
			jQuery("body").removeClass("loading");
		});
	</script>
</body>
</html>
