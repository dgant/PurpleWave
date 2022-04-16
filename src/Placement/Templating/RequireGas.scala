package Placement.Templating

import ProxyBwapi.Races.{Protoss, Terran, Zerg}

object RequireGas extends TemplatePointRequirement(Terran.Refinery, Protoss.Assimilator, Zerg.Extractor) {
  override val buildableBefore  : Boolean = false
  override val walkableBefore   : Boolean = false
  override val buildableAfter   : Boolean = false
  override val walkableAfter    : Boolean = false
  override val toString         : String  = "Anything"
}
