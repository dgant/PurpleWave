package Macro.BuildRequests

import Macro.Buildables.Buildable

class BuildRequest(val buildable: Buildable) {
  def add     : Int = 0
  def require : Int = 0
}
