package com.agimatec.tools.nls.output;

import com.agimatec.tools.nls.model.MBText;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Description: <br/>
 * User: roman <br/>
 * Date: 05.02.2009 <br/>
 * Time: 12:16:18 <br/>
 * Copyright: Agimatec GmbH
 */
final class MBTextConverter implements Converter {
    public void marshal(Object o, HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        MBText text = (MBText) o;
        if (text.getLocale() != null) {
            writer.addAttribute("locale", text.getLocale());
        }
        if(text.isReview()) {
            writer.addAttribute("review", "true");
        }
        if(text.isUseDefault()) {
            writer.addAttribute("useDefault", "true");
        }
        if (text.getValue() != null) {
            writer.setValue(text.getValue());
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        MBText text = new MBText();
        text.setLocale(reader.getAttribute("locale"));
        text.setReview(Boolean.parseBoolean(reader.getAttribute("review")));
        text.setUseDefault(Boolean.parseBoolean(reader.getAttribute("useDefault")));
        text.setValue(reader.getValue());
        /* BEGIN backward compatibility: <text><value>something</value></text> */
        if(reader.hasMoreChildren()) {
            reader.moveDown();
            if("value".equals(reader.getNodeName())) {
                text.setValue(reader.getValue());
            }
            reader.moveUp();
        }
        /* END backward compatibility */
        return text;
    }

    public boolean canConvert(Class aClass) {
        return MBText.class == aClass;
    }
}
