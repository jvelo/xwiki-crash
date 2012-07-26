package org.xwiki.crash;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.impl.command.CRaSH;
import org.crsh.shell.impl.command.CRaSHSession;
import org.crsh.standalone.Bootstrap;
import org.crsh.text.Style;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;

@Component
@Named("CRaSH")
public class CrashScriptService implements ScriptService
{

    // @Inject
    // private Request request;

    @Inject
    private Execution execution;

    /**
     * Helper class, copied over from crash-demo.
     */
    private class SerializableTransient<T>
    {
        public transient T object;

        public SerializableTransient(T object)
        {
            this.object = object;
        }

        public SerializableTransient()
        {
        }
    }

    public Map<String, Object> welcome()
    {
        Shell shell = getShell();
        String welcome = shell.getWelcome();
        String prompt = shell.getPrompt();
        JSONObject obj = new JSONObject();
        obj.put("welcome", welcome);
        obj.put("prompt", prompt);
        return obj;
    }
    
    public List<Object> doExecute(Map<String, String> parameters)
    {
        Shell shell = getShell();
        HttpSession session = getSession();
        String line = getRequest().getParameter("line");
        String widthP = getRequest().getParameter("width");
        int width = 80;
        if (widthP != null) {
            try {
                Integer parsed = Integer.parseInt(widthP);
                if (parsed > 0) {
                    width = parsed;
                }
            } catch (NumberFormatException ignore) {
            }
        }
        ShellProcess process = shell.createProcess(line);
        CommandExecution execution = new CommandExecution(process, width);
        session.setAttribute("execution", new SerializableTransient<CommandExecution>(execution));
        try {
            ShellResponse response = execution.execute();
            if (response != null) {
                Style style = Style.reset;
                JSONArray array = new JSONArray();
                for (Object o : response.getReader()) {
                    if (o instanceof Style) {
                        style = style.merge((Style) o);
                    } else {
                        JSONObject elt = new JSONObject();
                        if (style != null
                            && (style.getBackground() != null || style.getForeground() != null || style.getDecoration() != null)) {
                            if (style.getDecoration() != null) {
                                elt.put("decoration", style.getDecoration().name());
                            }
                            if (style.getForeground() != null) {
                                elt.put("fg", style.getForeground().name());
                            }
                            if (style.getBackground() != null) {
                                elt.put("bg", style.getBackground().name());
                            }
                        }
                        String text = o.toString();
                        elt.put("text", text);
                        array.add(elt);
                    }
                }
                return array;
            }
            return null;
        } finally {
            session.removeAttribute("execution");
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Shell getShell()
    {
        HttpSession session = this.getSession();
        SerializableTransient<Shell> wrapped = (SerializableTransient<Shell>) getRequest().getSession().getAttribute("crash");
        if (wrapped == null) {
            ServletContext context = session.getServletContext();
            CRaSH crash = getCRaSH();
            CRaSHSession cshell = crash.createSession(null);
            session.setAttribute("crash", new SerializableTransient<CRaSHSession>(cshell));
            return getShell();
        }
        Shell shell = wrapped.object; 
        return shell;
    }

    private CRaSH getCRaSH()
    {
        HttpSession session = this.getSession();
        ServletContext context = session.getServletContext();
        CRaSH crash = (CRaSH) context.getAttribute("crash");
        if (crash == null) {
            System.out.println("Starting");
            try {
                Bootstrap bootstrap = new Bootstrap(Thread.currentThread().getContextClassLoader());
                bootstrap.bootstrap();
                crash = new CRaSH(bootstrap.getContext());

                context.setAttribute("crash", crash);
            } catch (Exception e) {
                throw new UndeclaredThrowableException(e);
            }
            System.out.println("Started");
        }
        return crash;
    }

    private HttpSession getSession()
    {
        return getRequest().getSession();
    }

    private HttpServletRequest getRequest()
    {
        return ((XWikiContext) this.execution.getContext().getProperty("xwikicontext")).getRequest();
    }
}
