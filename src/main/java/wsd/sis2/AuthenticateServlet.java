package wsd.sis2;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import wsd.authen.LdapLoginModule;

/**
 *
 * @author cpliu
 */
@WebServlet(name = "AuthenticateServlet", urlPatterns = {"/authenticate"}, initParams = {
    @WebInitParam(name = "ldap.server", value = "ldap.forumsys.com")})
public class AuthenticateServlet extends HttpServlet {

    @Resource(name="churchDB")
    DataSource dataSource;
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
        response.setContentType("text/plain;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet AuthenticateServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AuthenticateServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

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
        response.setContentType("text/plain;charset=UTF-8");
        String user = request.getParameter("user");
        String pwd = request.getParameter("pwd");
        try (PrintWriter out = response.getWriter()) {
            if (user==null || pwd==null) {
                response.setStatus(401);
                out.println("need user and pwd");
            }
            else {
                boolean bound;
                try {
                    bound = bind(user, pwd);
                    response.setStatus(bound ? 200 : 401);
                    out.println("Done");
                }
                catch (NamingException ex) {
                    response.setStatus(200);
                    out.println (dataSource == null ? "Null DS" : dataSource.getClass().getName());    
                }
            }
        }
    }
    
    private boolean bind(String user, String pwd) throws NamingException {
	// Set up environment for creating initial context
	Hashtable env = new Hashtable(11);
	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.SECURITY_AUTHENTICATION, "simple");
	env.put(Context.SECURITY_PRINCIPAL, String.format(LdapLoginModule.USER_TEMPLATE, user));
	env.put(Context.SECURITY_CREDENTIALS, pwd);
        String url = String.format(LdapLoginModule.URL_TEMPLATE, "ldap.forumsys.com");
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Binding to {0}", url);
	env.put(Context.PROVIDER_URL, url);
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "prin: {0}, cred: {1}",
            new Object[] {
                user, pwd
            });
        DirContext ctx = new InitialLdapContext(env,null);
        ctx.close();
        return true;
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
        response.setContentType("text/plain;charset=UTF-8");
        String user = request.getParameter("user");
        String pwd = request.getParameter("pwd");
        response.setStatus(200);
        try (PrintWriter out = response.getWriter()) {
            out.println (user == null ? "Null User" : user);
        }
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
