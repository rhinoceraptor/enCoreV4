/*
 *  The MOOtcan MOO-client
 *  Copyright (C) 1999 Sindre S¯rensen
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


/**
 * a doubly linked list containing history elements.
 * any object type can be inserted into the history list
 *
 * @author Sindre S¯rensen
 */


public class HistoryList {

  int size, maxSize;
  HistoryNode latest, earliest, current;
  Object defaultElement;

  /**
   * @param defaultItem the (empty, if one wishes) object that will be returned when you have travelled beyond the latest point in history
   * @param maximumSize the maximum size the history list will become before removing the earliest items
   */
  HistoryList(Object defaultItem, int maximumSize) {
    defaultElement = defaultItem;
    maxSize = maximumSize;
    earliest = new HistoryNode();

    earliest.setPrev(earliest);
    earliest.setNext(earliest);
    earliest.setElement(defaultElement);

    latest = earliest;
    current = earliest;

    size = 0;
  }


  public Object getCurrentItem() {
    return current.getElem();
  }


  public Object getNextItem() {
    if (current.getNext() != null) {
      current = current.getNext();
		}
		else {
			if (! current.getElem().equals(defaultElement)) {
				current = appendEmptyItem();
			}
		}
    return getCurrentItem();
  }


  public void replaceCurrentItem(Object o) {
    current.setElement(o);
  }

  public Object getPreviousItem() {
    if (current != earliest)
      current = current.getPrev();
    return getCurrentItem();
  }


  public void insertItem(Object o) {
    latest.setElement(o);
    size++;
		appendEmptyItem();
  }


	public HistoryNode appendEmptyItem() {
    HistoryNode newNode = new HistoryNode(defaultElement, latest, null);
    latest.setNext(newNode);
    latest = newNode;
    current = latest;
    if (size > maxSize)
      removeEarliestItem();
		return current;
	}

  void removeEarliestItem() {
    earliest = earliest.getNext();
  }


}
