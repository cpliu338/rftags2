package wsd.sis2;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.security.Principal;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;
import wsd.authen.UserPrincipal;

/**
 *
 * @author cp_liu
 */
public class Sis2Servlet extends HttpServlet {

    String base_url;
    
    @Override
    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);
        base_url = getServletConfig().getInitParameter("base_url");
    }
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Sis2Servlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Sis2Servlet at " + request.getContextPath() + "</h1>");
            out.println("<h2>Path Info " + request.getPathInfo() + "</h2>");
            out.println("<h3>Base url " + base_url + "</h3>");
            try {
                out.println(grab("http://087-173.onebb.com/blogs/rsync"));
            } catch (InterruptedException ex) {
                Logger.getLogger(Sis2Servlet.class.getName()).log(Level.SEVERE, null, ex);
                out.println(ex.getMessage());
            }
            UserPrincipal principal = (UserPrincipal)request.getUserPrincipal();
            out.println("<p>User name " + principal.getName() + "</p>");
            out.println("<p>SN " + principal.getMap().get("sn") + "</p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    private String grab(String url) throws IOException, InterruptedException {
        final StringBuffer output = new StringBuffer();
        try (CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault()) {
        //try {
            final CountDownLatch latch2 = new CountDownLatch(1);
            final HttpGet request3 = new HttpGet(url);
            httpclient.start();
            HttpAsyncRequestProducer producer3 = HttpAsyncMethods.create(request3);
            AsyncCharConsumer<HttpResponse> consumer3 = new AsyncCharConsumer<HttpResponse>() {

                HttpResponse response;

                @Override
                protected void onResponseReceived(final HttpResponse response) {
                    this.response = response;
                }
                @Override
                protected void onCharReceived(final CharBuffer buf, final IOControl ioctrl) throws IOException {
                    // Do something useful
                    output.append(escapeHTML(buf.toString()));
                }
    /*
            @Override
            protected void releaseResources() {
            }
    */
                @Override
                protected HttpResponse buildResult(final HttpContext context) {
                    return this.response;
                }

            };
            httpclient.execute(producer3, consumer3, new FutureCallback<HttpResponse>() {

                @Override
                public void completed(final HttpResponse response3) {
                    latch2.countDown();
                    System.out.println(request3.getRequestLine() + "->" + response3.getStatusLine());
                }

                @Override
                public void failed(final Exception ex) {
                    latch2.countDown();
                    System.out.println(request3.getRequestLine() + "->" + ex);
                }

                @Override
                public void cancelled() {
                    latch2.countDown();
                    System.out.println(request3.getRequestLine() + " cancelled");
                }

                });
                latch2.await();

            }
            return output.toString();
        }
    
    private String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
