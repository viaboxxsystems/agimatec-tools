package com.agimatec.sql.meta.persistence;

import java.io.*;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 27.04.2007 <br/>
 * Time: 17:39:51 <br/>
 */
public class SerializerPersistencer implements ObjectPersistencer {
    public void save(Object obj, File target) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(target));
        try {
            out.writeObject(obj);
        } finally {
            out.close();
        }
    }

    public Object load(File source) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(source));
        try {
            return in.readObject();
        } finally {
            in.close();
        }

    }
}
