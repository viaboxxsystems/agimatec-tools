package com.agimatec.sql;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal -
 * This type is part of a SQLWriter and used to
 * create SQL aliases for SQL table names during
 * the generation of an SQL statement.
 */
public class AliasDictionary {
    /**
     * if table is named @, the alias will not be printed
     */
    public static final String ALIAS_RESERVATION = "@";
    /**
     * implementated to guarantee a stable ordering of the table names
     */
    private List<String[]> aliases =
            new ArrayList<String[]>(); // entry = String[] { alias , tableName }
    /**
     * the table with name = hiddenAlias is also not printed
     */
    private String hiddenAlias;

    /**
     * @param ha
     */
    public void setHiddenAlias(final String ha) {
        hiddenAlias = ha;
    }

    public String getHiddenAlias() {
        return hiddenAlias;
    }

    /**
     * AliasDictionary constructor comment.
     */
    public AliasDictionary() {
        hiddenAlias = null;
    }

    public void clear() {
        aliases.clear();
    }

    /**
     * Generate the TABLENAME alias, ... list onto the given stream.
     *
     * @param stream
     * @throws IOException
     */
    public void appendAliasListTo(final Writer stream) throws IOException {
        boolean hadEffect = (hiddenAlias != null);

        for (String[] aliase : aliases) {
            final String table = aliase[1];
            if (!aliase[0].equals(hiddenAlias) && !table.equals(ALIAS_RESERVATION)) {
                if (hadEffect) {
                    stream.write(", ");
                }

                hadEffect = true;

                stream.write(table);
                if (aliase[2].length() > 0) {
                    stream.write(aliase[2]);
                }
                if (aliase[0].length() > 0) {
                    stream.write(' ');
                    stream.write(aliase[0]);
                }
            }
        }
    }

    public int size() {
        return aliases.size();
    }

    /**
     * return the first alias for the given table
     */
    public String findAlias(final String tableName) {
        for (String[] aliase : aliases) {
            if (aliase[1].equalsIgnoreCase(tableName)) {
                return aliase[0];
            }
        }

        return null;
    }

    /**
     * remove the alias for the table with the given alias.
     * return the table for the removed alias, or null if none was found.
     *
     * @param alias - the alias (unique) to remove
     */
    public String removeAlias(final String alias) {
        for (int i = aliases.size() - 1; i >= 0; i--) {
            String[] entry = aliases.get(i);
            if (alias.equals(entry[0])) {
                aliases.remove(i);
                return entry[1];
            }
        }
        return null;
    }

    /**
     * set the alias for the given table
     */
    public void setAlias(final String tableName, String alias) {
        setAlias(tableName, alias, null);
    }

    public void setAlias(String aTableName, String aALias, String aDBLink) {
        if (aALias == null) aALias = "";
        if (aDBLink == null) aDBLink = "";
        for (int i = aliases.size() - 1; i >= 0; i--) {
            String[] entry = aliases.get(i);
            if (entry[0]
                    .equals(aALias)) { // replace tablename, do not add the same alias twice
                entry[1] = aTableName;
                entry[2] = aDBLink;
                return;
            }
        }
        // not found, add once
        aliases.add(new String[]{aALias, aTableName, aDBLink});
    }

    /**
     * return a string representation (no special format garanteed)
     */
    public String toString() {
        return aliases.toString();
    }
}

