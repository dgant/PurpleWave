package Macro.Requests

import Placement.Access.PlacementQuery
import ProxyBwapi.Buildable
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.Upgrades.Upgrade
import Tactic.Production._
import Utilities.Time.Frames
import bwapi.Race

abstract class RequestBuildable(
    val buildable     : Buildable,
    val quantity      : Int = 0,
    val minStartFrame : Int = 0,
    val placement     : Option[PlacementQuery] = None,
    val specificTrainee  : Option[FriendlyUnitInfo] = None) {
  def tech      : Option[Tech]      = buildable match { case c: Tech      => Some(c) case _ => None }
  def upgrade   : Option[Upgrade]   = buildable match { case c: Upgrade   => Some(c) case _ => None }
  def unit      : Option[UnitClass] = buildable match { case c: UnitClass => Some(c) case _ => None }

  def race            : Race  = buildable.race
  def buildFrames     : Int   = buildable.productionFrames(quantity)
  def mineralCost     : Int   = buildable.mineralCost(quantity)
  def gasCost         : Int   = buildable.gasCost(quantity)
  def supplyProvided  : Int   = buildable.supplyProvided
  def supplyRequired  : Int   = buildable.supplyRequired

  // TODO: Count addons as producers
  def techRequired      : Option[Tech]            = unit.flatMap(_.buildTechEnabling)
  def upgradeRequired   : Option[(Upgrade, Int)]  = upgrade.map(u => (u, quantity - 1)).filter(_._2 > 0)
  def unitsRequired     : Seq[UnitClass]          = unit.map(_.requiredUnits).orElse(upgrade.map(u => Seq(u.whatsRequired(quantity)))).getOrElse(tech.map(_.requiredUnit).toSeq).filterNot(UnitClasses.None==)
  def producerRequired  : UnitClass               = unit.map(_.whatBuilds._1).orElse(upgrade.map(_.whatUpgrades)).orElse(tech.map(_.whatResearches)).getOrElse(UnitClasses.None)
  def producersRequired : Int                     = unit.map(_.whatBuilds._2).getOrElse(1)
  def addonRequired     : Option[UnitClass]       = unit.flatMap(_.addonRequired)

  final def makeProduction(expectedFrames: Int): Production = {
    if (tech.isDefined) new ProduceTech(this, expectedFrames)
    else if (upgrade.isDefined) new ProduceUpgrade(this, expectedFrames)
    else {
      val unitClass = unit.get
      if (unitClass.isAddon)                                                  new ProduceAddon(this, expectedFrames)
      else if (unitClass.isBuilding && ! unitClass.whatBuilds._1.isBuilding)  new ProduceBuilding(this, expectedFrames)
      else if (unitClass.buildUnitsSpent.exists(_.isZerg))                    new ProduceUnitMorphed(this, expectedFrames)
      else                                                                    new ProduceUnitTrained(this, expectedFrames)
    }
  }

  private def stringWhat  : String = f" ${if (unit.isDefined) f"$quantity " else ""}$buildable${specificTrainee.map(u => f" ($u)").mkString("")}"
  private def stringLevel : String = if (upgrade.isDefined) f" lvl $quantity" else ""
  private def stringWhen  : String = if (minStartFrame <= 0) "" else f" after ${Frames(minStartFrame)}"
  private def stringWhere : String = if (placement.isEmpty) "" else f" @ ${placement.get}".replace("PlacementQuery ", "")

  override def toString: String = f"Buildable$stringWhat$stringLevel$stringWhen$stringWhere"
}