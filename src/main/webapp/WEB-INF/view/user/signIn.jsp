<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!-- header.jsp  -->
<%@ include file="/WEB-INF/view/layout/header.jsp"%>

<div class="col-sm-8">
	<h2>로그인</h2>
	<h5>어서오세요 환영합니다</h5>
	<form action="/user/sign-in" method="post">
		<div class="form-group">
			<label for="username">username:</label> <input type="text"
				name="username" class="form-control" placeholder="Enter username"
				id="username" value="길동">
		</div>
		<div class="form-group">
			<label for="pwd">password:</label> <input type="password"
				name="password" class="form-control" placeholder="Enter password"
				id="pwd" value="1234">
		</div>
		<button type="submit" class="btn btn-primary">로그인</button>
		<a
			href="https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=22b5d7d5af96960c53924dbcef20a6e3&redirect_uri=http://localhost:80/user/kakao-callback">
			<img alt="" src="/images/kakao_login_small.png" width="75"
			height="38">
		</a>
		<!-- <a
			href="https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=oLRZrhIDwIY3zzTgv5BB&state=test&redirect_uri=http://localhost:80/user/naver-callback">
			<img alt="" src="/images/btnG_축약형.png" width="75"
			height="38">
		</a> -->
	</form>
</div>
</br>
</div>

<!-- footer.jsp  -->
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>