package Macro.BuildRequests

import Macro.Buildables.Buildable

class BuildRequest(val buildable: Buildable) {
  def total : Int = 0

  override def toString: String = (
    "Request up to " + total + " " + buildable
  )
}
