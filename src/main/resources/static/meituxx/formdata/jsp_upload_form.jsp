<%@ page language="java" contentType="text/html; charset=GB18030"
    pageEncoding="GB18030"%>
<%@ page import="org.apache.commons.fileupload.*"%>
<%@ page import="org.apache.commons.fileupload.servlet.*"%>
<%@ page import="org.apache.commons.fileupload.disk.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
 
<%
    File uploadPath = new File((String)request.getRealPath("photo")+"/");//�ϴ��ļ�Ŀ¼
    
    if (!uploadPath.exists()) {
       uploadPath.mkdirs();
    }
    // ��ʱ�ļ�Ŀ¼
    File tempPathFile = new File((String)request.getRealPath("photo")+"/");
    if (!tempPathFile.exists()) {
       tempPathFile.mkdirs();
    }
    try {
       // Create a factory for disk-based file items
       DiskFileItemFactory factory = new DiskFileItemFactory();
 
       // Set factory constraints
       factory.setSizeThreshold(4096); // ���û�������С��������4kb
       factory.setRepository(tempPathFile);//���û�����Ŀ¼
 
       // Create a new file upload handler
       ServletFileUpload upload = new ServletFileUpload(factory);
 
       // Set overall request size constraint
       upload.setSizeMax(4194304); // ��������ļ��ߴ磬������4MB
 
       List<FileItem> items = upload.parseRequest(request);//�õ����е��ļ�
       Iterator<FileItem> i = items.iterator();
       while (i.hasNext()) {
           FileItem fi = (FileItem) i.next();
           String fileName = fi.getName();
           System.out.println(fileName);
           if (fileName != null) {
		       File fullFile = new File(fi.getName());
		       long currentTime=System.currentTimeMillis() ;
 			   String newFileName = "MT_"+Long.toString(currentTime)+".jpg";
		       File savedFile = new File(uploadPath, newFileName);
		       fi.write(savedFile);
           }
       }
       out.print("upload succeed");
    } catch (Exception e) {
       e.printStackTrace();
    }
%>
