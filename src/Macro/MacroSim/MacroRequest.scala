package Macro.MacroSim

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.Upgrade

final class MacroRequest {
  var unit    : Option[UnitClass] = None
  var upgrade : Option[Upgrade] = None
  var tech    : Option[Tech] = None
  var min     : Int = 1
  var max     : Int = 1
  // TODO: Placement details

  def minMin: Int = Math.min(min, max)
  def maxMax: Int = Math.max(min, max)

  def framesRequired: Int = unit.map(_.buildFrames).orElse(upgrade.map(_.upgradeFrames(min))).orElse(tech.map(_.researchFrames)).getOrElse(0)
  def mineralsRequired: Int = unit.map(_.mineralPrice).orElse(upgrade.map(_.mineralPrice(min))).orElse(tech.map(_.mineralPrice)).getOrElse(0)
  def gasRequired: Int = unit.map(_.gasPrice).orElse(upgrade.map(_.gasPrice(min))).orElse(tech.map(_.gasPrice)).getOrElse(0)
  def supplyRequired: Int = unit.map(_.supplyRequired).getOrElse(0)
  def upgradeRequired: Option[(Upgrade, Int)] = upgrade.map(u => (u, min - 1)).filter(_._2 > 0)
  def techRequired: Option[Tech] = unit.flatMap(_.buildTechEnabling)
  def producerRequired: UnitClass = unit.map(_.whatBuilds._1).orElse(upgrade.map(_.whatUpgrades)).orElse(tech.map(_.whatResearches)).getOrElse(UnitClasses.None)
  def producersRequired: Int = unit.map(_.whatBuilds._2).getOrElse(1)
  def unitsRequired: Iterable[UnitClass] = unit.map(_.requiredUnits.keys).orElse(upgrade.map(u => Seq(u.whatsRequired(min)))).getOrElse(tech.map(_.requiredUnit).toSeq)

  override def toString: String = s"Request ${if (tech.isDefined) "" else if (upgrade.isDefined) f"lvl $maxMax " else f"$maxMax "}${unit.orElse(upgrade).orElse(tech).map(_.toString).getOrElse("???")}"
}
