package ProxyBwapi.Races

import ProxyBwapi.UnitClasses.UnitClasses
import bwapi.UnitType

object Neutral {
  lazy val Geyser = UnitClasses.get(UnitType.Resource_Vespene_Geyser)
  lazy val PsiDisruptor = UnitClasses.get(UnitType.Special_Psi_Disrupter)
  lazy val Autosupply = UnitClasses.get(UnitType.Unused_Cantina)
}
