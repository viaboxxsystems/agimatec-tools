package com.agimatec.commons.util;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 29.03.2010<br>
 * Time: 10:37:13<br>
 * viaboxx GmbH, 2010
 */
public class ResourceUtilsTest extends TestCase {

    public void testURLUtils() throws IOException, URISyntaxException {
        System.out.println(getName() + " >> ");
        Collection<String> list = ResourceUtils.getResources("com/agimatec/commons/util/");
        for (String name : list) {
            System.out.println(name);
        }
    }

    public void test3() throws Exception {
        System.out.println(getName() + " >> ");
        Collection<String> list = ResourceUtils.getResources("java/util/");
        for (String name : list) {
            System.out.println(name);
        }
    }



}
