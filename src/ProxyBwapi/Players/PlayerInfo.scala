package ProxyBwapi.Players

import Debugging.Visualizations.Colors
import ProxyBwapi.Upgrades.Upgrade
import bwapi.{Player, Race}

case class PlayerInfo(basePlayer:Player) extends PlayerProxy(basePlayer) {
  
  lazy val isTerran   : Boolean = race == Race.Terran
  lazy val isProtoss  : Boolean = race == Race.Protoss
  lazy val isZerg     : Boolean = race == Race.Zerg
  lazy val isFriendly : Boolean = isUs || isAlly
  
  def hasUpgrade(upgrade: Upgrade): Boolean = getUpgradeLevel(upgrade) > 0
  
  lazy val colorMidnight: bwapi.Color   =
    if      (isUs)      Colors.MidnightViolet
    else if (isNeutral) Colors.MidnightTeal
    else if (isAlly)    Colors.MidnightBlue
    else                Colors.MidnightRed
  
  lazy val colorDeep: bwapi.Color   =
    if      (isUs)      Colors.DeepViolet
    else if (isNeutral) Colors.DeepTeal
    else if (isAlly)    Colors.DeepBlue
    else                Colors.DeepRed
  
  lazy val colorDark: bwapi.Color   =
    if      (isUs)      Colors.DarkViolet
    else if (isNeutral) Colors.DarkTeal
    else if (isAlly)    Colors.DarkBlue
    else                Colors.DarkRed
  
  lazy val colorMedium: bwapi.Color   =
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
}
