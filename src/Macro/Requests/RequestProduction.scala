package Macro.Requests

import ProxyBwapi.BuildableType
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses._
import ProxyBwapi.Upgrades.Upgrade
import Tactic.Production._
import bwapi.Race

abstract class RequestProduction(val buildableType: BuildableType, val quantity: Int = 0) extends {
  def tech      : Option[Tech]      = buildableType match { case c: Tech      => Some(c) case _ => None }
  def upgrade   : Option[Upgrade]   = buildableType match { case c: Upgrade   => Some(c) case _ => None }
  def unit      : Option[UnitClass] = buildableType match { case c: UnitClass => Some(c) case _ => None }

  def race            : Race  = buildableType.race
  def buildFrames     : Int   = buildableType.productionFrames(quantity)
  def mineralCost     : Int   = buildableType.mineralCost(quantity)
  def gasCost         : Int   = buildableType.gasCost(quantity)
  def supplyProvided  : Int   = buildableType.supplyProvided
  def supplyRequired  : Int   = buildableType.supplyRequired

  // TODO: Count addons as producers
  def techRequired      : Option[Tech]            = unit.flatMap(_.buildTechEnabling)
  def upgradeRequired   : Option[(Upgrade, Int)]  = upgrade.map(u => (u, quantity - 1)).filter(_._2 > 0)
  def unitsRequired     : Seq[UnitClass]          = unit.map(_.requiredUnits).orElse(upgrade.map(u => Seq(u.whatsRequired(quantity)))).getOrElse(tech.map(_.requiredUnit).toSeq).filterNot(UnitClasses.None==)
  def producerRequired  : UnitClass               = unit.map(_.whatBuilds._1).orElse(upgrade.map(_.whatUpgrades)).orElse(tech.map(_.whatResearches)).getOrElse(UnitClasses.None)
  def producersRequired : Int                     = unit.map(_.whatBuilds._2).getOrElse(1)

  final def makeProduction: Production = {
    if (tech.isDefined) new ResearchTech(this)
    else if (upgrade.isDefined) new ResearchUpgrade(this)
    else {
      val unitClass = unit.get
      if (unitClass.isAddon)                                                  new BuildAddon(this)
      else if (unitClass.isBuilding && ! unitClass.whatBuilds._1.isBuilding)  new BuildBuilding(this)
      else if (unitClass.buildUnitsSpent.exists(_.isZerg))                    new MorphUnit(this)
      else                                                                    new TrainUnit(this)
    }
  }

  /**
    * Is this buildable a satisfactory instance of producing some requirement?
    */
  def satisfies(requirement: RequestProduction): Boolean = {
    if (tech != requirement.tech) return false
    if (upgrade != requirement.upgrade) return false
    if (unit != requirement.unit) return false
    if (upgrade.isDefined && quantity < requirement.quantity) return false
    true
  }

  override def hashCode(): Int = buildableType.hashCode() ^ quantity
  override def equals(anyOther: scala.Any): Boolean = {
    if ( ! anyOther.isInstanceOf[RequestProduction]) return false
    val other = anyOther.asInstanceOf[RequestProduction]
    buildableType == other.buildableType && quantity == other.quantity
  }

  override def toString: String = f"Buildable ${if (unit.isDefined) f"$quantity " else ""}$buildableType${if (upgrade.isDefined) f" lvl $quantity" else ""}"
}