package com.vmware.ph.phservice.common;

public class Pair<F, S> {
  private final F _first;
  
  private final S _second;
  
  public Pair(F first, S second) {
    this._first = first;
    this._second = second;
  }
  
  public int hashCode() {
    int hashFirst = (this._first != null) ? this._first.hashCode() : 0;
    int hashSecond = (this._second != null) ? this._second.hashCode() : 0;
    return (hashFirst + hashSecond) * hashSecond + hashFirst;
  }
  
  public boolean equals(Object other) {
    if (!(other instanceof Pair))
      return false; 
    Pair<?, ?> otherPair = (Pair<?, ?>)other;
    if (this._first == null && otherPair._first != null)
      return false; 
    if (this._second == null && otherPair._second != null)
      return false; 
    return (this._first.equals(otherPair._first) && this._second.equals(otherPair._second));
  }
  
  public String toString() {
    return "(" + this._first + ", " + this._second + ")";
  }
  
  public F getFirst() {
    return this._first;
  }
  
  public S getSecond() {
    return this._second;
  }
}
