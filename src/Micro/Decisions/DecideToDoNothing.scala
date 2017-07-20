package Micro.Decisions

import Debugging.Visualizations.Colors
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class DecideToDoNothing(argAgent: FriendlyUnitInfo) extends MicroDecision(argAgent) {
  
  override def frames: Double = {
    0.0
  }
  
  override def legal: Boolean = {
    true
  }
  
  override def execute() { }
  
  override def renderMap() {
    renderWith(currentPixel, currentPixel, Colors.DarkGreen)
  }
}
