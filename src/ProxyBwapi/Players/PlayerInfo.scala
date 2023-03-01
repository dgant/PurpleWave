package ProxyBwapi.Players

import Debugging.Visualizations.Colors
import Information.Geography.NeoGeo.Internal.NeoColors.Hues
import Information.Geography.Types.Base
import Lifecycle.With
import Performance.Cache
import ProxyBwapi.Techs.Tech
import ProxyBwapi.Upgrades.Upgrade
import bwapi.{GameType, Player, Race}

case class PlayerInfo(bwapiPlayer: Player) extends PlayerProxy(bwapiPlayer) {
  
  private var permanentRace: Option[Race] = None
  def raceCurrent: Race = raceCurrentCache()
  private val raceCurrentCache = new Cache(() => {
    permanentRace = permanentRace.orElse(if (Array(Race.Terran, Race.Protoss, Race.Zerg).contains(raceInitial)) Some(raceInitial) else None)
    permanentRace = permanentRace.orElse(With.units.all.find(_.player == this).map(_.unitClass.race))
    permanentRace.getOrElse(raceInitial)
  })

  val isPlayer            : Boolean = ! isNeutral
  def isTerran            : Boolean = raceCurrent == Race.Terran
  def isProtoss           : Boolean = raceCurrent == Race.Protoss
  def isZerg              : Boolean = raceCurrent == Race.Zerg
  def isUnknownOrTerran   : Boolean = raceCurrent == Race.Unknown || raceCurrent == Race.Terran
  def isUnknownOrProtoss  : Boolean = raceCurrent == Race.Unknown || raceCurrent == Race.Protoss
  def isUnknownOrZerg     : Boolean = raceCurrent == Race.Unknown || raceCurrent == Race.Zerg

  def isFriendly : Boolean = isUs || isAlly

  def enemies: Iterable[PlayerInfo] = if (isUs) {
    With.enemies
  } else if (isEnemy) {
    Players.all.filter(p => p != this && (p.isFriendly || With.strategy.isFfa))
  } else {
    With.enemies
  }

  def allies: Iterable[PlayerInfo] = if (isUs) {
    Players.all.filterNot(==).filter(_.isAlly)
  } else if (isAlly) {
    Players.all.filterNot(With.self==).filter(_.isAlly)
  } else if (isNeutral) {
    Iterable.empty
  } else if (With.game.getGameType == GameType.Free_For_All) {
    Iterable.empty
  } else { // We can't know enemy team structures programatically, so we assume the worst
    Players.all.filterNot(==).filter(_.isEnemy)
  }
  
  def hasUpgrade(upgrade: Upgrade): Boolean = getUpgradeLevel(upgrade) > 0

  override def hasTech(tech: Tech): Boolean = {
    super.hasTech(tech) || With.scouting.techsOwned.get(this).exists(_.contains(tech))
  }

  lazy val colorMidnight: bwapi.Color =
    if      (isUs)      Colors.MidnightViolet
    else if (isNeutral) Colors.MidnightGray
    else if (isAlly)    Colors.MidnightBlue
    else                Colors.MidnightRed
  
  lazy val colorDeep: bwapi.Color =
    if      (isUs)      Colors.DeepViolet
    else if (isNeutral) Colors.DeepGray
    else if (isAlly)    Colors.DeepBlue
    else                Colors.DeepRed
  
  lazy val colorDark: bwapi.Color =
    if      (isUs)      Colors.DarkViolet
    else if (isNeutral) Colors.DarkGray
    else if (isAlly)    Colors.DarkBlue
    else                Colors.DarkRed
  
  lazy val colorMedium: bwapi.Color =
    if      (isUs)      Colors.MediumViolet
    else if (isNeutral) Colors.MediumGray
    else if (isAlly)    Colors.MediumBlue
    else                Colors.MediumRed
  
  lazy val colorBright: bwapi.Color =
    if      (isUs)      Colors.BrightViolet
    else if (isNeutral) Colors.BrightGray
    else if (isAlly)    Colors.BrightBlue
    else                Colors.BrightRed
  
  lazy val colorNeon: bwapi.Color =
    if      (isUs)      Colors.NeonViolet
    else if (isNeutral) Colors.White
    else if (isAlly)    Colors.NeonBlue
    else                Colors.NeonRed

  lazy val hue: Int =
    if      (isUs)      Hues.Violet
    else if (isNeutral) 0
    else if (isAlly)    Hues.Blue
    else                Hues.Red

  def bases: Vector[Base] = With.geography.bases.filter(_.owner == this)

  def allianceDescription: String = if (isUs) "Us" else if (isNeutral) "Neutral" else if (isAlly) "Ally" else "Enemy"
  def raceDescription: String = if (raceCurrent == raceInitial) f"always $raceCurrent" else f"$raceCurrent; originally $raceInitial"
  def fullDescription: String = f"$name ($allianceDescription, $raceDescription)"
  override def toString: String = name
}
