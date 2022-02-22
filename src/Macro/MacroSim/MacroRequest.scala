package Macro.MacroSim

import Macro.Buildables.Buildable
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.Upgrade

final class MacroRequest extends Buildable {
  var unit    : Option[UnitClass] = None
  var upgrade : Option[Upgrade] = None
  var tech    : Option[Tech] = None
  var count   : Int = 1

  def framesRequired: Int = unit.map(_.buildFrames).orElse(upgrade.map(_.upgradeFrames(count))).orElse(tech.map(_.researchFrames)).getOrElse(0)
  def mineralsRequired: Int = unit.map(_.mineralPrice).orElse(upgrade.map(_.mineralPrice(count))).orElse(tech.map(_.mineralPrice)).getOrElse(0)
  def gasRequired: Int = unit.map(_.gasPrice).orElse(upgrade.map(_.gasPrice(count))).orElse(tech.map(_.gasPrice)).getOrElse(0)
  def supplyRequired: Int = unit.map(_.supplyRequired).getOrElse(0)
  def upgradeRequired: Option[(Upgrade, Int)] = upgrade.map(u => (u, count - 1)).filter(_._2 > 0)
  def techRequired: Option[Tech] = unit.flatMap(_.buildTechEnabling)
  def producerRequired: UnitClass = unit.map(_.whatBuilds._1).orElse(upgrade.map(_.whatUpgrades)).orElse(tech.map(_.whatResearches)).getOrElse(UnitClasses.None)
  def producersRequired: Int = unit.map(_.whatBuilds._2).getOrElse(1)
  def unitsRequired: Iterable[UnitClass] = unit.map(_.requiredUnits.keys).orElse(upgrade.map(u => Seq(u.whatsRequired(count)))).getOrElse(tech.map(_.requiredUnit).toSeq)

  /**
    * Shim for replacing Scheduler with MacroQueue
    */
  override def unitOption      : Option[UnitClass]   = unit
  override def techOption      : Option[Tech]        = tech
  override def upgradeOption   : Option[Upgrade]     = upgrade
  override def upgradeLevel    : Int                 = count
  override def frames          : Int                 = unit.map(_.buildFrames).orElse(tech.map(_.researchFrames)).orElse(upgrade.map(_.upgradeFrames(upgradeLevel))).getOrElse(0)

  override def toString: String = s"Request ${if (tech.isDefined) "" else if (upgrade.isDefined) f"lvl $count " else f"$count "}${unit.orElse(upgrade).orElse(tech).map(_.toString).getOrElse("???")}"
}
