package ProxyBwapi.Players

import Debugging.Visualizations.Colors
import Information.Geography.Types.Base
import Lifecycle.With
import Performance.Cache
import ProxyBwapi.Upgrades.Upgrade
import bwapi.{Player, Race}

case class PlayerInfo(basePlayer: Player) extends PlayerProxy(basePlayer) {
  
  private var permanentRace: Option[Race] = None
  def raceCurrent: Race = raceCurrentCache()
  private val raceCurrentCache = new Cache(() => {
    permanentRace = permanentRace.orElse(if (Array(Race.Terran, Race.Protoss, Race.Zerg).contains(raceInitial)) Some(raceInitial) else None)
    permanentRace = permanentRace.orElse(With.units.all.find(_.player == this).map(_.unitClass.race))
    permanentRace.getOrElse(raceInitial)
  })
  
  def isTerran            : Boolean = raceCurrent == Race.Terran
  def isProtoss           : Boolean = raceCurrent == Race.Protoss
  def isZerg              : Boolean = raceCurrent == Race.Zerg
  def isUnknownOrTerran   : Boolean = raceCurrent == Race.Unknown || raceCurrent == Race.Terran
  def isUnknownOrProtoss  : Boolean = raceCurrent == Race.Unknown || raceCurrent == Race.Protoss
  def isUnknownOrZerg     : Boolean = raceCurrent == Race.Unknown || raceCurrent == Race.Zerg
  lazy val isFriendly : Boolean = isUs || isAlly
  
  def hasUpgrade(upgrade: Upgrade): Boolean = getUpgradeLevel(upgrade) > 0
  
  lazy val colorMidnight: bwapi.Color   =
    if      (isUs)      Colors.MidnightViolet
    else if (isNeutral) Colors.MidnightTeal
    else if (isAlly)    Colors.MidnightBlue
    else                Colors.MidnightRed
  
  lazy val colorDeep: bwapi.Color  =
    if      (isUs)      Colors.DeepViolet
    else if (isNeutral) Colors.DeepTeal
    else if (isAlly)    Colors.DeepBlue
    else                Colors.DeepRed
  
  lazy val colorDark: bwapi.Color =
    if      (isUs)      Colors.DarkViolet
    else if (isNeutral) Colors.DarkTeal
    else if (isAlly)    Colors.DarkBlue
    else                Colors.DarkRed
  
  lazy val colorMedium: bwapi.Color =
    if      (isUs)      Colors.MediumViolet
    else if (isNeutral) Colors.MediumTeal
    else if (isAlly)    Colors.MediumBlue
    else                Colors.MediumRed
  
  lazy val colorBright: bwapi.Color   =
    if      (isUs)      Colors.BrightViolet
    else if (isNeutral) Colors.BrightTeal
    else if (isAlly)    Colors.BrightBlue
    else                Colors.BrightRed
  
  lazy val colorNeon: bwapi.Color   =
    if      (isUs)      Colors.NeonViolet
    else if (isNeutral) Colors.NeonTeal
    else if (isAlly)    Colors.NeonBlue
    else                Colors.NeonRed

  def bases: Vector[Base] = With.geography.bases.filter(_.owner == this)

  override def toString: String = name
}
