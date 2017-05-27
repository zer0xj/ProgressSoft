<!DOCTYPE html>
<html>
<head>
 
<meta charset="UTF-8">
 
<title>File Loading Status</title>
</head>
<body>
 
<jsp:useBean id="parser" class="org.ProgressSoft.webapp.ParseCSV"/>
  
<h3>${parser.status}</h3>

<br>
<a href="loadfile.jsp">Load file</a>
<br>
<a href="setfile.jsp">Choose file to load</a>

<meta http-equiv="refresh" content="5; url=${pageContext.request.contextPath}/index.jsp" />

</body>
</html>
