package com.agimatec.database;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: Top-level invoker tool to invoke multiple tools in a single java-launch...<br/>
 * User: roman.stumm <br/>
 * Date: 01.06.2007 <br/>
 * Time: 15:11:38 <br/>
 */
public class InvokerTool {
    public static void main(String[] args) throws Exception {
        List<InvocationDef> tools = new ArrayList();
        InvocationDef def = null;

        for (String arg : args) {
            if ("{".equals(arg)) {
                def = new InvocationDef();
            } else if (def != null && def.tool == null) {
                def.tool = arg;
            } else if ("}".equals(arg)) {
                tools.add(def);
                def = null;
            } else {
                def.params.add(arg);
            }
        }
        if (args.length == 0 || def != null) {
            printUsage();
            return;
        }
        for (InvocationDef each : tools) {
            System.out.println("####### About to invoke " + each.tool + " ...");
            Class toolClass;
            try {
                toolClass = Class.forName(each.tool);
            } catch (Exception ex) {
                try {
                    toolClass = Class.forName("com.agimatec.database." + each.tool);
                } catch (Exception ex2) {
                    ex.printStackTrace();
                    throw ex2;
                }
            }
            Method mainMethod = toolClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object)each.getArgs());
        }
        System.out.println("####### END #######");
    }

    private static void printUsage() {
        System.out.println("java " + InvokerTool.class.getName() +
                " { toolclass1 params... } { toolclass2 params...} ...");
    }

    static class InvocationDef {
        String tool;
        List params = new ArrayList();

        String[] getArgs() {
            return (String[]) params.toArray(new String[params.size()]);
        }

        public String toString() {
            return "InvocationDef{" + "tool='" + tool + '\'' + ", params=" + params + '}';
        }
    }
}
