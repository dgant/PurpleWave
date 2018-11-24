package Macro.BuildRequests

import Macro.Buildables.Buildable

class BuildRequest(val buildable: Buildable) {
  def add     : Int = 0
  def require : Int = 0
  
  override def toString: String = (
    "Request "
    + (if (add > 0) "additional " + add else "up to " + require)
    + " "
    + buildable
  )
}
