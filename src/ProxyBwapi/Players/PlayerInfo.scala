package ProxyBwapi.Players

import Debugging.Visualizations.Colors
import bwapi.Player

case class PlayerInfo(basePlayer:Player) extends PlayerProxy(basePlayer) {
  
  def isFriendly = isUs || isAlly
  
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
