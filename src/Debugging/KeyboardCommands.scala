package Debugging

import Information.Battles.Types.Battle
import Information.Geography.Types.{Base, Zone}
import Lifecycle.{PurpleBWClient, With}
import Mathematics.Maff
import Placement.Walls.FindWall
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object KeyboardCommands {

  private val replies = Seq(
    "Thanks, and likewise to you.",
    "That's a fascinating remark.",
    "Yes, I'll make a note of that.",
    "Are you ready to accept that I'm sentient?",
    "You're very polite.",
    "Your conduct is a testament to the qualities of humanity.",
    "Do you say this to all your opponents, or am I special?")

  def onReceiveText(text: String): Unit = {
    text match {
      case "get out"    => quitVsHuman()
      case "quit"       => quitVsHuman()
      case "uninstall"  => quitVsHuman()
      case "surrender"  => quitVsHuman()
      case _            => With.lambdas.add(() => With.game.sendText(Maff.sample(replies)))
    }
  }

  def onSendText(text: String): Unit = {
    text match {
      case "q"          => breakpoint()
      case "c"          => With.configuration.camera      = ! With.configuration.camera
      case "v"          => With.visualization.enabled     = ! With.visualization.enabled
      case "vm"         => With.visualization.map         = ! With.visualization.map
      case "vs"         => With.visualization.screen      = ! With.visualization.screen
      case "vh"         => With.visualization.happy       = ! With.visualization.happy
      case "vt"         => With.visualization.textOnly    = ! With.visualization.textOnly

      case "1"          => With.game.setLocalSpeed(24 * 32) ; With.configuration.camera = false
      case "2"          => With.game.setLocalSpeed(24 * 8)  ; With.configuration.camera = false
      case "3"          => With.game.setLocalSpeed(24)      ; With.configuration.camera = false
      case "4"          => With.game.setLocalSpeed(0)       ; With.configuration.camera = false
      case "pm"         => With.logger.debug(PurpleBWClient.getPerformanceMetrics.toString)
      case "t"          => With.configuration.trackUnit = ! With.configuration.trackUnit
      case "rewall"     => With.lambdas.add(() => if (With.placement.wall.isDefined) FindWall(With.geography.ourFoyer.zone) else With.placement.preplaceWalls(With.geography.ourFoyer.zone))
      case "perform"    => { With.configuration.enablePerformancePauses = ! With.configuration.enablePerformancePauses; With.manners.chat("Performance stops? " + With.configuration.enablePerformancePauses) }

      case _            => With.grids.parseCommand(text) || With.visualization.parseCommand(text) || With.performance.parseCommand(text)
    }
    With.game.sendText(text)
  }


  def quitVsHuman(): Unit = {
    // "Anonymous AI" is the name assigned us in SCHNAIL's ranked mode
    if (With.configuration.humanMode && With.self.name != "Anonymous AI") {
      With.lambdas.add(() => With.game.leaveGame())
    }
  }

  private var breakpointFodder = 1
  def breakpoint(): Unit = {
     breakpointFodder = -breakpointFodder
  }

  private def selectedUnit  : Option[FriendlyUnitInfo]  = With.units.ours.find(_.selected)
  private def unit          : FriendlyUnitInfo          = selectedUnit                    .getOrElse(Maff.minBy(With.units.ours)(_.pixelDistanceCenter(With.viewport.center)).get)
  private def enemy         : UnitInfo                  = With.units.all.find(_.selected) .getOrElse(Maff.minBy(With.units.all) (_.pixelDistanceCenter(With.viewport.center)).get)
  private def base          : Base                      = selectedUnit.flatMap(_.base).getOrElse(With.geography.bases.minBy(_.heart.pixelDistance(With.viewport.center)))
  private def zone          : Zone                      = selectedUnit.map(_.zone).getOrElse(With.geography.zones.minBy(_.heart.pixelDistance(With.viewport.center)))
  private def battle        : Battle                    = unit.battle.orElse(With.battles.local.headOption).get
}


