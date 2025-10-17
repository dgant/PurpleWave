package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.Protoss
import Utilities.Time.{GameTime, Minutes, Seconds}

class FingerprintMissingDragoonRange extends Fingerprint {

  override protected def investigate: Boolean = {
    if ( ! With.enemies.exists(_.isProtoss))          return false
    if (With.enemies.exists(Protoss.DragoonRange(_))) return false
    if (With.frame > Minutes(8)())                    return false

    val upgradeFrames   = Protoss.DragoonRange.upgradeFrames(1)
    val core            = Maff.minBy(With.units.enemy.filter(Protoss.CyberneticsCore))(_.completionFrame)
    val coreCompletion  = core
      .map(_.completionFrame)
      .getOrElse(
        if (With.fingerprints.nexusFirst()) {
          GameTime(5, 30)() // Not timed, totally just guessed
        } else if (With.fingerprints.twoGate()) {
          GameTime(4, 10)() // Assuming 5 Zealots before Core
        } else if (With.fingerprints.coreBeforeZ()) {
          GameTime(2, 48)()
        } else {
          GameTime(3, 11)()
        })
    val rangeCompletion = coreCompletion + upgradeFrames
    val rangeIsLate     = With.frame > rangeCompletion + Seconds(10)()
    val goonIsVisible   = With.units.enemy.exists(u => Protoss.Dragoon(u) && u.visible)

    rangeIsLate && goonIsVisible
  }
}
