<!DOCTYPE html>
<html>
<head>
 
<meta charset="UTF-8">
 
<title>Choose File to Load</title>
</head>
<body>

<form action="${pageContext.request.contextPath}/CSVServlet" method="post">
	Please enter the file name:
	<input TYPE="text" NAME="filename">
	<br>
	<input TYPE="submit" value="Submit">
</form>

</body>
</html>
