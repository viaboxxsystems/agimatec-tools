package com.agimatec.utility.fileimport;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 16.06.2008 <br/>
 * Time: 12:08:04 <br/>
 * Copyright: Agimatec GmbH
 */
public enum ImportState {
    /** import not yet started / scheduled / waiting to receive lock */
    IDLE,
    /** import currently running */
    RUNNING,
    /** import completly done (with or without errors) */
    DONE,
    /** import cancelled, finished abnormally */
    CANCELLED
}
