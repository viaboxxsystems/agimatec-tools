package com.agimatec.sql.query;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A LookupResult is the result of executing a query. It holds
 * a list of result instances and the information if there are more
 * results on the server that are not included (because the client
 * did not want more results than requested.)
 */
public class QueryResult<E> implements Serializable, Iterable<E> {
  private List<E> myList = Collections.emptyList();
  private final boolean isComplete;

  /**
   * @param list - a serializable list
   */
  public QueryResult(final List<E> list) {
    this(list, true);
  }

  /**
   * @param list        - a list (should be serializable!)
   * @param aIsComplete
   */
  public QueryResult(final List<E> list, final boolean aIsComplete) {
    if (list != null) {
      myList = list;
    }
    this.isComplete = aIsComplete;
  }

  /**
   * @return a list of objects  = the results of the query
   */
  public List<E> getList() {
    return myList;
  }
 
  public Iterator<E> iterator()
  {
	  return myList.iterator();
  }

  /**
   * @return true when there are more results on the server that are not included in the list
   */
  public boolean isComplete() {
    return isComplete;
  }

  /**
   * convenience - the number of objects in the list
   */
  public int size() {
    return myList.size();
  }

  /**
   * convenience - return whether the list is empty.
   *
   * @return true when the list is empty
   */
  public boolean isEmpty() {
    return myList.isEmpty();
  }

  /**
   * convenience -
   *
   * @return the first object in the list or null when the list is empty
   */
  public E getFirst() {
    return (myList.isEmpty()) ? null : myList.get(0);
  }

  /**
   * convenience -
   *
   * @return the last object in the list or null when the list is empty
   */
  public E getLast() {
    return (myList.isEmpty()) ? null : myList.get(myList.size() - 1);
  }
}
