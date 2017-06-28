package Macro.BuildRequests

import Macro.Architecture.BuildingDescriptor
import Macro.Buildables.Buildable

abstract class BuildRequest(val buildable: Buildable) {
  def add     : Int = 0
  def require : Int = 0
}
