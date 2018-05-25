package ProxyBwapi.Races

import ProxyBwapi.UnitClasses.UnitClasses
import bwapi.UnitType

object Neutral {
  def Geyser = UnitClasses.get(UnitType.Resource_Vespene_Geyser)
  def PsiDisruptor = UnitClasses.get(UnitType.Special_Psi_Disrupter)
}
