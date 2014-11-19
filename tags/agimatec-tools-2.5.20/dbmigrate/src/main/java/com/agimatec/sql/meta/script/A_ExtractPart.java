package com.agimatec.sql.meta.script;


/**
 * <b>Description:</b>   <br>
 * <b>Creation Date:</b> 10.12.2007
 *
 * @author Roman Stumm
 */
abstract class A_ExtractPart {
    public static final int C_FIT_NOT =
            -1; // NOT OK, use next part, next token              | NP | NT | NOK |
    public static final int C_ERROR =
            0;  // NOT OK, error. stop!                             | -  | -  | NOT |
    public static final int C_FIT =
            1; // OK, use next part, next token                       | NP | NT | OK  |
    public static final int C_MAY_FIT =
            2; // OK, keep part, keep (or concat) token           | KP | KT | OK  |
    public static final int C_NOT_HANDLED =
            3; // UNKNOWN, use next part, keep token          | NP | KT | -   |

    /**
     * 0 - C_ERROR does not fit
     * 1 - C_FIT does fit, finished
     * 2 - C_MAY_FIT may fit, concat word and recheck with same part
     * 3 - C_NOT_HANDLED not handled (optional that does not fit), keep word and check with next part
     *
     * @param aToken
     * @param extractor
     * @return
     */
    public abstract int process(String aToken, PropertiesExtractor extractor);

    public int fits(String aToken) {
        return C_ERROR;
    }

    protected boolean isOptional() {
        return false;
    }
}

