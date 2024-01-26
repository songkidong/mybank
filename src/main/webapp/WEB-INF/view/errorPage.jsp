<%@ page language="java" contentType="text/html; charset=EUC-KR"
	pageEncoding="EUC-KR"%>
<!-- 에러 페이지 생성시 아래코드 무조건 넣기 -->
<%@ page isErrorPage="true"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="EUC-KR">
<title>Insert title here</title>
</head>
<body>
	<h1>에러 페이지</h1>
	<p>${satusCode}</p>
	<p>${message}</p>
</body>
</html>