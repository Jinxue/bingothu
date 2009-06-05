<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="Bingo.search.SearchResultBean" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Welcome to Bingo: A Video Search Engine</title>
</head>
<body bgcolor="FFFFFF">
	<TABLE align="center" valign="middle">
		<TBODY>
			<TR>
			</TR>
			<TR>
				<TD><img src="logo.jpg"></TD>
			</TR>
			<TR>
				<TD><FORM action="SearchProcess" method="POST">
					<INPUT name="searchWord" id="searchWord" type="text" size="40">
					<INPUT id="doSearch" type="submit" value="search">
				</FORM></TD>
			</TR>
			<TR></TR><TR></TR><TR></TR><TR></TR><TR></TR><TR></TR><TR></TR>
			<TR>
				<TD align="center"> <FONT face="Arial" size="-1">Copyright @Tsinghua University. 2009</FONT></TD>
			</TR>
		</TBODY>
	</TABLE>
	
	<TABLE>
		<TBODY>
		<%
			ArrayList<SearchResultBean> searchResult = (ArrayList)request.getAttribute("searchResult");
			int resultCount = 0;
			if(null != searchResult){
				resultCount = searchResult.size();
			}
			for (int i = 0; i < resultCount; i ++){
				SearchResultBean resultBean = (SearchResultBean) searchResult.get(i);
				String title = resultBean.getTitle();
				String desc = resultBean.getDescription();
				String url = resultBean.getUrl();
				String imageFileName = resultBean.getImageFileName();
		%>
			<TR>
				<TD>
					<A href="<%=url %>"><%=title %></A>
					<p><%=desc %></p>
				</TD>
			</TR>
			<TR><TD><HR /></TD></TR>
		<%
			}
		%>	
			
		</TBODY>
		
	</TABLE>
</body>
</html>