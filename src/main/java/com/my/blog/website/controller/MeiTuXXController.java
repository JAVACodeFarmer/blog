package com.my.blog.website.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.ui.ModelMap;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Y.YH
 * @date 2020/4/22 17:41
 * @description
 */
@RestController
public class MeiTuXXController {

    /**
     * Content-type:application/octet-stream； 处理方式
     * @param session
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/meitu/octetstream")
    @ResponseBody
    public Object octetstream(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        /* 1. Content-type:application/octet-stream； 处理方式：*/
        HashMap<String, Object> map = new HashMap<>();
        //1.1 第一种方式
        /*
        String fileName = request.getParameter("filename");
        Path path = Paths.get("d:\\" + fileName);
        try {
            FileCopyUtils.copy(request.getInputStream(), Files.newOutputStream(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        //1.2 第二种方式
        try{
            System.out.println("json map :"+ JSON.toJSONString(request.getParameterMap()));
            String temp = session.getId();//获得sessionId
            String fileName = request.getParameter("filename");
            String filetype = request.getParameter("filetype");
            System.out.println(String.format("文件名:%s,类型：%s", fileName, filetype));
            File f1 = new File(request.getRealPath("photo")+"/","tmp" + temp +"/" + fileName);    //获得photo所在的目录，并加上sessionId
            if (!f1.exists()) {
                // 先得到文件的上级目录，并创建上级目录，在创建文件
                f1.getParentFile().mkdirs();
                f1.createNewFile();
            }

            FileOutputStream o=new FileOutputStream(f1);//文件输出流指向上传文件所在路径
            System.out.println(o);
            InputStream in = request.getInputStream();                                         //从客户端获得文件输入流
            int n;
            byte b[]=new byte[10000000];//设置缓冲数组的大小
            while((n=in.read(b))!=-1){
                o.write(b,0,n);                                                  //将数据从输入流读入到缓冲数组然后再从缓冲数组写入到文件中
            }
            o.close();
            in.close();                   //关闭输入流和文件输出流
            RandomAccessFile random=new RandomAccessFile(f1,"r");       //文件随机读取写入流
            int second=1;
            String secondLine=null;
            while(second<=2){
                secondLine=random.readLine();//读入临时文件名
                second++;
            }
            int position=secondLine.lastIndexOf('\\');
            String filename=new String((secondLine.substring(position+1,secondLine.length()-1)).getBytes("iso-8859-1"),   "gb2312");//去掉临时文件名中的sessionId，获得文件名，并用iso-8859-1编码， 避免出现中文乱码问题
            random.seek(0);
            long forthEnPosition=0;
            int forth=1;
            while((n=random.readByte())!=1&&forth<=4){
                if(n=='\n'){
                    forthEnPosition=random.getFilePointer();
                    forth++;
                }//去掉临时文件开头的4个'\n'字符
            }
            long currentTime=System.currentTimeMillis() ;
            String newFileName = "MT_"+Long.toString(currentTime);
            File f2=new File((String)request.getRealPath("photo")+"/", newFileName+"."+filetype);   //以文件的名创建另一个文件随机读取
            RandomAccessFile random2=new RandomAccessFile(f2,"rw");
            random.seek(random.length());
            long endPosition=random.getFilePointer(); //以文件的名创建另一个文件随机读取写入流
            int j=1;
            long mark=endPosition;
            while(mark<=0&&j<=6){  //去掉临时文件末尾的6个'\n'字符
                mark--;
                random.seek(mark);
                n=random.readByte();
                if(n=='\n'){
                    endPosition=random.getFilePointer();
                    j++;
                }
            }
            random.seek(forthEnPosition);
            long startPosition=random.getFilePointer();
            while(startPosition<endPosition-1){//将临时文件去掉头尾后写入到新建的文件中
                n=random.readByte();
                random2.write(n);
                startPosition=random.getFilePointer();
            }
            random2.close();
            random.close();
            f1.delete();
            System.out.println("上传文件成功！");
            map.put("result", "上传文件成功！");
        }catch(Exception e)  {
            map.put("result", "上传文件失败！"+ e.getMessage());
            System.out.println("上传文件失败！");
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Content-type:multipart/form-data； 处理方式
     * @param session
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/meitu/formdata")
    @ResponseBody
    public Object formdata(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        HashMap<Object, Object> map = new HashMap<>();

//        MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
//        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multiRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            MultipartFile file = entry.getValue();
            map.put("originalFilename", file.getOriginalFilename());
            map.put("name", file.getName());
            map.put("fileSize", file.getSize());
            String photo = request.getRealPath("photo");
            System.out.println("photo: "+ photo);
            String fileName = file.getOriginalFilename();
            Path path = Paths.get("d:\\" + fileName);
            try {
                FileCopyUtils.copy(file.getInputStream(), Files.newOutputStream(path));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //--------官方处理方式 不会用--------------
//
//        File uploadPath = new File(request.getRealPath("photo")+"/");//上传文件目录
//        System.out.println(uploadPath);
//        if (!uploadPath.exists()) {
//            uploadPath.mkdirs();
//        }
//        // 临时文件目录
//        File tempPathFile = new File(request.getRealPath("photo")+"/");
//        if (!tempPathFile.exists()) {
//            tempPathFile.mkdirs();
//        }
//        try {
//            // Create a factory for disk-based file items
//            DiskFileItemFactory factory = new DiskFileItemFactory();
//
//            // Set factory constraints
//            factory.setSizeThreshold(4096); // 设置缓冲区大小，这里是4kb
//            factory.setRepository(tempPathFile);//设置缓冲区目录
//
//            // Create a new file upload handler
//            ServletFileUpload upload = new ServletFileUpload(factory);
//
//            // Set overall request size constraint
//            upload.setSizeMax(4194304); // 设置最大文件尺寸，这里是4MB
//
//            List<FileItem> items = upload.parseRequest(request);//得到所有的文件
//            Iterator<FileItem> i = items.iterator();
//            while (i.hasNext()) {
//                FileItem fi =  i.next();
//                String fileName = fi.getName();
//                System.out.println(fileName);
//                if (fileName != null) {
//                    File fullFile = new File(fi.getName());
//                    long currentTime=System.currentTimeMillis() ;
//                    String newFileName = "MT_"+Long.toString(currentTime)+".jpg";
//                    File savedFile = new File(uploadPath, newFileName);
//                    fi.write(savedFile);
//                }
//            }
//            System.out.println("upload succeed");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
        return map;
    }


    /**
     * base64方式接受文件
     * @param session
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/meitu/base64")
    @ResponseBody
    public Object base64(HttpSession session, HttpServletRequest request, HttpServletResponse response, ModelMap req) throws IOException {
        HashMap<Object, Object> map = new HashMap<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        while ((i = request.getInputStream().read()) != -1) {
            baos.write(i);
        }
        String str = baos.toString();
        Map<String, String> map1 = JSON.parseObject(str, new TypeReference<Map<String, String>>() {
        });
        System.out.println(JSON.toJSONString(map1));
        byte[] data = Base64Utils.decodeFromString(map1.get("data"));

        map.put("originalFilename", map1.get("fileName"));
        map.put("name", map1.get("fileType"));
        map.put("fileSize", data.length);
        String fileName =  map1.get("fileName");
        Path path = Paths.get("d:\\" + fileName);
        map.put("filePath", "d:\\" + fileName);
        try {
            FileCopyUtils.copy(data, Files.newOutputStream(path));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    //可用来代替 crossdomain.xml
//    @GetMapping("/crossdomain.xml")
//    public String crossdomain(){
//        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                "<cross-domain-policy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://www.adobe.com/xml/schemas/PolicyFile.xsd\">\n" +
//                "\t<allow-access-from domain=\"*.meitu.com\" secure=\"false\"/>\n" +
//                "</cross-domain-policy>";
//    }


}
