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


/** a doubly linked node for history lists */
public class HistoryNode {

  private Object element;
  private HistoryNode next, prev;

  HistoryNode() {
    this(null, null, null);
  }


  HistoryNode(Object element, HistoryNode prev, HistoryNode next) {
    this.element = element;
    this.next = next;
    this.prev = prev;
  }


  public void setElement(Object newElem) {
    element = newElem;
  }


  public void setNext(HistoryNode newNext) {
    next = newNext;
  }


  public void setPrev(HistoryNode newPrev) {
    next = newPrev;
  }


  public Object getElem() {
    return element;
  }

  public HistoryNode getNext() {
    return next;
  }

  public HistoryNode getPrev() {
    return prev;
  }

}
