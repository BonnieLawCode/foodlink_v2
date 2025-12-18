package com.foodlink.controller.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ImageServlet
 */
/**
 * JP：外部保存した画像を配信するServlet
 * CN：把外部目录保存的图片通过URL输出
 */
@WebServlet("/uploads/*")
public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Path BASE_DIR = Paths.get("D:/foodlink_uploads");
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		
		// 例：/uploads/products/xxx.jpg
        String reqPath = request.getPathInfo(); // 例：/products/xxx.jpg になる場合もある
        // 由于使用 /uploads/* 映射，getPathInfo() 通常是 "/products/xxx.jpg"
        if (reqPath == null || reqPath.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // JP：実ファイルパスを組み立て / CN：拼出真实文件路径
        Path file = BASE_DIR.resolve("uploads").resolve(reqPath.substring(1)).normalize();
        // -> C:/foodlink_uploads/uploads/products/xxx.jpg

        // JP：ディレクトリ外参照対策 / CN：防止 ../ 越界
        Path allowedRoot = BASE_DIR.resolve("uploads").normalize();
        if (!file.startsWith(allowedRoot) || !Files.exists(file) || Files.isDirectory(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // JP：Content-Type 自動判定 / CN：自动判断类型
        String contentType = Files.probeContentType(file);
        if (contentType == null) contentType = "application/octet-stream";
        response.setContentType(contentType);

        // JP：出力 / CN：输出
        Files.copy(file, response.getOutputStream());
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
